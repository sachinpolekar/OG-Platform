/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.definition.BondFutureTransaction;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the present value and its sensitivities for an bond future with discounting (using the cheapest-to-deliver). 
 * The delivery option is not taken into account.
 */
public class BondFutureTransactionDiscountingMethod extends BondFutureTransactionMethod {

  /**
   * The bond future security method.
   */
  private static final BondFutureSecurityDiscountingMethod METHOD_SECURITY = new BondFutureSecurityDiscountingMethod();

  /**
   * Computes the present value of future from the curves using an the cheapest-to-deliver and computing the value as a forward..
   * @param future The future.
   * @param curves The yield curves. Should contain the credit and repo curves associated to the instrument.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final BondFutureTransaction future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    double futurePrice = METHOD_SECURITY.priceFromCurves(future.getUnderlyingFuture(), curves);
    double pv = presentValueFromPrice(future, futurePrice);
    return CurrencyAmount.of(future.getUnderlyingFuture().getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(InterestRateDerivative instrument, YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof BondFutureTransaction, "Bond future transaction");
    return presentValue((BondFutureTransaction) instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a bond future by discounting.
   * @param future The future.
   * @param curves The yield curves. Should contain the credit and repo curves associated. 
   * @return The present value rate sensitivity.
   */
  public PresentValueSensitivity presentValueCurveSensitivity(final BondFutureTransaction future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    PresentValueSensitivity priceSensitivity = METHOD_SECURITY.priceCurveSensitivity(future.getUnderlyingFuture(), curves);
    PresentValueSensitivity transactionSensitivity = priceSensitivity.multiply(future.getQuantity());
    return transactionSensitivity;
  }

}
