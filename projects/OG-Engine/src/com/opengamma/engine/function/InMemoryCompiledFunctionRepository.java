/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.time.Instant;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.ArgumentChecker;

/**
 * An in-memory implementation of {@link CompiledFunctionRepository}.
 */
public class InMemoryCompiledFunctionRepository implements CompiledFunctionRepository {

  private static final FunctionInvoker MISSING = new FunctionInvoker() {
    @Override
    public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
      return null;
    }
  };

  private final ConcurrentMap<String, CompiledFunctionDefinition> _functionDefinitions = new ConcurrentHashMap<String, CompiledFunctionDefinition>();
  private final ConcurrentMap<String, FunctionInvoker> _functionInvokers = new ConcurrentHashMap<String, FunctionInvoker>();
  private final FunctionCompilationContext _compilationContext;
  private Instant _earliestInvocationTime;
  private Instant _latestInvocationTime;

  public InMemoryCompiledFunctionRepository(final FunctionCompilationContext compilationContext) {
    _compilationContext = compilationContext;
  }

  public void addFunction(final CompiledFunctionDefinition function) {
    ArgumentChecker.notNull(function, "Function definition");
    final String uid = function.getFunctionDefinition().getUniqueIdentifier();
    _functionDefinitions.put(uid, function);
    Instant time = function.getEarliestInvocationTime();
    if (time != null) {
      if (_earliestInvocationTime != null) {
        if (time.isAfter(_earliestInvocationTime)) {
          _earliestInvocationTime = time;
        }
      } else {
        _earliestInvocationTime = time;
      }
    }
    time = function.getLatestInvocationTime();
    if (time != null) {
      if (_latestInvocationTime != null) {
        if (time.isBefore(_latestInvocationTime)) {
          _latestInvocationTime = time;
        }
      } else {
        _latestInvocationTime = time;
      }
    }
  }

  @Override
  public Collection<CompiledFunctionDefinition> getAllFunctions() {
    return _functionDefinitions.values();
  }

  @Override
  public CompiledFunctionDefinition getDefinition(final String uniqueIdentifier) {
    return findDefinition(uniqueIdentifier);
  }

  /**
   * Separate "find" operation that can be used to bypass a sub-class implementation that may
   * be doing some form of lazy compilation of function definitions.
   * @param uniqueIdentifier  the definition identifier, not null
   * @return the definition
   */
  public CompiledFunctionDefinition findDefinition(final String uniqueIdentifier) {
    return _functionDefinitions.get(uniqueIdentifier);
  }

  @Override
  public FunctionInvoker getInvoker(final String uniqueIdentifier) {
    FunctionInvoker invoker = _functionInvokers.get(uniqueIdentifier);
    if (invoker == null) {
      final CompiledFunctionDefinition definition = getDefinition(uniqueIdentifier);
      if (definition == null) {
        invoker = MISSING;
      } else {
        invoker = definition.getFunctionInvoker();
      }
      final FunctionInvoker previous = _functionInvokers.putIfAbsent(uniqueIdentifier, invoker);
      if (previous != null) {
        invoker = previous;
      }
    }
    return (invoker == MISSING) ? null : invoker;
  }

  @Override
  public FunctionCompilationContext getCompilationContext() {
    return _compilationContext;
  }

  public Instant getEarliestInvocationTime() {
    return _earliestInvocationTime;
  }

  public Instant getLatestInvocationTime() {
    return _latestInvocationTime;
  }

}