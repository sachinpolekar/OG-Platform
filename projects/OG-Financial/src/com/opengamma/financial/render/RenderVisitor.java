/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.render;

import com.opengamma.financial.greeks.GreekResultCollection;
import com.opengamma.financial.model.interestrate.curve.InterpolatedDiscountCurve;
import com.opengamma.financial.model.volatility.surface.VolatilitySurface;

/**
 * Visitor pattern visitor.
 * 
 * @param <T>  the type being visited
 */
public interface RenderVisitor<T> {

  /**
   * Visit discount curve.
   * @param discountCurve  the curve, not null
   * @return the visitor type
   */
  T visitDiscountCurve(InterpolatedDiscountCurve discountCurve);

  /**
   * Visit volatility surface.
   * @param volatilitySurface  the surface, not null
   * @return the visitor type
   */
  T visitVolatilitySurface(VolatilitySurface volatilitySurface);

  /**
   * Visit discount curves.
   * @param greekResultCollection  the greek results, not null
   * @return the visitor type
   */
  T visitGreekResultCollection(GreekResultCollection greekResultCollection);

}