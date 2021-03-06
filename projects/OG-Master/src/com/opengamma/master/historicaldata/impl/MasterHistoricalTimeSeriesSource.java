/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.historicaldata.HistoricalTimeSeries;
import com.opengamma.core.historicaldata.HistoricalTimeSeriesSource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesDocument;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesGetRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchRequest;
import com.opengamma.master.historicaldata.HistoricalTimeSeriesSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A {@code HistoricalTimeSeriesSource} implemented using an underlying {@code HistoricalTimeSeriesMaster}.
 * <p>
 * The {@link HistoricalTimeSeriesSource} interface provides time-series to the engine via a narrow API.
 * This class provides the source on top of a standard {@link HistoricalTimeSeriesMaster}.
 */
@PublicSPI
public class MasterHistoricalTimeSeriesSource implements HistoricalTimeSeriesSource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(MasterHistoricalTimeSeriesSource.class);
  /**
   * The historical time-series master.
   */
  private final HistoricalTimeSeriesMaster _master;
  /**
   * The resolver.
   */
  private final HistoricalTimeSeriesResolver _resolver;

  /**
   * Creates an instance wrapping an underlying historical time-series master.
   * 
   * @param master  the historical time-series master, not null
   * @param resolver  the resolver, not null
   */
  public MasterHistoricalTimeSeriesSource(HistoricalTimeSeriesMaster master, HistoricalTimeSeriesResolver resolver) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(resolver, "resolver");
    _master = master;
    _resolver = resolver;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying historical time-series master.
   * 
   * @return the historical time-series master, not null
   */
  public HistoricalTimeSeriesMaster getMaster() {
    return _master;
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueIdentifier uniqueId) {
    return doGetHistoricalTimeSeries(uniqueId, null, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueIdentifier uniqueId, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (start != null && !inclusiveStart) {
      start = start.plusDays(1);
    }
    if (end != null && exclusiveEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(uniqueId, start, end);
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(UniqueIdentifier uniqueId, LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    HistoricalTimeSeriesGetRequest request = new HistoricalTimeSeriesGetRequest(uniqueId);
    request.setLoadEarliestLatest(false);
    request.setLoadTimeSeries(true);
    request.setStart(start);
    request.setEnd(end);
    HistoricalTimeSeriesDocument doc = getMaster().get(request);
    return doc.getSeries();
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle securityBundle, String dataSource, String dataProvider, String dataField) {
    return doGetHistoricalTimeSeries(securityBundle, (LocalDate) null, dataSource, dataProvider, dataField, (LocalDate) null, (LocalDate) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle securityBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField) {
    return doGetHistoricalTimeSeries(securityBundle, identifierValidityDate, dataSource, dataProvider, dataField, (LocalDate) null, (LocalDate) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle securityBundle, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (start != null && !inclusiveStart) {
      start = start.plusDays(1);
    }
    if (end != null && exclusiveEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(securityBundle, (LocalDate) null, dataSource, dataProvider, dataField, start, end);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle securityBundle, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (start != null && !inclusiveStart) {
      start = start.plusDays(1);
    }
    if (end != null && exclusiveEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(securityBundle, identifierValidityDate, dataSource, dataProvider, dataField, start, end);
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(
      IdentifierBundle identifiers, LocalDate identifierValidityDate, String dataSource, String dataProvider, String dataField,
      LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "field");
    
    HistoricalTimeSeriesSearchRequest request = new HistoricalTimeSeriesSearchRequest();
    request.setIdentifiers(identifiers);
    request.setIdentifierValidityDate(identifierValidityDate);
    request.setDataSource(dataSource);
    request.setDataProvider(dataProvider);
    request.setDataField(dataField);
    request.setStart(start);
    request.setEnd(end);
    request.setLoadTimeSeries(true);
    
    HistoricalTimeSeriesSearchResult searchResult = getMaster().search(request);
    List<HistoricalTimeSeriesDocument> documents = searchResult.getDocuments();
    if (documents.isEmpty()) {
      return null;
    }
    if (documents.size() > 1) {
      Object[] param = new Object[]{identifiers, dataSource, dataProvider, dataField, start, end};
      s_logger.warn("multiple timeseries returned for identifiers={}, dataSource={}, dataProvider={}, dataField={}, start={} end={}", param);
    }
    HistoricalTimeSeriesDocument doc = documents.get(0);
    return doc.getSeries();
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, String resolutionKey) {
    return doGetHistoricalTimeSeries(dataField, identifierBundle, resolutionKey, (LocalDate) null, (LocalDate) null, (LocalDate) null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, String resolutionKey, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (start != null && !inclusiveStart) {
      start = start.plusDays(1);
    }
    if (end != null && exclusiveEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(dataField, identifierBundle, resolutionKey, (LocalDate) null, start, end);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    return doGetHistoricalTimeSeries(dataField, identifierBundle, resolutionKey, identifierValidityDate, null, null);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    if (start != null && !inclusiveStart) {
      start = start.plusDays(1);
    }
    if (end != null && exclusiveEnd) {
      end = end.minusDays(1);
    }
    return doGetHistoricalTimeSeries(dataField, identifierBundle, resolutionKey, identifierValidityDate, start, end);
  }

  private HistoricalTimeSeries doGetHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, String resolutionKey, LocalDate identifierValidityDate,
      LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notEmpty(identifierBundle, "identifierBundle");
    if (StringUtils.isBlank(resolutionKey)) {
      resolutionKey = HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;
    }
    UniqueIdentifier uniqueId = _resolver.resolve(dataField, identifierBundle, resolutionKey);
    if (uniqueId == null) {
      return null;
    }
    return doGetHistoricalTimeSeries(uniqueId, start, end);
  }

  //-------------------------------------------------------------------------
  @Override
  public Map<IdentifierBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
      Set<IdentifierBundle> identifierSet, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    // TODO [PLAT-1046]
    throw new NotImplementedException();
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "MasterHistoricalTimeSeriesSource[" + getMaster() + "]";
  }

}
