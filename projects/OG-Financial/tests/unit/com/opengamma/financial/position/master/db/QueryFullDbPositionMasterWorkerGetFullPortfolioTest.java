/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.master.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.TimeZone;

import javax.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioNode;
import com.opengamma.engine.position.Position;
import com.opengamma.financial.position.master.FullPortfolioGetRequest;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Tests QueryFullDbPositionMasterWorker.
 */
public class QueryFullDbPositionMasterWorkerGetFullPortfolioTest extends AbstractDbPositionMasterWorkerTest {
  // superclass sets up dummy database

  private static final Logger s_logger = LoggerFactory.getLogger(QueryFullDbPositionMasterWorkerGetFullPortfolioTest.class);

  private QueryFullDbPositionMasterWorker _worker;
  private DbPositionMasterWorker _queryWorker;

  public QueryFullDbPositionMasterWorkerGetFullPortfolioTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    _worker = new QueryFullDbPositionMasterWorker();
    _worker.init(_posMaster);
    _queryWorker = new QueryPositionDbPositionMasterWorker();
    _queryWorker.init(_posMaster);
  }

  @After
  public void tearDown() throws Exception {
    super.tearDown();
    _worker = null;
    _queryWorker = null;
  }

  //-------------------------------------------------------------------------
  @Test(expected = NullPointerException.class)
  public void test_getFullPortfolio_nullUID() {
    _worker.getFullPortfolio(null);
  }

  @Test
  public void test_getFullPortfolio_notFound() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "0");
    FullPortfolioGetRequest request = new FullPortfolioGetRequest(uid);
    Portfolio test = _worker.getFullPortfolio(request);
    assertNull(test);
  }

  @Test
  public void test_getFullPortfolio_101() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "101");
    FullPortfolioGetRequest request = new FullPortfolioGetRequest(uid);
    Portfolio test = _worker.getFullPortfolio(request);
    
    assertEquals(UniqueIdentifier.of("DbPos", "101"), test.getUniqueIdentifier().toLatest());
    assertEquals("TestPortfolio101", test.getName());
    PortfolioNode testRoot = test.getRootNode();
    assertEquals(UniqueIdentifier.of("DbPos", "111"), testRoot.getUniqueIdentifier().toLatest());
    assertEquals("TestNode111", testRoot.getName());
    assertEquals(0, testRoot.getPositions().size());
    assertEquals(1, testRoot.getChildNodes().size());
    
    PortfolioNode testChild112 = testRoot.getChildNodes().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "112"), testChild112.getUniqueIdentifier().toLatest());
    assertEquals("TestNode112", testChild112.getName());
    assertEquals(2, testChild112.getPositions().size());
    assertEquals(1, testChild112.getChildNodes().size());
    
    PortfolioNode testChild113 = testChild112.getChildNodes().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "113"), testChild113.getUniqueIdentifier().toLatest());
    assertEquals("TestNode113", testChild113.getName());
    assertEquals(0, testChild113.getPositions().size());
    assertEquals(0, testChild113.getChildNodes().size());
    
    Position testPos121 = testChild112.getPositions().get(0);
    assertEquals(UniqueIdentifier.of("DbPos", "121", "0"), testPos121.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(121.987), testPos121.getQuantity());
    IdentifierBundle testSecKey121 = testPos121.getSecurityKey();
    assertNotNull(testSecKey121);
    assertEquals(2, testSecKey121.size());
    assertEquals(true, testSecKey121.getIdentifiers().contains(Identifier.of("TICKER", "MSFT")));
    assertEquals(true, testSecKey121.getIdentifiers().contains(Identifier.of("NASDAQ", "Micro")));
    
    Position testPos122 = testChild112.getPositions().get(1);
    assertEquals(UniqueIdentifier.of("DbPos", "122", "0"), testPos122.getUniqueIdentifier());
    assertEquals(BigDecimal.valueOf(122.987), testPos122.getQuantity());
    IdentifierBundle testSecKey122 = testPos122.getSecurityKey();
    assertNotNull(testSecKey122);
    assertEquals(1, testSecKey122.size());
    assertEquals(Identifier.of("TICKER", "ORCL"), testSecKey122.getIdentifiers().iterator().next());
  }

  @Test
  public void test_getFullPortfolio_101_sameUidDifferentInstant() {
    UniqueIdentifier uid = UniqueIdentifier.of("DbPos", "101");
    Instant now = Instant.now(_posMaster.getTimeSource());
    FullPortfolioGetRequest request = new FullPortfolioGetRequest(uid, now, now);
    Portfolio testNow = _worker.getFullPortfolio(request);
    
    Instant later = now.plusSeconds(100);
    FullPortfolioGetRequest requestLater = new FullPortfolioGetRequest(uid, later, later);
    Portfolio testLater = _worker.getFullPortfolio(requestLater);
    
    assertEquals(testLater.getUniqueIdentifier(), testNow.getUniqueIdentifier());
    assertEquals(testLater.getRootNode().getUniqueIdentifier(), testNow.getRootNode().getUniqueIdentifier());
    assertEquals(testLater.getRootNode().getChildNodes().get(0).getUniqueIdentifier(), testNow.getRootNode().getChildNodes().get(0).getUniqueIdentifier());
    assertEquals(testLater.getRootNode().getChildNodes().get(0).getChildNodes().get(0).getUniqueIdentifier(), testNow.getRootNode().getChildNodes().get(0).getChildNodes().get(0).getUniqueIdentifier());
  }

  @Test
  public void test_getFullPortfolio_latest_notLatest() {
    // latest
    Instant now = Instant.now(_posMaster.getTimeSource());
    FullPortfolioGetRequest requestLatest = new FullPortfolioGetRequest(UniqueIdentifier.of("DbPos", "201"), now, now);
    Portfolio testLatest = _worker.getFullPortfolio(requestLatest);
    assertEquals(UniqueIdentifier.of("DbPos", "201"), testLatest.getUniqueIdentifier().toLatest());
    
    // earlier
    Instant earlier = _version1Instant.plusSeconds(5);
    FullPortfolioGetRequest requestEarlier = new FullPortfolioGetRequest(UniqueIdentifier.of("DbPos", "201"), earlier, earlier);
    Portfolio testEarlier = _worker.getFullPortfolio(requestEarlier);
    assertEquals(UniqueIdentifier.of("DbPos", "201"), testEarlier.getUniqueIdentifier().toLatest());
    
    // ensure earlier is actually earlier
    assertTrue(testEarlier.getUniqueIdentifier().getVersion().compareTo(testLatest.getUniqueIdentifier().getVersion()) < 0);
  }

  @Test
  public void test_getFullPortfolio_byFullPortfolioUid_latest() {
    // not latest version
    Instant later = _version2Instant.plusSeconds(5);
    FullPortfolioGetRequest requestBase = new FullPortfolioGetRequest(UniqueIdentifier.of("DbPos", "201"), later, later);
    Portfolio testBase = _worker.getFullPortfolio(requestBase);
    assertEquals(UniqueIdentifier.of("DbPos", "201"), testBase.getUniqueIdentifier().toLatest());
    
    // retrieve using full portfolio uid
    FullPortfolioGetRequest request = new FullPortfolioGetRequest(testBase.getUniqueIdentifier());  // get using returned uid
    Portfolio test = _worker.getFullPortfolio(request);
    assertEquals(UniqueIdentifier.of("DbPos", "201"), test.getUniqueIdentifier().toLatest());
    assertEquals(testBase.getUniqueIdentifier(), test.getUniqueIdentifier());
    assertEquals(Long.toHexString(_version2Instant.toEpochMillisLong()) + "-0", test.getUniqueIdentifier().getVersion());
  }

  @Test
  public void test_getFullPortfolio_byFullPortfolioUid_notLatest() {
    // not latest version
    Instant earlier = _version1Instant.plusSeconds(5);
    FullPortfolioGetRequest requestBase = new FullPortfolioGetRequest(UniqueIdentifier.of("DbPos", "201"), earlier, earlier);
    Portfolio testBase = _worker.getFullPortfolio(requestBase);
    assertEquals(UniqueIdentifier.of("DbPos", "201"), testBase.getUniqueIdentifier().toLatest());
    
    // retrieve using full portfolio uid
    FullPortfolioGetRequest request = new FullPortfolioGetRequest(testBase.getUniqueIdentifier());  // get using returned uid
    Portfolio test = _worker.getFullPortfolio(request);
    assertEquals(UniqueIdentifier.of("DbPos", "201"), test.getUniqueIdentifier().toLatest());
    assertEquals(testBase.getUniqueIdentifier(), test.getUniqueIdentifier());
    assertEquals(Long.toHexString(_version1Instant.toEpochMillisLong()) + "-0", test.getUniqueIdentifier().getVersion());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toString() {
    assertEquals(_worker.getClass().getSimpleName() + "[DbPos]", _worker.toString());
  }

}