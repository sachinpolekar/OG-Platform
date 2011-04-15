/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import javax.time.Instant;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;

/**
 * Implementation of {@link ViewResultListener} which does nothing, designed for overriding specific methods.
 */
public abstract class AbstractViewResultListener implements ViewResultListener {

  @Override
  public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition) {
  }

  @Override
  public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
  }

  @Override
  public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
  }

  @Override
  public void processCompleted() {
  }

  @Override
  public void processTerminated(boolean executionInterrupted) {
  }

}