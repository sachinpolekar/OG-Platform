/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PositionAccumulator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.YieldCurveFunction;
import com.opengamma.util.ArgumentChecker;

// REVIEW kirk 2010-01-02 -- This version aggregates from the leaf positions for all inputs.
// For non-linear aggregates and large portfolios, you'll want to use a more refined
// form which loads values from sub-nodes and positions, rather than just leaf positions.

/**
 * Able to sum a particular requirement name from a set of underlying
 * positions.
 * <p>
 * While in general we assume that basic linear aggregates will be performed in the
 * presentation layer on demand, this Function can be used to perform aggregate
 * in the engine.
 * <p>
 * In addition, it is an excellent demonstration of how to write portfolio-node-specific
 * functions.
 */
public class SummingFunction extends PropertyPreservingFunction {

  /**
   * Value of the {@link ValuePropertyNames#AGGREGATION} property set on the output produced. This
   * allows the result to be distinguished from a related summing function that doesn't apply
   * uniformly to all inputs (e.g. it might filter or weight them). 
   */
  public static final String AGGREGATION_STYLE = "Full";

  @Override
  protected Collection<String> getPreservedProperties() {
    return Collections.singleton(ValuePropertyNames.CURRENCY);
  }
  
  @Override
  protected Collection<String> getOptionalPreservedProperties() {
    return Arrays.asList(
        ValuePropertyNames.CUBE,
        ValuePropertyNames.CURVE,
        ValuePropertyNames.CURVE_CURRENCY,
        YieldCurveFunction.PROPERTY_FORWARD_CURVE,
        YieldCurveFunction.PROPERTY_FUNDING_CURVE);
  }

  @Override
  protected void applyAdditionalResultProperties(final ValueProperties.Builder builder) {
    super.applyAdditionalResultProperties(builder);
    builder.with(ValuePropertyNames.AGGREGATION, AGGREGATION_STYLE);
  }

  private final String _requirementName;

  public SummingFunction(final String requirementName) {
    ArgumentChecker.notNull(requirementName, "Requirement name");
    _requirementName = requirementName;
  }

  /**
   * @return the requirementName
   */
  public String getRequirementName() {
    return _requirementName;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final PortfolioNode node = target.getPortfolioNode();
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    Object currentSum = null;
    for (final Position position : allPositions) {
      final ValueRequirement requirement = new ValueRequirement(_requirementName, ComputationTargetType.POSITION, position.getUniqueId());
      final Object positionValue = inputs.getValue(requirement);
      currentSum = addValue(currentSum, positionValue);
    }
    final ComputedValue computedValue = new ComputedValue(new ValueSpecification(_requirementName, target.toSpecification(), getResultPropertiesFromInputs(inputs.getAllValues())), currentSum);
    return Collections.singleton(computedValue);
  }

  protected Object addValue(final Object previousSum, final Object currentValue) {
    return SumUtils.addValue(previousSum, currentValue, getRequirementName());
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.PORTFOLIO_NODE;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final PortfolioNode node = target.getPortfolioNode();
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    for (final Position position : allPositions) {
      requirements.add(new ValueRequirement(_requirementName, ComputationTargetType.POSITION, position.getUniqueId(), getInputConstraint(desiredValue)));
    }
    return requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueSpecification result = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties());
    return Collections.singleton(result);
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification result = new ValueSpecification(_requirementName, target.toSpecification(), getResultProperties(inputs.keySet()));
    return Collections.singleton(result);
  }

  @Override
  public String getShortName() {
    return "Sum(" + _requirementName + ")";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

}
