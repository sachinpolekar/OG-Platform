/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.curve;

import static org.junit.Assert.assertEquals;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.FXOptionDataBundle;
import com.opengamma.financial.model.volatility.curve.FXVannaVolgaVolatilityCurveDataBundle;
import com.opengamma.financial.model.volatility.curve.FXVannaVolgaVolatilityCurveModel;
import com.opengamma.financial.model.volatility.curve.VolatilityCurve;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class FXVannaVolgaVolatilityCurveModelTest {
  private static final YieldAndDiscountCurve DOMESTIC = new ConstantDiscountCurve(0.9902752);
  private static final YieldAndDiscountCurve FOREIGN = new ConstantDiscountCurve(0.9945049);
  private static final double SPOT = 1.205;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final ZonedDateTime MATURITY = DateUtil.getDateOffsetWithYearFraction(DATE, 94. / 365);
  private static final double RR = -0.005;
  private static final double ATM = 0.0905;
  private static final double VWB = 0.0013;
  private static final FXOptionDataBundle DATA = new FXOptionDataBundle(DOMESTIC, FOREIGN, new ConstantVolatilitySurface(ATM), SPOT, DATE);
  private static final FXVannaVolgaVolatilityCurveDataBundle MARKET_DATA = new FXVannaVolgaVolatilityCurveDataBundle(0.25, RR, ATM, VWB, MATURITY);
  private static final FXVannaVolgaVolatilityCurveModel MODEL = new FXVannaVolgaVolatilityCurveModel();

  @Test(expected = IllegalArgumentException.class)
  public void testNullMarketQuotes() {
    MODEL.getCurve(null, DATA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    MODEL.getCurve(MARKET_DATA, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPair() {
    MODEL.getCurve(MARKET_DATA, DATA).getVolatility(null);
  }

  @Test
  public void test() {
    final VolatilityCurve curve = MODEL.getCurve(MARKET_DATA, DATA);
    assertEquals(curve.getVolatility(1.1733), 0.0943, 1e-4);
    assertEquals(curve.getVolatility(1.2114), 0.0905, 1e-4);
    assertEquals(curve.getVolatility(1.2487), 0.0893, 1e-4);
    assertEquals(curve.getVolatility(1e-7), curve.getVolatility(1e-4), 1e-4);
    assertEquals(curve.getVolatility(5.), curve.getVolatility(6.), 1e-4);
  }
}