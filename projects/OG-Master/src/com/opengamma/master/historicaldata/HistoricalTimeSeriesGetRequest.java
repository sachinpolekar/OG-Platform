/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaldata;

import java.util.Map;

import javax.time.calendar.LocalDate;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.PublicSPI;

/**
 * Request containing options for getting a single historical time-series.
 * <p>
 * Each time-series is potentially large, thus options are available
 * to filter the resulting document.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
@BeanDefinition
public class HistoricalTimeSeriesGetRequest extends DirectBean {

  /**
   * The unique identifier of the historical time-series.
   * This must not be null for a valid search,
   */
  @PropertyDefinition
  private UniqueIdentifier _uniqueId;
  /**
   * Set to true to load the earliest and latest date for time-series.
   * This is true by default.
   */
  @PropertyDefinition
  private boolean _loadEarliestLatest = true;
  /**
   * Set to true to load data points, otherwise return just info.
   * This is true by default.
   */
  @PropertyDefinition
  private boolean _loadTimeSeries = true;
  /**
   * The start date, inclusive, null returns data from the earliest valid date.
   * This is null by default.
   */
  @PropertyDefinition
  private LocalDate _start; 
  /**
   * The end date, inclusive, null returns data up to the latest valid date.
   * This is null by default.
   */
  @PropertyDefinition
  private LocalDate _end;

  /**
   * Creates an instance.
   */
  public HistoricalTimeSeriesGetRequest() {
  }

  /**
   * Creates an instance using a single search identifier.
   * 
   * @param uniqueId  the unique identifier to search for, not null
   */
  public HistoricalTimeSeriesGetRequest(UniqueIdentifier uniqueId) {
    setUniqueId(uniqueId);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code HistoricalTimeSeriesGetRequest}.
   * @return the meta-bean, not null
   */
  public static HistoricalTimeSeriesGetRequest.Meta meta() {
    return HistoricalTimeSeriesGetRequest.Meta.INSTANCE;
  }

  @Override
  public HistoricalTimeSeriesGetRequest.Meta metaBean() {
    return HistoricalTimeSeriesGetRequest.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case -771242688:  // loadEarliestLatest
        return isLoadEarliestLatest();
      case 1833789738:  // loadTimeSeries
        return isLoadTimeSeries();
      case 109757538:  // start
        return getStart();
      case 100571:  // end
        return getEnd();
    }
    return super.propertyGet(propertyName);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        setUniqueId((UniqueIdentifier) newValue);
        return;
      case -771242688:  // loadEarliestLatest
        setLoadEarliestLatest((Boolean) newValue);
        return;
      case 1833789738:  // loadTimeSeries
        setLoadTimeSeries((Boolean) newValue);
        return;
      case 109757538:  // start
        setStart((LocalDate) newValue);
        return;
      case 100571:  // end
        setEnd((LocalDate) newValue);
        return;
    }
    super.propertySet(propertyName, newValue);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      HistoricalTimeSeriesGetRequest other = (HistoricalTimeSeriesGetRequest) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(isLoadEarliestLatest(), other.isLoadEarliestLatest()) &&
          JodaBeanUtils.equal(isLoadTimeSeries(), other.isLoadTimeSeries()) &&
          JodaBeanUtils.equal(getStart(), other.getStart()) &&
          JodaBeanUtils.equal(getEnd(), other.getEnd());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(isLoadEarliestLatest());
    hash += hash * 31 + JodaBeanUtils.hashCode(isLoadTimeSeries());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStart());
    hash += hash * 31 + JodaBeanUtils.hashCode(getEnd());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the historical time-series.
   * This must not be null for a valid search,
   * @return the value of the property
   */
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the historical time-series.
   * This must not be null for a valid search,
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueIdentifier uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This must not be null for a valid search,
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets set to true to load the earliest and latest date for time-series.
   * This is true by default.
   * @return the value of the property
   */
  public boolean isLoadEarliestLatest() {
    return _loadEarliestLatest;
  }

  /**
   * Sets set to true to load the earliest and latest date for time-series.
   * This is true by default.
   * @param loadEarliestLatest  the new value of the property
   */
  public void setLoadEarliestLatest(boolean loadEarliestLatest) {
    this._loadEarliestLatest = loadEarliestLatest;
  }

  /**
   * Gets the the {@code loadEarliestLatest} property.
   * This is true by default.
   * @return the property, not null
   */
  public final Property<Boolean> loadEarliestLatest() {
    return metaBean().loadEarliestLatest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets set to true to load data points, otherwise return just info.
   * This is true by default.
   * @return the value of the property
   */
  public boolean isLoadTimeSeries() {
    return _loadTimeSeries;
  }

  /**
   * Sets set to true to load data points, otherwise return just info.
   * This is true by default.
   * @param loadTimeSeries  the new value of the property
   */
  public void setLoadTimeSeries(boolean loadTimeSeries) {
    this._loadTimeSeries = loadTimeSeries;
  }

  /**
   * Gets the the {@code loadTimeSeries} property.
   * This is true by default.
   * @return the property, not null
   */
  public final Property<Boolean> loadTimeSeries() {
    return metaBean().loadTimeSeries().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the start date, inclusive, null returns data from the earliest valid date.
   * This is null by default.
   * @return the value of the property
   */
  public LocalDate getStart() {
    return _start;
  }

  /**
   * Sets the start date, inclusive, null returns data from the earliest valid date.
   * This is null by default.
   * @param start  the new value of the property
   */
  public void setStart(LocalDate start) {
    this._start = start;
  }

  /**
   * Gets the the {@code start} property.
   * This is null by default.
   * @return the property, not null
   */
  public final Property<LocalDate> start() {
    return metaBean().start().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the end date, inclusive, null returns data up to the latest valid date.
   * This is null by default.
   * @return the value of the property
   */
  public LocalDate getEnd() {
    return _end;
  }

  /**
   * Sets the end date, inclusive, null returns data up to the latest valid date.
   * This is null by default.
   * @param end  the new value of the property
   */
  public void setEnd(LocalDate end) {
    this._end = end;
  }

  /**
   * Gets the the {@code end} property.
   * This is null by default.
   * @return the property, not null
   */
  public final Property<LocalDate> end() {
    return metaBean().end().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code HistoricalTimeSeriesGetRequest}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueIdentifier> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", HistoricalTimeSeriesGetRequest.class, UniqueIdentifier.class);
    /**
     * The meta-property for the {@code loadEarliestLatest} property.
     */
    private final MetaProperty<Boolean> _loadEarliestLatest = DirectMetaProperty.ofReadWrite(
        this, "loadEarliestLatest", HistoricalTimeSeriesGetRequest.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code loadTimeSeries} property.
     */
    private final MetaProperty<Boolean> _loadTimeSeries = DirectMetaProperty.ofReadWrite(
        this, "loadTimeSeries", HistoricalTimeSeriesGetRequest.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code start} property.
     */
    private final MetaProperty<LocalDate> _start = DirectMetaProperty.ofReadWrite(
        this, "start", HistoricalTimeSeriesGetRequest.class, LocalDate.class);
    /**
     * The meta-property for the {@code end} property.
     */
    private final MetaProperty<LocalDate> _end = DirectMetaProperty.ofReadWrite(
        this, "end", HistoricalTimeSeriesGetRequest.class, LocalDate.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "loadEarliestLatest",
        "loadTimeSeries",
        "start",
        "end");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case -771242688:  // loadEarliestLatest
          return _loadEarliestLatest;
        case 1833789738:  // loadTimeSeries
          return _loadTimeSeries;
        case 109757538:  // start
          return _start;
        case 100571:  // end
          return _end;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends HistoricalTimeSeriesGetRequest> builder() {
      return new DirectBeanBuilder<HistoricalTimeSeriesGetRequest>(new HistoricalTimeSeriesGetRequest());
    }

    @Override
    public Class<? extends HistoricalTimeSeriesGetRequest> beanType() {
      return HistoricalTimeSeriesGetRequest.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code loadEarliestLatest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> loadEarliestLatest() {
      return _loadEarliestLatest;
    }

    /**
     * The meta-property for the {@code loadTimeSeries} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> loadTimeSeries() {
      return _loadTimeSeries;
    }

    /**
     * The meta-property for the {@code start} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> start() {
      return _start;
    }

    /**
     * The meta-property for the {@code end} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> end() {
      return _end;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
