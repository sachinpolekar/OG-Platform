/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.option.definition.EuropeanOptionOnEuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionDataBundle;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.time.Expiry;

/**
 * 
 */
public class EuropeanOptionOnEuropeanVanillaOptionModelTest {
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final double UNDERLYING_STRIKE = 520;
  private static final Expiry UNDERLYING_EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.5));
  private static final EuropeanVanillaOptionDefinition UNDERLYING = new EuropeanVanillaOptionDefinition(UNDERLYING_STRIKE, UNDERLYING_EXPIRY, true);
  private static final double STRIKE = 50;
  private static final Expiry EXPIRY = new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 0.25));
  private static final EuropeanOptionOnEuropeanVanillaOptionDefinition OPTION = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false, UNDERLYING);
  private static final EuropeanOptionOnEuropeanVanillaOptionModel MODEL = new EuropeanOptionOnEuropeanVanillaOptionModel();
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(new ConstantYieldCurve(0.08), 0.05, new ConstantVolatilitySurface(0.35), 500, DATE);
  private static final BlackScholesMertonModel BSM = new BlackScholesMertonModel();

  @Test(expected = IllegalArgumentException.class)
  public void testNullDefinition() {
    MODEL.getPricingFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getPricingFunction(OPTION).evaluate((StandardOptionDataBundle) null);
  }

  @Test
  public void test() {
    assertEquals(MODEL.getPricingFunction(OPTION).evaluate(DATA), 21.196, 1e-3);
    final EuropeanVanillaOptionDefinition call = new EuropeanVanillaOptionDefinition(UNDERLYING_STRIKE, UNDERLYING_EXPIRY, true);
    final EuropeanVanillaOptionDefinition put = new EuropeanVanillaOptionDefinition(UNDERLYING_STRIKE, EXPIRY, false);
    final EuropeanOptionOnEuropeanVanillaOptionDefinition callOnCall = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, call);
    final EuropeanOptionOnEuropeanVanillaOptionDefinition putOnCall = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false, call);
    assertEquals(MODEL.getPricingFunction(callOnCall).evaluate(DATA) - MODEL.getPricingFunction(putOnCall).evaluate(DATA), BSM.getPricingFunction(call).evaluate(DATA) - STRIKE
        * Math.exp(-0.08 * 0.25), 1e-3);
    final EuropeanOptionOnEuropeanVanillaOptionDefinition callOnPut = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, true, put);
    final EuropeanOptionOnEuropeanVanillaOptionDefinition putOnPut = new EuropeanOptionOnEuropeanVanillaOptionDefinition(STRIKE, EXPIRY, false, put);
    assertEquals(MODEL.getPricingFunction(callOnPut).evaluate(DATA) - MODEL.getPricingFunction(putOnPut).evaluate(DATA), BSM.getPricingFunction(put).evaluate(DATA) - STRIKE * Math.exp(-0.08 * 0.25),
        1e-3);
  }
}