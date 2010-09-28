/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Executors;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionCompilationService;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.livedata.LiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.LiveDataSnapshotProvider;
import com.opengamma.engine.position.MockPositionSource;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.engine.security.MockSecuritySource;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.view.calc.SingleNodeExecutorFactory;
import com.opengamma.engine.view.calc.ViewRecalculationJob;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.LocalCalculationNode;
import com.opengamma.engine.view.calcnode.LocalNodeJobInvoker;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.calcnode.ViewProcessorQuerySender;
import com.opengamma.engine.view.calcnode.stats.DiscardingInvocationStatisticsGatherer;
import com.opengamma.engine.view.permission.DefaultViewPermissionProvider;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.msg.UserPrincipal;
import com.opengamma.livedata.test.TestLiveDataClient;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.InMemoryByteArrayRequestConduit;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides access to a ready-made and customisable view processing environment for testing.
 */
public class ViewProcessorTestEnvironment {

  public static final UserPrincipal TEST_USER = UserPrincipal.getLocalUser();
  
  private static final String TEST_VIEW_DEFINITION_NAME = "Test View";
  private static final String TEST_CALC_CONFIG_NAME = "Test Calc Config";
  
  // Settings
  private boolean _localExecutorService;
  private LiveDataSnapshotProvider _userSnapshotProvider;
  private LiveDataAvailabilityProvider _userAvailabilityProvider;
  
