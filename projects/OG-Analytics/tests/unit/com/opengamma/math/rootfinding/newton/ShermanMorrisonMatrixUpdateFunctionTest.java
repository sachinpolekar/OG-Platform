/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding.newton;

import org.junit.Test;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.MatrixAlgebra;
import com.opengamma.math.matrix.OGMatrixAlgebra;

/**
 * 
 */
public class ShermanMorrisonMatrixUpdateFunctionTest {
  private static final MatrixAlgebra ALGEBRA = new OGMatrixAlgebra();
  private static final ShermanMorrisonMatrixUpdateFunction UPDATE = new ShermanMorrisonMatrixUpdateFunction(ALGEBRA);
  private static final DoubleMatrix1D V = new DoubleMatrix1D(new double[] {1, 2});
  private static final DoubleMatrix2D M = new DoubleMatrix2D(new double[][] {new double[] {3, 4}, new double[] {5, 6}});
  private static final Function1D<DoubleMatrix1D, DoubleMatrix2D> J = new Function1D<DoubleMatrix1D, DoubleMatrix2D>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
      return ALGEBRA.getOuterProduct(x, x);
    }

  };

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    new ShermanMorrisonMatrixUpdateFunction(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDeltaX() {
    UPDATE.getUpdatedMatrix(J, V, null, V, M);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullDeltaY() {
    UPDATE.getUpdatedMatrix(J, V, V, null, M);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMatrix() {
    UPDATE.getUpdatedMatrix(J, V, V, V, null);
  }
}