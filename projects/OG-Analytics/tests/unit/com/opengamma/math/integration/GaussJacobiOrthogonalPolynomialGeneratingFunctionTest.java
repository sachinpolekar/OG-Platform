/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class GaussJacobiOrthogonalPolynomialGeneratingFunctionTest extends OrthogonalPolynomialGeneratingFunctionTestCase {
  private static final QuadratureWeightAndAbscissaFunction GAUSS_LEGENDRE = new GaussLegendreOrthogonalPolynomialGeneratingFunction();
  private static final QuadratureWeightAndAbscissaFunction GAUSS_JACOBI_GL_EQUIV = new GaussJacobiOrthogonalPolynomialGeneratingFunction(0, 0);
  private static final QuadratureWeightAndAbscissaFunction GAUSS_JACOBI_CHEBYSHEV_EQUIV = new GaussJacobiOrthogonalPolynomialGeneratingFunction(-0.5, -0.5);
  private static final double EPS = 1e-8;

  @Test
  public void test() {
    final int n = 12;
    final GaussianQuadratureData f1 = GAUSS_LEGENDRE.generate(n);
    final GaussianQuadratureData f2 = GAUSS_JACOBI_GL_EQUIV.generate(n);
    final GaussianQuadratureData f3 = GAUSS_JACOBI_CHEBYSHEV_EQUIV.generate(n);
    final double[] w1 = f1.getWeights();
    final double[] w2 = f2.getWeights();
    final double[] x1 = f1.getAbscissas();
    final double[] x2 = f2.getAbscissas();
    assertTrue(w1.length == w2.length);
    assertTrue(x1.length == x2.length);
    for (int i = 0; i < n; i++) {
      assertEquals(w1[i], w2[i], EPS);
      assertEquals(x1[i], -x2[i], EPS);
    }
    final double[] w3 = f3.getWeights();
    final double[] x3 = f3.getAbscissas();
    final double chebyshevWeight = Math.PI / n;
    final Function1D<Integer, Double> chebyshevAbscissa = new Function1D<Integer, Double>() {

      @Override
      public Double evaluate(final Integer x) {
        return -Math.cos(Math.PI * (x + 0.5) / n);
      }

    };
    for (int i = 0; i < n; i++) {
      assertEquals(chebyshevWeight, w3[i], EPS);
      assertEquals(chebyshevAbscissa.evaluate(i), -x3[i], EPS);
    }
  }

  @Override
  protected QuadratureWeightAndAbscissaFunction getFunction() {
    return GAUSS_JACOBI_GL_EQUIV;
  }

}
