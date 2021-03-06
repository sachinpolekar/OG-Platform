/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import org.fudgemsg.types.FudgeSecondaryType;
import org.fudgemsg.types.SecondaryFieldType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.opengamma.financial.analytics.ircurve.StripInstrumentType;

/**
 * Converts Frequency instances to/from a Fudge string type.
 */
public final class StripInstrumentTypeSecondaryType extends SecondaryFieldType<StripInstrumentType, String> {

  /**
   * Singleton instance of the type.
   */
  @FudgeSecondaryType
  public static final StripInstrumentTypeSecondaryType INSTANCE = new StripInstrumentTypeSecondaryType();

  private StripInstrumentTypeSecondaryType() {
    super(FudgeWireType.STRING, StripInstrumentType.class);
  }

  @Override
  public String secondaryToPrimary(StripInstrumentType object) {
    return object.name();
  }

  @Override
  public StripInstrumentType primaryToSecondary(final String string) {
    return StripInstrumentType.valueOf(string);
  }

}
