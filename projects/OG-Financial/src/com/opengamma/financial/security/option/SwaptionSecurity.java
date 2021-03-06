// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class SwaptionSecurity extends com.opengamma.financial.security.FinancialSecurity implements java.io.Serializable {
          public <T> T accept (SwaptionSecurityVisitor<T> visitor) { return visitor.visitSwaptionSecurity(this); }
        public final <T> T accept(com.opengamma.financial.security.FinancialSecurityVisitor<T> visitor) { return visitor.visitSwaptionSecurity(this); }
  private static final long serialVersionUID = 2196456197423377578l;
  private boolean _isPayer;
  public static final String IS_PAYER_KEY = "isPayer";
  private com.opengamma.id.Identifier _underlyingIdentifier;
  public static final String UNDERLYING_IDENTIFIER_KEY = "underlyingIdentifier";
  private boolean _isLong;
  public static final String IS_LONG_KEY = "isLong";
  private com.opengamma.util.time.Expiry _expiry;
  public static final String EXPIRY_KEY = "expiry";
  private boolean _isCashSettled;
  public static final String IS_CASH_SETTLED_KEY = "isCashSettled";
  private com.opengamma.util.money.Currency _currency;
  public static final String CURRENCY_KEY = "currency";
  public static final String SECURITY_TYPE = "SWAPTION";
  public SwaptionSecurity (boolean isPayer, com.opengamma.id.Identifier underlyingIdentifier, boolean isLong, com.opengamma.util.time.Expiry expiry, boolean isCashSettled, com.opengamma.util.money.Currency currency) {
    super (SECURITY_TYPE);
    _isPayer = isPayer;
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
    _isLong = isLong;
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    _isCashSettled = isCashSettled;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  protected SwaptionSecurity (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (IS_PAYER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'isPayer' is not present");
    try {
      _isPayer = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'isPayer' is not boolean", e);
    }
    fudgeField = fudgeMsg.getByName (UNDERLYING_IDENTIFIER_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'underlyingIdentifier' is not present");
    try {
      _underlyingIdentifier = com.opengamma.id.Identifier.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'underlyingIdentifier' is not Identifier message", e);
    }
    fudgeField = fudgeMsg.getByName (IS_LONG_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'isLong' is not present");
    try {
      _isLong = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'isLong' is not boolean", e);
    }
    fudgeField = fudgeMsg.getByName (EXPIRY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'expiry' is not present");
    try {
      _expiry = com.opengamma.util.time.Expiry.fromFudgeMsg (fudgeContext, fudgeMsg.getFieldValue (org.fudgemsg.FudgeMsg.class, fudgeField));
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'expiry' is not Expiry message", e);
    }
    fudgeField = fudgeMsg.getByName (IS_CASH_SETTLED_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'isCashSettled' is not present");
    try {
      _isCashSettled = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'isCashSettled' is not boolean", e);
    }
    fudgeField = fudgeMsg.getByName (CURRENCY_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'currency' is not present");
    try {
      _currency = fudgeMsg.getFieldValue (com.opengamma.util.money.Currency.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a SwaptionSecurity - field 'currency' is not Currency typedef", e);
    }
  }
  public SwaptionSecurity (com.opengamma.id.UniqueIdentifier uniqueId, String name, String securityType, com.opengamma.id.IdentifierBundle identifiers, boolean isPayer, com.opengamma.id.Identifier underlyingIdentifier, boolean isLong, com.opengamma.util.time.Expiry expiry, boolean isCashSettled, com.opengamma.util.money.Currency currency) {
    super (uniqueId, name, securityType, identifiers);
    _isPayer = isPayer;
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
    _isLong = isLong;
    if (expiry == null) throw new NullPointerException ("'expiry' cannot be null");
    else {
      _expiry = expiry;
    }
    _isCashSettled = isCashSettled;
    if (currency == null) throw new NullPointerException ("currency' cannot be null");
    _currency = currency;
  }
  protected SwaptionSecurity (final SwaptionSecurity source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    _isPayer = source._isPayer;
    if (source._underlyingIdentifier == null) _underlyingIdentifier = null;
    else {
      _underlyingIdentifier = source._underlyingIdentifier;
    }
    _isLong = source._isLong;
    if (source._expiry == null) _expiry = null;
    else {
      _expiry = source._expiry;
    }
    _isCashSettled = source._isCashSettled;
    _currency = source._currency;
  }
  public SwaptionSecurity clone () {
    return new SwaptionSecurity (this);
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    msg.add (IS_PAYER_KEY, null, _isPayer);
    if (_underlyingIdentifier != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _underlyingIdentifier.getClass (), com.opengamma.id.Identifier.class);
      _underlyingIdentifier.toFudgeMsg (fudgeContext, fudge1);
      msg.add (UNDERLYING_IDENTIFIER_KEY, null, fudge1);
    }
    msg.add (IS_LONG_KEY, null, _isLong);
    if (_expiry != null)  {
      final org.fudgemsg.MutableFudgeMsg fudge1 = org.fudgemsg.mapping.FudgeSerializationContext.addClassHeader (fudgeContext.newMessage (), _expiry.getClass (), com.opengamma.util.time.Expiry.class);
      _expiry.toFudgeMsg (fudgeContext, fudge1);
      msg.add (EXPIRY_KEY, null, fudge1);
    }
    msg.add (IS_CASH_SETTLED_KEY, null, _isCashSettled);
    if (_currency != null)  {
      msg.add (CURRENCY_KEY, null, _currency);
    }
  }
  public static SwaptionSecurity fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.SwaptionSecurity".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.SwaptionSecurity)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new SwaptionSecurity (fudgeContext, fudgeMsg);
  }
  public boolean getIsPayer () {
    return _isPayer;
  }
  public void setIsPayer (boolean isPayer) {
    _isPayer = isPayer;
  }
  public com.opengamma.id.Identifier getUnderlyingIdentifier () {
    return _underlyingIdentifier;
  }
  public void setUnderlyingIdentifier (com.opengamma.id.Identifier underlyingIdentifier) {
    if (underlyingIdentifier == null) throw new NullPointerException ("'underlyingIdentifier' cannot be null");
    else {
      _underlyingIdentifier = underlyingIdentifier;
    }
  }
  public boolean getIsLong () {
    return _isLong;
  }
  public void setIsLong (boolean isLong) {
    _isLong = isLong;
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
  public boolean getIsCashSettled () {
    return _isCashSettled;
  }
  public void setIsCashSettled (boolean isCashSettled) {
    _isCashSettled = isCashSettled;
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
    if (!(o instanceof SwaptionSecurity)) return false;
    SwaptionSecurity msg = (SwaptionSecurity)o;
    if (_isPayer != msg._isPayer) return false;
    if (_underlyingIdentifier != null) {
      if (msg._underlyingIdentifier != null) {
        if (!_underlyingIdentifier.equals (msg._underlyingIdentifier)) return false;
      }
      else return false;
    }
    else if (msg._underlyingIdentifier != null) return false;
    if (_isLong != msg._isLong) return false;
    if (_expiry != null) {
      if (msg._expiry != null) {
        if (!_expiry.equals (msg._expiry)) return false;
      }
      else return false;
    }
    else if (msg._expiry != null) return false;
    if (_isCashSettled != msg._isCashSettled) return false;
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
    if (_isPayer) hc++;
    hc *= 31;
    if (_underlyingIdentifier != null) hc += _underlyingIdentifier.hashCode ();
    hc *= 31;
    if (_isLong) hc++;
    hc *= 31;
    if (_expiry != null) hc += _expiry.hashCode ();
    hc *= 31;
    if (_isCashSettled) hc++;
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
