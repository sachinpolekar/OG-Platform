/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeTypeDictionary;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeObjectBuilder;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.types.IndicatorType;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.serialization.InvokedSerializedForm;

/**
 * Fudge builders for alternative serialized forms of objects.
 */
/* package */final class SerializedForm {

  private SerializedForm() {
  }

  /**
   * Fudge builder for {@link InvokedSerializedForm}
   */
  @FudgeBuilderFor(InvokedSerializedForm.class)
  public static final class InvokedSerializedFormBuilder extends AbstractFudgeMessageBuilder<InvokedSerializedForm> implements FudgeObjectBuilder<Object> {

    @Override
    protected void buildMessage(final FudgeSerializationContext context, final MutableFudgeFieldContainer message, final InvokedSerializedForm object) {
      if (object.getOuterClass() != null) {
        message.add(null, null, object.getOuterClass().getName());
      } else {
        context.objectToFudgeMsg(message, null, null, substituteObject(object.getOuterInstance()));
      }
      if (object.getParameters().length == 0) {
        message.add(object.getMethod(), null, IndicatorType.INSTANCE);
      } else {
        final MutableFudgeFieldContainer parameters = context.newMessage();
        for (Object parameter : object.getParameters()) {
          context.objectToFudgeMsg(parameters, null, null, substituteObject(parameter));
        }
        message.add(object.getMethod(), null, parameters);
      }
    }

    @Override
    public Object buildObject(final FudgeDeserializationContext context, final FudgeFieldContainer message) {
      Object outer = null;
      String method = null;
      Object[] parameters = null;
      for (FudgeField field : message) {
        if (field.getName() == null) {
          if (field.getOrdinal() == null) {
            if (field.getValue() instanceof String) {
              try {
                outer = Class.forName((String) field.getValue());
              } catch (ClassNotFoundException e) {
                throw new OpenGammaRuntimeException("Class not found", e);
              }
            } else {
              outer = context.fieldValueToObject(field);
            }
          }
        } else {
          switch (field.getType().getTypeId()) {
            case FudgeTypeDictionary.FUDGE_MSG_TYPE_ID: {
              if (method != null) {
                throw new IllegalStateException("Parameters already set from " + method);
              }
              method = field.getName();
              final FudgeFieldContainer params = (FudgeFieldContainer) field.getValue();
              parameters = new Object[params.getNumFields()];
              int i = 0;
              for (FudgeField param : params) {
                parameters[i++] = context.fieldValueToObject(param);
              }
              break;
            }
            case FudgeTypeDictionary.INDICATOR_TYPE_ID:
              method = field.getName();
              break;
          }
        }
      }
      if (outer == null) {
        throw new IllegalArgumentException("Message did not contain an outer object");
      }
      if (method == null) {
        throw new IllegalArgumentException("Message did not contain an invocation reference");
      }
      final InvokedSerializedForm isr;
      if (parameters != null) {
        isr = new InvokedSerializedForm(outer, method, parameters);
      } else {
        isr = new InvokedSerializedForm(outer, method);
      }
      return isr.readReplace();
    }
  }

}