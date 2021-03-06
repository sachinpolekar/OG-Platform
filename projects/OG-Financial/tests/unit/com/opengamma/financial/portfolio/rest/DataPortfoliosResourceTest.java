/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.portfolio.rest;

import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.opengamma.financial.portfolio.rest.DataPortfolioResource;
import com.opengamma.financial.portfolio.rest.DataPortfoliosResource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPortfoliosResource.
 */
public class DataPortfoliosResourceTest {

  private PortfolioMaster _underlying;
  private UriInfo _uriInfo;
  private DataPortfoliosResource _resource;

  @BeforeMethod
  public void setUp() {
    _underlying = mock(PortfolioMaster.class);
    _uriInfo = mock(UriInfo.class);
    when(_uriInfo.getBaseUri()).thenReturn(URI.create("testhost"));
    _resource = new DataPortfoliosResource(_underlying);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testAddPortfolio() {
    final ManageablePortfolio portfolio = new ManageablePortfolio("Portfolio A");
    portfolio.getRootNode().setName("RootNode");
    portfolio.getRootNode().addChildNode(new ManageablePortfolioNode("Child"));
    final PortfolioDocument request = new PortfolioDocument(portfolio);
    
    final PortfolioDocument result = new PortfolioDocument(portfolio);
    result.setUniqueId(UniqueIdentifier.of("Test", "PortA"));
    when(_underlying.add(same(request))).thenReturn(result);
    
    Response test = _resource.add(_uriInfo, request);
    assertEquals(Status.CREATED.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testFindPortfolio() {
    DataPortfolioResource test = _resource.findPortfolio("Test~PortA");
    assertSame(_resource, test.getPortfoliosResource());
    assertEquals(UniqueIdentifier.of("Test", "PortA"), test.getUrlPortfolioId());
  }

}
