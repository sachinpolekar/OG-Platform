/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.Collections;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.analytics.forex.ForexSecurityConverter;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.forex.calculator.ForexConverter;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.FXSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.livedata.normalization.MarketDataRequirementNames;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class ForexForwardFunction extends AbstractFunction.NonCompiledInvoker {
  private final String _payCurveName;
  private final String _receiveCurveName;
  private final String _valueRequirementName;
  private ForexSecurityConverter _visitor;
  private SecuritySource _securitySource;

  public ForexForwardFunction(final String payCurveName, final String receiveCurveName, final String valueRequirementName) {
    Validate.notNull(payCurveName, "put curve name");
    Validate.notNull(receiveCurveName, "call curve name");
    Validate.notNull(valueRequirementName, "value requirement name");
    _payCurveName = payCurveName;
    _receiveCurveName = receiveCurveName;
    _valueRequirementName = valueRequirementName;
  }

  @Override
  public void init(final FunctionCompilationContext context) {
    _securitySource = OpenGammaCompilationContext.getSecuritySource(context);
    _visitor = new ForexSecurityConverter(_securitySource);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final FXForwardSecurity security = (FXForwardSecurity) target.getSecurity();
    final ForexConverter<?> definition = _visitor.visitFXForwardSecurity(security);
    final FXSecurity fx = (FXSecurity) _securitySource.getSecurity(IdentifierBundle.of(security.getUnderlyingIdentifier()));
    final Identifier spotIdentifier = FXUtils.getSpotIdentifier(fx, true);

    //TODO the marked off area should be done in init()
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    final Currency payCurrency = fx.getPayCurrency();
    final Currency receiveCurrency = fx.getReceiveCurrency();
    final String payCurveName = _payCurveName + "_" + payCurrency.getCode();
    final String receiveCurveName = _receiveCurveName + "_" + receiveCurrency.getCode();
    final String[] curveNames = new String[] {payCurveName, receiveCurveName};
    final Object putCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(payCurrency, _payCurveName, _payCurveName, _payCurveName));
    if (putCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + payCurveName + " curve");
    }
    final YieldAndDiscountCurve putCurve = (YieldAndDiscountCurve) putCurveObject;
    final Object callCurveObject = inputs.getValue(YieldCurveFunction.getCurveRequirement(receiveCurrency, _receiveCurveName, _receiveCurveName, _receiveCurveName));
    if (callCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + receiveCurveName + " curve");
    }
    final YieldAndDiscountCurve callCurve = (YieldAndDiscountCurve) putCurveObject;
    final YieldAndDiscountCurve[] curves = new YieldAndDiscountCurve[] {putCurve, callCurve};
    final ForexDerivative fxOption = definition.toDerivative(now, curveNames);
    final YieldCurveBundle yieldCurves = new YieldCurveBundle(curveNames, curves);
    //    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier);
    //    final Object spotObject = inputs.getValue(spotRequirement);
    //    if (spotObject == null) {
    //      throw new OpenGammaRuntimeException("Could not get " + spotRequirement);
    //    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @SuppressWarnings("unchecked")
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, _payCurveName)
                                                              .with(ValuePropertyNames.RECEIVE_CURVE, _receiveCurveName).get();
    final ValueSpecification spec = new ValueSpecification(_valueRequirementName, target.toSpecification(), properties);
    return Collections.singleton(new ComputedValue(spec, getResult(fxOption, yieldCurves)));
  }

  protected abstract Object getResult(ForexDerivative fxForward, YieldCurveBundle data);

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXForwardSecurity;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final FXForwardSecurity fxForward = (FXForwardSecurity) target.getSecurity();
    final FXSecurity fx = (FXSecurity) _securitySource.getSecurity(IdentifierBundle.of(fxForward.getUnderlyingIdentifier()));
    final ValueRequirement payCurve = YieldCurveFunction.getCurveRequirement(fx.getPayCurrency(), _payCurveName, _payCurveName, _payCurveName);
    final ValueRequirement receiveCurve = YieldCurveFunction.getCurveRequirement(fx.getReceiveCurrency(), _receiveCurveName, _receiveCurveName, _receiveCurveName);
    final Identifier spotIdentifier = FXUtils.getSpotIdentifier(fx, true);
    final ValueRequirement spotRequirement = new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, spotIdentifier);
    return Sets.newHashSet(payCurve, receiveCurve, spotRequirement);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties().with(ValuePropertyNames.PAY_CURVE, _payCurveName)
                                                              .with(ValuePropertyNames.RECEIVE_CURVE, _receiveCurveName).get();
    return Collections.singleton(new ValueSpecification(_valueRequirementName, target.toSpecification(),
        properties));
  }
}
