/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.function.rest;

import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.function.FunctionDefinition;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.FunctionRepository;

/**
 * 
 */
public class FunctionRepositoryResource {

  private final FunctionRepository _underlying;
  private final FudgeContext _fudgeContext;

  public FunctionRepositoryResource(final FunctionRepository underlying, final FudgeContext fudgeContext) {
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  protected FunctionRepository getUnderlying() {
    return _underlying;
  }

  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @GET
  @Path("/functionsByUniqueId")
  public FudgeMsgEnvelope getFunctionsByUniqueId() {
    final Collection<FunctionDefinition> allFunctions = getUnderlying().getAllFunctions();
    final FudgeSerializationContext fsc = new FudgeSerializationContext(getFudgeContext());
    final MutableFudgeMsg msg = fsc.newMessage();
    for (FunctionDefinition function : allFunctions) {
      final MutableFudgeMsg submsg = fsc.newMessage();
      submsg.add("shortName", function.getShortName());
      fsc.addToMessageWithClassHeaders(submsg, "defaultParameters", null, function.getDefaultParameters(), FunctionParameters.class);
      msg.add(function.getUniqueId(), submsg);
    }
    return new FudgeMsgEnvelope(msg);
  }

  @GET
  @Path("/functionsByShortName")
  public FudgeMsgEnvelope getFunctionsByShortName() {
    final Collection<FunctionDefinition> allFunctions = getUnderlying().getAllFunctions();
    final FudgeSerializationContext fsc = new FudgeSerializationContext(getFudgeContext());
    final MutableFudgeMsg msg = fsc.newMessage();
    for (FunctionDefinition function : allFunctions) {
      final MutableFudgeMsg submsg = fsc.newMessage();
      submsg.add("uniqueId", function.getUniqueId());
      fsc.addToMessageWithClassHeaders(submsg, "defaultParameters", null, function.getDefaultParameters(), FunctionParameters.class);
      msg.add(function.getShortName(), submsg);
    }
    return new FudgeMsgEnvelope(msg);
  }

}
