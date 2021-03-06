/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.time.Instant;
import javax.time.InstantProvider;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.VersionedSource;
import com.opengamma.master.listener.BasicMasterChangeManager;
import com.opengamma.master.listener.MasterChangeManager;
import com.opengamma.master.listener.MasterChangedType;
import com.opengamma.master.listener.NotifyingMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * An in-memory master for yield curve definitions, backed by a hash-map.
 */
public class InMemoryInterpolatedYieldCurveDefinitionMaster implements InterpolatedYieldCurveDefinitionMaster, InterpolatedYieldCurveDefinitionSource, VersionedSource, NotifyingMaster {
  
  /**
   * Default scheme used for identifiers created.
   */
  public static final String DEFAULT_SCHEME = "InMemoryInterpolatedYieldCurveDefinition";

  private final Map<Pair<Currency, String>, TreeMap<Instant, YieldCurveDefinition>> _definitions = new HashMap<Pair<Currency, String>, TreeMap<Instant, YieldCurveDefinition>>();
  private final MasterChangeManager _changeManager = new BasicMasterChangeManager();  // TODO

  private String _identifierScheme;
  private VersionCorrection _sourceVersionCorrection = VersionCorrection.LATEST;

  public InMemoryInterpolatedYieldCurveDefinitionMaster() {
    setIdentifierScheme(DEFAULT_SCHEME);
  }

  public void setIdentifierScheme(final String identifierScheme) {
    ArgumentChecker.notNull(identifierScheme, "identifierScheme");
    _identifierScheme = identifierScheme;
  }

  public String getIdentifierScheme() {
    return _identifierScheme;
  }

  // InterpolatedYieldCurveDefinitionSource

