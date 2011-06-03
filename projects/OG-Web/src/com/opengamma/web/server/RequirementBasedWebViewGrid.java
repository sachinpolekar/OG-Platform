/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.cometd.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphExplorer;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewTargetResultModel;
import com.opengamma.engine.view.calc.EngineResourceReference;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.tuple.Pair;
import com.opengamma.web.server.conversion.ResultConverter;
import com.opengamma.web.server.conversion.ResultConverterCache;

/**
 * An abstract base class for dynamically-structured, requirement-based grids.
 */
public abstract class RequirementBasedWebViewGrid extends WebViewGrid {

  private static final Logger s_logger = LoggerFactory.getLogger(RequirementBasedWebViewGrid.class);
  private static final String GRID_STRUCTURE_ROOT_CHANNEL = "/gridStructure";
  
  private final String _columnStructureChannel;
  private final RequirementBasedGridStructure _gridStructure;
  private final String _nullCellValue;
  
  // Column-based state: few entries expected so using an array set 
  private final LongSet _historyOutputs = new LongArraySet();
  
  // Cell-based state
  private final ConcurrentMap<WebGridCell, WebViewDepGraphGrid> _depGraphGrids = new ConcurrentHashMap<WebGridCell, WebViewDepGraphGrid>();
  
  protected RequirementBasedWebViewGrid(String name, ViewClient viewClient, CompiledViewDefinition compiledViewDefinition, List<UniqueIdentifier> targets,
      EnumSet<ComputationTargetType> targetTypes, ResultConverterCache resultConverterCache, Client local, Client remote, String nullCellValue) {
    super(name, viewClient, resultConverterCache, local, remote);
    
    _columnStructureChannel = GRID_STRUCTURE_ROOT_CHANNEL + "/" + name + "/columns";
    List<RequirementBasedColumnKey> requirements = getRequirements(compiledViewDefinition.getViewDefinition(), targetTypes);    
    _gridStructure = new RequirementBasedGridStructure(compiledViewDefinition, targetTypes, requirements, targets);
    _nullCellValue = nullCellValue;
  }
  
  //-------------------------------------------------------------------------
  
  public void processTargetResult(ComputationTargetSpecification target, ViewTargetResultModel resultModel, Long resultTimestamp) {
    Long rowId = getGridStructure().getRowId(target.getUniqueId());
    if (rowId == null) {
      // Result not in the grid
      return;
    }

    Map<String, Object> valuesToSend = null;
    
    // Whether or not the row is in the viewport, we may have to store history
    for (String calcConfigName : resultModel.getCalculationConfigurationNames()) {
      for (ComputedValue value : resultModel.getAllValues(calcConfigName)) {
        ValueSpecification specification = value.getSpecification();
        WebViewGridColumn column = getGridStructure().getColumn(calcConfigName, specification);
        if (column == null) {
          // Expect a column for every value
          s_logger.warn("Could not find column for calculation configuration {} with value specification {}", calcConfigName, specification);
          continue;
        }
        
        long colId = column.getId();
        WebGridCell cell = WebGridCell.of(rowId, colId);
        Object originalValue = value.getValue();
        ResultConverter<Object> converter = originalValue != null ? getConverter(column, value.getSpecification().getValueName(), originalValue.getClass()) : null;
        Map<String, Object> cellData = processCellValue(cell, specification, originalValue, resultTimestamp, converter);
        Object depGraph = getDepGraphIfRequested(cell, calcConfigName, specification, resultTimestamp);
        if (depGraph != null) {
          if (cellData == null) {
            cellData = new HashMap<String, Object>();
          }
          cellData.put("dg", depGraph);
        }
        if (cellData != null) {
          if (valuesToSend == null) {
            valuesToSend = new HashMap<String, Object>();
            valuesToSend.put("rowId", rowId);
          }
          valuesToSend.put(Long.toString(colId), cellData);
        }
      }
    }
    if (valuesToSend != null) {
      getRemoteClient().deliver(getLocalClient(), getUpdateChannel(), valuesToSend, null);
    }
  }
  
  @SuppressWarnings("unchecked")
  private ResultConverter<Object> getConverter(WebViewGridColumn column, String valueName, Class<?> valueType) {
    // Ensure the converter is cached against the value name before sending the column details 
    ResultConverter<Object> converter = (ResultConverter<Object>) getConverterCache().getAndCacheConverter(valueName, valueType);
    if (!column.isTypeKnown()) {
      sendColumnDetails(Collections.singleton(column));
    }
    return converter;
  }

  private void sendColumnDetails(Collection<WebViewGridColumn> columnDetails) {
    getRemoteClient().deliver(getLocalClient(), _columnStructureChannel, getJsonColumnStructures(columnDetails), null);
  }
  
  @Override
  public Map<String, Object> getInitialJsonGridStructure() {
    Map<String, Object> gridStructure = super.getInitialJsonGridStructure();
    gridStructure.put("columns", getJsonColumnStructures(getGridStructure().getColumns()));
    return gridStructure;
  }
  
  @Override
  protected List<Object> getInitialJsonRowStructures() {
    List<Object> rowStructures = new ArrayList<Object>();
    for (Map.Entry<UniqueIdentifier, Long> targetEntry : getGridStructure().getTargets().entrySet()) {
      Map<String, Object> rowDetails = new HashMap<String, Object>();
      UniqueIdentifier target = targetEntry.getKey();
      long rowId = targetEntry.getValue();
      rowDetails.put("rowId", rowId);
      addRowDetails(target, rowId, rowDetails);
      rowStructures.add(rowDetails);
    }
    return rowStructures;
  }
  
