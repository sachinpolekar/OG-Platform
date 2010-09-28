/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.position.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.financial.position.master.ManageablePosition;
import com.opengamma.financial.position.master.PositionDocument;
import com.opengamma.financial.position.master.PositionMaster;
import com.opengamma.financial.position.master.rest.DataPositionResource;
import com.opengamma.financial.position.master.rest.DataPositionsResource;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * Tests DataPositionResource.
 */
public class DataPositionResourceTest {

  private static final UniqueIdentifier UID = UniqueIdentifier.of("Test", "PosA");
  private PositionMaster _underlying;
  private DataPositionResource _resource;

  @Before
  public void setUp() {
    _underlying = mock(PositionMaster.class);
    _resource = new DataPositionResource(new DataPositionsResource(_underlying), UID);
  }

  //-------------------------------------------------------------------------
  @Test
  public void testGetPosition() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    final PositionDocument result = new PositionDocument(position, UniqueIdentifier.of("Test", "Node"));
    when(_underlying.getPosition(eq(UID))).thenReturn(result);
    
    Response test = _resource.get();
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testUpdatePosition() {
    final ManageablePosition position = new ManageablePosition(BigDecimal.TEN, Identifier.of("A", "B"));
    final PositionDocument request = new PositionDocument(position);
    request.setPositionId(UID);
    
    final PositionDocument result = new PositionDocument(position);
    result.setPositionId(UID);
    when(_underlying.updatePosition(same(request))).thenReturn(result);
    
    Response test = _resource.put(request);
    assertEquals(Status.OK.getStatusCode(), test.getStatus());
    assertSame(result, test.getEntity());
  }

  @Test
  public void testDeletePosition() {
    Response test = _resource.delete();
    verify(_underlying).removePosition(UID);
    assertEquals(Status.NO_CONTENT.getStatusCode(), test.getStatus());
  }

}