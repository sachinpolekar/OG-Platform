// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/language/connector/Procedure.proto:12(19)
package com.opengamma.language.connector;
public abstract class Procedure extends com.opengamma.language.connector.UserMessagePayload implements java.io.Serializable {
          public <T1,T2> T1 accept (final UserMessagePayloadVisitor<T1,T2> visitor, final T2 data) { return visitor.visitProcedure (this, data); }
        public <T1,T2> T1 accept (final com.opengamma.language.procedure.ProcedureVisitor<T1,T2> visitor, final T2 data) { return visitor.visitUnexpected (this, data); }
  private static final long serialVersionUID = 1l;
  public Procedure () {
  }
  protected Procedure (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    super (fudgeMsg);
  }
  protected Procedure (final Procedure source) {
    super (source);
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
    super.toFudgeMsg (fudgeContext, msg);
  }
  public static Procedure fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.connector.Procedure".equals (className)) break;
      try {
        return (com.opengamma.language.connector.Procedure)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    throw new UnsupportedOperationException ("Procedure is an abstract message");
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof Procedure)) return false;
    Procedure msg = (Procedure)o;
    return super.equals (msg);
  }
  public int hashCode () {
    int hc = super.hashCode ();
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File