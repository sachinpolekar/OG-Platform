/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cern.jet.random.engine.MersenneTwister64;

import com.opengamma.financial.timeseries.analysis.AutocorrelationFunctionCalculator;
import com.opengamma.financial.timeseries.analysis.DoubleTimeSeriesStatisticsCalculator;
import com.opengamma.math.statistics.descriptive.MeanCalculator;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class AutoregressiveTimeSeriesModelTest {
  private static final double MEAN = 0;
  private static final double STD = 0.25;
  private static final AutoregressiveTimeSeriesModel MODEL = new AutoregressiveTimeSeriesModel(new NormalDistribution(MEAN, STD, new MersenneTwister64(
      MersenneTwister64.DEFAULT_SEED)));
  private static final int ORDER = 2;
  private static final DoubleTimeSeries<Long> MA;
  private static final double[] PHI;
  private static double LIMIT = 3;

  static {
    final int n = 20000;
    final long[] dates = new long[n];
    for (int i = 0; i < n; i++) {
      dates[i] = i;
    }
    PHI = new double[ORDER + 1];
    for (int i = 0; i <= ORDER; i++) {
      PHI[i] = (i + 1) / 10.;
    }
    MA = MODEL.getSeries(PHI, ORDER, dates);
    LIMIT /= Math.sqrt(n);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadConstructor() {
    new AutoregressiveTimeSeriesModel(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullPhis() {
    MODEL.getSeries(null, 2, new long[] {1});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyPhis() {
    MODEL.getSeries(new double[0], 2, new long[] {1});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeOrder() {
    MODEL.getSeries(new double[] {0.2}, -3, new long[] {1});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInsufficientPhis() {
    MODEL.getSeries(new double[] {0.2}, 4, new long[] {1});
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDates() {
    MODEL.getSeries(new double[] {0.3, 0.4}, 1, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyDates() {
    MODEL.getSeries(new double[] {0.3, 0.4}, 1, new long[0]);
  }

  @Test
  public void testACF() {
    final double eps = 1e-2;
    final double[] rho = new AutocorrelationFunctionCalculator().evaluate(MA);
    final double rho1 = PHI[1] / (1 - PHI[2]);
    assertEquals(rho[0], 1, 1e-16);
    assertEquals(rho[1], rho1, eps);
    assertEquals(rho[2], rho1 * PHI[1] + PHI[2], eps);
    final Double mean = new DoubleTimeSeriesStatisticsCalculator(new MeanCalculator()).evaluate(MA);
    assertEquals(mean, PHI[0] / (1 - PHI[1] - PHI[2]), eps);
  }
}