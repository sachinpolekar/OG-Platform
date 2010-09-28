/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaldata.rest;

import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.HISTORICALDATASOURCE_TIMESERIES;
import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.HISTORICALDATASOURCE_UNIQUEIDENTIFIER;
import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.NULL_VALUE;

import java.util.List;

import javax.time.calendar.LocalDate;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.historicaldata.HistoricalDataSource;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * REST resource wrapper for a {@link HistoricalDataSource}.
 */
public class HistoricalDataSourceResource {

  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;
  /**
   * The underlying data source.
   */
  private final HistoricalDataSource _dataSource;

  /**
   * Creates a resource to expose a data source over REST.
   * @param fudgeContext  the context, not null
   * @param dataSource  the data source, not null
   */
  public HistoricalDataSourceResource(final FudgeContext fudgeContext, final HistoricalDataSource dataSource) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(dataSource, "dataSource");
    _fudgeContext = fudgeContext;
    _dataSource = dataSource;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the Fudge context.
   * @return the context, not null
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the data source.
   * @return the data source, not null
   */
  protected HistoricalDataSource getHistoricalDataSource() {
    return _dataSource;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the serialization context derived from the main Fudge context.
   * @return the context, not null
   */
  protected FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }

  private IdentifierBundle identifiersToBundle(final List<String> identifiers) {
    IdentifierBundle bundle = new IdentifierBundle();
    for (String identifier : identifiers) {
      bundle = bundle.withIdentifier(Identifier.parse(identifier));
    }
    return bundle;
  }

  private FudgeMsgEnvelope encodePairMessage(final Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> result) {
    if (result == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsgWithClassHeaders(message, HISTORICALDATASOURCE_UNIQUEIDENTIFIER, null, result.getKey(), UniqueIdentifier.class);
    context.objectToFudgeMsgWithClassHeaders(message, HISTORICALDATASOURCE_TIMESERIES, null, result.getValue(), LocalDateDoubleTimeSeries.class);
    return new FudgeMsgEnvelope(message);
  }

  private FudgeMsgEnvelope encodeTimeSeriesMessage(final LocalDateDoubleTimeSeries result) {
    if (result == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeFieldContainer message = context.newMessage();
    context.objectToFudgeMsgWithClassHeaders(message, HISTORICALDATASOURCE_TIMESERIES, null, result, LocalDateDoubleTimeSeries.class);
    return new FudgeMsgEnvelope(message);
  }

  @GET
  @Path("all/{dataSource}/{dataProvider}/{dataField}")
  public FudgeMsgEnvelope getAll(@PathParam("dataSource") String dataSource, @PathParam("dataProvider") String dataProvider, @PathParam("dataField") String dataField,
      @QueryParam("id") List<String> identifiers) {
    return encodePairMessage(getHistoricalDataSource().getHistoricalData(identifiersToBundle(identifiers), dataSource, NULL_VALUE.equals(dataProvider) ? null : dataProvider, dataField));
  }

  @GET
  @Path("allByDate/{dataSource}/{dataProvider}/{dataField}/{start}/{end}")
  public FudgeMsgEnvelope getAllByDate(@PathParam("dataSource") String dataSource, @PathParam("dataProvider") String dataProvider, @PathParam("dataField") String dataField,
      @PathParam("start") String start, @PathParam("end") String end, @QueryParam("id") List<String> identifiers) {
    return encodePairMessage(getHistoricalDataSource().getHistoricalData(identifiersToBundle(identifiers), dataSource, NULL_VALUE.equals(dataProvider) ? null : dataProvider, dataField,
        NULL_VALUE.equals(start) ? null : LocalDate.parse(start), NULL_VALUE.equals(end) ? null : LocalDate.parse(end)));
  }

  @GET
  @Path("default")
  public FudgeMsgEnvelope getDefault(@QueryParam("id") List<String> identifiers) {
    return encodePairMessage(getHistoricalDataSource().getHistoricalData(identifiersToBundle(identifiers)));
  }

  @GET
  @Path("defaultByDate/{start}/{end}")
  public FudgeMsgEnvelope getDefaultByDate(@PathParam("start") String start, @PathParam("end") String end, @QueryParam("id") List<String> identifiers) {
    return encodePairMessage(getHistoricalDataSource().getHistoricalData(identifiersToBundle(identifiers), NULL_VALUE.equals(start) ? null : LocalDate.parse(start),
        NULL_VALUE.equals(end) ? null : LocalDate.parse(end)));
  }

  @GET
  @Path("uid/{uid}")
  public FudgeMsgEnvelope getUid(@PathParam("uid") String uid) {
    return encodeTimeSeriesMessage(getHistoricalDataSource().getHistoricalData(UniqueIdentifier.parse(uid)));
  }

  @GET
  @Path("uidByDate/{uid}/{start}/{end}")
  public FudgeMsgEnvelope getUidByDate(@PathParam("uid") String uid, @PathParam("start") String start, @PathParam("end") String end) {
    return encodeTimeSeriesMessage(getHistoricalDataSource().getHistoricalData(UniqueIdentifier.parse(uid), NULL_VALUE.equals(start) ? null : LocalDate.parse(start),
        NULL_VALUE.equals(end) ? null : LocalDate.parse(end)));
  }

  /**
   * For debugging purposes only.
   * 
   * @return some debug information about the state of this resource object; e.g. which underlying objects is it connected to.
   */
  @GET
  @Path("debugInfo")
  public FudgeMsgEnvelope getDebugInfo() {
    final MutableFudgeFieldContainer message = getFudgeContext().newMessage();
    message.add("fudgeContext", getFudgeContext().toString());
    message.add("historicalDataSource", getHistoricalDataSource().toString());
    return new FudgeMsgEnvelope(message);
  }

}