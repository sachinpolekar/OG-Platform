/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import java.util.Collection;
import java.util.Map;

import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSchemeDelegator;
import com.opengamma.util.ArgumentChecker;

/**
 * A source of securities that uses the scheme of the unique identifier to determine which
 * underlying source should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 */
public class DelegatingFinancialSecuritySource
    extends UniqueIdentifierSchemeDelegator<FinancialSecuritySource>
    implements FinancialSecuritySource {

  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource  the source to use when no scheme matches, not null
   */
  public DelegatingFinancialSecuritySource(FinancialSecuritySource defaultSource) {
    super(defaultSource);
  }

  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultSource  the source to use when no scheme matches, not null
   * @param schemePrefixToSourceMap  the map of sources by scheme to switch on, not null
   */
  public DelegatingFinancialSecuritySource(FinancialSecuritySource defaultSource, Map<String, FinancialSecuritySource> schemePrefixToSourceMap) {
    super(defaultSource, schemePrefixToSourceMap);
  }

  //-------------------------------------------------------------------------
  @Override
  public Security getSecurity(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    return chooseDelegate(uid).getSecurity(uid);
  }

  @Override
  public Collection<Security> getSecurities(IdentifierBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    // best implementation is to return first matching result
    for (SecuritySource delegateSource : getDelegates().values()) {
      Collection<Security> result = delegateSource.getSecurities(bundle);
      if (!result.isEmpty()) {
        return result;
      }
    }
    return getDefaultDelegate().getSecurities(bundle);
  }

  @Override
  public Security getSecurity(IdentifierBundle bundle) {
    ArgumentChecker.notNull(bundle, "bundle");
    // best implementation is to return first matching result
    for (SecuritySource delegateSource : getDelegates().values()) {
      Security result = delegateSource.getSecurity(bundle);
      if (result != null) {
        return result;
      }
    }
    return getDefaultDelegate().getSecurity(bundle);
  }

  @Override
  public Collection<Security> getBondsWithIssuerName(String issuerName) {
    // best implementation is to return first matching result
    for (FinancialSecuritySource delegateSource : getDelegates().values()) {
      Collection<Security> result = delegateSource.getBondsWithIssuerName(issuerName);
      if (!result.isEmpty()) {
        return result;
      }
    }
    return getDefaultDelegate().getBondsWithIssuerName(issuerName);
  }

}
