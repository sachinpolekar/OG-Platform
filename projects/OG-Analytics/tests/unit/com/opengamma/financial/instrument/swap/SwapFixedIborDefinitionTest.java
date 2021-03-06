/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swap;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.instrument.annuity.AnnuityCouponFixedDefinition;
import com.opengamma.financial.instrument.annuity.AnnuityCouponIborDefinition;
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Class testing the Fixed vs Ibor swap definition.
 */
public class SwapFixedIborDefinitionTest {

  // Swap 2Y
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Period ANNUITY_TENOR = Period.ofYears(2);
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2011, 3, 17);
  private static final double NOTIONAL = 1000000;
  private static final ZonedDateTime MATURITY_DATE = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, BUSINESS_DAY, CALENDAR, IS_EOM, ANNUITY_TENOR);
  //Fixed leg: Semi-annual bond
  private static final PeriodFrequency FIXED_PAYMENT_FREQUENCY = PeriodFrequency.SEMI_ANNUAL;
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final double RATE = 0.0325;
  private static final boolean FIXED_IS_PAYER = true;
  private static final ZonedDateTime[] FIXED_PAYMENT_DATES_UNADJUSTED = ScheduleCalculator.getUnadjustedDateSchedule(SETTLEMENT_DATE, MATURITY_DATE, FIXED_PAYMENT_FREQUENCY);
  private static final ZonedDateTime[] FIXED_PAYMENT_DATES = ScheduleCalculator.getAdjustedDateSchedule(FIXED_PAYMENT_DATES_UNADJUSTED, BUSINESS_DAY, CALENDAR);
  //Ibor leg: quarterly money
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final PeriodFrequency INDEX_FREQUENCY = PeriodFrequency.QUARTERLY;
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final Currency CUR = Currency.USD;
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  private static final ZonedDateTime[] IBOR_PAYMENT_DATES_UNADJUSTED = ScheduleCalculator.getUnadjustedDateSchedule(SETTLEMENT_DATE, MATURITY_DATE, INDEX_FREQUENCY);
  private static final ZonedDateTime[] IBOR_PAYMENT_DATES = ScheduleCalculator.getAdjustedDateSchedule(IBOR_PAYMENT_DATES_UNADJUSTED, BUSINESS_DAY, CALENDAR);

  //  private static final LocalDate REFERENCE_DATE = LocalDate.of(2011, 3, 15); //For conversion to derivative

  @Test
  public void test() {
    double sign = FIXED_IS_PAYER ? -1.0 : 1.0;
    // Fixed leg
    CouponFixedDefinition[] couponsFixed = new CouponFixedDefinition[FIXED_PAYMENT_DATES.length];
    couponsFixed[0] = new CouponFixedDefinition(CUR, FIXED_PAYMENT_DATES[0], SETTLEMENT_DATE, FIXED_PAYMENT_DATES[0], FIXED_DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, FIXED_PAYMENT_DATES[0]),
        sign * NOTIONAL, RATE);
    for (int loopcpn = 1; loopcpn < FIXED_PAYMENT_DATES.length; loopcpn++) {
      couponsFixed[loopcpn] = new CouponFixedDefinition(CUR, FIXED_PAYMENT_DATES[loopcpn], FIXED_PAYMENT_DATES[loopcpn - 1], FIXED_PAYMENT_DATES[loopcpn], FIXED_DAY_COUNT.getDayCountFraction(
          FIXED_PAYMENT_DATES[loopcpn - 1], FIXED_PAYMENT_DATES[loopcpn]), sign * NOTIONAL, RATE);
    }
    AnnuityCouponFixedDefinition fixedAnnuity = new AnnuityCouponFixedDefinition(couponsFixed);
    // Ibor leg
    CouponIborDefinition[] couponsIbor = new CouponIborDefinition[IBOR_PAYMENT_DATES.length];
    CouponFixedDefinition coupon = new CouponFixedDefinition(CUR, IBOR_PAYMENT_DATES[0], SETTLEMENT_DATE, IBOR_PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, IBOR_PAYMENT_DATES[0]),
        -sign * NOTIONAL, 0.0);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
    couponsIbor[0] = CouponIborDefinition.from(coupon, fixingDate, IBOR_INDEX);
    for (int loopcpn = 1; loopcpn < IBOR_PAYMENT_DATES.length; loopcpn++) {
      coupon = new CouponFixedDefinition(CUR, IBOR_PAYMENT_DATES[loopcpn], IBOR_PAYMENT_DATES[loopcpn - 1], IBOR_PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(IBOR_PAYMENT_DATES[loopcpn - 1],
          IBOR_PAYMENT_DATES[loopcpn]), -sign * NOTIONAL, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(IBOR_PAYMENT_DATES[loopcpn - 1], BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
      couponsIbor[loopcpn] = CouponIborDefinition.from(coupon, fixingDate, IBOR_INDEX);
    }
    AnnuityCouponIborDefinition iborAnnuity = new AnnuityCouponIborDefinition(couponsIbor);
    //Swap
    SwapFixedIborDefinition swap = new SwapFixedIborDefinition(fixedAnnuity, iborAnnuity);
    assertEquals(swap.getFixedLeg(), fixedAnnuity);
    assertEquals(swap.getIborLeg(), iborAnnuity);
    assertEquals(swap.getFirstLeg(), fixedAnnuity);
    assertEquals(swap.getSecondLeg(), iborAnnuity);

    // CMS index builder
    CMSIndex cmsIndex = new CMSIndex(FIXED_PAYMENT_FREQUENCY.getPeriod(), FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR);
    SwapFixedIborDefinition swapFromCMSIndex = SwapFixedIborDefinition.from(SETTLEMENT_DATE, cmsIndex, NOTIONAL, RATE, FIXED_IS_PAYER);
    assertEquals(swap, swapFromCMSIndex);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixedLeg() {
    // Ibor leg
    CouponIborDefinition[] couponsIbor = new CouponIborDefinition[IBOR_PAYMENT_DATES.length];
    CouponFixedDefinition coupon = new CouponFixedDefinition(CUR, IBOR_PAYMENT_DATES[0], SETTLEMENT_DATE, IBOR_PAYMENT_DATES[0], DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, IBOR_PAYMENT_DATES[0]),
        NOTIONAL, 0.0);
    ZonedDateTime fixingDate = ScheduleCalculator.getAdjustedDate(SETTLEMENT_DATE, BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
    couponsIbor[0] = CouponIborDefinition.from(coupon, fixingDate, IBOR_INDEX);
    for (int loopcpn = 1; loopcpn < IBOR_PAYMENT_DATES.length; loopcpn++) {
      coupon = new CouponFixedDefinition(CUR, IBOR_PAYMENT_DATES[loopcpn], IBOR_PAYMENT_DATES[loopcpn - 1], IBOR_PAYMENT_DATES[loopcpn], DAY_COUNT.getDayCountFraction(IBOR_PAYMENT_DATES[loopcpn - 1],
          IBOR_PAYMENT_DATES[loopcpn]), NOTIONAL, 0.0);
      fixingDate = ScheduleCalculator.getAdjustedDate(IBOR_PAYMENT_DATES[loopcpn - 1], BUSINESS_DAY, CALENDAR, -SETTLEMENT_DAYS);
      couponsIbor[loopcpn] = CouponIborDefinition.from(coupon, fixingDate, IBOR_INDEX);
    }
    AnnuityCouponIborDefinition iborAnnuity = new AnnuityCouponIborDefinition(couponsIbor);

    new SwapFixedIborDefinition(null, iborAnnuity);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIborLeg() {
    // Fixed leg
    CouponFixedDefinition[] couponsFixed = new CouponFixedDefinition[FIXED_PAYMENT_DATES.length];
    couponsFixed[0] = new CouponFixedDefinition(CUR, FIXED_PAYMENT_DATES[0], SETTLEMENT_DATE, FIXED_PAYMENT_DATES[0], FIXED_DAY_COUNT.getDayCountFraction(SETTLEMENT_DATE, FIXED_PAYMENT_DATES[0]),
        NOTIONAL, RATE);
    for (int loopcpn = 1; loopcpn < FIXED_PAYMENT_DATES.length; loopcpn++) {
      couponsFixed[loopcpn] = new CouponFixedDefinition(CUR, FIXED_PAYMENT_DATES[loopcpn], FIXED_PAYMENT_DATES[loopcpn - 1], FIXED_PAYMENT_DATES[loopcpn], FIXED_DAY_COUNT.getDayCountFraction(
          FIXED_PAYMENT_DATES[loopcpn - 1], FIXED_PAYMENT_DATES[loopcpn]), NOTIONAL, RATE);
    }
    AnnuityCouponFixedDefinition fixedAnnuity = new AnnuityCouponFixedDefinition(couponsFixed);

    new SwapFixedIborDefinition(fixedAnnuity, null);
  }

  // TODO: test to derivative

}
