/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import static org.junit.Assert.*;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.junit.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentificationScheme;

/**
 * 
 *
 * @author pietari
 */
public class LiveDataSpecificationTest {
  
  public static final IdentificationScheme TEST_IDENTIFICATION_SCHEME = new IdentificationScheme("bar");
  public static final LiveDataSpecification TEST_LIVE_DATA_SPEC = new LiveDataSpecification("Foo", new Identifier(TEST_IDENTIFICATION_SCHEME, "baz"));
  
  @Test
  public void fudge() {
    FudgeFieldContainer container = TEST_LIVE_DATA_SPEC.toFudgeMsg(new FudgeContext());
    
    LiveDataSpecification deserialized = LiveDataSpecification.fromFudgeMsg(container);
    assertNotNull(deserialized);
    assertEquals("Foo", deserialized.getNormalizationRuleSetId());    
    assertEquals("baz", deserialized.getIdentifier(new IdentificationScheme("bar")));
  }

}