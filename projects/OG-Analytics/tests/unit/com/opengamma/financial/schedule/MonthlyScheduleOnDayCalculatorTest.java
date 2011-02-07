/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.schedule;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.junit.Test;

import com.opengamma.financial.schedule.MonthlyScheduleOnDayCalculator;
import com.opengamma.util.time.DateUtil;

/**
 * 
 */
public class MonthlyScheduleOnDayCalculatorTest extends ScheduleCalculatorTestCase {
  private static final MonthlyScheduleOnDayCalculator CALCULATOR = new MonthlyScheduleOnDayCalculator(15);

  @Override
  public MonthlyScheduleOnDayCalculator getScheduleCalculator() {
    return CALCULATOR;
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNegativeDays() {
    new MonthlyScheduleOnDayCalculator(-10);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testHighDays() {
    new MonthlyScheduleOnDayCalculator(36);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStartAndEndSameButInvalid1() {
    final LocalDate date = LocalDate.of(2001, 2, 13);
    CALCULATOR.getSchedule(date, date, false, true);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testStartAndEndSameButInvalid2() {
    final ZonedDateTime date = DateUtil.getUTCDate(2001, 2, 13);
    CALCULATOR.getSchedule(date, date, false, true);
  }

  @Test
  public void testStartAndEndSame1() {
    final LocalDate date = LocalDate.of(2001, 2, 15);
    final LocalDate[] dates = CALCULATOR.getSchedule(date, date, false, true);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void testStartAndEndSame2() {
    final ZonedDateTime date = DateUtil.getUTCDate(2001, 2, 15);
    final ZonedDateTime[] dates = CALCULATOR.getSchedule(date, date, false, true);
    assertEquals(dates.length, 1);
    assertEquals(dates[0], date);
  }

  @Test
  public void testMonthlyOnDay1() {
    LocalDate startDate = LocalDate.of(2000, 1, 1);
    LocalDate endDate = LocalDate.of(2000, 1, 30);
    LocalDate[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 1);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
    final LocalDate date = LocalDate.of(2000, 1, 15);
    assertEquals(forward[0], date);
    startDate = LocalDate.of(2002, 2, 1);
    endDate = LocalDate.of(2002, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 0);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
    startDate = LocalDate.of(2000, 1, 1);
    endDate = LocalDate.of(2002, 2, 9);
    final int months = 25;
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, months);
    assertEquals(forward[0], date);
    assertEquals(forward[months - 1], LocalDate.of(2002, 1, 15));
    for (int i = 1; i < months; i++) {
      if (forward[i].getYear() == forward[i - 1].getYear()) {
        assertEquals(forward[i].getMonthOfYear().getValue() - forward[i - 1].getMonthOfYear().getValue(), 1);
      } else {
        assertEquals(forward[i].getMonthOfYear().getValue() - forward[i - 1].getMonthOfYear().getValue(), -11);
      }
      assertEquals(forward[i].getDayOfMonth(), 15);
    }
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
  }

  @Test
  public void testMonthlyOnDay2() {
    ZonedDateTime startDate = DateUtil.getUTCDate(2000, 1, 1);
    ZonedDateTime endDate = DateUtil.getUTCDate(2000, 1, 30);
    ZonedDateTime[] forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 1);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
    final ZonedDateTime date = DateUtil.getUTCDate(2000, 1, 15);
    assertEquals(forward[0], date);
    startDate = DateUtil.getUTCDate(2002, 2, 1);
    endDate = DateUtil.getUTCDate(2002, 2, 9);
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, 0);
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
    startDate = DateUtil.getUTCDate(2000, 1, 1);
    endDate = DateUtil.getUTCDate(2002, 2, 9);
    final int months = 25;
    forward = CALCULATOR.getSchedule(startDate, endDate, false, true);
    assertEquals(forward.length, months);
    assertEquals(forward[0], date);
    assertEquals(forward[months - 1], DateUtil.getUTCDate(2002, 1, 15));
    for (int i = 1; i < months; i++) {
      if (forward[i].getYear() == forward[i - 1].getYear()) {
        assertEquals(forward[i].getMonthOfYear().getValue() - forward[i - 1].getMonthOfYear().getValue(), 1);
      } else {
        assertEquals(forward[i].getMonthOfYear().getValue() - forward[i - 1].getMonthOfYear().getValue(), -11);
      }
      assertEquals(forward[i].getDayOfMonth(), 15);
    }
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, true));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, false, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate, true, false));
    assertArrayEquals(forward, CALCULATOR.getSchedule(startDate, endDate));
  }
}