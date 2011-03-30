/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.definition;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;
import com.opengamma.math.function.Function1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class describing the SABR parameter surfaces used in interest rate modeling.
 */
public class SABRInterestRateParameter {

  /**
   * The alpha (volatility level) surface.
   */
  private final VolatilitySurface _alphaSurface;
  //TODO: Should it be a surface of <double, Period>?
  /**
   * The beta (elasticity) surface.
   */
  private final VolatilitySurface _betaSurface;
  /**
   * The rho (correlation) surface.
   */
  private final VolatilitySurface _rhoSurface;
  /**
   * The nu (volatility of volatility) surface.
   */
  private final VolatilitySurface _nuSurface;
  /**
   * The function containing the Hagan SABR volatility formula.
   */
  private static final SABRHaganVolatilityFunction SABR_FUNCTION = new SABRHaganVolatilityFunction();

  public SABRInterestRateParameter(final VolatilitySurface alpha, final VolatilitySurface beta, final VolatilitySurface rho, final VolatilitySurface nu) {
    Validate.notNull(alpha, "alpha surface");
    Validate.notNull(beta, "beta surface");
    Validate.notNull(rho, "rho surface");
    Validate.notNull(nu, "nu surface");
    _alphaSurface = alpha;
    _betaSurface = beta;
    _rhoSurface = rho;
    _nuSurface = nu;
  }

  /**
   * Return the alpha parameter for a pair of time to expiry and instrument maturity.
   * @param expiryMaturity The expiry/maturity pair.
   * @return The alpha parameter.
   */
  public double getAlpha(DoublesPair expiryMaturity) {
    return _alphaSurface.getVolatility(expiryMaturity);
  }

  /**
   * Return the beta parameter for a pair of time to expiry and instrument maturity.
   * @param expiryMaturity The expiry/maturity pair.
   * @return The beta parameter.
   */
  public double getBeta(DoublesPair expiryMaturity) {
    return _betaSurface.getVolatility(expiryMaturity);
  }

  /**
   * Return the rho parameter for a pair of time to expiry and instrument maturity.
   * @param expiryMaturity The expiry/maturity pair.
   * @return The rho parameter.
   */
  public double getRho(DoublesPair expiryMaturity) {
    return _rhoSurface.getVolatility(expiryMaturity);
  }

  /**
   * Return the nu parameter for a pair of time to expiry and instrument maturity.
   * @param expiryMaturity The expiry/maturity pair.
   * @return The nu parameter.
   */
  public double getNu(DoublesPair expiryMaturity) {
    return _nuSurface.getVolatility(expiryMaturity);
  }

  /**
   * Return the volatility for a expiry/maturity pair, a strike and a forward rate.
   * @param expiryMaturity The expiry/maturity pair.
   * @param strike The strike.
   * @param forward The forward.
   * @return The volatility.
   */
  public double getVolatility(DoublesPair expiryMaturity, double strike, double forward) {
    SABRFormulaData data = new SABRFormulaData(forward, getAlpha(expiryMaturity), getBeta(expiryMaturity), getRho(expiryMaturity), getNu(expiryMaturity));
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiryMaturity.first, true);
    Function1D<SABRFormulaData, Double> funcSabrLongPayer = SABR_FUNCTION.getVolatilityFunction(option);
    return funcSabrLongPayer.evaluate(data);
  }

}