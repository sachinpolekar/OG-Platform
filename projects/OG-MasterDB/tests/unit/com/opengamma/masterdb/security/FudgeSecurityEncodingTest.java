/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security;

import static org.testng.AssertJUnit.fail;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.core.security.Security;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

@Test
public class FudgeSecurityEncodingTest extends SecurityTestCase {

  private static final Logger s_logger = LoggerFactory.getLogger(FudgeSecurityEncodingTest.class);

  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  @Override
  protected <T extends ManageableSecurity> void assertSecurity(Class<T> securityClass, T security) {
    final FudgeSerializationContext context = new FudgeSerializationContext(s_fudgeContext);
    FudgeMsg msg = context.objectToFudgeMsg(security);
    s_logger.debug("Security {}", security);
    s_logger.debug("Encoded to {}", msg);
    final byte[] bytes = s_fudgeContext.toByteArray(msg);
    msg = s_fudgeContext.deserialize(bytes).getMessage();
    s_logger.debug("Serialised to to {}", msg);
    final Security decoded = s_fudgeContext.fromFudgeMsg(securityClass, msg);
    s_logger.debug("Decoded to {}", decoded);
    if (!security.equals(decoded)) {
      s_logger.warn("Expected {}", security);
      s_logger.warn("Received {}", decoded);
      fail();
    }
  }

}
