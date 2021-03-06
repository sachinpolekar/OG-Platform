/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.security.impl;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collection;

import javax.time.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;

/**
 * Test MasterSecuritySource.
 */
@Test
public class MasterSecuritySourceTest {

  private static final UniqueIdentifier UID = UniqueIdentifier.of("A", "B");
  private static final Identifier ID1 = Identifier.of("C", "D");
  private static final Identifier ID2 = Identifier.of("E", "F");
  private static final IdentifierBundle BUNDLE = IdentifierBundle.of(ID1, ID2);
  private static final Instant NOW = Instant.now();
  private static final VersionCorrection VC = VersionCorrection.of(NOW.minusSeconds(2), NOW.minusSeconds(1));

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_1arg_nullMaster() throws Exception {
    new MasterSecuritySource(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_2arg_nullMaster() throws Exception {
    new MasterSecuritySource(null, null);
  }

  //-------------------------------------------------------------------------
  public void test_getSecurity_noOverride_found() throws Exception {
    SecurityMaster mock = mock(SecurityMaster.class);
    
    SecurityDocument doc = new SecurityDocument(example());
    when(mock.get(UID)).thenReturn(doc);
    MasterSecuritySource test = new MasterSecuritySource(mock);
    Security testResult = test.getSecurity(UID);
    verify(mock, times(1)).get(UID);
    
    assertEquals(example(), testResult);
  }

  public void test_getSecurity_found() throws Exception {
    SecurityMaster mock = mock(SecurityMaster.class);
    
    SecurityDocument doc = new SecurityDocument(example());
    when(mock.get(UID, VC)).thenReturn(doc);
    MasterSecuritySource test = new MasterSecuritySource(mock, VC);
    Security testResult = test.getSecurity(UID);
    verify(mock, times(1)).get(UID, VC);
    
    assertEquals(example(), testResult);
  }

  public void test_getSecurity_notFound() throws Exception {
    SecurityMaster mock = mock(SecurityMaster.class);
    
    when(mock.get(UID, VC)).thenThrow(new DataNotFoundException(""));
    MasterSecuritySource test = new MasterSecuritySource(mock, VC);
    Security testResult = test.getSecurity(UID);
    verify(mock, times(1)).get(UID, VC);
    
    assertEquals(null, testResult);
  }

  //-------------------------------------------------------------------------
  public void test_getSecuritiesByIdentifierBundle() throws Exception {
    SecurityMaster mock = mock(SecurityMaster.class);
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(ID1);
    request.addSecurityKey(ID2);
    request.setVersionCorrection(VC);
    ManageableSecurity security = example();
    SecuritySearchResult result = new SecuritySearchResult();
    result.getDocuments().add(new SecurityDocument(security));
    
    when(mock.search(request)).thenReturn(result);
    MasterSecuritySource test = new MasterSecuritySource(mock, VC);
    Collection<Security> testResult = test.getSecurities(BUNDLE);
    verify(mock, times(1)).search(request);
    
    assertEquals(UID, testResult.iterator().next().getUniqueId());
    assertEquals("Test", testResult.iterator().next().getName());
  }

  //-------------------------------------------------------------------------
  public void test_getSecurity_Identifier() throws Exception {
    SecurityMaster mock = mock(SecurityMaster.class);
    SecuritySearchRequest request = new SecuritySearchRequest();
    request.addSecurityKey(ID1);
    request.addSecurityKey(ID2);
    request.setVersionCorrection(VC);
    ManageableSecurity security = example();
    SecuritySearchResult result = new SecuritySearchResult();
    result.getDocuments().add(new SecurityDocument(security));
    
    when(mock.search(request)).thenReturn(result);
    MasterSecuritySource test = new MasterSecuritySource(mock, VC);
    Security testResult = test.getSecurity(BUNDLE);
    verify(mock, times(1)).search(request);
    
    assertEquals(UID, testResult.getUniqueId());
    assertEquals("Test", testResult.getName());
  }

  //-------------------------------------------------------------------------
  protected ManageableSecurity example() {
    return new ManageableSecurity(UID, "Test", "EQUITY", IdentifierBundle.EMPTY);
  }

}
