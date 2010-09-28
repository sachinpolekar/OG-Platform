/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.model.interestrate.curve.ConstantYieldCurve;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.volatility.surface.ConstantVolatilitySurface;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class FXOptionDataBundleTest {
  private static final YieldAndDiscountCurve DOMESTIC = new ConstantYieldCurve(0.03);
  private static final YieldAndDiscountCurve FOREIGN = new ConstantYieldCurve(0.05);
  private static final VolatilitySurface SIGMA = new ConstantVolatilitySurface(0.3);
  private static final double SPOT = 1.5;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 7, 1);
  private static final FXOptionDataBundle DATA = new FXOptionDataBundle(DOMESTIC, FOREIGN, SIGMA, SPOT, DATE);

  @Test
  public void testGetters() {
    assertEquals(DATA.getCostOfCarry(), -0.02, 1e-15);
    assertEquals(DATA.getForeignInterestRate(Math.random()), 0.05, 0);
    assertEquals(DATA.getForeignInterestRateCurve(), FOREIGN);
  }

  @Test
  public void testEqualsAndHashCode() {
    FXOptionDataBundle other = new FXOptionDataBundle(DOMESTIC, FOREIGN, SIGMA, SPOT, DATE);
    assertEquals(other, DATA);
    assertEquals(other.hashCode(), DATA.hashCode());
    other = new FXOptionDataBundle(FOREIGN, FOREIGN, SIGMA, SPOT, DATE);
    assertFalse(other.equals(DATA));
    other = new FXOptionDataBundle(DOMESTIC, DOMESTIC, SIGMA, SPOT, DATE);
    assertFalse(other.equals(DATA));
    other = new FXOptionDataBundle(DOMESTIC, FOREIGN, new ConstantVolatilitySurface(0.2), SPOT, DATE);
    assertFalse(other.equals(DATA));
    other = new FXOptionDataBundle(DOMESTIC, FOREIGN, SIGMA, SPOT + 1, DATE);
    assertFalse(other.equals(DATA));
    other = new FXOptionDataBundle(DOMESTIC, FOREIGN, SIGMA, SPOT, DATE.plusDays(2));
    assertFalse(other.equals(DATA));
  }
}