  // Environment
  private ViewProcessorImpl _viewProcessor;
  private InMemoryLKVSnapshotProvider _snapshotProvider;
  private ViewDefinition _testDefinition;
  private ViewCalculationConfiguration _calcConfig;
  private final ValueRequirement _primitive1 = new ValueRequirement("Value1", ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Scheme", "PrimitiveValue"));
  private final ValueRequirement _primitive2 = new ValueRequirement("Value2", ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Scheme", "PrimitiveValue"));
  
  public void init() {
    _testDefinition = new ViewDefinition(TEST_VIEW_DEFINITION_NAME, TEST_USER);
    _calcConfig = new ViewCalculationConfiguration(_testDefinition, TEST_CALC_CONFIG_NAME);
    _calcConfig.addSpecificRequirement(_primitive1);
    _calcConfig.addSpecificRequirement(_primitive2);
    _testDefinition.addViewCalculationConfiguration(_calcConfig);

    _viewProcessor = new ViewProcessorImpl();
    
    FudgeContext fudgeContext = new FudgeContext();
    FunctionRepository functionRepository = new InMemoryFunctionRepository();
    PositionSource positionSource = new MockPositionSource();
    SecuritySource securitySource = new MockSecuritySource();
    FunctionCompilationContext functionCompilationContext = new FunctionCompilationContext();
    functionCompilationContext.setSecuritySource(securitySource);
    
    MapViewDefinitionRepository viewDefinitionRepository = new MapViewDefinitionRepository();
    viewDefinitionRepository.addDefinition(_testDefinition);
    
    InMemoryViewComputationCacheSource cacheSource = new InMemoryViewComputationCacheSource(fudgeContext);
    _viewProcessor.setComputationCacheSource(cacheSource);
    
    SingleNodeExecutorFactory dependencyGraphExecutorFactory = new SingleNodeExecutorFactory();
    _viewProcessor.setDependencyGraphExecutorFactory(dependencyGraphExecutorFactory);
    assertEquals(dependencyGraphExecutorFactory, _viewProcessor.getDependencyGraphExecutorFactory());
    
    _viewProcessor.setFunctionCompilationService(new FunctionCompilationService(functionRepository, functionCompilationContext));
    
    TestLiveDataClient liveDataClient = new TestLiveDataClient();
    _viewProcessor.setLiveDataClient(liveDataClient);
    assertEquals(liveDataClient, _viewProcessor.getLiveDataClient());
    
    if (_userSnapshotProvider != null) {
      _viewProcessor.setLiveDataSnapshotProvider(_userSnapshotProvider);
      _viewProcessor.setLiveDataAvailabilityProvider(_userAvailabilityProvider);
    } else {
      _snapshotProvider = new InMemoryLKVSnapshotProvider();
      _snapshotProvider.addValue(_primitive1, 0);
      _snapshotProvider.addValue(_primitive2, 0);
      _viewProcessor.setLiveDataSnapshotProvider(_snapshotProvider);
      _viewProcessor.setLiveDataAvailabilityProvider(_snapshotProvider);
    }
    
    _viewProcessor.setPositionSource(positionSource);
    _viewProcessor.setSecuritySource(securitySource);
    _viewProcessor.setViewDefinitionRepository(viewDefinitionRepository);
    _viewProcessor.setViewPermissionProvider(new DefaultViewPermissionProvider());
    
    ViewProcessorQueryReceiver calcNodeQueryReceiver = new ViewProcessorQueryReceiver();
    FudgeRequestDispatcher calcNodeQueryRequestDispatcher = new FudgeRequestDispatcher(calcNodeQueryReceiver);
    InMemoryByteArrayRequestConduit calcNodeQueryRequestConduit = new InMemoryByteArrayRequestConduit(calcNodeQueryRequestDispatcher);
    ByteArrayFudgeRequestSender calcNodeQueryRequestSender = new ByteArrayFudgeRequestSender(calcNodeQueryRequestConduit);
    ViewProcessorQuerySender calcNodeQuerySender = new ViewProcessorQuerySender(calcNodeQueryRequestSender);
    _viewProcessor.setViewProcessorQueryReceiver(calcNodeQueryReceiver);
    assertEquals(calcNodeQueryReceiver, _viewProcessor.getViewProcessorQueryReceiver());
    
    if (isLocalExecutorService()) {
      _viewProcessor.setLocalExecutorService(true);
    } else {
      _viewProcessor.setExecutorService(Executors.newSingleThreadExecutor());
    }
    
    FunctionExecutionContext functionExecutionContext = new FunctionExecutionContext();
    functionExecutionContext.setSecuritySource(securitySource);
    
    LocalCalculationNode localCalcNode = new LocalCalculationNode(cacheSource, functionRepository,
        functionExecutionContext, new DefaultComputationTargetResolver(securitySource, positionSource),
        calcNodeQuerySender, Executors.newCachedThreadPool(), new DiscardingInvocationStatisticsGatherer());
    LocalNodeJobInvoker jobInvoker = new LocalNodeJobInvoker(localCalcNode);
    _viewProcessor.setComputationJobDispatcher(new JobDispatcher(jobInvoker));
  }

  // Pre-init configuration
  //-------------------------------------------------------------------------
  public boolean isLocalExecutorService() {
    return _localExecutorService;
  }

  public void setLocalExecutorService(boolean localExecutorService) {
    _localExecutorService = localExecutorService;
  }
  
  public LiveDataSnapshotProvider getUserSnapshotProvider() {
    return _userSnapshotProvider;
  }
  
  public void setUserProviders(LiveDataSnapshotProvider liveDataSnapshotProvider, LiveDataAvailabilityProvider liveDataAvailabilityProvider) {
    ArgumentChecker.notNull(liveDataSnapshotProvider, "liveDataSnapshotProvider");
    ArgumentChecker.notNull(liveDataAvailabilityProvider, "liveDataAvailabilityProvider");
    _userSnapshotProvider = liveDataSnapshotProvider;
    _userAvailabilityProvider = liveDataAvailabilityProvider;
  }
  
  // Environment accessors
  //-------------------------------------------------------------------------
  public ViewProcessorImpl getViewProcessor() {
    return _viewProcessor;
  }
  
  public ViewRecalculationJob getCurrentRecalcJob(ViewImpl view) {
    return view.getRecalcJob();
  }
  
  public Thread getCurrentRecalcThread(ViewImpl view) {
    return view.getRecalcThread();
  }
  
  public ViewDefinition getViewDefinition() {
    return _testDefinition;
  }
  
  public InMemoryLKVSnapshotProvider getSnapshotProvider() {
    return _snapshotProvider;
  }
  
  public ValueRequirement getPrimitive1() {
    return _primitive1;
  }
  
  public ValueRequirement getPrimitive2() {
    return _primitive2;
  }
  
  public ViewCalculationResultModel getCalculationResult(ViewResultModel result) {
    return result.getCalculationResult(TEST_CALC_CONFIG_NAME);
  }
}