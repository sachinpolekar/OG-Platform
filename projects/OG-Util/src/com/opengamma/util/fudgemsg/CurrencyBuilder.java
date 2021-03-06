/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.util.money.Currency;

/**
 * Fudge builder for {@code Currency}.
 */
@FudgeBuilderFor(Currency.class)
public final class CurrencyBuilder implements FudgeBuilder<Currency> {

  /** Field name. */
  public static final String CURRENCY_KEY = "currency";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, Currency object) {
    final MutableFudgeMsg msg = context.newMessage();
    FudgeSerializationContext.addClassHeader(msg, Currency.class);
    context.addToMessage(msg, CURRENCY_KEY, null, object.getCode());
    return msg;
  }

  @Override
  public Currency buildObject(FudgeDeserializationContext context, FudgeMsg msg) {
    final String currencyStr = msg.getString(CURRENCY_KEY);
    if (currencyStr == null) {
      throw new IllegalArgumentException("Fudge message is not a Currency - field 'currency' is not present");
    }
    return Currency.of(currencyStr);
  }

}
