/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import com.opengamma.id.UniqueIdentifier;


/**
 * A management bean for a GrapExecutionStatistics
 *
 */
public interface GraphExecutionStatisticsMBean {

  UniqueIdentifier getViewProcessId();
  
  String getViewDefinitionName();

  String getCalcConfigName();

  Long getProcessedGraphs();

  Long getExecutedGraphs();

  Long getExecutedNodes();

  Long getExecutionTime();

  Long getActualTime();

  Long getProcessedJobs();

  Long getProcessedJobSize();

  Long getProcessedJobCycleCost();

  Long getProcessedJobDataCost();

  String getLastProcessedTime();

  String getLastExecutedTime();

  void reset();

}
