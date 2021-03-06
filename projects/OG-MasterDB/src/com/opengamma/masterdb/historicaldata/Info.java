/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaldata;

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

import com.opengamma.id.IdentifierBundleWithDates;

/**
 * The info about historical time-series.
 * <p>
 * This is for the internal use of the data store.
 */
@BeanDefinition
/*package*/ class Info extends DirectBean {

  /**
   * The historical time-series db id.
   */
  @PropertyDefinition
  private Long _historicalTimeSeriesId;
  /**
   * The identifier bundle id.
   */
  @PropertyDefinition
  private Long _identifierBundleId;
  /**
   * The data store.
   */
  @PropertyDefinition
  private String _dataSource;
  /**
   * The data provider.
   */
  @PropertyDefinition
  private String _dataProvider;
  /**
   * The data field.
   */
  @PropertyDefinition
  private String _dataField;
  /**
   * The observation time.
   */
  @PropertyDefinition
  private String _observationTime;
  /**
   * Identifiers with valid dates if available
   */
  @PropertyDefinition
  private IdentifierBundleWithDates _identifiers;
  /**
   * The start date.
   */
  @PropertyDefinition
  private LocalDate _earliestDate;
  /**
   * The end date.
   */
  @PropertyDefinition
  private LocalDate _latestDate;

  /**
   * Creates an instance.
   */
  public Info() {
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code Info}.
   * @return the meta-bean, not null
   */
  public static Info.Meta meta() {
    return Info.Meta.INSTANCE;
  }

  @Override
  public Info.Meta metaBean() {
    return Info.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case 529494473:  // historicalTimeSeriesId
        return getHistoricalTimeSeriesId();
      case 704215302:  // identifierBundleId
        return getIdentifierBundleId();
      case 1272470629:  // dataSource
        return getDataSource();
      case 339742651:  // dataProvider
        return getDataProvider();
      case -386794640:  // dataField
        return getDataField();
      case 951232793:  // observationTime
        return getObservationTime();
      case 1368189162:  // identifiers
        return getIdentifiers();
      case 239226785:  // earliestDate
        return getEarliestDate();
      case -125315115:  // latestDate
        return getLatestDate();
    }
    return super.propertyGet(propertyName);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case 529494473:  // historicalTimeSeriesId
        setHistoricalTimeSeriesId((Long) newValue);
        return;
      case 704215302:  // identifierBundleId
        setIdentifierBundleId((Long) newValue);
        return;
      case 1272470629:  // dataSource
        setDataSource((String) newValue);
        return;
      case 339742651:  // dataProvider
        setDataProvider((String) newValue);
        return;
      case -386794640:  // dataField
        setDataField((String) newValue);
        return;
      case 951232793:  // observationTime
        setObservationTime((String) newValue);
        return;
      case 1368189162:  // identifiers
        setIdentifiers((IdentifierBundleWithDates) newValue);
        return;
      case 239226785:  // earliestDate
        setEarliestDate((LocalDate) newValue);
        return;
      case -125315115:  // latestDate
        setLatestDate((LocalDate) newValue);
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
      Info other = (Info) obj;
      return JodaBeanUtils.equal(getHistoricalTimeSeriesId(), other.getHistoricalTimeSeriesId()) &&
          JodaBeanUtils.equal(getIdentifierBundleId(), other.getIdentifierBundleId()) &&
          JodaBeanUtils.equal(getDataSource(), other.getDataSource()) &&
          JodaBeanUtils.equal(getDataProvider(), other.getDataProvider()) &&
          JodaBeanUtils.equal(getDataField(), other.getDataField()) &&
          JodaBeanUtils.equal(getObservationTime(), other.getObservationTime()) &&
          JodaBeanUtils.equal(getIdentifiers(), other.getIdentifiers()) &&
          JodaBeanUtils.equal(getEarliestDate(), other.getEarliestDate()) &&
          JodaBeanUtils.equal(getLatestDate(), other.getLatestDate());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdentifierBundleId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataSource());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataProvider());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataField());
    hash += hash * 31 + JodaBeanUtils.hashCode(getObservationTime());
    hash += hash * 31 + JodaBeanUtils.hashCode(getIdentifiers());
    hash += hash * 31 + JodaBeanUtils.hashCode(getEarliestDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getLatestDate());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the historical time-series db id.
   * @return the value of the property
   */
  public Long getHistoricalTimeSeriesId() {
    return _historicalTimeSeriesId;
  }

  /**
   * Sets the historical time-series db id.
   * @param historicalTimeSeriesId  the new value of the property
   */
  public void setHistoricalTimeSeriesId(Long historicalTimeSeriesId) {
    this._historicalTimeSeriesId = historicalTimeSeriesId;
  }

  /**
   * Gets the the {@code historicalTimeSeriesId} property.
   * @return the property, not null
   */
  public final Property<Long> historicalTimeSeriesId() {
    return metaBean().historicalTimeSeriesId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the identifier bundle id.
   * @return the value of the property
   */
  public Long getIdentifierBundleId() {
    return _identifierBundleId;
  }

  /**
   * Sets the identifier bundle id.
   * @param identifierBundleId  the new value of the property
   */
  public void setIdentifierBundleId(Long identifierBundleId) {
    this._identifierBundleId = identifierBundleId;
  }

  /**
   * Gets the the {@code identifierBundleId} property.
   * @return the property, not null
   */
  public final Property<Long> identifierBundleId() {
    return metaBean().identifierBundleId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data store.
   * @return the value of the property
   */
  public String getDataSource() {
    return _dataSource;
  }

  /**
   * Sets the data store.
   * @param dataSource  the new value of the property
   */
  public void setDataSource(String dataSource) {
    this._dataSource = dataSource;
  }

  /**
   * Gets the the {@code dataSource} property.
   * @return the property, not null
   */
  public final Property<String> dataSource() {
    return metaBean().dataSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data provider.
   * @return the value of the property
   */
  public String getDataProvider() {
    return _dataProvider;
  }

  /**
   * Sets the data provider.
   * @param dataProvider  the new value of the property
   */
  public void setDataProvider(String dataProvider) {
    this._dataProvider = dataProvider;
  }

  /**
   * Gets the the {@code dataProvider} property.
   * @return the property, not null
   */
  public final Property<String> dataProvider() {
    return metaBean().dataProvider().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data field.
   * @return the value of the property
   */
  public String getDataField() {
    return _dataField;
  }

  /**
   * Sets the data field.
   * @param dataField  the new value of the property
   */
  public void setDataField(String dataField) {
    this._dataField = dataField;
  }

  /**
   * Gets the the {@code dataField} property.
   * @return the property, not null
   */
  public final Property<String> dataField() {
    return metaBean().dataField().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the observation time.
   * @return the value of the property
   */
  public String getObservationTime() {
    return _observationTime;
  }

  /**
   * Sets the observation time.
   * @param observationTime  the new value of the property
   */
  public void setObservationTime(String observationTime) {
    this._observationTime = observationTime;
  }

  /**
   * Gets the the {@code observationTime} property.
   * @return the property, not null
   */
  public final Property<String> observationTime() {
    return metaBean().observationTime().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets identifiers with valid dates if available
   * @return the value of the property
   */
  public IdentifierBundleWithDates getIdentifiers() {
    return _identifiers;
  }

  /**
   * Sets identifiers with valid dates if available
   * @param identifiers  the new value of the property
   */
  public void setIdentifiers(IdentifierBundleWithDates identifiers) {
    this._identifiers = identifiers;
  }

  /**
   * Gets the the {@code identifiers} property.
   * @return the property, not null
   */
  public final Property<IdentifierBundleWithDates> identifiers() {
    return metaBean().identifiers().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the start date.
   * @return the value of the property
   */
  public LocalDate getEarliestDate() {
    return _earliestDate;
  }

  /**
   * Sets the start date.
   * @param earliestDate  the new value of the property
   */
  public void setEarliestDate(LocalDate earliestDate) {
    this._earliestDate = earliestDate;
  }

  /**
   * Gets the the {@code earliestDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> earliestDate() {
    return metaBean().earliestDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the end date.
   * @return the value of the property
   */
  public LocalDate getLatestDate() {
    return _latestDate;
  }

  /**
   * Sets the end date.
   * @param latestDate  the new value of the property
   */
  public void setLatestDate(LocalDate latestDate) {
    this._latestDate = latestDate;
  }

  /**
   * Gets the the {@code latestDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> latestDate() {
    return metaBean().latestDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code Info}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code historicalTimeSeriesId} property.
     */
    private final MetaProperty<Long> _historicalTimeSeriesId = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesId", Info.class, Long.class);
    /**
     * The meta-property for the {@code identifierBundleId} property.
     */
    private final MetaProperty<Long> _identifierBundleId = DirectMetaProperty.ofReadWrite(
        this, "identifierBundleId", Info.class, Long.class);
    /**
     * The meta-property for the {@code dataSource} property.
     */
    private final MetaProperty<String> _dataSource = DirectMetaProperty.ofReadWrite(
        this, "dataSource", Info.class, String.class);
    /**
     * The meta-property for the {@code dataProvider} property.
     */
    private final MetaProperty<String> _dataProvider = DirectMetaProperty.ofReadWrite(
        this, "dataProvider", Info.class, String.class);
    /**
     * The meta-property for the {@code dataField} property.
     */
    private final MetaProperty<String> _dataField = DirectMetaProperty.ofReadWrite(
        this, "dataField", Info.class, String.class);
    /**
     * The meta-property for the {@code observationTime} property.
     */
    private final MetaProperty<String> _observationTime = DirectMetaProperty.ofReadWrite(
        this, "observationTime", Info.class, String.class);
    /**
     * The meta-property for the {@code identifiers} property.
     */
    private final MetaProperty<IdentifierBundleWithDates> _identifiers = DirectMetaProperty.ofReadWrite(
        this, "identifiers", Info.class, IdentifierBundleWithDates.class);
    /**
     * The meta-property for the {@code earliestDate} property.
     */
    private final MetaProperty<LocalDate> _earliestDate = DirectMetaProperty.ofReadWrite(
        this, "earliestDate", Info.class, LocalDate.class);
    /**
     * The meta-property for the {@code latestDate} property.
     */
    private final MetaProperty<LocalDate> _latestDate = DirectMetaProperty.ofReadWrite(
        this, "latestDate", Info.class, LocalDate.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "historicalTimeSeriesId",
        "identifierBundleId",
        "dataSource",
        "dataProvider",
        "dataField",
        "observationTime",
        "identifiers",
        "earliestDate",
        "latestDate");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 529494473:  // historicalTimeSeriesId
          return _historicalTimeSeriesId;
        case 704215302:  // identifierBundleId
          return _identifierBundleId;
        case 1272470629:  // dataSource
          return _dataSource;
        case 339742651:  // dataProvider
          return _dataProvider;
        case -386794640:  // dataField
          return _dataField;
        case 951232793:  // observationTime
          return _observationTime;
        case 1368189162:  // identifiers
          return _identifiers;
        case 239226785:  // earliestDate
          return _earliestDate;
        case -125315115:  // latestDate
          return _latestDate;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends Info> builder() {
      return new DirectBeanBuilder<Info>(new Info());
    }

    @Override
    public Class<? extends Info> beanType() {
      return Info.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code historicalTimeSeriesId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Long> historicalTimeSeriesId() {
      return _historicalTimeSeriesId;
    }

    /**
     * The meta-property for the {@code identifierBundleId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Long> identifierBundleId() {
      return _identifierBundleId;
    }

    /**
     * The meta-property for the {@code dataSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> dataSource() {
      return _dataSource;
    }

    /**
     * The meta-property for the {@code dataProvider} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> dataProvider() {
      return _dataProvider;
    }

    /**
     * The meta-property for the {@code dataField} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> dataField() {
      return _dataField;
    }

    /**
     * The meta-property for the {@code observationTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> observationTime() {
      return _observationTime;
    }

    /**
     * The meta-property for the {@code identifiers} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<IdentifierBundleWithDates> identifiers() {
      return _identifiers;
    }

    /**
     * The meta-property for the {@code earliestDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> earliestDate() {
      return _earliestDate;
    }

    /**
     * The meta-property for the {@code latestDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> latestDate() {
      return _latestDate;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
