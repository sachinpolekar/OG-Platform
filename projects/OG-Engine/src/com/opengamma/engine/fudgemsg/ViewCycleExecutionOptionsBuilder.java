/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import javax.time.Instant;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;

/**
 * Fudge message builder for {@link ViewCycleExecutionOptions}
 */
@FudgeBuilderFor(ViewCycleExecutionOptions.class)
public class ViewCycleExecutionOptionsBuilder implements FudgeBuilder<ViewCycleExecutionOptions> {

  private static final String VALUATION_TIME_FIELD = "valuation";
  private static final String MARKET_DATA_SPECIFICATION = "marketDataSpecification";

  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, ViewCycleExecutionOptions object) {
    MutableFudgeMsg msg = context.newMessage();
    msg.add(VALUATION_TIME_FIELD, object.getValuationTime());
    context.addToMessageWithClassHeaders(msg, MARKET_DATA_SPECIFICATION, null, object.getMarketDataSpecification());
    return msg;
  }

  @Override
  public ViewCycleExecutionOptions buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    ViewCycleExecutionOptions result = new ViewCycleExecutionOptions();
    FudgeField valuationTimeField = msg.getByName(VALUATION_TIME_FIELD);
    if (valuationTimeField != null) {
      result.setValuationTime(context.fieldValueToObject(Instant.class, valuationTimeField));
    }
    FudgeField marketDataSpecificationField = msg.getByName(MARKET_DATA_SPECIFICATION);
    if (marketDataSpecificationField != null) {
      result.setMarketDataSpecification(context.fieldValueToObject(MarketDataSpecification.class, marketDataSpecificationField));
    }
    return result;
  }

}
