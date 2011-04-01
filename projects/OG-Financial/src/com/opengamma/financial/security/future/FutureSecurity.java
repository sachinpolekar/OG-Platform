// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.future;
public abstract class FutureSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          public abstract <T> T accept(FutureSecurityVisitor<T> visitor);
        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitFutureSecurity(this); }
  private static final long serialVersionUID = -359772637316411559l;
  private com.opengamma.util.time.Expiry _expiry;
  public static final String EXPIRY_KEY = "expiry";
  private String _tradingExchange;
  public static final String TRADING_EXCHANGE_KEY = "tradingExchange";
  private String _settlementExchange;
  public static final String SETTLEMENT_EXCHANGE_KEY = "settlementExchange";
  private com.opengamma.util.money.Currency _currency;
  public static final String CURRENCY_KEY = "currency";
  public static final String SECURITY_TYPE = "FUTURE";
  public FutureSecurity (com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency) {
    super (SECURITY_TYPE);
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    if (tradingExchange == null) throw new NullPointerException ("tradingExchange' cannot be null");
    _tradingExchange = tradingExchange;
    if (settlementExchange == null) throw new NullPointerException ("settlementExchange' cannot be null");
    _settlementExchange = settlementExchange;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  protected FutureSecurity (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (EXPIRY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FutureSecurity - field 'expiry' is not present");
    try {
      _expiry = com.opengamma.util.time.Expiry.fromFudgeMsg (fudgeMsg.getFieldValue (org.fudgemsg.FudgeFieldContainer.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FutureSecurity - field 'expiry' is not Expiry message", e);
    }
    fudgeField = fudgeMsg.getByName (TRADING_EXCHANGE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FutureSecurity - field 'tradingExchange' is not present");
    try {
      _tradingExchange = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FutureSecurity - field 'tradingExchange' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (SETTLEMENT_EXCHANGE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FutureSecurity - field 'settlementExchange' is not present");
    try {
      _settlementExchange = fudgeField.getValue ().toString ();
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FutureSecurity - field 'settlementExchange' is not string", e);
    }
    fudgeField = fudgeMsg.getByName (CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a FutureSecurity - field 'currency' is not present");
    try {
      _currency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a FutureSecurity - field 'currency' is not Currency typedef", e);
    }
  }
  public FutureSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, com.opengamma.util.time.Expiry expiry, String tradingExchange, String settlementExchange, com.opengamma.util.money.Currency currency) {
    super (uniqueId, name, securityType, identifiers);
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    if (tradingExchange == null) throw new NullPointerException ("tradingExchange' cannot be null");
    _tradingExchange = tradingExchange;
    if (settlementExchange == null) throw new NullPointerException ("settlementExchange' cannot be null");
    _settlementExchange = settlementExchange;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  protected FutureSecurity (final FutureSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._expiry == null) _expiry = null;
    else {
      _expiry = source._expiry;
    }
    _tradingExchange = source._tradingExchange;
    _settlementExchange = source._settlementExchange;
    _currency = source._currency;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_expiry != null)  {
      final org.fudgemsg.MutableFudgeFieldContainer fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _expiry.getClass (), com.opengamma.util.time.Expiry.class);
      _expiry.toFudgeMsg (fudgeContext, fudge1);
      msg.add (EXPIRY_KEY, null, fudge1);
    }
    if (_tradingExchange != null)  {
      msg.add (TRADING_EXCHANGE_KEY, null, _tradingExchange);
    }
    if (_settlementExchange != null)  {
      msg.add (SETTLEMENT_EXCHANGE_KEY, null, _settlementExchange);
    }
    if (_currency != null)  {
      msg.add (CURRENCY_KEY, null, _currency);
    }
  }
  public static FutureSecurity fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.future.FutureSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.future.FutureSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    throw new UnsupportedOperationException ("FutureSecurity is an abstract message");
  }
  public com.opengamma.util.time.Expiry getExpiry () {
    return _expiry;
  }
  public void setExpiry (com.opengamma.util.time.Expiry expiry) {
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
  }
  public String getTradingExchange () {
    return _tradingExchange;
  }
  public void setTradingExchange (String tradingExchange) {
    if (tradingExchange == null) throw new NullPointerException ("tradingExchange' cannot be null");
    _tradingExchange = tradingExchange;
  }
  public String getSettlementExchange () {
    return _settlementExchange;
  }
  public void setSettlementExchange (String settlementExchange) {
    if (settlementExchange == null) throw new NullPointerException ("settlementExchange' cannot be null");
    _settlementExchange = settlementExchange;
  }
  public com.opengamma.util.money.Currency getCurrency () {
    return _currency;
  }
  public void setCurrency (com.opengamma.util.money.Currency currency) {
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof FutureSecurity)) return false;
    FutureSecurity msg = (FutureSecurity)o;
    if (_expiry != null) {
      if (msg._expiry != null) {
        if (!_expiry.equals (msg._expiry)) return false;
      }
      else return false;
    }
    else if (msg._expiry != null) return false;
    if (_tradingExchange != null) {
      if (msg._tradingExchange != null) {
        if (!_tradingExchange.equals (msg._tradingExchange)) return false;
      }
      else return false;
    }
    else if (msg._tradingExchange != null) return false;
    if (_settlementExchange != null) {
      if (msg._settlementExchange != null) {
        if (!_settlementExchange.equals (msg._settlementExchange)) return false;
      }
      else return false;
    }
    else if (msg._settlementExchange != null) return false;
    if (_currency != null) {
      if (msg._currency != null) {
        if (!_currency.equals (msg._currency)) return false;
      }
      else return false;
    }
    else if (msg._currency != null) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_expiry != null) hc += _expiry.hashCode ();
    hc *= 31;
    if (_tradingExchange != null) hc += _tradingExchange.hashCode ();
    hc *= 31;
    if (_settlementExchange != null) hc += _settlementExchange.hashCode ();
    hc *= 31;
    if (_currency != null) hc += _currency.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File