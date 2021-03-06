/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.exchange;

import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;

/**
 * Utilities and constants for exchanges.
 */
@PublicAPI
public class ExchangeUtils {

  /**
   * Identification scheme for the MIC exchange code ISO standard.
   */
  public static final IdentificationScheme ISO_MIC = IdentificationScheme.of("ISO_MIC");

  /**
   * Restricted constructor.
   */
  protected ExchangeUtils() {
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an ISO MIC country code.
   * <p>
   * Examples might be {@code XLON} or {@code XNYS}.
   * 
   * @param code  the code, not null
   * @return the region identifier, not null
   */
  public static Identifier isoMicExchangeId(String code) {
    ArgumentChecker.notNull(code, "code");
    if (code.matches("[A-Z0-9]{4}([-][A-Z0-9]{3})?") == false) {
      throw new IllegalArgumentException("ISO MIC code is invalid: " + code);
    }
    return Identifier.of(ISO_MIC, code);
  }

}
