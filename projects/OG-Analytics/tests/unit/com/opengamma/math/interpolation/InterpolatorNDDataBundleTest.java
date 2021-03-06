/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import org.testng.annotations.Test;
import java.util.ArrayList;
import java.util.List;

import com.opengamma.math.interpolation.data.InterpolatorNDDataBundle;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterpolatorNDDataBundleTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData() {
    new InterpolatorNDDataBundle(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData() {
    List<Pair<double[], Double>> data = new ArrayList<Pair<double[], Double>>();
    new InterpolatorNDDataBundle(data);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData2() {
    List<Pair<double[], Double>> data = new ArrayList<Pair<double[], Double>>();
    double[] temp = new double[] {};
    Pair<double[], Double> pair = new ObjectsPair<double[], Double>(temp, 0.0);
    data.add(pair);
    new InterpolatorNDDataBundle(data);
  }

}
