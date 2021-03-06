/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.net.URI;

import com.opengamma.core.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * URIs for web-based securities.
 */
public class WebSecuritiesUris {

  /**
   * The data.
   */
  private final WebSecuritiesData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebSecuritiesUris(WebSecuritiesData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI.
   * @return the URI
   */
  public URI securities() {
    return WebSecuritiesResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param identifier  the identifier to search for, may be null
   * @return the URI
   */
  public URI securities(final Identifier identifier) {
    return WebSecuritiesResource.uri(_data, IdentifierBundle.of(identifier));
  }

  /**
   * Gets the URI.
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI
   */
  public URI securities(final IdentifierBundle identifiers) {
    return WebSecuritiesResource.uri(_data, identifiers);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI security() {
    return WebSecurityResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param security  the security, not null
   * @return the URI
   */
  public URI security(final Security security) {
    return WebSecurityResource.uri(_data, security.getUniqueId());
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI securityVersions() {
    return WebSecurityVersionsResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI securityVersion() {
    return WebSecurityVersionResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param security  the security, not null
   * @return the URI
   */
  public URI securityVersion(final Security security) {
    return WebSecurityVersionResource.uri(_data, security.getUniqueId());
  }

}
