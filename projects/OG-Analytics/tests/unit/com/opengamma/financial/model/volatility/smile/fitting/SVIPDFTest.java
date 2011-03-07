/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.junit.Test;

import com.opengamma.financial.model.option.DistributionFromImpliedVolatility;
import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.smile.function.SVIFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SVIVolatilityFunction;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.math.statistics.leastsquare.LeastSquareResults;

/**
 * 
 */
public class SVIPDFTest {
  private static final double A = 0.7;
  private static final double B = -1.0;
  private static final double RHO = 0.4;
  private static final double SIGMA = 0.4;
  private static final double M = 0.04;
  private static final double F = 0.04;
  private static final double T = 2.5;
  private static final SVIFormulaData DATA = new SVIFormulaData(A, B, RHO, SIGMA, M);
  private static final ProbabilityDistribution<Double> SVI_DIST;
  private static final Function1D<Double, Double> SVI = new Function1D<Double, Double>() {
    final SVIVolatilityFunction svi = new SVIVolatilityFunction();

    @SuppressWarnings("synthetic-access")
    @Override
    public Double evaluate(final Double k) {
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, true);
      return svi.getVolatilityFunction(option).evaluate(DATA);
    }
  };

  static {
    SVI_DIST = new DistributionFromImpliedVolatility(F, T, SVI);
  }

  @Test
  public void testSABR() {
    final double[] strikes = new double[] {0.02, 0.03, 0.035, 0.0375, 0.04, 0.0425, 0.045, 0.05, 0.06};
    final int n = strikes.length;
    final double[] vols = new double[n];
    final double[] errors = new double[n];
    final EuropeanVanillaOption[] options = new EuropeanVanillaOption[n];
    for (int i = 0; i < n; i++) {
      errors[i] = 0.001;
      vols[i] = SVI.evaluate(strikes[i]);
      options[i] = new EuropeanVanillaOption(strikes[i], T, true);
    }
    final double[] initialValues = new double[] {0.04, 1, 0.2, -0.3};
    final BitSet fixed = new BitSet();
    final SABRHaganVolatilityFunction sabr = new SABRHaganVolatilityFunction();
    final SABRFormulaData data = new SABRFormulaData(F, initialValues[0], initialValues[1], initialValues[2], initialValues[3]);

    final SABRLeastSquaresFitter fitter = new SABRLeastSquaresFitter(sabr);
    final LeastSquareResults result = fitter.solve(options, data, vols, errors, initialValues, fixed, 0, false);

    final double chiSqr = result.getChiSq();
    final DoubleMatrix1D params = result.getParameters();
    final SABRFormulaData fittedData = new SABRFormulaData(F, params.getEntry(0), params.getEntry(1), params.getEntry(2), params.getEntry(3));

    final Function1D<Double, Double> sabrFunction = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double k) {
        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, true);
        return sabr.getVolatilityFunction(option).evaluate(fittedData);
      }
    };

    final ProbabilityDistribution<Double> sabrDist = new DistributionFromImpliedVolatility(F, T, sabrFunction);

    for (int i = 0; i < 100; i++) {
      final double k = 0.001 + i * 0.1 / 100;

      final double vol = sabrFunction.evaluate(k);
      final double pdf = sabrDist.getPDF(k);
      final double cdf = sabrDist.getCDF(k);
    }
    //TODO this test does nothing
  }
}