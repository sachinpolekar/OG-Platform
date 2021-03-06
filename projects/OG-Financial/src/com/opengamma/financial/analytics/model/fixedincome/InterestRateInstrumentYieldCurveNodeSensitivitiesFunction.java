/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.Clock;
import javax.time.calendar.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaldata.HistoricalTimeSeriesSource;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.region.RegionSource;
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
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.OpenGammaCompilationContext;
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.DoubleLabelledMatrix1D;
import com.opengamma.financial.analytics.fixedincome.CashSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FRASecurityConverter;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeConverterDataProvider;
import com.opengamma.financial.analytics.fixedincome.FixedIncomeInstrumentCurveExposureHelper;
import com.opengamma.financial.analytics.fixedincome.InterestRateInstrumentType;
import com.opengamma.financial.analytics.fixedincome.SwapSecurityConverter;
import com.opengamma.financial.analytics.fixedincome.YieldCurveNodeSensitivityDataBundle;
import com.opengamma.financial.analytics.interestratefuture.InterestRateFutureSecurityConverter;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionSource;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.interestrate.InstrumentSensitivityCalculator;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueNodeSensitivityCalculator;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class InterestRateInstrumentYieldCurveNodeSensitivitiesFunction extends AbstractFunction.NonCompiledInvoker {
  private static final InstrumentSensitivityCalculator CALCULATOR = InstrumentSensitivityCalculator.getInstance();
  private static final String VALUE_REQUIREMENT = ValueRequirementNames.YIELD_CURVE_NODE_SENSITIVITIES;
  // TODO: This will be hit for a curve definition on each calculation cycle, so it really needs to cache stuff rather than do any I/O
  private InterpolatedYieldCurveDefinitionSource _definitionSource;
  private FinancialSecurityVisitorAdapter<FixedIncomeInstrumentConverter<?>> _visitor;
  private FixedIncomeConverterDataProvider _definitionConverter;

  @Override
  public void init(final FunctionCompilationContext context) {
    _definitionSource = OpenGammaCompilationContext.getInterpolatedYieldCurveDefinitionSource(context);
    final HolidaySource holidaySource = OpenGammaCompilationContext.getHolidaySource(context);
    final RegionSource regionSource = OpenGammaCompilationContext.getRegionSource(context);
    final ConventionBundleSource conventionSource = OpenGammaCompilationContext
        .getConventionBundleSource(context);
    final CashSecurityConverter cashConverter = new CashSecurityConverter(holidaySource, conventionSource);
    final FRASecurityConverter fraConverter = new FRASecurityConverter(holidaySource, regionSource, conventionSource);
    final SwapSecurityConverter swapConverter = new SwapSecurityConverter(holidaySource, conventionSource,
        regionSource);
    final InterestRateFutureSecurityConverter irFutureConverter = new InterestRateFutureSecurityConverter(holidaySource, conventionSource, regionSource);
    _visitor =
        FinancialSecurityVisitorAdapter.<FixedIncomeInstrumentConverter<?>>builder()
            .cashSecurityVisitor(cashConverter).fraSecurityVisitor(fraConverter).swapSecurityVisitor(swapConverter)
            .futureSecurityVisitor(irFutureConverter).create();
    _definitionConverter = new FixedIncomeConverterDataProvider("BLOOMBERG", "PX_LAST", conventionSource); //TODO this should not be hard-coded
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
      final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final FinancialSecurity security = (FinancialSecurity) target.getSecurity();
    final Clock snapshotClock = executionContext.getValuationClock();
    final ZonedDateTime now = snapshotClock.zonedDateTime();
    final HistoricalTimeSeriesSource dataSource = OpenGammaExecutionContext.getHistoricalTimeSeriesSource(executionContext);
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(desiredValues);
    final String forwardCurveName = curveNames.getFirst();
    final String fundingCurveName = curveNames.getSecond();
    final ValueRequirement forwardCurveRequirement = getCurveRequirement(target, forwardCurveName, null, null);
    final Object forwardCurveObject = inputs.getValue(forwardCurveRequirement);
    if (forwardCurveObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + forwardCurveRequirement);
    }
    Object fundingCurveObject = null;
    if (!forwardCurveName.equals(fundingCurveName)) {
      final ValueRequirement fundingCurveRequirement = getCurveRequirement(target, fundingCurveName, null, null);
      fundingCurveObject = inputs.getValue(fundingCurveRequirement);
      if (fundingCurveObject == null) {
        throw new OpenGammaRuntimeException("Could not get " + fundingCurveRequirement);
      }
    }
    final Object jacobianObject = inputs.getValue(ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    if (jacobianObject == null) {
      throw new OpenGammaRuntimeException("Could not get " + ValueRequirementNames.YIELD_CURVE_JACOBIAN);
    }
    final YieldAndDiscountCurve forwardCurve = (YieldAndDiscountCurve) forwardCurveObject;
    final YieldAndDiscountCurve fundingCurve = fundingCurveObject == null ? forwardCurve
        : (YieldAndDiscountCurve) fundingCurveObject;
    final FixedIncomeInstrumentConverter<?> definition = security.accept(_visitor);
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition for security " + security + " was null");
    }
    final InterestRateDerivative derivative = _definitionConverter.convert(security, definition, now,
        FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security,
            fundingCurveName, forwardCurveName), dataSource);
    final double[][] array = decodeJacobian(jacobianObject);
    final YieldCurveBundle bundle = new YieldCurveBundle(new String[] {forwardCurveName, fundingCurveName},
        new YieldAndDiscountCurve[] {forwardCurve, fundingCurve});
    final DoubleMatrix2D parRateJacobian = new DoubleMatrix2D(array);
    final LinkedHashMap<String, YieldAndDiscountCurve> interpolatedCurves = new LinkedHashMap<String, YieldAndDiscountCurve>();
    interpolatedCurves.put(forwardCurveName, bundle.getCurve(forwardCurveName));
    interpolatedCurves.put(fundingCurveName, bundle.getCurve(fundingCurveName));
    final DoubleMatrix1D sensitivitiesForCurves = CALCULATOR.calculateFromParRate(derivative, null, interpolatedCurves, parRateJacobian, PresentValueNodeSensitivityCalculator.getDefaultInstance());
    final Currency currency = FinancialSecurityUtils.getCurrency(target.getSecurity());
    if (fundingCurveName.equals(forwardCurveName)) {
      return getSensitivitiesForSingleCurve(target, security, curveNames.getFirst(), bundle, sensitivitiesForCurves, currency);
    }
    return getSensitivitiesForMultipleCurves(target, security, forwardCurveName, fundingCurveName, bundle, sensitivitiesForCurves, currency);
  }

  private Set<ComputedValue> getSensitivitiesForSingleCurve(final ComputationTarget target, final FinancialSecurity security, final String curveName,
      final YieldCurveBundle bundle, final DoubleMatrix1D sensitivitiesForCurve, final Currency currency) {
    final int n = sensitivitiesForCurve.getNumberOfElements();
    final YieldAndDiscountCurve curve = bundle.getCurve(curveName);
    final Double[] keys = curve.getCurve().getXData();
    final double[] values = new double[n];
    final Object[] labels = YieldCurveLabelGenerator.getLabels(_definitionSource, currency, curveName);
    DoubleLabelledMatrix1D labelledMatrix = new DoubleLabelledMatrix1D(keys, labels, values);
    for (int i = 0; i < n; i++) {
      labelledMatrix = (DoubleLabelledMatrix1D) labelledMatrix.add(keys[i], labels[i], sensitivitiesForCurve.getEntry(i));
    }
    final YieldCurveNodeSensitivityDataBundle data = new YieldCurveNodeSensitivityDataBundle(currency, labelledMatrix, curveName);
    ValueProperties resultProperties = FixedIncomeInstrumentCurveExposureHelper.getValuePropertiesForSecurity(
        (FinancialSecurity) target.getSecurity(), curveName, curveName, createValueProperties());
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    resultProperties = resultProperties.copy().with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode()).get();
    final ValueSpecification specification = new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), resultProperties);
    return Collections.singleton(new ComputedValue(specification, data.getLabelledMatrix()));
  }

  //TODO at some point this needs to deal with more than two curves
  private Set<ComputedValue> getSensitivitiesForMultipleCurves(final ComputationTarget target, final FinancialSecurity security, final String forwardCurveName, final String fundingCurveName,
      final YieldCurveBundle bundle, final DoubleMatrix1D sensitivitiesForCurves, final Currency currency) {
    final int nForward = bundle.getCurve(forwardCurveName).getCurve().size();
    final int nFunding = bundle.getCurve(fundingCurveName).getCurve().size();
    final Map<String, DoubleMatrix1D> sensitivities = new HashMap<String, DoubleMatrix1D>();
    sensitivities.put(forwardCurveName, new DoubleMatrix1D(Arrays.copyOfRange(sensitivitiesForCurves.toArray(), 0, nForward)));
    sensitivities.put(fundingCurveName, new DoubleMatrix1D(Arrays.copyOfRange(sensitivitiesForCurves.toArray(), nForward, nFunding + 1)));
    final String[] relevantCurvesForDerivative = FixedIncomeInstrumentCurveExposureHelper.getCurveNamesForSecurity(security,
        fundingCurveName, forwardCurveName);
    final Set<ComputedValue> results = new HashSet<ComputedValue>();
    for (final String curveName : relevantCurvesForDerivative) {
      results.addAll(getSensitivitiesForSingleCurve(target, security, curveName, bundle, sensitivities.get(curveName), currency));
    }
    return results;
  }

  private double[][] decodeJacobian(final Object jacobianObject) {
    final double[][] array;
    // Fudge encodings of double[][] and List<double[]> are identical, so receiving either is valid.
    if (jacobianObject instanceof double[][]) {
      array = (double[][]) jacobianObject;
    } else if (jacobianObject instanceof List<?>) {
      @SuppressWarnings("unchecked")
      final List<double[]> parRateJacobianList = (List<double[]>) jacobianObject;
      final int rows = parRateJacobianList.size();
      array = new double[rows][];
      int i = 0;
      for (final double[] d : parRateJacobianList) {
        array[i++] = d;
      }
    } else {
      throw new ClassCastException("Jacobian object " + jacobianObject + " not List<double[]> or double[][]");
    }
    return array;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Pair<String, String> curveNames = YieldCurveFunction.getDesiredValueCurveNames(context, desiredValue);
    if (curveNames.getFirst().equals(curveNames.getSecond())) {
      return Sets.newHashSet(getCurveRequirement(target, curveNames.getFirst(), null, null), getJacobianRequirement(target, curveNames.getFirst(), curveNames.getSecond()));
    } else {
      return Sets.newHashSet(
          getCurveRequirement(target, curveNames.getFirst(), curveNames.getFirst(), curveNames.getSecond()),
          getCurveRequirement(target, curveNames.getSecond(), curveNames.getFirst(), curveNames.getSecond()),
          getJacobianRequirement(target, curveNames.getFirst(), curveNames.getSecond()));
    }
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.SECURITY;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    if (!(target.getSecurity() instanceof FinancialSecurity)) {
      return false;
    }
    return InterestRateInstrumentType.isFixedIncomeInstrumentType((FinancialSecurity) target.getSecurity());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    ValueProperties resultProperties = FixedIncomeInstrumentCurveExposureHelper.getValuePropertiesForSecurity(
        (FinancialSecurity) target.getSecurity(), createValueProperties());
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    resultProperties = resultProperties.copy().with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode()).get();
    return Collections.singleton(new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), resultProperties));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target,
      final Map<ValueSpecification, ValueRequirement> inputs) {
    final Pair<String, String> curveNames = YieldCurveFunction.getInputCurveNames(inputs);
    ValueProperties resultProperties = FixedIncomeInstrumentCurveExposureHelper.getValuePropertiesForSecurity(
        (FinancialSecurity) target.getSecurity(), curveNames.getSecond(), curveNames.getFirst(), createValueProperties());
    final Currency ccy = FinancialSecurityUtils.getCurrency(target.getSecurity());
    resultProperties = resultProperties.copy().with(ValuePropertyNames.CURVE_CURRENCY, ccy.getCode()).get();
    return Collections
        .singleton(new ValueSpecification(VALUE_REQUIREMENT, target.toSpecification(), resultProperties));
  }

  @Override
  public String getShortName() {
    return "InterestRateInstrumentYieldCurveNodeSensitivitiesFunction";
  }

  protected ValueRequirement getCurveRequirement(final ComputationTarget target, final String curveName, final String advisoryForward, final String advisoryFunding) {
    return YieldCurveFunction.getCurveRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), curveName, advisoryForward, advisoryFunding);
  }

  protected ValueRequirement getJacobianRequirement(final ComputationTarget target, final String forwardCurveName, final String fundingCurveName) {
    return YieldCurveFunction.getJacobianRequirement(FinancialSecurityUtils.getCurrency(target.getSecurity()), forwardCurveName, fundingCurveName);
  }

}
