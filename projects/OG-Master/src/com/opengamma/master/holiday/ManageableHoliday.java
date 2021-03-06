/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.holiday;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.money.Currency;

/**
 * The manageable implementation of a set of holiday dates.
 * <p>
 * This implementation is used by the holiday master to store and manipulate the data.
 */
@PublicSPI
@BeanDefinition
public class ManageableHoliday extends DirectBean implements Holiday, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the holiday.
   * This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition
  private UniqueIdentifier _uniqueId;
  /**
   * The type of the holiday.
   * This field must not be null for the object to be valid.
   */
  @PropertyDefinition
  private HolidayType _type;
  /**
   * The region key identifier bundle, used when this is a holiday of type BANK.
   * This must be null if the type is not BANK.
   */
  @PropertyDefinition
  private Identifier _regionKey;
  /**
   * The exchange key identifier bundle, used when this is a holiday of type SETTLEMENT or TRADING.
   * This must be null if the type is not SETTLEMENT or TRADING.
   */
  @PropertyDefinition
  private Identifier _exchangeKey;
  /**
   * The currency, used when this is a holiday of type CURRENCY.
   * This must be null if the type is not CURRENCY.
   */
  @PropertyDefinition
  private Currency _currency;
  /**
   * The list of dates that the target (currency/region/exchange) is on holiday, not null.
   */
  @PropertyDefinition
  private final List<LocalDate> _holidayDates = new ArrayList<LocalDate>();

  /**
   * Creates an instance.
   */
  public ManageableHoliday() {
  }

  /**
   * Create an instance from another holiday instance.
   * <p>
   * This copies the specified holiday creating an independent copy.
   * 
   * @param holiday  the holiday to copy, not null
   */
  public ManageableHoliday(final Holiday holiday) {
    ArgumentChecker.notNull(holiday, "holiday");
    setUniqueId(holiday.getUniqueId());
    setType(holiday.getType());
    setRegionKey(holiday.getRegionKey());
    setExchangeKey(holiday.getExchangeKey());
    setCurrency(holiday.getCurrency());
    setHolidayDates(holiday.getHolidayDates());
  }

  /**
   * Create a CURRENCY holiday from a collection of holiday dates.
   * <p>
   * The unique identifier is managed separately using {@link #setUniqueId}.
   * 
   * @param currency  the currency of this CURRENCY holiday schedule, not null
   * @param holidaySeries  the dates on which holidays fall, not null
   */
  public ManageableHoliday(Currency currency, Collection<LocalDate> holidaySeries) {
    ArgumentChecker.notNull(currency, "currency");
    ArgumentChecker.notNull(holidaySeries, "holidaySeries");
    setCurrency(currency);
    setType(HolidayType.CURRENCY);
    getHolidayDates().addAll(holidaySeries);
  }

  /**
   * Create a BANK, SETTLEMENT or TRADING holiday from a collection of holiday dates.
   * <p>
   * The unique identifier is managed separately using {@link #setUniqueId}.
   * 
   * @param holidayType  the type of the holiday, not null
   * @param regionOrExchangeKey  the identifier for either a region (for a BANK holiday) or an exchange (for a SETTLEMENT or TRADING holiday), not null
   * @param holidaySeries  a collection of dates on which holidays fall, not null
   */
  public ManageableHoliday(HolidayType holidayType, Identifier regionOrExchangeKey, Collection<LocalDate> holidaySeries) {
    ArgumentChecker.notNull(holidayType, "holidayType");
    ArgumentChecker.notNull(regionOrExchangeKey, "regionOrExchangeId");
    ArgumentChecker.notNull(holidaySeries, "holidaySeries");
    switch (holidayType) {
      case BANK:
        setRegionKey(regionOrExchangeKey);
        break;
      case SETTLEMENT:
      case TRADING:
        setExchangeKey(regionOrExchangeKey);
        break;
      case CURRENCY:
      default:
        throw new IllegalArgumentException("Use the Currency constructor for a currency related Holiday");
    }
    setType(holidayType);
    getHolidayDates().addAll(holidaySeries);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageableHoliday}.
   * @return the meta-bean, not null
   */
  public static ManageableHoliday.Meta meta() {
    return ManageableHoliday.Meta.INSTANCE;
  }

  @Override
  public ManageableHoliday.Meta metaBean() {
    return ManageableHoliday.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        return getUniqueId();
      case 3575610:  // type
        return getType();
      case 74328779:  // regionKey
        return getRegionKey();
      case -1755004612:  // exchangeKey
        return getExchangeKey();
      case 575402001:  // currency
        return getCurrency();
      case -367347:  // holidayDates
        return getHolidayDates();
    }
    return super.propertyGet(propertyName);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue) {
    switch (propertyName.hashCode()) {
      case -294460212:  // uniqueId
        setUniqueId((UniqueIdentifier) newValue);
        return;
      case 3575610:  // type
        setType((HolidayType) newValue);
        return;
      case 74328779:  // regionKey
        setRegionKey((Identifier) newValue);
        return;
      case -1755004612:  // exchangeKey
        setExchangeKey((Identifier) newValue);
        return;
      case 575402001:  // currency
        setCurrency((Currency) newValue);
        return;
      case -367347:  // holidayDates
        setHolidayDates((List<LocalDate>) newValue);
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
      ManageableHoliday other = (ManageableHoliday) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getType(), other.getType()) &&
          JodaBeanUtils.equal(getRegionKey(), other.getRegionKey()) &&
          JodaBeanUtils.equal(getExchangeKey(), other.getExchangeKey()) &&
          JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getHolidayDates(), other.getHolidayDates());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionKey());
    hash += hash * 31 + JodaBeanUtils.hashCode(getExchangeKey());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHolidayDates());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unique identifier of the holiday.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  public UniqueIdentifier getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the holiday.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @param uniqueId  the new value of the property
   */
  public void setUniqueId(UniqueIdentifier uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the property, not null
   */
  public final Property<UniqueIdentifier> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the type of the holiday.
   * This field must not be null for the object to be valid.
   * @return the value of the property
   */
  public HolidayType getType() {
    return _type;
  }

  /**
   * Sets the type of the holiday.
   * This field must not be null for the object to be valid.
   * @param type  the new value of the property
   */
  public void setType(HolidayType type) {
    this._type = type;
  }

  /**
   * Gets the the {@code type} property.
   * This field must not be null for the object to be valid.
   * @return the property, not null
   */
  public final Property<HolidayType> type() {
    return metaBean().type().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region key identifier bundle, used when this is a holiday of type BANK.
   * This must be null if the type is not BANK.
   * @return the value of the property
   */
  public Identifier getRegionKey() {
    return _regionKey;
  }

  /**
   * Sets the region key identifier bundle, used when this is a holiday of type BANK.
   * This must be null if the type is not BANK.
   * @param regionKey  the new value of the property
   */
  public void setRegionKey(Identifier regionKey) {
    this._regionKey = regionKey;
  }

  /**
   * Gets the the {@code regionKey} property.
   * This must be null if the type is not BANK.
   * @return the property, not null
   */
  public final Property<Identifier> regionKey() {
    return metaBean().regionKey().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the exchange key identifier bundle, used when this is a holiday of type SETTLEMENT or TRADING.
   * This must be null if the type is not SETTLEMENT or TRADING.
   * @return the value of the property
   */
  public Identifier getExchangeKey() {
    return _exchangeKey;
  }

  /**
   * Sets the exchange key identifier bundle, used when this is a holiday of type SETTLEMENT or TRADING.
   * This must be null if the type is not SETTLEMENT or TRADING.
   * @param exchangeKey  the new value of the property
   */
  public void setExchangeKey(Identifier exchangeKey) {
    this._exchangeKey = exchangeKey;
  }

  /**
   * Gets the the {@code exchangeKey} property.
   * This must be null if the type is not SETTLEMENT or TRADING.
   * @return the property, not null
   */
  public final Property<Identifier> exchangeKey() {
    return metaBean().exchangeKey().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency, used when this is a holiday of type CURRENCY.
   * This must be null if the type is not CURRENCY.
   * @return the value of the property
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency, used when this is a holiday of type CURRENCY.
   * This must be null if the type is not CURRENCY.
   * @param currency  the new value of the property
   */
  public void setCurrency(Currency currency) {
    this._currency = currency;
  }

  /**
   * Gets the the {@code currency} property.
   * This must be null if the type is not CURRENCY.
   * @return the property, not null
   */
  public final Property<Currency> currency() {
    return metaBean().currency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the list of dates that the target (currency/region/exchange) is on holiday, not null.
   * @return the value of the property
   */
  public List<LocalDate> getHolidayDates() {
    return _holidayDates;
  }

  /**
   * Sets the list of dates that the target (currency/region/exchange) is on holiday, not null.
   * @param holidayDates  the new value of the property
   */
  public void setHolidayDates(List<LocalDate> holidayDates) {
    this._holidayDates.clear();
    this._holidayDates.addAll(holidayDates);
  }

  /**
   * Gets the the {@code holidayDates} property.
   * @return the property, not null
   */
  public final Property<List<LocalDate>> holidayDates() {
    return metaBean().holidayDates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageableHoliday}.
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
        this, "uniqueId", ManageableHoliday.class, UniqueIdentifier.class);
    /**
     * The meta-property for the {@code type} property.
     */
    private final MetaProperty<HolidayType> _type = DirectMetaProperty.ofReadWrite(
        this, "type", ManageableHoliday.class, HolidayType.class);
    /**
     * The meta-property for the {@code regionKey} property.
     */
    private final MetaProperty<Identifier> _regionKey = DirectMetaProperty.ofReadWrite(
        this, "regionKey", ManageableHoliday.class, Identifier.class);
    /**
     * The meta-property for the {@code exchangeKey} property.
     */
    private final MetaProperty<Identifier> _exchangeKey = DirectMetaProperty.ofReadWrite(
        this, "exchangeKey", ManageableHoliday.class, Identifier.class);
    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofReadWrite(
        this, "currency", ManageableHoliday.class, Currency.class);
    /**
     * The meta-property for the {@code holidayDates} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<LocalDate>> _holidayDates = DirectMetaProperty.ofReadWrite(
        this, "holidayDates", ManageableHoliday.class, (Class) List.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "type",
        "regionKey",
        "exchangeKey",
        "currency",
        "holidayDates");

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
        case 3575610:  // type
          return _type;
        case 74328779:  // regionKey
          return _regionKey;
        case -1755004612:  // exchangeKey
          return _exchangeKey;
        case 575402001:  // currency
          return _currency;
        case -367347:  // holidayDates
          return _holidayDates;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ManageableHoliday> builder() {
      return new DirectBeanBuilder<ManageableHoliday>(new ManageableHoliday());
    }

    @Override
    public Class<? extends ManageableHoliday> beanType() {
      return ManageableHoliday.class;
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
     * The meta-property for the {@code type} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HolidayType> type() {
      return _type;
    }

    /**
     * The meta-property for the {@code regionKey} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Identifier> regionKey() {
      return _regionKey;
    }

    /**
     * The meta-property for the {@code exchangeKey} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Identifier> exchangeKey() {
      return _exchangeKey;
    }

    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> currency() {
      return _currency;
    }

    /**
     * The meta-property for the {@code holidayDates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<LocalDate>> holidayDates() {
      return _holidayDates;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
