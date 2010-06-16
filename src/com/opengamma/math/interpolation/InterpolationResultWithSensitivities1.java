/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.util.Arrays;

/**
 * 
 */
public class InterpolationResultWithSensitivities1 {

  private final double _result;
  private final double[] _sensitivities;

  /**
   * @param result
   */
  public InterpolationResultWithSensitivities1(final double result, final double[] sensitivities) {
    _result = result;
    _sensitivities = sensitivities;
  }

  public double[] getSensitivities() {
    return _sensitivities;
  }

  public double getResult() {
    return _result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + Arrays.hashCode(_sensitivities);
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    InterpolationResultWithSensitivities1 other = (InterpolationResultWithSensitivities1) obj;
    if (!Arrays.equals(_sensitivities, other._sensitivities)) {
      return false;
    }
    return true;
  }

}
