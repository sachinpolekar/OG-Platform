/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.riskfactor.option;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.pnl.UnderlyingType;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;

/**
 * 
 * 
 *
 * @author elaine
 */
public class UnderlyingTypeToValueRequirementMapper {

  public static ValueRequirement getValueRequirement(SecuritySource secMaster, final UnderlyingType underlying, final Security security) {
    if (security instanceof EquityOptionSecurity) {
      final EquityOptionSecurity option = (EquityOptionSecurity) security;
      Security optionUnderlying = secMaster.getSecurity(IdentifierBundle.of(option.getUnderlyingIdentifier()));
      switch (underlying) {
        case SPOT_PRICE:
          return new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, ComputationTargetType.SECURITY, optionUnderlying.getUniqueId());
        case SPOT_VOLATILITY:
          throw new NotImplementedException("Don't know how to get spot volatility for " + option.getUniqueId());
        case IMPLIED_VOLATILITY:
          throw new NotImplementedException("Don't know how to get implied volatility for " + option.getUniqueId());
        case INTEREST_RATE:
          return new ValueRequirement(ValueRequirementNames.YIELD_CURVE, ComputationTargetType.PRIMITIVE, option.getUniqueId());
        case COST_OF_CARRY:
          throw new NotImplementedException("Don't know how to get cost of carry for " + option.getUniqueId());
        default:
          throw new NotImplementedException("Don't know how to get ValueRequirement for " + underlying);
      }
    }
    throw new NotImplementedException("Can only get ValueRequirements for options (was " + security + ")");
  }
}
