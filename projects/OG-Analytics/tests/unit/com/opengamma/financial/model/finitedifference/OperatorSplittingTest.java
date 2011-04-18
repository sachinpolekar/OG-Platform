/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.testng.annotations.Test;

import com.opengamma.financial.model.finitedifference.ConvectionDiffusionPDESolver2D;
import com.opengamma.financial.model.finitedifference.OperatorSplittingFiniteDifference2D;

/**
 * NOT WORKING 
 */
public class OperatorSplittingTest {

  private static final HestonPDETestCase HESTON_TESTER = new HestonPDETestCase();
  private static final SpreadOptionPDETestCase SPREAD_OPTION_TESTER = new SpreadOptionPDETestCase();
  private static final ConvectionDiffusionPDESolver2D SOLVER = new OperatorSplittingFiniteDifference2D();

  @Test(enabled = false)
  public void testSpreadOption() {

    int timeSteps = 10;
    int xSteps = 100;
    int ySteps = 100;

    SPREAD_OPTION_TESTER.testAgaintBSPrice(SOLVER, timeSteps, xSteps, ySteps);
  }

  @Test(enabled = false)
  public void testHeston() {

    int timeSteps = 25;
    int xSteps = 100;
    int ySteps = 100;

    HESTON_TESTER.testCallPrice(SOLVER, timeSteps, xSteps, ySteps);
  }

}