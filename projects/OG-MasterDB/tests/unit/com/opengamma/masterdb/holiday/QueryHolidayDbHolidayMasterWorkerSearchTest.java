/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.holiday;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierSearch;
import com.opengamma.id.ObjectIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.DBTest;

/**
 * Tests QueryHolidayDbHolidayMasterWorker.
 */
public class QueryHolidayDbHolidayMasterWorkerSearchTest extends AbstractDbHolidayMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryHolidayDbHolidayMasterWorkerSearchTest.class);

  @Factory(dataProvider = "databases", dataProviderClass = DBTest.class)
  public QueryHolidayDbHolidayMasterWorkerSearchTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_searchHolidays_documents() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(Integer.MAX_VALUE, test.getPaging().getPagingSize());
    assertEquals(_totalHolidays, test.getPaging().getTotalItems());
    
    assertEquals(_totalHolidays, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_pageOne() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setPagingRequest(PagingRequest.of(1, 2));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(1, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalHolidays, test.getPaging().getTotalItems());
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_pageTwo() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setPagingRequest(PagingRequest.of(2, 2));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(3, test.getPaging().getFirstItem());
    assertEquals(2, test.getPaging().getPagingSize());
    assertEquals(_totalHolidays, test.getPaging().getTotalItems());
    
    assertEquals(1, test.getDocuments().size());
    assert202(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_name_noMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("FooBar");
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_name() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("TestHoliday102");
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_case() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("TESTHoliday102");
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_name_wildcard() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("TestHoliday1*");
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  @Test
  public void test_search_name_wildcardCase() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setName("TESTHoliday1*");
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_type() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setType(HolidayType.CURRENCY);
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_providerNoMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setProviderKey(Identifier.of("A", "B"));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_providerFound() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setProviderKey(Identifier.of("COPP_CLARK", "2"));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_regionEmptyBundle() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setRegionKeys(new IdentifierSearch());
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_regionNoMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.addRegionKey(Identifier.of("A", "B"));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_exchange_empty() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setExchangeKeys(new IdentifierSearch());
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_exchange_noMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.addExchangeKey(Identifier.of("A", "B"));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_currency_noMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setCurrency(Currency.USD);
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_currency_oneMatch() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setCurrency(Currency.EUR);
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(1, test.getDocuments().size());
    assert102(test.getDocuments().get(0));
  }

  @Test
  public void test_search_currency_twoMatches() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setCurrency(Currency.GBP);
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_holidayIds_none() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setHolidayIds(new ArrayList<ObjectIdentifier>());
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_holidayIds() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.addHolidayId(ObjectIdentifier.of("DbHol", "101"));
    request.addHolidayId(ObjectIdentifier.of("DbHol", "201"));
    request.addHolidayId(ObjectIdentifier.of("DbHol", "9999"));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(2, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert202(test.getDocuments().get(1));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_search_holidayIds_badSchemeValidOid() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.addHolidayId(ObjectIdentifier.of("Rubbish", "120"));
    _holMaster.search(request);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_search_versionAsOf_below() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.minusSeconds(5)));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(0, test.getDocuments().size());
  }

  @Test
  public void test_search_versionAsOf_mid() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version1Instant.plusSeconds(5)));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert201(test.getDocuments().get(2));  // old version
  }

  @Test
  public void test_search_versionAsOf_above() {
    HolidaySearchRequest request = new HolidaySearchRequest();
    request.setVersionCorrection(VersionCorrection.ofVersionAsOf(_version2Instant.plusSeconds(5)));
    HolidaySearchResult test = _holMaster.search(request);
    
    assertEquals(3, test.getDocuments().size());
    assert101(test.getDocuments().get(0));
    assert102(test.getDocuments().get(1));
    assert202(test.getDocuments().get(2));  // new version
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_holMaster.getClass().getSimpleName() + "[DbHol]", _holMaster.toString());
  }

}
