/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.time.calendar.LocalDate;
import javax.time.calendar.format.CalendricalParseException;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBean;

import com.google.common.base.Supplier;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleWithDates;
import com.opengamma.id.IdentifierWithDates;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;
import com.opengamma.master.historicaldata.DataPointDocument;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesDocument;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesGetRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesHistoryRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesHistoryResult;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchResult;
import com.opengamma.master.historicaldata.ManageableHistoricalTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.Paging;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.MapLocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * An in-memory implementation of a historical time-series master.
 */
public class InMemoryHistoricalTimeSeriesMaster implements HistoricalTimeSeriesMaster {

  /**
   * The default scheme used for each {@link UniqueIdentifier}.
   */
  public static final String DEFAULT_UID_SCHEME = "MemHTS";

  /**
   * The store of historical time-series.
   */
  private final ConcurrentHashMap<UniqueIdentifier, HistoricalTimeSeriesDocument> _store = new ConcurrentHashMap<UniqueIdentifier, HistoricalTimeSeriesDocument>();
  /**
   * The supplied of identifiers.
   */
  private final Supplier<UniqueIdentifier> _uniqueIdSupplier;

  /**
   * Creates an instance using the default scheme for any {@code UniqueIdentifier} created.
   */
  public InMemoryHistoricalTimeSeriesMaster() {
    this(new UniqueIdentifierSupplier(DEFAULT_UID_SCHEME));
  }

  /**
   * Creates an instance specifying the supplier of unique identifiers.
   * 
   * @param uniqueIdSupplier  the supplier of unique identifiers, not null
   */
  private InMemoryHistoricalTimeSeriesMaster(final Supplier<UniqueIdentifier> uniqueIdSupplier) {
    ArgumentChecker.notNull(uniqueIdSupplier, "uniqueIdSupplier");
    _uniqueIdSupplier = uniqueIdSupplier;
  }

