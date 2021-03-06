/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.calculator;

import com.opengamma.financial.forex.derivative.Forex;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.forex.derivative.ForexSwap;
import com.opengamma.financial.forex.method.ForexDiscountingMethod;
import com.opengamma.financial.forex.method.ForexOptionVanillaBlackMethod;
import com.opengamma.financial.forex.method.ForexSwapDiscountingMethod;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculator of the currency exposure for Forex derivatives.
 */
public class CurrencyExposureForexCalculator extends AbstractForexDerivativeVisitor<YieldCurveBundle, MultipleCurrencyAmount> {

  /**
   * The unique instance of the calculator.
   */
  private static final CurrencyExposureForexCalculator s_instance = new CurrencyExposureForexCalculator();

  /**
   * Gets the calculator instance.
   * @return The calculator.
   */
  public static CurrencyExposureForexCalculator getInstance() {
    return s_instance;
  }

  /**
   * Constructor.
   */
  public CurrencyExposureForexCalculator() {
  }

  @Override
  public MultipleCurrencyAmount visitForex(final Forex derivative, final YieldCurveBundle data) {
    final ForexDiscountingMethod method = ForexDiscountingMethod.getInstance();
    return method.currencyExposure(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexSwap(final ForexSwap derivative, final YieldCurveBundle data) {
    final ForexSwapDiscountingMethod method = ForexSwapDiscountingMethod.getInstance();
    return method.currencyExposure(derivative, data);
  }

  @Override
  public MultipleCurrencyAmount visitForexOptionVanilla(final ForexOptionVanilla derivative, final YieldCurveBundle data) {
    final ForexOptionVanillaBlackMethod method = ForexOptionVanillaBlackMethod.getInstance();
    return method.currencyExposure(derivative, data);
  }

}
