/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueCouponSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.security.FinancialSecurity;

/**
 * 
 */
public class InterestRateInstrumentPresentValueCouponSensitivityFunction extends InterestRateInstrumentFunction {
  private static final PresentValueCouponSensitivityCalculator CALCULATOR = PresentValueCouponSensitivityCalculator
      .getInstance();
  private static final String VALUE_REQUIREMENT = ValueRequirementNames.PRESENT_VALUE_COUPON_SENSITIVITY;

  public InterestRateInstrumentPresentValueCouponSensitivityFunction() {
    super(VALUE_REQUIREMENT);
  }

  @Override
  public Set<ComputedValue> getComputedValues(InterestRateDerivative derivative, YieldCurveBundle bundle,
      FinancialSecurity security, String forwardCurveName, String fundingCurveName) {
    final Double presentValue = CALCULATOR.visit(derivative, bundle);
    final ValueSpecification specification = new ValueSpecification(new ValueRequirement(
        VALUE_REQUIREMENT, security), FixedIncomeInstrumentCurveExposureHelper.getValuePropertiesForSecurity(security,
            fundingCurveName, forwardCurveName, createValueProperties()));
    return Collections.singleton(new ComputedValue(specification, presentValue));
  }

}