  /**
   * Gets a yield curve definition for a currency and name.
   * @param currency  the currency, not null
   * @param name  the name, not null
   * @return the definition, null if not found
   */
  @Override
  public synchronized YieldCurveDefinition getDefinition(Currency currency, String name) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(name, "name");
    final TreeMap<Instant, YieldCurveDefinition> definitions = _definitions.get(Pair.of(currency, name));
    if (definitions == null) {
      return null;
    }
    final Map.Entry<Instant, YieldCurveDefinition> entry;
    if (_sourceVersionCorrection.getVersionAsOf() == null) {
      entry = definitions.lastEntry();
    } else {
      entry = definitions.floorEntry(_sourceVersionCorrection.getVersionAsOf());
    }
    if (entry == null) {
      return null;
    }
    return entry.getValue();
  }

  /**
   * Gets a yield curve definition for a currency, name and version.
   * @param currency  the currency, not null
   * @param name  the name, not null
   * @param version  the version instant, not null
   * @return the definition, null if not found
   */
  @Override
  public YieldCurveDefinition getDefinition(final Currency currency, final String name, final InstantProvider version) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(name, "name");
    final TreeMap<Instant, YieldCurveDefinition> definitions = _definitions.get(Pair.of(currency, name));
    if (definitions == null) {
      return null;
    }
    final Map.Entry<Instant, YieldCurveDefinition> entry = definitions.floorEntry(Instant.of(version));
    if (entry == null) {
      return null;
    }
    return entry.getValue();
  }

  // VersionedSource

  @Override
  public synchronized void setVersionCorrection(final VersionCorrection versionCorrection) {
    _sourceVersionCorrection = VersionCorrection.of(versionCorrection);
  }

  // InterpolatedYieldCurveDefinitionMaster

  @Override
  public synchronized YieldCurveDefinitionDocument add(YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.yieldCurveDefinition");
    final Currency currency = document.getYieldCurveDefinition().getCurrency();
    final String name = document.getYieldCurveDefinition().getName();
    final Pair<Currency, String> key = Pair.of(currency, name);
    if (_definitions.containsKey(key)) {
      throw new IllegalArgumentException("Duplicate definition");
    }
    final TreeMap<Instant, YieldCurveDefinition> value = new TreeMap<Instant, YieldCurveDefinition>();
    Instant now = Instant.now();
    value.put(now, document.getYieldCurveDefinition());
    _definitions.put(key, value);
    final UniqueIdentifier uid = UniqueIdentifier.of(getIdentifierScheme(), name + "_" + currency.getCode());
    document.setUniqueId(uid);
    changeManager().masterChanged(MasterChangedType.ADDED, null, uid, now);
    return document;
  }

  @Override
  public synchronized YieldCurveDefinitionDocument addOrUpdate(YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.yieldCurveDefinition");
    final Currency currency = document.getYieldCurveDefinition().getCurrency();
    final String name = document.getYieldCurveDefinition().getName();
    final Pair<Currency, String> key = Pair.of(currency, name);
    TreeMap<Instant, YieldCurveDefinition> value = _definitions.get(key);
    final UniqueIdentifier uid = UniqueIdentifier.of(getIdentifierScheme(), name + "_" + currency.getCode());
    Instant now = Instant.now();
    if (value != null) {
      if (_sourceVersionCorrection.getVersionAsOf() != null) {
        // Don't need to keep the old values before the one needed by "versionAsOfInstant"
        final Instant oldestNeeded = value.floorKey(_sourceVersionCorrection.getVersionAsOf());
        if (oldestNeeded != null) {
          value.headMap(oldestNeeded).clear();
        }
      } else {
        // Don't need any old values
        value.clear();
      }
      value.put(now, document.getYieldCurveDefinition());
      changeManager().masterChanged(MasterChangedType.UPDATED, uid, uid, now);
    } else {
      value = new TreeMap<Instant, YieldCurveDefinition>();
      value.put(now, document.getYieldCurveDefinition());
      _definitions.put(key, value);
      changeManager().masterChanged(MasterChangedType.ADDED, null, uid, now);
    }
    document.setUniqueId(uid);
    return document;
  }

  @Override
  public YieldCurveDefinitionDocument correct(YieldCurveDefinitionDocument document) {
    throw new UnsupportedOperationException();
  }

  @Override
  public synchronized YieldCurveDefinitionDocument get(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    if (!uid.isLatest()) {
      throw new IllegalArgumentException("Only latest version supported by '" + getIdentifierScheme() + "'");
    }
    if (!getIdentifierScheme().equals(uid.getScheme())) {
      throw new DataNotFoundException("Scheme '" + uid.getScheme() + "' not valid for '" + getIdentifierScheme() + "'");
    }
    final int i = uid.getValue().indexOf('_');
    if (i <= 0) {
      throw new DataNotFoundException("Identifier '" + uid.getValue() + "' not valid for '" + getIdentifierScheme() + "'");
    }
    final String name = uid.getValue().substring(0, i);
    final String iso = uid.getValue().substring(i + 1);
    final Currency currency;
    try {
      currency = Currency.of(iso);
    } catch (IllegalArgumentException e) {
      throw new DataNotFoundException("Identifier '" + uid.getValue() + "' not valid for '" + getIdentifierScheme() + "'", e);
    }
    final TreeMap<Instant, YieldCurveDefinition> definitions = _definitions.get(Pair.of(currency, name));
    if (definitions == null) {
      throw new DataNotFoundException("Curve definition not found");
    }
    final YieldCurveDefinition definition = definitions.lastEntry().getValue();
    if (definition == null) {
      throw new DataNotFoundException("Curve definition not found");
    }
    return new YieldCurveDefinitionDocument(uid, definition);
  }

  @Override
  public synchronized YieldCurveDefinitionDocument get(ObjectIdentifiable objectIdable, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectIdable, "objectIdable");
    ObjectIdentifier objectId = objectIdable.getObjectId();
    if (!getIdentifierScheme().equals(objectId.getScheme())) {
      throw new DataNotFoundException("Scheme '" + objectId.getScheme() + "' not valid for '" + getIdentifierScheme() + "'");
    }
    final int i = objectId.getValue().indexOf('_');
    if (i <= 0) {
      throw new DataNotFoundException("Identifier '" + objectId.getValue() + "' not valid for '" + getIdentifierScheme() + "'");
    }
    final String name = objectId.getValue().substring(0, i);
    final String iso = objectId.getValue().substring(i + 1);
    final Currency currency;
    try {
      currency = Currency.of(iso);
    } catch (IllegalArgumentException e) {
      throw new DataNotFoundException("Identifier '" + objectId.getValue() + "' not valid for '" + getIdentifierScheme() + "'", e);
    }
    final TreeMap<Instant, YieldCurveDefinition> definitions = _definitions.get(Pair.of(currency, name));
    if (definitions == null) {
      throw new DataNotFoundException("Curve definition not found");
    }
    final YieldCurveDefinition definition = definitions.lastEntry().getValue();
    if (definition == null) {
      throw new DataNotFoundException("Curve definition not found");
    }
    return new YieldCurveDefinitionDocument(objectId.atLatestVersion(), definition);
  }

  @Override
  public synchronized void remove(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    if (!uid.isLatest()) {
      throw new IllegalArgumentException("Only latest version supported by '" + getIdentifierScheme() + "'");
    }
    if (!getIdentifierScheme().equals(uid.getScheme())) {
      throw new DataNotFoundException("Scheme '" + uid.getScheme() + "' not valid for '" + getIdentifierScheme() + "'");
    }
    final int i = uid.getValue().indexOf('_');
    if (i <= 0) {
      throw new DataNotFoundException("Identifier '" + uid.getValue() + "' not valid for '" + getIdentifierScheme() + "'");
    }
    final String name = uid.getValue().substring(0, i);
    final String iso = uid.getValue().substring(i + 1);
    final Currency currency;
    try {
      currency = Currency.of(iso);
    } catch (IllegalArgumentException e) {
      throw new DataNotFoundException("Identifier '" + uid.getValue() + "' not valid for '" + getIdentifierScheme() + "'", e);
    }
    final Pair<Currency, String> key = Pair.of(currency, name);
    if (_sourceVersionCorrection.getVersionAsOf() != null) {
      final TreeMap<Instant, YieldCurveDefinition> value = _definitions.get(key);
      if (value == null) {
        throw new DataNotFoundException("Curve definition not found");
      }
      // Don't need to keep the old values before the one needed by "versionAsOfInstant"
      final Instant oldestNeeded = value.floorKey(_sourceVersionCorrection.getVersionAsOf());
      if (oldestNeeded != null) {
        value.headMap(oldestNeeded).clear();
      }
      // Store a null to indicate the delete
      value.put(Instant.now(), null);
    } else {
      if (_definitions.remove(key) == null) {
        throw new DataNotFoundException("Curve definition not found");
      }
    }
    changeManager().masterChanged(MasterChangedType.REMOVED, uid, null, Instant.now());
  }

  @Override
  public synchronized YieldCurveDefinitionDocument update(YieldCurveDefinitionDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getYieldCurveDefinition(), "document.yieldCurveDefinition");
    final Currency currency = document.getYieldCurveDefinition().getCurrency();
    final String name = document.getYieldCurveDefinition().getName();
    final UniqueIdentifier uid = UniqueIdentifier.of(getIdentifierScheme(), name + "_" + currency.getCode());
    if (!uid.equals(document.getUniqueId())) {
      throw new IllegalArgumentException("Invalid unique identifier");
    }
    final Pair<Currency, String> key = Pair.of(currency, name);
    final TreeMap<Instant, YieldCurveDefinition> value = _definitions.get(key);
    if (value == null) {
      throw new DataNotFoundException("UID '" + uid + "' not found");
    }
    if (_sourceVersionCorrection.getVersionAsOf() != null) {
      // Don't need to keep the old values before the one needed by "versionAsOfInstant"
      final Instant oldestNeeded = value.floorKey(_sourceVersionCorrection.getVersionAsOf());
      value.headMap(oldestNeeded).clear();
    } else {
      // Don't need any old values
      value.clear();
    }
    Instant now = Instant.now();
    value.put(now, document.getYieldCurveDefinition());
    document.setUniqueId(uid);
    changeManager().masterChanged(MasterChangedType.UPDATED, uid, uid, now);
    return document;
  }

  @Override
  public MasterChangeManager changeManager() {
    return _changeManager;
  }

}
