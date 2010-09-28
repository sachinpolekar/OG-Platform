/**

 * 0Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.riskfactor;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.apache.commons.math.util.MathUtils;

import com.opengamma.financial.greeks.MixedOrderUnderlying;
import com.opengamma.financial.greeks.NthOrderUnderlying;
import com.opengamma.financial.greeks.Underlying;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class TaylorExpansionMultiplierCalculator {

  public static double getMultiplier(final Underlying underlying) {
    Validate.notNull(underlying, "underlying");
    if (underlying instanceof NthOrderUnderlying) {
      final NthOrderUnderlying nthOrder = (NthOrderUnderlying) underlying;
      final int n = nthOrder.getOrder();
      if (n == 0) {
        return 1;
      }
      return 1. / MathUtils.factorial(n);
    } else if (underlying instanceof MixedOrderUnderlying) {
      final MixedOrderUnderlying mixedOrder = (MixedOrderUnderlying) underlying;
      double result = 1;
      for (final NthOrderUnderlying underlyingOrder : mixedOrder.getUnderlyingOrders()) {
        result *= getMultiplier(underlyingOrder);
      }
      return result;
    }
    throw new IllegalArgumentException("Order was neither NthOrderUnderlying nor MixedOrderUnderlying: have " + underlying.getClass());
  }

  public static double getMultiplier(final Map<Object, Double> underlyingData, final Underlying underlying) {
    Validate.notNull(underlying, "underlying");
    Validate.notNull(underlyingData, "underlying data");
    ArgumentChecker.notEmpty(underlyingData, "underlying data");
    if (underlying instanceof NthOrderUnderlying) {
      final NthOrderUnderlying nthOrder = (NthOrderUnderlying) underlying;
      final int n = nthOrder.getOrder();
      if (n == 0) {
        return 1;
      }
      final UnderlyingType type = nthOrder.getUnderlyings().iterator().next();
      final Double x = underlyingData.get(type);
      if (x == null) {
        throw new IllegalArgumentException("Could not get data for " + type);
      }
      return getMultiplier(underlying) * Math.pow(x, n);
    } else if (underlying instanceof MixedOrderUnderlying) {
      final MixedOrderUnderlying mixedOrder = (MixedOrderUnderlying) underlying;
      double result = 1;
      for (final NthOrderUnderlying underlyingOrder : mixedOrder.getUnderlyingOrders()) {
        result *= getMultiplier(underlyingData, underlyingOrder);
      }
      return result;
    }
    throw new IllegalArgumentException("Order was neither NthOrderUnderlying nor MixedOrderUnderlying: have " + underlying.getClass());
  }

}