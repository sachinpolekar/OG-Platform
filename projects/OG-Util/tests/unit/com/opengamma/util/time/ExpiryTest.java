/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.testng.annotations.Test;

import com.opengamma.util.fudgemsg.ExpiryBuilder;

/**
 * Test Expiry.
 */
@Test
public class ExpiryTest {

  private static final FudgeContext s_fudgeContext = new FudgeContext();

  static {
    s_fudgeContext.getTypeDictionary().addType(ExpiryBuilder.SECONDARY_TYPE_INSTANCE);
    s_fudgeContext.getObjectDictionary().addBuilder(Expiry.class, new ExpiryBuilder());
  }

  private static FudgeMsg cycleMessage(final FudgeMsg message) {
    final byte[] encoded = s_fudgeContext.toByteArray(message);
    return s_fudgeContext.deserialize(encoded).getMessage();
  }

  private static void testExpiry(final Expiry expiry) {
    final FudgeSerializationContext serContext = new FudgeSerializationContext(s_fudgeContext);
    final MutableFudgeMsg messageIn = serContext.newMessage();
    serContext.addToMessage(messageIn, "test", null, expiry);
    final FudgeMsg messageOut = cycleMessage(messageIn);
    final FudgeDeserializationContext dsrContext = new FudgeDeserializationContext(s_fudgeContext);
    final Expiry result = dsrContext.fieldValueToObject(Expiry.class, messageOut.getByName("test"));
    assertEquals(expiry, result);
    assertEquals(expiry.getExpiry().getZone(), result.getExpiry().getZone());
  }

  public void testFudgeMessageUTC() {
    for (ExpiryAccuracy accuracy : ExpiryAccuracy.values()) {
      testExpiry(new Expiry(ZonedDateTime.of(LocalDateTime.now(), TimeZone.UTC), accuracy));
    }
  }

  public void testFudgeMessageNonUTC() {
    for (ExpiryAccuracy accuracy : ExpiryAccuracy.values()) {
      testExpiry(new Expiry(ZonedDateTime.of(LocalDateTime.now(), TimeZone.of("GMT+02:00")), accuracy));
    }
  }

  public void testEqualsToAccuracy() {
    final ZonedDateTime zdtMinute = ZonedDateTime.of(2011, 7, 12, 12, 30, 0, 0, TimeZone.UTC);
    final ZonedDateTime zdtHour = ZonedDateTime.of(2011, 7, 12, 12, 45, 0, 0, TimeZone.UTC);
    final ZonedDateTime zdtDay = ZonedDateTime.of(2011, 7, 12, 11, 45, 0, 0, TimeZone.UTC);
    final ZonedDateTime zdtMonth = ZonedDateTime.of(2011, 7, 11, 11, 45, 0, 0, TimeZone.UTC);
    final ZonedDateTime zdtYear = ZonedDateTime.of(2011, 6, 11, 11, 45, 0, 0, TimeZone.UTC);
    final ZonedDateTime zdtNone = ZonedDateTime.of(2010, 6, 11, 11, 45, 0, 0, TimeZone.UTC);
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR, zdtMinute, zdtMinute));
    assertFalse(Expiry.equalsToAccuracy(ExpiryAccuracy.MIN_HOUR_DAY_MONTH_YEAR, zdtMinute, zdtHour));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.HOUR_DAY_MONTH_YEAR, zdtHour, zdtMinute));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.HOUR_DAY_MONTH_YEAR, zdtHour, zdtHour));
    assertFalse(Expiry.equalsToAccuracy(ExpiryAccuracy.HOUR_DAY_MONTH_YEAR, zdtHour, zdtDay));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.DAY_MONTH_YEAR, zdtDay, zdtHour));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.DAY_MONTH_YEAR, zdtDay, zdtDay));
    assertFalse(Expiry.equalsToAccuracy(ExpiryAccuracy.DAY_MONTH_YEAR, zdtDay, zdtMonth));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.MONTH_YEAR, zdtMonth, zdtDay));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.MONTH_YEAR, zdtMonth, zdtMonth));
    assertFalse(Expiry.equalsToAccuracy(ExpiryAccuracy.MONTH_YEAR, zdtMonth, zdtYear));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.YEAR, zdtYear, zdtMinute));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.YEAR, zdtYear, zdtMonth));
    assertTrue(Expiry.equalsToAccuracy(ExpiryAccuracy.YEAR, zdtYear, zdtYear));
    assertFalse(Expiry.equalsToAccuracy(ExpiryAccuracy.YEAR, zdtYear, zdtNone));
  }

}
