// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
package com.opengamma.financial.security.option;
public class ExtremeSpreadPayoffStyle extends com.opengamma.financial.security.option.PayoffStyle implements java.io.Serializable {
  public <T> T accept (PayoffStyleVisitor<T> visitor) { return visitor.visitExtremeSpreadPayoffStyle(this); }
  private static final long serialVersionUID = 15252329591747l;
  private final javax.time.calendar.ZonedDateTime _periodEnd;
  public static final String PERIOD_END_KEY = "periodEnd";
  private final boolean _isReverse;
  public static final String IS_REVERSE_KEY = "isReverse";
  public ExtremeSpreadPayoffStyle (javax.time.calendar.ZonedDateTime periodEnd, boolean isReverse) {
    if (periodEnd == null) throw new NullPointerException ("'periodEnd' cannot be null");
    else {
      _periodEnd = periodEnd;
    }
    _isReverse = isReverse;
  }
  protected ExtremeSpreadPayoffStyle (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    super (fudgeContext, fudgeMsg);
    org.fudgemsg.FudgeField fudgeField;
    fudgeField = fudgeMsg.getByName (PERIOD_END_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ExtremeSpreadPayoffStyle - field 'periodEnd' is not present");
    try {
      _periodEnd = fudgeContext.fieldValueToObject (javax.time.calendar.ZonedDateTime.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ExtremeSpreadPayoffStyle - field 'periodEnd' is not ZonedDateTime typedef", e);
    }
    fudgeField = fudgeMsg.getByName (IS_REVERSE_KEY);
    if (fudgeField == null) throw new IllegalArgumentException ("Fudge message is not a ExtremeSpreadPayoffStyle - field 'isReverse' is not present");
    try {
      _isReverse = fudgeMsg.getFieldValue (Boolean.class, fudgeField);
    }
    catch (IllegalArgumentException e) {
      throw new IllegalArgumentException ("Fudge message is not a ExtremeSpreadPayoffStyle - field 'isReverse' is not boolean", e);
    }
  }
  protected ExtremeSpreadPayoffStyle (final ExtremeSpreadPayoffStyle source) {
    super (source);
    if (source == null) throw new NullPointerException ("'source' must not be null");
    if (source._periodEnd == null) _periodEnd = null;
    else {
      _periodEnd = source._periodEnd;
    }
    _isReverse = source._isReverse;
  }
  public org.fudgemsg.FudgeMsg toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeMsg msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.mapping.FudgeSerializationContext fudgeContext, final org.fudgemsg.MutableFudgeMsg msg) {
    super.toFudgeMsg (fudgeContext, msg);
    if (_periodEnd != null)  {
      fudgeContext.addToMessage (msg, PERIOD_END_KEY, null, _periodEnd);
    }
    msg.add (IS_REVERSE_KEY, null, _isReverse);
  }
  public static ExtremeSpreadPayoffStyle fromFudgeMsg (final org.fudgemsg.mapping.FudgeDeserializationContext fudgeContext, final org.fudgemsg.FudgeMsg fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.financial.security.option.ExtremeSpreadPayoffStyle".equals (className)) break;
      try {
        return (com.opengamma.financial.security.option.ExtremeSpreadPayoffStyle)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.mapping.FudgeDeserializationContext.class, org.fudgemsg.FudgeMsg.class).invoke (null, fudgeContext, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new ExtremeSpreadPayoffStyle (fudgeContext, fudgeMsg);
  }
  public javax.time.calendar.ZonedDateTime getPeriodEnd () {
    return _periodEnd;
  }
  public boolean getIsReverse () {
    return _isReverse;
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof ExtremeSpreadPayoffStyle)) return false;
    ExtremeSpreadPayoffStyle msg = (ExtremeSpreadPayoffStyle)o;
    if (_periodEnd != null) {
      if (msg._periodEnd != null) {
        if (!_periodEnd.equals (msg._periodEnd)) return false;
      }
      else return false;
    }
    else if (msg._periodEnd != null) return false;
    if (_isReverse != msg._isReverse) return false;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    hc *= 31;
    if (_periodEnd != null) hc += _periodEnd.hashCode ();
    hc *= 31;
    if (_isReverse) hc++;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File
