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
import com.opengamma.util.time.Expiry;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.integer.FastArrayIntDoubleTimeSeries;

/**
 * 
 */
public class FadeInOptionDefinitionTest {
  private static final double SPOT = 50;
  private static final double DIFF = 5;
  private static final double LOWER = 50 - DIFF;
  private static final double UPPER = 50 + DIFF;
  private static final DoubleTimeSeries<?> ALL_WITHIN_RANGE = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_DDMMYYYY, new int[] {20100501, 20100502, 20100503, 20100504, 20100505},
      new double[] {SPOT, SPOT, SPOT, SPOT, SPOT});
  private static final DoubleTimeSeries<?> ONE_WITHIN_RANGE = new FastArrayIntDoubleTimeSeries(DateTimeNumericEncoding.DATE_DDMMYYYY, new int[] {20100501, 20100502, 20100503, 20100504, 20100505},
      new double[] {SPOT + 2 * DIFF, SPOT + 3 * DIFF, SPOT, SPOT - 1.5 * DIFF, SPOT - 4 * DIFF});
  private static final YieldAndDiscountCurve CURVE = new ConstantYieldCurve(0.06);
  private static final double B = 0.04;
  private static final VolatilitySurface SURFACE = new ConstantVolatilitySurface(0.4);
  private static final ZonedDateTime DATE = DateUtil.getUTCDate(2010, 5, 6);
  private static final StandardOptionDataBundle DATA = new StandardOptionDataBundle(CURVE, B, SURFACE, SPOT, DATE);
  private static final StandardOptionWithSpotTimeSeriesDataBundle ALL_DATA = new StandardOptionWithSpotTimeSeriesDataBundle(DATA, ALL_WITHIN_RANGE);
  private static final StandardOptionWithSpotTimeSeriesDataBundle ONE_DATA = new StandardOptionWithSpotTimeSeriesDataBundle(DATA, ONE_WITHIN_RANGE);
  private static final Expiry EXPIRY = new Expiry(DATE);
  private static final FadeInOptionDefinition CALL = new FadeInOptionDefinition(SPOT, EXPIRY, true, LOWER, UPPER);
  private static final FadeInOptionDefinition PUT = new FadeInOptionDefinition(SPOT, EXPIRY, false, LOWER, UPPER);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeLowerBound() {
    new FadeInOptionDefinition(SPOT, EXPIRY, true, -LOWER, UPPER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeUpperBound() {
    new FadeInOptionDefinition(SPOT, EXPIRY, true, LOWER, -UPPER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpperSmallerThanHigher() {
    new FadeInOptionDefinition(SPOT, EXPIRY, true, UPPER, LOWER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPayoffWithNullDataBundle() {
    CALL.getPayoffFunction().getPayoff(null, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPayoffWithNullTS() {
    CALL.getPayoffFunction().getPayoff(new StandardOptionWithSpotTimeSeriesDataBundle(DATA, null), null);
  }

  @Test
  public void testExerciseFunction() {
    OptionExerciseFunction<StandardOptionWithSpotTimeSeriesDataBundle> exercise = CALL.getExerciseFunction();
    assertFalse(exercise.shouldExercise(ALL_DATA, null));
    assertFalse(exercise.shouldExercise(ONE_DATA, null));
    exercise = PUT.getExerciseFunction();
    assertFalse(exercise.shouldExercise(ALL_DATA, null));
    assertFalse(exercise.shouldExercise(ONE_DATA, null));
  }

  @Test
  public void testPayoff() {
    final double eps = 1e-15;
    OptionPayoffFunction<StandardOptionWithSpotTimeSeriesDataBundle> payoff = CALL.getPayoffFunction();
    assertEquals(payoff.getPayoff(ALL_DATA.withSpot(SPOT - 1), null), 0, eps);
    assertEquals(payoff.getPayoff(ALL_DATA.withSpot(SPOT + 1), null), 1, eps);
    assertEquals(payoff.getPayoff(ONE_DATA.withSpot(SPOT - 1), null), 0, eps);
    assertEquals(payoff.getPayoff(ONE_DATA.withSpot(SPOT + 1), null), 0.2, eps);
    payoff = PUT.getPayoffFunction();
    assertEquals(payoff.getPayoff(ALL_DATA.withSpot(SPOT - 1), null), 1, eps);
    assertEquals(payoff.getPayoff(ALL_DATA.withSpot(SPOT + 1), null), 0, eps);
    assertEquals(payoff.getPayoff(ONE_DATA.withSpot(SPOT - 1), null), 0.2, eps);
    assertEquals(payoff.getPayoff(ONE_DATA.withSpot(SPOT + 1), null), 0, eps);
  }

  @Test
  public void testEqualsAndHashCode() {
    OptionDefinition call = new FadeInOptionDefinition(SPOT, EXPIRY, true, LOWER, UPPER);
    final OptionDefinition put = new FadeInOptionDefinition(SPOT, EXPIRY, false, LOWER, UPPER);
    assertEquals(call, CALL);
    assertEquals(put, PUT);
    assertEquals(call.hashCode(), CALL.hashCode());
    assertEquals(put.hashCode(), PUT.hashCode());
    assertFalse(call.equals(put));
    call = new FadeInOptionDefinition(SPOT + 1, EXPIRY, true, LOWER, UPPER);
    assertFalse(call.equals(CALL));
    call = new FadeInOptionDefinition(SPOT, new Expiry(DateUtil.getDateOffsetWithYearFraction(DATE, 1)), true, LOWER, UPPER);
    assertFalse(call.equals(CALL));
    call = new FadeInOptionDefinition(SPOT, EXPIRY, true, LOWER + 1, UPPER);
    assertFalse(call.equals(CALL));
    call = new FadeInOptionDefinition(SPOT, EXPIRY, true, LOWER, UPPER + 1);
    assertFalse(call.equals(CALL));
  }
}