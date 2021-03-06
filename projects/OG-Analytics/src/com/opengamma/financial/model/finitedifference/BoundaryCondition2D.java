/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

/**
 * 
 */
public interface BoundaryCondition2D {

  double[] getLeftMatrixCondition(final PDEDataBundle data, final double t, final double boundaryPosition);

  double[] getRightMatrixCondition(final PDEDataBundle data, final double t, final double boundaryPosition);

  double getConstant(final PDEDataBundle data, final double t, final double boundaryPosition, final double gridSpacing);

  double getLevel();

}
