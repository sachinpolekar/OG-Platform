/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.view.ViewProcessorInternal;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.VersionedSource;
import com.opengamma.master.listener.MasterChangeListener;
import com.opengamma.master.listener.MasterChanged;
import com.opengamma.master.listener.NotifyingMaster;
import com.opengamma.util.ArgumentChecker;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Manages a set of view processors that share common resources, making system configuration appear atomic to them.
 * <p>
 * If given a set of masters and the sources that expose them to the rest of the system, update notifications from
 * the masters can be used to "latch" the sources between view processor suspensions to create an atomic view.
 */
public class ViewProcessorManager implements Lifecycle {

  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessorManager.class);

  private final Set<ViewProcessorInternal> _viewProcessors = new HashSet<ViewProcessorInternal>();
  private final Set<CompiledFunctionService> _functions = new HashSet<CompiledFunctionService>();
  private final Map<NotifyingMaster, VersionedSource> _masterToSource = new HashMap<NotifyingMaster, VersionedSource>();
  private final Map<NotifyingMaster, MasterChangeListener> _masterToListener = new HashMap<NotifyingMaster, MasterChangeListener>();
  private Map<VersionedSource, Instant> _latchInstants = new HashMap<VersionedSource, Instant>();
  private final ReentrantLock _lifecycleLock = new ReentrantLock();
  private final ReentrantLock _changeLock = new ReentrantLock();
  private final ExecutorService _executor = Executors.newCachedThreadPool();
  private final Set<UniqueIdentifier> _watchSet = new HashSet<UniqueIdentifier>();
  private final Set<WatchSetProvider> _watchSetProviders = new HashSet<WatchSetProvider>();
  private boolean _isRunning;

  public ViewProcessorManager() {
  }

  private void assertNotRunning() {
    if (isRunning()) {
      throw new IllegalStateException("Already running");
    }
  }

  public void setViewProcessor(final ViewProcessorInternal viewProcessor) {
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    assertNotRunning();
    _viewProcessors.clear();
    _viewProcessors.add(viewProcessor);
  }

  public void setViewProcessors(final Collection<ViewProcessorInternal> viewProcessors) {
    ArgumentChecker.notNull(viewProcessors, "viewProcessors");
    assertNotRunning();
    _viewProcessors.clear();
    _viewProcessors.addAll(viewProcessors);
  }

  @SuppressWarnings("unchecked")
  public Set<ViewProcessorInternal> getViewProcessors() {
    return Collections.unmodifiableSet(_viewProcessors);
  }

  public void setMasterAndSource(final NotifyingMaster master, final VersionedSource source) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(source, "source");
    assertNotRunning();
    _masterToSource.clear();
    _masterToSource.put(master, source);
  }

  public void setMastersAndSources(final Map<NotifyingMaster, VersionedSource> masterToSource) {
    ArgumentChecker.notNull(masterToSource, "masterToSource");
    assertNotRunning();
    _masterToSource.clear();
    _masterToSource.putAll(masterToSource);
  }

  public void setWatchSetProviders(final Set<WatchSetProvider> watchSetProviders) {
    ArgumentChecker.notNull(watchSetProviders, "watchSetProviders");
    assertNotRunning();
    _watchSetProviders.clear();
    _watchSetProviders.addAll(watchSetProviders);
  }

  @Override
  public boolean isRunning() {
    _lifecycleLock.lock();
    try {
      return _isRunning;
    } finally {
      _lifecycleLock.unlock();
    }
  }

  @Override
  public void start() {
    _lifecycleLock.lock();
    try {
      if (!_isRunning) {
        _changeLock.lock();
        try {
          final Instant now = Instant.now();
          for (Map.Entry<NotifyingMaster, VersionedSource> entry : _masterToSource.entrySet()) {
            final NotifyingMaster master = entry.getKey();
            final VersionedSource source = entry.getValue();
            final MasterChangeListener listener = new MasterChangeListener() {
              @Override
              public void masterChanged(MasterChanged event) {
                if (_watchSet.contains(event.getBeforeId())) {
                  ViewProcessorManager.this.onMasterChanged(Instant.now(), source, event.getBeforeId());
                }
              }
            };
            master.changeManager().addChangeListener(listener);
            _masterToListener.put(master, listener);
            s_logger.debug("Latching {} to {}", source, now);
            // TODO this isn't ideal if there is clock drift between nodes - the time needs to be the system time at the master
            source.setVersionCorrection(VersionCorrection.ofVersionAsOf(now));  // TODO ignores correction
          }
        } finally {
          _changeLock.unlock();
        }
        _functions.clear();
        for (ViewProcessorInternal viewProcessor : _viewProcessors) {
          _functions.add(viewProcessor.getFunctionCompilationService());
        }
        s_logger.info("Initializing functions");
        for (CompiledFunctionService function : _functions) {
          final Set<UniqueIdentifier> watch = function.initialize();
          _watchSet.addAll(watch);
          addAlternateWatchSet(watch);
        }
        s_logger.debug("WatchSet = {}", _watchSet);
        s_logger.info("Starting view processors");
        for (ViewProcessorInternal viewProcessor : _viewProcessors) {
          viewProcessor.start();
        }
        _isRunning = true;
      }
    } finally {
      _lifecycleLock.unlock();
    }
  }

  @Override
  public void stop() {
    _lifecycleLock.lock();
    try {
      if (_isRunning) {
        for (ViewProcessorInternal viewProcessor : _viewProcessors) {
          viewProcessor.stop();
        }
        final Iterator<Map.Entry<NotifyingMaster, MasterChangeListener>> itr = _masterToListener.entrySet().iterator();
        while (itr.hasNext()) {
          Map.Entry<NotifyingMaster, MasterChangeListener> entry = itr.next();
          entry.getKey().changeManager().removeChangeListener(entry.getValue());
          itr.remove();
        }
        _isRunning = false;
      }
    } finally {
      _lifecycleLock.unlock();
    }
  }

  private void onMasterChanged(final Instant latchInstant, final VersionedSource source, final UniqueIdentifier uniqueIdentifier) {
    s_logger.debug("Change timestamp {} for {} - change from {}", new Object[] {latchInstant, source, uniqueIdentifier});
    _changeLock.lock();
    try {
      if (_latchInstants.isEmpty()) {
        s_logger.debug("Starting latching job");
        // Kick off a latch; this may take some time if the view processors must wait for their views to finish calculating first
        _executor.submit(new Runnable() {
          @Override
          public void run() {
            latchSources();
          }
        });
      } else {
        s_logger.debug("Latching job already active");
      }
      final Instant previousInstant = _latchInstants.get(source);
      if ((previousInstant == null) || previousInstant.isBefore(latchInstant)) {
        _latchInstants.put(source, latchInstant);
      }
    } finally {
      _changeLock.unlock();
    }
  }

  private void latchSources() {
    _lifecycleLock.lock();
    s_logger.info("Begin configuration change");
    try {
      final List<Runnable> resumes = new ArrayList<Runnable>(_viewProcessors.size());
      final List<Future<Runnable>> suspends = new ArrayList<Future<Runnable>>(_viewProcessors.size());
      s_logger.debug("Suspending view processors");
      for (ViewProcessorInternal viewProcessor : _viewProcessors) {
        suspends.add(viewProcessor.suspend(_executor));
      }
      while (!suspends.isEmpty()) {
        final Future<Runnable> future = suspends.remove(suspends.size() - 1);
        try {
          resumes.add(future.get(3000, TimeUnit.MILLISECONDS));
        } catch (TimeoutException e) {
          s_logger.warn("Timeout waiting for view to suspend");
          suspends.add(future);
        } catch (Throwable t) {
          s_logger.warn("Couldn't suspend view", t);
        }
      }
      Map<VersionedSource, Instant> latchInstants;
      _changeLock.lock();
      try {
        latchInstants = _latchInstants;
        _latchInstants = new HashMap<VersionedSource, Instant>();
      } finally {
        _changeLock.unlock();
      }
      for (Map.Entry<VersionedSource, Instant> entry : latchInstants.entrySet()) {
        s_logger.debug("Latching {} to {}", entry.getKey(), entry.getValue());
        entry.getKey().setVersionCorrection(VersionCorrection.ofVersionAsOf(entry.getValue()));
      }
      s_logger.debug("Re-initializing functions");
      _watchSet.clear();
      for (CompiledFunctionService functions : _functions) {
        final Set<UniqueIdentifier> watch = functions.reinitialize();
        _watchSet.addAll(watch);
        addAlternateWatchSet(watch);
      }
      s_logger.debug("WatchSet = {}", _watchSet);
      s_logger.debug("Resuming view processors");
      for (Runnable resume : resumes) {
        resume.run();
      }
      s_logger.info("Configuration change complete");
    } finally {
      _lifecycleLock.unlock();
    }
  }

  private void addAlternateWatchSet(final Set<UniqueIdentifier> watchSet) {
    for (WatchSetProvider provider : _watchSetProviders) {
      final Set<UniqueIdentifier> additional = provider.getAdditionalWatchSet(watchSet);
      if (additional != null) {
        _watchSet.addAll(additional);
      }
    }
  }

}
