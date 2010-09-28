/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.junit.Assert.assertEquals;

import org.fudgemsg.FudgeMsgField;
import org.fudgemsg.types.StringFieldType;
import org.junit.Test;

import com.opengamma.financial.Currency;

public class CurrencyTest extends FinancialTestBase {

  private static final Currency s_ref = Currency.getInstance("USD");

  @Test
  public void testCycle() {
    assertEquals(s_ref, cycleObject(Currency.class, s_ref));
  }

  @Test
  public void testFromString() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(Currency.class,
        new FudgeMsgField(StringFieldType.INSTANCE, s_ref.getISOCode(), null, null)));
  }

  @Test
  public void testFromUniqueIdentifier() {
    assertEquals(s_ref, getFudgeContext().getFieldValue(Currency.class,
        new FudgeMsgField(StringFieldType.INSTANCE, s_ref.getUniqueIdentifier().toString(), null, null)));
  }

}