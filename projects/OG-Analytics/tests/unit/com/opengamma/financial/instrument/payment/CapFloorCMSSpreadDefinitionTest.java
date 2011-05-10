/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.payment;

import static org.testng.AssertJUnit.assertEquals;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.Period;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.CMSIndex;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.swap.SwapFixedIborDefinition;
import com.opengamma.financial.interestrate.payments.CapFloorCMSSpread;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.DateUtil;

/**
 * Test related to CapFloorCMSSpreadDefinition construction.
 */
public class CapFloorCMSSpreadDefinitionTest {

  //Swaps
  private static final Currency CUR = Currency.USD;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final ZonedDateTime SETTLEMENT_DATE = DateUtil.getUTCDate(2011, 3, 17);
  private static final Period FIXED_PAYMENT_PERIOD = Period.ofMonths(6);
  private static final DayCount FIXED_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("30/360");
  private static final boolean FIXED_IS_PAYER = true; // Irrelevant for the underlying
  private static final double RATE = 0.0; // Irrelevant for the underlying
  private static final Period INDEX_TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final DayCount DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final IborIndex IBOR_INDEX = new IborIndex(CUR, INDEX_TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT, BUSINESS_DAY, IS_EOM);
  // Swap 10Y
  private static final Period ANNUITY_TENOR_1 = Period.ofYears(10);
  private static final CMSIndex CMS_INDEX_1 = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR_1);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_1 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX_1, 1.0, RATE, FIXED_IS_PAYER);
  // Swap 2Y
  private static final Period ANNUITY_TENOR_2 = Period.ofYears(2);
  private static final CMSIndex CMS_INDEX_2 = new CMSIndex(FIXED_PAYMENT_PERIOD, FIXED_DAY_COUNT, IBOR_INDEX, ANNUITY_TENOR_2);
  private static final SwapFixedIborDefinition SWAP_DEFINITION_2 = SwapFixedIborDefinition.from(SETTLEMENT_DATE, CMS_INDEX_2, 1.0, RATE, FIXED_IS_PAYER);
  // CMS spread coupon
  private static final double NOTIONAL = 10000000;
  private static final ZonedDateTime PAYMENT_DATE = DateUtil.getUTCDate(2011, 4, 6);
  private static final ZonedDateTime FIXING_DATE = DateUtil.getUTCDate(2010, 12, 30);
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 1, 5);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 4, 5);
  private static final DayCount PAYMENT_DAY_COUNT = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final double PAYMENT_ACCRUAL_FACTOR = PAYMENT_DAY_COUNT.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double STRIKE = 0.0050; // 50 bps
  private static final boolean IS_CAP = true;
  private static final CapFloorCMSSpreadDefinition CMS_SPREAD_DEFINITION = new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL,
      FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE, IS_CAP);

  // to derivatives
  private static final LocalDate REFERENCE_DATE = LocalDate.of(2010, 8, 18);
  private static final String FUNDING_CURVE_NAME = "Funding";
  private static final String FORWARD_CURVE_1_NAME = "Forward 1";
  //  private static final String FORWARD_CURVE_2_NAME = "Forward 2";
  private static final String[] CURVES_2_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_1_NAME};
  //  private static final String[] CURVES_3_NAME = {FUNDING_CURVE_NAME, FORWARD_CURVE_1_NAME, FORWARD_CURVE_2_NAME};
  private static final FixedCouponSwap<? extends Payment> SWAP_1 = SWAP_DEFINITION_1.toDerivative(REFERENCE_DATE, CURVES_2_NAME);
  private static final FixedCouponSwap<? extends Payment> SWAP_2 = SWAP_DEFINITION_2.toDerivative(REFERENCE_DATE, CURVES_2_NAME);
  private static final DayCount ACT_ACT = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
  private static final ZonedDateTime REFERENCE_DATE_ZONED = ZonedDateTime.of(LocalDateTime.ofMidnight(REFERENCE_DATE), TimeZone.UTC);
  private static final double PAYMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, PAYMENT_DATE);
  private static final double FIXING_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, FIXING_DATE);
  private static final double SETTLEMENT_TIME = ACT_ACT.getDayCountFraction(REFERENCE_DATE_ZONED, SWAP_DEFINITION_1.getFixedLeg().getNthPayment(0).getAccrualStartDate());

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new CapFloorCMSSpreadDefinition(null, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2,
        CMS_INDEX_2, STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPaymentDate() {
    new CapFloorCMSSpreadDefinition(CUR, null, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2,
        STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualStart() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, null, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE,
        IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAccrualEnd() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, null, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE,
        IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullFixingDate() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, null, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2,
        STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testSwap1() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, null, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE,
        IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex1() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, null, SWAP_DEFINITION_2, CMS_INDEX_2,
        STRIKE, IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSwap2() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, null, CMS_INDEX_2, STRIKE,
        IS_CAP);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullIndex2() {
    new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, null,
        STRIKE, IS_CAP);
  }

  @Test
  public void testGetter() {
    assertEquals(SWAP_DEFINITION_1, CMS_SPREAD_DEFINITION.getUnderlyingSwap1());
    assertEquals(CMS_INDEX_1, CMS_SPREAD_DEFINITION.getCmsIndex1());
    assertEquals(SWAP_DEFINITION_2, CMS_SPREAD_DEFINITION.getUnderlyingSwap2());
    assertEquals(CMS_INDEX_2, CMS_SPREAD_DEFINITION.getCmsIndex2());
    assertEquals(STRIKE, CMS_SPREAD_DEFINITION.geStrike(), 1E-10);
    assertEquals(IS_CAP, CMS_SPREAD_DEFINITION.isCap());
  }

  @Test
  public void testEqualHash() {
    CapFloorCMSSpreadDefinition newCMSSpread = new CapFloorCMSSpreadDefinition(CUR, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
        SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE, IS_CAP);
    assertEquals(newCMSSpread.equals(CMS_SPREAD_DEFINITION), true);
    assertEquals(newCMSSpread.hashCode() == CMS_SPREAD_DEFINITION.hashCode(), true);
    Currency newCur = Currency.EUR;
    CapFloorCMSSpreadDefinition cmsSpreadCur = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE,
        SWAP_DEFINITION_1, CMS_INDEX_1, SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE, IS_CAP);
    assertEquals(cmsSpreadCur.equals(CMS_SPREAD_DEFINITION), false);
    CapFloorCMSSpreadDefinition cmsSpreadModified;
    cmsSpreadModified = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_2, CMS_INDEX_1,
        SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE, IS_CAP);
    assertEquals(cmsSpreadModified.equals(CMS_SPREAD_DEFINITION), false);
    cmsSpreadModified = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_2,
        SWAP_DEFINITION_2, CMS_INDEX_2, STRIKE, IS_CAP);
    assertEquals(cmsSpreadModified.equals(CMS_SPREAD_DEFINITION), false);
    cmsSpreadModified = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1,
        SWAP_DEFINITION_1, CMS_INDEX_2, STRIKE, IS_CAP);
    assertEquals(cmsSpreadModified.equals(CMS_SPREAD_DEFINITION), false);
    cmsSpreadModified = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1,
        SWAP_DEFINITION_2, CMS_INDEX_1, STRIKE, IS_CAP);
    assertEquals(cmsSpreadModified.equals(CMS_SPREAD_DEFINITION), false);
    cmsSpreadModified = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1,
        SWAP_DEFINITION_2, CMS_INDEX_1, STRIKE + 0.0001, IS_CAP);
    assertEquals(cmsSpreadModified.equals(CMS_SPREAD_DEFINITION), false);
    cmsSpreadModified = new CapFloorCMSSpreadDefinition(newCur, PAYMENT_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, SWAP_DEFINITION_1, CMS_INDEX_1,
        SWAP_DEFINITION_2, CMS_INDEX_1, STRIKE, !IS_CAP);
    assertEquals(cmsSpreadModified.equals(CMS_SPREAD_DEFINITION), false);
  }

  @Test
  public void testToDerivative() {
    CapFloorCMSSpread cmsSpread = (CapFloorCMSSpread) CMS_SPREAD_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_2_NAME);
    assertEquals(SWAP_1, cmsSpread.getUnderlyingSwap1());
    assertEquals(SWAP_2, cmsSpread.getUnderlyingSwap2());
    CapFloorCMSSpread cmsSpreadExpected = new CapFloorCMSSpread(CUR, PAYMENT_TIME, PAYMENT_ACCRUAL_FACTOR, NOTIONAL, FIXING_TIME, SWAP_1, CMS_INDEX_1, SWAP_2, CMS_INDEX_2, SETTLEMENT_TIME, STRIKE,
        IS_CAP, FUNDING_CURVE_NAME);
    assertEquals("CMS Spread to derivatives", cmsSpreadExpected, cmsSpread);

  }

}