  private Map<String, Object> getJsonColumnStructures(Collection<WebViewGridColumn> columns) {
    Map<String, Object> columnStructures = new HashMap<String, Object>();
    for (WebViewGridColumn columnDetails : columns) {
      columnStructures.put(Long.toString(columnDetails.getId()), getJsonColumnStructure(columnDetails));
    }
    return columnStructures;
  }
  
  private Map<String, Object> getJsonColumnStructure(WebViewGridColumn column) {
    Map<String, Object> detailsToSend = new HashMap<String, Object>();
    long colId = column.getId();
    detailsToSend.put("colId", colId);
    detailsToSend.put("header", column.getHeader());
    detailsToSend.put("description", column.getDescription());
    detailsToSend.put("nullValue", _nullCellValue);
    
    String resultType = getConverterCache().getKnownResultTypeName(column.getValueName());
    if (resultType != null) {
      column.setTypeKnown(true);
      detailsToSend.put("dataType", resultType);
      
      // Hack - the client should decide which columns it requires history for, taking into account the capabilities of
      // the renderer.
      if (resultType.equals("DOUBLE")) {
        addHistoryOutput(column.getId());
      }
    }
    return detailsToSend;
  }
  
  protected abstract void addRowDetails(UniqueIdentifier target, long rowId, Map<String, Object> details);
  
  //-------------------------------------------------------------------------
  
  protected RequirementBasedGridStructure getGridStructure() {
    return _gridStructure;
  }
  
  //-------------------------------------------------------------------------
  
  private void addHistoryOutput(long colId) {
    _historyOutputs.add(colId);
  }

  @Override
  protected boolean isHistoryOutput(WebGridCell cell) {
    return _historyOutputs.contains(cell.getColumnId());
  }
  
  //-------------------------------------------------------------------------
  
  public WebViewGrid setIncludeDepGraph(WebGridCell cell, boolean includeDepGraph) {
    if (includeDepGraph) {
      String gridName = getName() + ".depgraph-" + cell.getRowId() + "-" + cell.getColumnId();      
      WebViewDepGraphGrid grid = new WebViewDepGraphGrid(gridName, getViewClient(), getConverterCache(), getLocalClient(), getRemoteClient());
      _depGraphGrids.putIfAbsent(cell, grid);
      return grid;
    } else {
      WebViewDepGraphGrid grid = _depGraphGrids.remove(cell);
      return grid;
    }
  }
  
  private Object getDepGraphIfRequested(WebGridCell cell, String calcConfigName, ValueSpecification valueSpecification, Long resultTimestamp) {
    WebViewDepGraphGrid depGraphGrid = _depGraphGrids.get(cell);
    if (depGraphGrid == null) {
      return null;
    }
    
    // TODO: this may not be the cycle corresponding to the result - some tracking of cycle IDs required
    EngineResourceReference<? extends ViewCycle> cycleReference = getViewClient().createLatestCycleReference();
    if (cycleReference == null) {
      // Unable to get a cycle reference - perhaps no cycle has completed since enabling introspection
      return null;
    }
    
    try {
      Object gridStructure = null;
      if (!depGraphGrid.isInit()) {
        DependencyGraphExplorer explorer = cycleReference.get().getCompiledViewDefinition().getDependencyGraphExplorer(calcConfigName);
        DependencyGraph subgraph = explorer.getSubgraphProducing(valueSpecification);
        if (subgraph == null) {
          s_logger.warn("No subgraph producing value specification {}", valueSpecification);
          return null;
        }
        if (depGraphGrid.init(subgraph, calcConfigName, valueSpecification)) {
          gridStructure = depGraphGrid.getInitialJsonGridStructure();
        }
      }
      Map<String, Object> depGraph = depGraphGrid.processViewCycle(cycleReference.get(), resultTimestamp);
      if (gridStructure != null) {
        Map<String, Object> structureMessage = new HashMap<String, Object>();
        structureMessage.put("grid", gridStructure);
        structureMessage.put("update", depGraph);
        return structureMessage;
      } else {
        return depGraph;
      }
    } finally {
      cycleReference.release();
    }
  }
  
  //-------------------------------------------------------------------------

  private static List<RequirementBasedColumnKey> getRequirements(ViewDefinition viewDefinition, EnumSet<ComputationTargetType> targetTypes) {
    List<RequirementBasedColumnKey> result = new ArrayList<RequirementBasedColumnKey>();
    for (ViewCalculationConfiguration calcConfig : viewDefinition.getAllCalculationConfigurations()) {
      String calcConfigName = calcConfig.getName();
      if (targetTypes.contains(ComputationTargetType.POSITION) || targetTypes.contains(ComputationTargetType.PORTFOLIO_NODE)) {
        for (Pair<String, ValueProperties> portfolioOutput : calcConfig.getAllPortfolioRequirements()) {
          String valueName = portfolioOutput.getFirst();
          ValueProperties constraints = portfolioOutput.getSecond();
          RequirementBasedColumnKey columnKey = new RequirementBasedColumnKey(calcConfigName, valueName, constraints);
          result.add(columnKey);
        }
      }
      
      for (ValueRequirement specificRequirement : calcConfig.getSpecificRequirements()) {
        if (!targetTypes.contains(specificRequirement.getTargetSpecification().getType())) {
          continue;
        }
        String valueName = specificRequirement.getValueName();
        ValueProperties constraints = specificRequirement.getConstraints();
        RequirementBasedColumnKey columnKey = new RequirementBasedColumnKey(calcConfigName, valueName, constraints);
        result.add(columnKey);
      }
    }
    return result;
  }
  
}