/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

import com.opengamma.math.surface.Surface;

/**
 * Explicit solver for the PDE $\frac{\partial f}{\partial t} + a(t,x) \frac{\partial^2 f}{\partial x^2}$ + b(t,x) \frac{\partial f}{\partial x} + (t,x)V = 0$
 */
public class ExplicitFiniteDifference {

  public double[] solve(ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary,
      final Surface<Double, Double, Double> freeBoundary) {

    double dt = tMax / (tSteps);
    double dx = (upperBoundary.getLevel() - lowerBoundary.getLevel()) / (xSteps);
    double nu1 = dt / dx / dx;
    double nu2 = dt / dx;

    double[] f = new double[xSteps + 1];
    double[] x = new double[xSteps + 1];

    double currentX = lowerBoundary.getLevel();

    for (int j = 0; j <= xSteps; j++) {
      currentX = lowerBoundary.getLevel() + j * dx;
      x[j] = currentX;
      double value = pdeData.getInitialValue(currentX);
      f[j] = value;
    }

    double t = 0.0;
    for (int i = 0; i < tSteps; i++) {
      double[] fNew = new double[xSteps + 1];
      for (int j = 1; j < xSteps; j++) {
        double a = pdeData.getA(t, x[j]);
        double b = pdeData.getB(t, x[j]);
        double c = pdeData.getC(t, x[j]);
        double aa = -nu1 * a + 0.5 * nu2 * b;
        double bb = 2 * nu1 * a - dt * c + 1;
        double cc = -nu1 * a - 0.5 * nu2 * b;
        fNew[j] = aa * f[j - 1] + bb * f[j] + cc * f[j + 1];
      }

      double[] temp = lowerBoundary.getRightMatrixCondition(pdeData, t);
      double sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[k];
      }
      double q = sum + lowerBoundary.getConstant(pdeData, t);

      sum = 0;
      temp = lowerBoundary.getLeftMatrixCondition(pdeData, t);
      for (int k = 1; k < temp.length; k++) {
        sum += temp[k] * fNew[k];
      }
      fNew[0] = (q - sum) / temp[0];

      temp = upperBoundary.getRightMatrixCondition(pdeData, t);
      sum = 0;
      for (int k = 0; k < temp.length; k++) {
        sum += temp[k] * f[xSteps + k + 1 - temp.length];
      }
      q = sum + upperBoundary.getConstant(pdeData, t);

      sum = 0;
      temp = upperBoundary.getLeftMatrixCondition(pdeData, t);
      for (int k = 0; k < temp.length - 1; k++) {
        sum += temp[k] * fNew[xSteps + k + 1 - temp.length];
      }

      fNew[xSteps] = (q - sum) / temp[temp.length - 1];

      // TODO American payoff
      t += dt;
      f = fNew;
    }

    return f;

  }
}