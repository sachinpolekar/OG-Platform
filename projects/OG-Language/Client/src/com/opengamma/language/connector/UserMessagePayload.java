// Automatically created - do not modify
///CLOVER:OFF
// CSOFF: Generated File
// Created from com/opengamma/language/connector/UserMessagePayload.proto:13(10)
package com.opengamma.language.connector;
public class UserMessagePayload implements java.io.Serializable {
          public static final UserMessagePayload EMPTY_PAYLOAD = new UserMessagePayload ();
        
        public <T1,T2> T1 accept (final UserMessagePayloadVisitor<T1,T2> visitor, final T2 data) {
          return visitor.visitUserMessagePayload (this, data);
        }
  private static final long serialVersionUID = 1l;
  public UserMessagePayload () {
  }
  protected UserMessagePayload (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
  }
  protected UserMessagePayload (final UserMessagePayload source) {
  }
  public org.fudgemsg.FudgeFieldContainer toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext) {
    if (fudgeContext == null) throw new NullPointerException ("fudgeContext must not be null");
    final org.fudgemsg.MutableFudgeFieldContainer msg = fudgeContext.newMessage ();
    toFudgeMsg (fudgeContext, msg);
    return msg;
  }
  public void toFudgeMsg (final org.fudgemsg.FudgeMsgFactory fudgeContext, final org.fudgemsg.MutableFudgeFieldContainer msg) {
  }
  public static UserMessagePayload fromFudgeMsg (final org.fudgemsg.FudgeFieldContainer fudgeMsg) {
    final java.util.List<org.fudgemsg.FudgeField> types = fudgeMsg.getAllByOrdinal (0);
    for (org.fudgemsg.FudgeField field : types) {
      final String className = (String)field.getValue ();
      if ("com.opengamma.language.connector.UserMessagePayload".equals (className)) break;
      try {
        return (com.opengamma.language.connector.UserMessagePayload)Class.forName (className).getDeclaredMethod ("fromFudgeMsg", org.fudgemsg.FudgeFieldContainer.class).invoke (null, fudgeMsg);
      }
      catch (Throwable t) {
        // no-action
      }
    }
    return new UserMessagePayload (fudgeMsg);
  }
  public boolean equals (final Object o) {
    if (o == this) return true;
    if (!(o instanceof UserMessagePayload)) return false;
    UserMessagePayload msg = (UserMessagePayload)o;
    return true;
  }
  public int hashCode () {
    int hc = 1;
    return hc;
  }
  public String toString () {
    return org.apache.commons.lang.builder.ToStringBuilder.reflectionToString(this, org.apache.commons.lang.builder.ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
///CLOVER:ON
// CSON: Generated File