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
 * A document used to update a data point in a time-series.
 * <p>
 * This class is mutable and not thread-safe.
 */
@PublicSPI
@BeanDefinition
public class DataPointDocument extends DirectBean {

  /**
   * The parent time-series object identifier.
   */
  @PropertyDefinition
  private UniqueIdentifier _historicalTimeSeriesId;
  /**
   * The data point unique identifier.
   */
  @PropertyDefinition
  private UniqueIdentifier _dataPointId;
  /**
   * The date the value refers to.
   */
  @PropertyDefinition
  private LocalDate _date;
  /**
   * The time-series value.
   */
  @PropertyDefinition
  private Double _value;

  /**
   * Creates an instance.
   */
  public DataPointDocument() {
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DataPointDocument}.
   * @return the meta-bean, not null
   */
  public static DataPointDocument.Meta meta() {
    return DataPointDocument.Meta.INSTANCE;
  }

  @Override
  public DataPointDocument.Meta metaBean() {
    return DataPointDocument.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case 529494473:  // historicalTimeSeriesId
        return getHistoricalTimeSeriesId();
      case -1881813055:  // dataPointId
        return getDataPointId();
      case 3076014:  // date
        return getDate();
      case 111972721:  // value
        return getValue();
    }
    return super.propertyGet(propertyName);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case 529494473:  // historicalTimeSeriesId
        setHistoricalTimeSeriesId((UniqueIdentifier) newValue);
        return;
      case -1881813055:  // dataPointId
        setDataPointId((UniqueIdentifier) newValue);
        return;
      case 3076014:  // date
        setDate((LocalDate) newValue);
        return;
      case 111972721:  // value
        setValue((Double) newValue);
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
      DataPointDocument other = (DataPointDocument) obj;
      return JodaBeanUtils.equal(getHistoricalTimeSeriesId(), other.getHistoricalTimeSeriesId()) &&
          JodaBeanUtils.equal(getDataPointId(), other.getDataPointId()) &&
          JodaBeanUtils.equal(getDate(), other.getDate()) &&
          JodaBeanUtils.equal(getValue(), other.getValue());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDataPointId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getValue());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the parent time-series object identifier.
   * @return the value of the property
   */
  public UniqueIdentifier getHistoricalTimeSeriesId() {
    return _historicalTimeSeriesId;
  }

  /**
   * Sets the parent time-series object identifier.
   * @param historicalTimeSeriesId  the new value of the property
   */
  public void setHistoricalTimeSeriesId(UniqueIdentifier historicalTimeSeriesId) {
    this._historicalTimeSeriesId = historicalTimeSeriesId;
  }

  /**
   * Gets the the {@code historicalTimeSeriesId} property.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> historicalTimeSeriesId() {
    return metaBean().historicalTimeSeriesId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the data point unique identifier.
   * @return the value of the property
   */
  public UniqueIdentifier getDataPointId() {
    return _dataPointId;
  }

  /**
   * Sets the data point unique identifier.
   * @param dataPointId  the new value of the property
   */
  public void setDataPointId(UniqueIdentifier dataPointId) {
    this._dataPointId = dataPointId;
  }

  /**
   * Gets the the {@code dataPointId} property.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> dataPointId() {
    return metaBean().dataPointId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the date the value refers to.
   * @return the value of the property
   */
  public LocalDate getDate() {
    return _date;
  }

  /**
   * Sets the date the value refers to.
   * @param date  the new value of the property
   */
  public void setDate(LocalDate date) {
    this._date = date;
  }

  /**
   * Gets the the {@code date} property.
   * @return the property, not null
   */
  public final Property<LocalDate> date() {
    return metaBean().date().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time-series value.
   * @return the value of the property
   */
  public Double getValue() {
    return _value;
  }

  /**
   * Sets the time-series value.
   * @param value  the new value of the property
   */
  public void setValue(Double value) {
    this._value = value;
  }

  /**
   * Gets the the {@code value} property.
   * @return the property, not null
   */
  public final Property<Double> value() {
    return metaBean().value().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DataPointDocument}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code historicalTimeSeriesId} property.
     */
    private final MetaProperty<UniqueIdentifier> _historicalTimeSeriesId = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesId", DataPointDocument.class, UniqueIdentifier.class);
    /**
     * The meta-property for the {@code dataPointId} property.
     */
    private final MetaProperty<UniqueIdentifier> _dataPointId = DirectMetaProperty.ofReadWrite(
        this, "dataPointId", DataPointDocument.class, UniqueIdentifier.class);
    /**
     * The meta-property for the {@code date} property.
     */
    private final MetaProperty<LocalDate> _date = DirectMetaProperty.ofReadWrite(
        this, "date", DataPointDocument.class, LocalDate.class);
    /**
     * The meta-property for the {@code value} property.
     */
    private final MetaProperty<Double> _value = DirectMetaProperty.ofReadWrite(
        this, "value", DataPointDocument.class, Double.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "historicalTimeSeriesId",
        "dataPointId",
        "date",
        "value");

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
        case -1881813055:  // dataPointId
          return _dataPointId;
        case 3076014:  // date
          return _date;
        case 111972721:  // value
          return _value;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DataPointDocument> builder() {
      return new DirectBeanBuilder<DataPointDocument>(new DataPointDocument());
    }

    @Override
    public Class<? extends DataPointDocument> beanType() {
      return DataPointDocument.class;
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
    public final MetaProperty<UniqueIdentifier> historicalTimeSeriesId() {
      return _historicalTimeSeriesId;
    }

    /**
     * The meta-property for the {@code dataPointId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueIdentifier> dataPointId() {
      return _dataPointId;
    }

    /**
     * The meta-property for the {@code date} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> date() {
      return _date;
    }

    /**
     * The meta-property for the {@code value} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> value() {
      return _value;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
