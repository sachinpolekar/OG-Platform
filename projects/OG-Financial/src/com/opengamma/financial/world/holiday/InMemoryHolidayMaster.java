/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.world.holiday;

import java.util.Collection;
import java.util.Collections;

import javax.time.calendar.DayOfWeek;
import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.Currency;
import com.opengamma.financial.world.exchange.Exchange;
import com.opengamma.financial.world.exchange.master.ExchangeSource;
import com.opengamma.financial.world.region.RegionSource;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.IdentifierBundleMapper;
import com.opengamma.id.UniqueIdentifier;
/**
 * In-memory implementation of HolidayRepository that is populated from CSV files.
 * THIS IMPLEMENTAION DOES NOT IMPLEMENT VERSIONING, DATES PASSED IN ARE IGNORED
 */
public class InMemoryHolidayMaster implements HolidayMaster {
  @SuppressWarnings("unused")
  private static final Logger s_logger = LoggerFactory.getLogger(InMemoryHolidayMaster.class);
  // TODO: jim 2-Jul-2010 -- Make this cope with versioning...
  /**
   * Scheme used for UniqueIdentfiers generated by this class
   */
  public static final String HOLIDAY_SCHEME = "HOLIDAY_SCHEME";
  private static final String HOLIDAY_CURRENCY_SCHEME = "HOLIDAY_CURRENCY_SCHEME";
  
  private IdentifierBundleMapper<HolidayImpl> _idMapper = new IdentifierBundleMapper<HolidayImpl>(HOLIDAY_SCHEME);
  private RegionSource _regionSource;
  private ExchangeSource _exchangeSource;

  public InMemoryHolidayMaster(RegionSource regionSource, ExchangeSource exchangeSource) {
    _regionSource = regionSource;
    _exchangeSource = exchangeSource;
  }

  @Override
  public synchronized UniqueIdentifier addHoliday(Currency currency, Collection<LocalDate> holidayDates) {
    HolidayImpl holiday = new HolidayImpl(currency, holidayDates);
    UniqueIdentifier uniqueIdentifier = _idMapper.add(IdentifierBundle.of(Identifier.of(HOLIDAY_CURRENCY_SCHEME, currency.getISOCode())), holiday);
    holiday.setUniqueIdentifier(uniqueIdentifier);
    return uniqueIdentifier;
  }

  @Override
  public synchronized UniqueIdentifier addHoliday(Identifier exchangeOrRegionId, HolidayType holidayType, Collection<LocalDate> holidayDates) {
    Validate.notNull(holidayType, "HolidayType");
    HolidayImpl holiday = new HolidayImpl(exchangeOrRegionId, holidayType, holidayDates);
    UniqueIdentifier uniqueIdentifier = _idMapper.add(IdentifierBundle.of(exchangeOrRegionId), holiday);
    holiday.setUniqueIdentifier(uniqueIdentifier);
    return uniqueIdentifier;   
  }

  @Override
  public HolidayDocument getHoliday(UniqueIdentifier uniqueId) {
    return new HolidayDocument(_idMapper.get(uniqueId));
  }

  @Override
  public HolidaySearchResult searchHistoricHolidays(HolidaySearchHistoricRequest searchHistoricRequest) {
    Identifier identifier;
    Collection<HolidayImpl> holidays;
    switch (searchHistoricRequest.getHolidayType()) {
      case CURRENCY:
        identifier = Identifier.of(HOLIDAY_CURRENCY_SCHEME, searchHistoricRequest.getCurrency().getISOCode());
        holidays = _idMapper.get(identifier);
        break;
      case BANK:
        identifier = searchHistoricRequest.getRegionId();
        IdentifierBundle allIdentifiersForRegion = _regionSource.getHighestLevelRegion(identifier).getIdentifiers();
        holidays = _idMapper.get(allIdentifiersForRegion);
        break;
      case SETTLEMENT:
      case TRADING:
        identifier = searchHistoricRequest.getExchangeId();
        Exchange exchange = _exchangeSource.getSingleExchange(identifier);
        IdentifierBundle allIdentifiersForExchange = exchange.getIdentifiers();
        holidays = _idMapper.get(allIdentifiersForExchange);
        break;
      default:
        throw new OpenGammaRuntimeException("Unsupported holiday type");
    }
    return processResults(searchHistoricRequest, holidays, searchHistoricRequest.getHolidayType());
  }
  
  public HolidaySearchResult processResults(HolidaySearchHistoricRequest searchHistoricRequest, Collection<HolidayImpl> holidays, HolidayType holidayType) {
    for (Holiday holiday : holidays) {
      if (holiday.getHolidayType().equals(holidayType)) {
        if (searchHistoricRequest.getHolidayDate() == null) {
          return singletonSearchResult(holiday);
        } else {
          boolean isCurrencyHoliday = holiday.getHolidays().contains(searchHistoricRequest.getHolidayDate());
          return booleanSearchResult(searchHistoricRequest.getHolidayDate(), isCurrencyHoliday); 
        }          
      }
    }
    // there wasn't a match, so return an appropriate empty/failed search result.
    if (searchHistoricRequest.getHolidayDate() == null) {
      return new HolidaySearchResult(Collections.<HolidayDocument>emptyList());
    } else {
      return new HolidaySearchResult(null, false);
    }
  }

  @Override
  public HolidaySearchResult searchHolidays(HolidaySearchRequest searchRequest) {
    IdentifierBundle identifiers;
    switch (searchRequest.getHolidayType()) {
      case CURRENCY:
        identifiers = IdentifierBundle.of(Identifier.of(HOLIDAY_CURRENCY_SCHEME, searchRequest.getCurrency().getISOCode()));
        break;
      case BANK:
        identifiers = searchRequest.getRegionIdentifiers();
        break;
      case SETTLEMENT:
      case TRADING:
        identifiers = searchRequest.getExchangeIdentifiers();
        break;
      default:
        throw new OpenGammaRuntimeException("Unsupported holiday type");
    }
    Collection<HolidayImpl> holidays = _idMapper.get(identifiers);
    return processResults(searchRequest, holidays, searchRequest.getHolidayType());
  }
  
  public HolidaySearchResult processResults(HolidaySearchRequest searchRequest, Collection<HolidayImpl> holidays, HolidayType holidayType) {
    for (Holiday holiday : holidays) {
      if (holiday.getHolidayType().equals(holidayType)) {
        if (searchRequest.getHolidayDate() == null) {
          return singletonSearchResult(holiday);
        } else {
          boolean isCurrencyHoliday = holiday.getHolidays().contains(searchRequest.getHolidayDate());
          return booleanSearchResult(searchRequest.getHolidayDate(), isCurrencyHoliday); 
        }          
      }
    }
    // there wasn't a match, so return an appropriate empty/failed search result.
    if (searchRequest.getHolidayDate() == null) {
      return new HolidaySearchResult(Collections.<HolidayDocument>emptyList());
    } else {
      return new HolidaySearchResult(null, false);
    }
  }
  
  private HolidaySearchResult booleanSearchResult(LocalDate date, boolean result) {
    return new HolidaySearchResult(date, result); 
  }
  
  private HolidaySearchResult singletonSearchResult(Holiday result) {
    if (result != null) {
      HolidayDocument holidayDoc = new HolidayDocument(result);
      return new HolidaySearchResult(Collections.singleton(holidayDoc));
    } else {
      return null;
    }
  }

  @Override
  public HolidayDocument updateHoliday(HolidayDocument holidayDocument) {
    // no-op in memory.
    return holidayDocument;
  }

}