  //-------------------------------------------------------------------------
  @Override
  public List<IdentifierBundleWithDates> getAllIdentifiers() {
    List<IdentifierBundleWithDates> result = new ArrayList<IdentifierBundleWithDates>();
    Collection<HistoricalTimeSeriesDocument> docs = _store.values();
    for (HistoricalTimeSeriesDocument tsDoc : docs) {
      result.add(tsDoc.getSeries().getIdentifiers());
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesSearchResult search(final HistoricalTimeSeriesSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    List<HistoricalTimeSeriesDocument> list = new ArrayList<HistoricalTimeSeriesDocument>();
    for (HistoricalTimeSeriesDocument doc : _store.values()) {
      if (request.matches(doc)) {
        list.add(filter(
            doc, request.isLoadEarliestLatest(), request.isLoadTimeSeries(), request.getStart(), request.getEnd()));
      }
    }
    final HistoricalTimeSeriesSearchResult result = new HistoricalTimeSeriesSearchResult();
    result.setPaging(Paging.of(request.getPagingRequest(), list));
    result.getDocuments().addAll(request.getPagingRequest().select(list));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesDocument get(UniqueIdentifier uniqueId) {
    validateId(uniqueId);
    final HistoricalTimeSeriesDocument document = _store.get(uniqueId);
    if (document == null) {
      throw new DataNotFoundException("Historical time-series not found: " + uniqueId);
    }
    return document;
  }

  public HistoricalTimeSeriesDocument get(HistoricalTimeSeriesGetRequest request) {
    final HistoricalTimeSeriesDocument document = _store.get(request.getUniqueId());
    return filter(document, request.isLoadEarliestLatest(), request.isLoadTimeSeries(), request.getStart(), request.getEnd());
  }

  private HistoricalTimeSeriesDocument filter(
      HistoricalTimeSeriesDocument original, boolean loadEarliestLatest, boolean loadTimeSeries, LocalDate start, LocalDate end) {
    HistoricalTimeSeriesDocument copy = original;
    if (loadEarliestLatest) {
      copy = clone(original, copy);
      copy.setLatest(copy.getSeries().getTimeSeries().getLatestTime());
      copy.setEarliest(copy.getSeries().getTimeSeries().getEarliestTime());
    }
    if (loadTimeSeries) {
      if (start != null && end != null) {
        copy = clone(original, copy);
        LocalDateDoubleTimeSeries subseries = copy.getSeries().getTimeSeries().subSeries(start, true, end, false).toLocalDateDoubleTimeSeries();
        copy.getSeries().setTimeSeries(subseries);
      }
    } else {
      copy = clone(original, copy);
      copy.getSeries().setTimeSeries(null);
    }
    return copy;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesDocument add(HistoricalTimeSeriesDocument document) {
    validateDocument(document);
    if (!contains(document)) {
      final UniqueIdentifier uniqueId = _uniqueIdSupplier.get();
      final HistoricalTimeSeriesDocument doc = clone(document);
      doc.setUniqueId(uniqueId);
      _store.put(uniqueId, doc);  // unique identifier should be unique
      document.setUniqueId(uniqueId);
      return document;
    } else {
      throw new IllegalArgumentException("Cannot add duplicate time-series for identifiers " + document.getSeries().getIdentifiers());
    }
  }

  private boolean contains(HistoricalTimeSeriesDocument document) {
    ManageableHistoricalTimeSeries series = document.getSeries();
    for (IdentifierWithDates identifierWithDates : series.getIdentifiers()) {
      Identifier identifier = identifierWithDates.asIdentifier();
      UniqueIdentifier uniqueId = resolveIdentifier(
          IdentifierBundle.of(identifier), 
          identifierWithDates.getValidFrom(), 
          series.getDataSource(), 
          series.getDataProvider(), 
          series.getDataField());
      if (uniqueId != null) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public HistoricalTimeSeriesDocument update(HistoricalTimeSeriesDocument document) {
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    validateDocument(document);
    
    final UniqueIdentifier uniqueId = document.getUniqueId();
    final HistoricalTimeSeriesDocument storedDocument = _store.get(uniqueId);
    if (storedDocument == null) {
      throw new DataNotFoundException("Historical time-series not found: " + uniqueId);
    }
    if (_store.replace(uniqueId, storedDocument, document) == false) {
      throw new IllegalArgumentException("Concurrent modification");
    }
    return document;
  }
  
  @Override
  public void remove(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    if (_store.remove(uniqueId) == null) {
      throw new DataNotFoundException("Historical time-series not found: " + uniqueId);
    }
  }

  @Override
  public HistoricalTimeSeriesHistoryResult history(final HistoricalTimeSeriesHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    final HistoricalTimeSeriesHistoryResult result = new HistoricalTimeSeriesHistoryResult();
    final HistoricalTimeSeriesDocument doc = get(request.getObjectId().atLatestVersion());
    if (doc != null) {
      result.getDocuments().add(doc);
    }
    result.setPaging(Paging.of(result.getDocuments()));
    return result;
  }

  //-------------------------------------------------------------------------
  @Override
  public DataPointDocument updateDataPoint(DataPointDocument document) {
    ArgumentChecker.notNull(document, "dataPoint document");
    ArgumentChecker.notNull(document.getDate(), "data point date");
    ArgumentChecker.notNull(document.getValue(), "data point value");
    
    UniqueIdentifier timeSeriesId = document.getHistoricalTimeSeriesId();
    validateId(timeSeriesId);
    
    HistoricalTimeSeriesDocument storeDoc = _store.get(timeSeriesId);
    LocalDateDoubleTimeSeries timeSeries = storeDoc.getSeries().getTimeSeries();
    MapLocalDateDoubleTimeSeries mutableTS = new MapLocalDateDoubleTimeSeries(timeSeries);
    mutableTS.putDataPoint(document.getDate(), document.getValue());
    storeDoc.getSeries().setTimeSeries(mutableTS);
    return document;
  }

  @Override
  public DataPointDocument addDataPoint(DataPointDocument document) {
    ArgumentChecker.notNull(document, "dataPoint document");
    ArgumentChecker.notNull(document.getDate(), "data point date");
    ArgumentChecker.notNull(document.getValue(), "data point value");
    UniqueIdentifier timeSeriesId = document.getHistoricalTimeSeriesId();
    validateId(timeSeriesId);
    
    HistoricalTimeSeriesDocument storedDoc = _store.get(timeSeriesId);
    MapLocalDateDoubleTimeSeries mutableTS = new MapLocalDateDoubleTimeSeries();
    mutableTS.putDataPoint(document.getDate(), document.getValue());
    LocalDateDoubleTimeSeries mergedTS = storedDoc.getSeries().getTimeSeries().noIntersectionOperation(mutableTS).toLocalDateDoubleTimeSeries();
    storedDoc.getSeries().setTimeSeries(mergedTS);
    
    String uniqueId = new StringBuilder(timeSeriesId.getValue()).append("/").append(DateUtil.printYYYYMMDD(document.getDate())).toString();
    document.setDataPointId(UniqueIdentifier.of(DEFAULT_UID_SCHEME, uniqueId));
    return document;
    
  }
  
  @Override
  public DataPointDocument getDataPoint(UniqueIdentifier uniqueId) {
    Pair<Long, LocalDate> uniqueIdPair = validateAndGetDataPointId(uniqueId);
    
    Long tsId = uniqueIdPair.getFirst();
    LocalDate date = uniqueIdPair.getSecond();
    
    final DataPointDocument result = new DataPointDocument();
    result.setDate(uniqueIdPair.getSecond());
    result.setHistoricalTimeSeriesId(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
    result.setDataPointId(uniqueId);
    
    HistoricalTimeSeriesDocument storedDoc = _store.get(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
    Double value = storedDoc.getSeries().getTimeSeries().getValue(date);
    result.setValue(value);
       
    return result;
  }
  
  private Pair<Long, LocalDate> validateAndGetDataPointId(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "DataPoint UID");
    ArgumentChecker.isTrue(uniqueId.getScheme().equals(DEFAULT_UID_SCHEME), "UID not TssMemory");
    ArgumentChecker.isTrue(uniqueId.getValue() != null, "Uid value cannot be null");
    String[] tokens = StringUtils.split(uniqueId.getValue(), '/');
    if (tokens.length != 2) {
      throw new IllegalArgumentException("UID not expected format<12345/date> " + uniqueId);
    }
    String id = tokens[0];
    String dateStr = tokens[1];
    LocalDate date = null;
    Long tsId = Long.MIN_VALUE;
    if (id != null && dateStr != null) {
      try {
        date = DateUtil.toLocalDate(dateStr);
      } catch (CalendricalParseException ex) {
        throw new IllegalArgumentException("UID not expected format<12345/date> " + uniqueId, ex);
      }
      try {
        tsId = Long.parseLong(id);
      } catch (NumberFormatException ex) {
        throw new IllegalArgumentException("UID not expected format<12345/date> " + uniqueId, ex);
      }
    } else {
      throw new IllegalArgumentException("UID not expected format<12345/date> " + uniqueId);
    }
    return Pair.of(tsId, date);
  }

  @Override
  public void removeDataPoint(UniqueIdentifier uniqueId) {
    Pair<Long, LocalDate> uniqueIdPair = validateAndGetDataPointId(uniqueId);
    
    Long tsId = uniqueIdPair.getFirst();
    HistoricalTimeSeriesDocument storedDoc = _store.get(UniqueIdentifier.of(DEFAULT_UID_SCHEME, String.valueOf(tsId)));
    
    MapLocalDateDoubleTimeSeries mutableTS = new MapLocalDateDoubleTimeSeries(storedDoc.getSeries().getTimeSeries());
    mutableTS.removeDataPoint(uniqueIdPair.getSecond());
    storedDoc.getSeries().setTimeSeries(mutableTS);
  }

  @Override
  public void appendTimeSeries(HistoricalTimeSeriesDocument document) {
    validateDocument(document);
    
    validateId(document.getUniqueId());
    UniqueIdentifier uniqueId = document.getUniqueId();
    HistoricalTimeSeriesDocument storedDoc = _store.get(uniqueId);
    LocalDateDoubleTimeSeries mergedTS = storedDoc.getSeries().getTimeSeries().noIntersectionOperation(document.getSeries().getTimeSeries()).toLocalDateDoubleTimeSeries();
    storedDoc.getSeries().setTimeSeries(mergedTS);
  }

  @Override
  public UniqueIdentifier resolveIdentifier(IdentifierBundle securityBundle, String dataSource, String dataProvider, String dataField) {
    return resolveIdentifier(securityBundle, (LocalDate) null, dataSource, dataProvider, dataField);
  }

  @Override
  public UniqueIdentifier resolveIdentifier(IdentifierBundle securityKey, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    ArgumentChecker.notNull(securityKey, "securityBundle");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataProvider, "dataProvider");
    ArgumentChecker.notNull(dataField, "dataField");
    
    for (Entry<UniqueIdentifier, HistoricalTimeSeriesDocument> entry : _store.entrySet()) {
      UniqueIdentifier uniqueId = entry.getKey();
      ManageableHistoricalTimeSeries mhts = entry.getValue().getSeries();
      if (mhts.getDataSource().equals(dataSource) && mhts.getDataProvider().equals(dataProvider) && mhts.getDataField().equals(dataField)) {
        for (IdentifierWithDates idWithDates : mhts.getIdentifiers()) {
          if (securityKey.contains(idWithDates.asIdentifier())) {
            LocalDate validFrom = idWithDates.getValidFrom();
            LocalDate validTo = idWithDates.getValidTo();
            if (currentDate != null) {
              if (currentDate.equals(validFrom) || (currentDate.isAfter(validFrom) && currentDate.isBefore(validTo)) || currentDate.equals(validTo)) {
                return uniqueId;
              }
            } else {
              return uniqueId;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public void removeDataPoints(UniqueIdentifier timeSeriesUid, LocalDate firstDateToRetain) {
    validateId(timeSeriesUid);
    HistoricalTimeSeriesDocument storedDoc = _store.get(timeSeriesUid);
    LocalDateDoubleTimeSeries timeSeries = storedDoc.getSeries().getTimeSeries();
    LocalDateDoubleTimeSeries subSeries = timeSeries.subSeries(firstDateToRetain, true, timeSeries.getLatestTime(), false).toLocalDateDoubleTimeSeries();
    storedDoc.getSeries().setTimeSeries(subSeries);
  }

  //-------------------------------------------------------------------------
  private HistoricalTimeSeriesDocument clone(HistoricalTimeSeriesDocument original, HistoricalTimeSeriesDocument copy) {
    if (copy != original) {
      return copy;
    }
    return clone(original);
  }

  @SuppressWarnings("unchecked")
  private <T extends Bean> T clone(T original) {
    BeanBuilder<? extends Bean> builder = original.metaBean().builder();
    for (MetaProperty<Object> mp : original.metaBean().metaPropertyIterable()) {
      if (mp.readWrite().isWritable()) {
        Object value = mp.get(original);
        if (value instanceof DirectBean) {
          value = clone((DirectBean) value);
        }
        builder.set(mp.name(), value);
      }
    }
    return (T) builder.build();
  }

  private long validateId(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.isTrue(uniqueId.getScheme().equals(DEFAULT_UID_SCHEME), "historicalTimeSeriesId scheme invalid");
    try {
      return Long.parseLong(uniqueId.getValue());
    } catch (NumberFormatException ex) {
      throw new IllegalArgumentException("Invalid uniqueId " + uniqueId);
    }
  }

  private void validateDocument(HistoricalTimeSeriesDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getSeries(), "document.series");
    ArgumentChecker.notNull(document.getSeries().getTimeSeries(), "document.series.timeSeries");
    ArgumentChecker.notNull(document.getSeries().getIdentifiers(), "document.series.identifiers");
    ArgumentChecker.isTrue(document.getSeries().getIdentifiers().asIdentifierBundle().getIdentifiers().size() > 0, "document.series.identifiers must not be empty");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getSeries().getDataSource()), "document.series.dataSource must not be blank");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getSeries().getDataProvider()), "document.series.dataProvider must not be blank");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getSeries().getDataField()), "document.series.dataField must not be blank");
    ArgumentChecker.isTrue(StringUtils.isNotBlank(document.getSeries().getObservationTime()), "document.series.observationTime must not be blank");
  }

}
