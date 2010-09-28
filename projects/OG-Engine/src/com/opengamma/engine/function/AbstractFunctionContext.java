/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListMap;

// NOTE kirk 2010-03-07 -- This class is intentionally NOT Serializable, as we expect
// that this will contain lots of interface implementations for things like data providers
// which are not Serializable. It's thus a runtime configuration object rather than a
// static configuration object.

/**
 * The base class for any multi-valued map-like context which may be provided
 * to a {@link FunctionDefinition} or {@link FunctionInvoker}.
 *
 * @author kirk
 */
/* package */abstract class AbstractFunctionContext {
  private final Map<String, Object> _backingMap = new ConcurrentSkipListMap<String, Object>();

  protected AbstractFunctionContext() {
  }

  protected AbstractFunctionContext(final AbstractFunctionContext copyFrom) {
    _backingMap.putAll(copyFrom._backingMap);
  }

  public Object get(String elementName) {
    return _backingMap.get(elementName);
  }

  public Object put(String elementName, Object value) {
    return _backingMap.put(elementName, value);
  }

  public Set<String> getAllElementNames() {
    // See UTL-20. No need to reorder into a TreeSet<>.
    return new TreeSet<String>(_backingMap.keySet());
  }

  public abstract AbstractFunctionContext clone();

}