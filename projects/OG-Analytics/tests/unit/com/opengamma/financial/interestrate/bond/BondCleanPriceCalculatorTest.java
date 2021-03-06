/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.bond;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.bond.definition.Bond;
import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class BondCleanPriceCalculatorTest {
  private static final BondCleanPriceCalculator CALCULATOR = BondCleanPriceCalculator.getInstance();
  private static final YieldCurveBundle CURVES = new YieldCurveBundle(new String[] {"A", "B"}, new YieldCurve[] {new YieldCurve(ConstantDoublesCurve.from(0.1)),
      new YieldCurve(ConstantDoublesCurve.from(0.11))});
  private static final double ACCRUED_INTEREST = 0.03;
  private static final double DIRTY_PRICE = 1.03;
  private static final Currency CUR = Currency.USD;
  private static final Bond BOND = new Bond(CUR, new double[] {1, 2, 3}, new double[] {0.02, 0.02, 0.02}, new double[] {1, 1, 1}, ACCRUED_INTEREST, "A");

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBond1() {
    CALCULATOR.calculate(null, 100);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBond2() {
    CALCULATOR.calculate(null, CURVES);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurves() {
    CALCULATOR.calculate(BOND, null);
  }

  @Test
  public void test() {
    assertEquals(CALCULATOR.calculate(BOND, DIRTY_PRICE), 1, 0);
    final double pv = 0.02 * (Math.exp(-0.1) + Math.exp(-0.2)) + 1.02 * Math.exp(-0.3);
    assertEquals(pv - ACCRUED_INTEREST, CALCULATOR.calculate(BOND, CURVES), 1e-12);
    final double cleanPrice = 96.8;
    assertEquals(CALCULATOR.calculate(BOND, BondDirtyPriceCalculator.getInstance().calculate(BOND, cleanPrice)), cleanPrice, 1e-15);
  }
}
