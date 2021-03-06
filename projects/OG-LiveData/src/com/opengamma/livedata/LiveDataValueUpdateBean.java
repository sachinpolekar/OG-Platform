/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.fudgemsg.FudgeMsgFactory;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;

import com.opengamma.util.PublicAPI;

/**
 * A simple JavaBean-based implementation of {@link LiveDataValueUpdate}.
 */
@PublicAPI
public class LiveDataValueUpdateBean implements LiveDataValueUpdate,
    Serializable {
  private static final String SEQUENCE_NUMBER_FIELD_NAME = "sequenceNumber";
  private static final String SPECIFICATION_FIELD_NAME = "specification";
  private static final String FIELDS_FIELD_NAME = "fields";
  private final long _sequenceNumber;
  private final LiveDataSpecification _specification;
  private final FudgeMsg _fieldContainer;
  
  public LiveDataValueUpdateBean(long sequenceNumber, LiveDataSpecification specification, FudgeMsg fieldContainer) {
    // TODO kirk 2009-09-29 -- Check Inputs.
    _sequenceNumber = sequenceNumber;
    _specification = specification;
    _fieldContainer = fieldContainer;
  }

  @Override
  public FudgeMsg getFields() {
    return _fieldContainer;
  }

  @Override
  public long getSequenceNumber() {
    return _sequenceNumber;
  }

  @Override
  public LiveDataSpecification getSpecification() {
    return _specification;
  }
  
  public FudgeMsg toFudgeMsg(FudgeMsgFactory fudgeMessageFactory) {
    MutableFudgeMsg msg = fudgeMessageFactory.newMessage();
    msg.add(SEQUENCE_NUMBER_FIELD_NAME, getSequenceNumber());
    if (getSpecification() != null) {
      msg.add(SPECIFICATION_FIELD_NAME, getSpecification().toFudgeMsg(fudgeMessageFactory));
    }
    if (getFields() != null) {
      msg.add(FIELDS_FIELD_NAME, getFields());
    }
    return msg;
  
  }
  public static LiveDataValueUpdateBean fromFudgeMsg(FudgeDeserializationContext fudgeContext, FudgeMsg msg) {
    Long sequenceNumber = msg.getLong(SEQUENCE_NUMBER_FIELD_NAME);
    FudgeMsg specificationFields = msg.getMessage(SPECIFICATION_FIELD_NAME);
    FudgeMsg fields = msg.getMessage(FIELDS_FIELD_NAME);
    // REVIEW kirk 2009-10-28 -- Right thing to do here?
    if (sequenceNumber == null) {
      return null;
    }
    if (specificationFields == null) {
      return null;
    }
    if (fields == null) {
      return null;
    }
    LiveDataSpecification spec = LiveDataSpecification.fromFudgeMsg(fudgeContext, specificationFields);
    return new LiveDataValueUpdateBean(sequenceNumber, spec, fields);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
