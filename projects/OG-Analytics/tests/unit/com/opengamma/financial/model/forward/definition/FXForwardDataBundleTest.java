/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.forward.definition;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.time.DateUtil;

/**
 *
 */
public class FXForwardDataBundleTest {
  private static final YieldAndDiscountCurve FOREIGN = new YieldCurve(ConstantDoublesCurve.from(0.03));
  private static final YieldAndDiscountCurve DOMESTIC = new YieldCurve(ConstantDoublesCurve.from(0.05));
  private static final double SPOT = 1.5;
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 1, 1);
  private static final FXForwardDataBundle DATA = new FXForwardDataBundle(DOMESTIC, FOREIGN, SPOT, DATE);

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDomesticCurveConstructor() {
    new FXForwardDataBundle(null, FOREIGN, SPOT, DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testForeignCurveConstructor() {
    new FXForwardDataBundle(DOMESTIC, null, -SPOT, DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeSpotConstructor() {
    new FXForwardDataBundle(DOMESTIC, FOREIGN, -SPOT, DATE);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateConstructor() {
    new FXForwardDataBundle(DOMESTIC, FOREIGN, SPOT, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDomesticCurveBuilder() {
    DATA.withDiscountCurve(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForeignCurveBuilder() {
    DATA.withForeignCurve(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNegativeSpotBuilder() {
    DATA.withSpot(-SPOT);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDateBuilder() {
    DATA.withDate(null);
  }

  @Test
  public void testEqualsAndHashCode() {
    ForwardDataBundle data = new FXForwardDataBundle(DOMESTIC, FOREIGN, SPOT, DATE);
    assertEquals(data, DATA);
    assertEquals(data.hashCode(), DATA.hashCode());
    data = new FXForwardDataBundle(new YieldCurve(ConstantDoublesCurve.from(0.12)), FOREIGN, SPOT, DATE);
    assertFalse(data.equals(DATA));
    data = new FXForwardDataBundle(DOMESTIC, new YieldCurve(ConstantDoublesCurve.from(0.07)), SPOT, DATE);
    assertFalse(data.equals(DATA));
    data = new FXForwardDataBundle(DOMESTIC, FOREIGN, SPOT - 1, DATE);
    assertFalse(data.equals(DATA));
    data = new FXForwardDataBundle(DOMESTIC, FOREIGN, SPOT, DATE.plusDays(4));
    assertFalse(data.equals(DATA));

  }

  @Test
  public void testBuilders() {
    final YieldAndDiscountCurve curve = new YieldCurve(ConstantDoublesCurve.from(0.02));
    final double spot = 2;
    final ZonedDateTime date = DateUtil.getUTCDate(2010, 2, 1);
    assertEquals(DATA.withDate(date), new FXForwardDataBundle(DOMESTIC, FOREIGN, SPOT, date));
    assertEquals(DATA.withDiscountCurve(curve), new FXForwardDataBundle(curve, FOREIGN, SPOT, DATE));
    assertEquals(DATA.withForeignCurve(curve), new FXForwardDataBundle(DOMESTIC, curve, SPOT, DATE));
    assertEquals(DATA.withSpot(spot), new FXForwardDataBundle(DOMESTIC, FOREIGN, spot, DATE));
  }
}
