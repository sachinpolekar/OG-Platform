/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments.method;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;
import java.util.Map;

import javax.time.calendar.Period;
import javax.time.calendar.ZonedDateTime;

import org.testng.annotations.Test;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.financial.instrument.payment.CouponFixedDefinition;
import com.opengamma.financial.instrument.payment.CouponIborDefinition;
import com.opengamma.financial.instrument.payment.CouponIborGearingDefinition;
import com.opengamma.financial.interestrate.PresentValueCalculator;
import com.opengamma.financial.interestrate.PresentValueSensitivity;
import com.opengamma.financial.interestrate.PresentValueSensitivityCalculator;
import com.opengamma.financial.interestrate.TestsDataSets;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.method.SensitivityFiniteDifference;
import com.opengamma.financial.interestrate.payments.CouponIborGearing;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.schedule.ScheduleCalculator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests related to the pricing and sensitivities of Ibor coupon with gearing factor and spread in the discounting method.
 */
public class CouponIborGearingDiscountingMethodTest {
  // The index: Libor 3m
  private static final Period TENOR = Period.ofMonths(3);
  private static final int SETTLEMENT_DAYS = 2;
  private static final Calendar CALENDAR = new MondayToFridayCalendar("A");
  private static final DayCount DAY_COUNT_INDEX = DayCountFactory.INSTANCE.getDayCount("Actual/360");
  private static final BusinessDayConvention BUSINESS_DAY = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following");
  private static final boolean IS_EOM = true;
  private static final Currency CUR = Currency.USD;
  private static final IborIndex INDEX = new IborIndex(CUR, TENOR, SETTLEMENT_DAYS, CALENDAR, DAY_COUNT_INDEX, BUSINESS_DAY, IS_EOM);
  // Coupon
  private static final DayCount DAY_COUNT_COUPON = DayCountFactory.INSTANCE.getDayCount("Actual/365");
  private static final ZonedDateTime ACCRUAL_START_DATE = DateUtil.getUTCDate(2011, 5, 23);
  private static final ZonedDateTime ACCRUAL_END_DATE = DateUtil.getUTCDate(2011, 8, 22);
  private static final double ACCRUAL_FACTOR = DAY_COUNT_COUPON.getDayCountFraction(ACCRUAL_START_DATE, ACCRUAL_END_DATE);
  private static final double NOTIONAL = 1000000; //1m
  private static final double FACTOR = 2.0;
  private static final double SPREAD = 0.0050;
  private static final ZonedDateTime FIXING_DATE = ScheduleCalculator.getAdjustedDate(ACCRUAL_START_DATE, CALENDAR, -SETTLEMENT_DAYS);
  private static final CouponIborGearingDefinition COUPON_DEFINITION = new CouponIborGearingDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL,
      FIXING_DATE, INDEX, SPREAD, FACTOR);
  private static final ZonedDateTime REFERENCE_DATE = DateUtil.getUTCDate(2010, 12, 27);
  private static final YieldCurveBundle CURVES_BUNDLE = TestsDataSets.createCurves1();
  private static final String[] CURVES_NAMES = CURVES_BUNDLE.getAllNames().toArray(new String[0]);
  private static final CouponIborGearingDiscountingMethod METHOD = CouponIborGearingDiscountingMethod.getInstance();
  private static final CouponIborGearing COUPON = (CouponIborGearing) COUPON_DEFINITION.toDerivative(REFERENCE_DATE, CURVES_NAMES);
  private static final PresentValueCalculator PVC = PresentValueCalculator.getInstance();
  private static final PresentValueSensitivityCalculator PVCSC = PresentValueSensitivityCalculator.getInstance();

  @Test
  /**
   * Tests the present value.
   */
  public void presentValue() {
    final CurrencyAmount pv = METHOD.presentValue(COUPON, CURVES_BUNDLE);
    final CouponIborDefinition couponIborDefinition = new CouponIborDefinition(CUR, ACCRUAL_END_DATE, ACCRUAL_START_DATE, ACCRUAL_END_DATE, ACCRUAL_FACTOR, NOTIONAL, FIXING_DATE, INDEX);
    final Payment couponIbor = couponIborDefinition.toDerivative(REFERENCE_DATE, CURVES_NAMES);
    final CouponFixedDefinition couponFixedDefinition = new CouponFixedDefinition(couponIborDefinition, SPREAD);
    final Payment couponFixed = couponFixedDefinition.toDerivative(REFERENCE_DATE, CURVES_NAMES);
    final PresentValueCalculator pvc = PresentValueCalculator.getInstance();
    final double pvIbor = pvc.visit(couponIbor, CURVES_BUNDLE);
    final double pvFixed = pvc.visit(couponFixed, CURVES_BUNDLE);
    assertEquals("Present value by discounting", pvIbor * FACTOR + pvFixed, pv.getAmount());
  }

  @Test
  /**
   * Tests the present value in method vs calculator.
   */
  public void presentValueMethodVsCalculator() {
    final CurrencyAmount pvMethod = METHOD.presentValue(COUPON, CURVES_BUNDLE);
    final double pvCalculator = PVC.visit(COUPON, CURVES_BUNDLE);
    assertEquals("Coupon with gearing and spread - present value: Method vs Calculator", pvMethod.getAmount(), pvCalculator);
  }

  @Test
  /**
   * Test the present value curves sensitivity.
   */
  public void presentValueCurveSensitivity() {
    final PresentValueSensitivity pvsFuture = METHOD.presentValueCurveSensitivity(COUPON, CURVES_BUNDLE);
    pvsFuture.clean();
    final double deltaTolerancePrice = 1.0E+2;
    //Testing note: Sensitivity is for a movement of 1. 1E+2 = 1 cent for a 1 bp move. Tolerance increased to cope with numerical imprecision of finite difference.
    final double deltaShift = 1.0E-6;
    // 1. Forward curve sensitivity
    final String bumpedCurveName = "Bumped Curve";
    final Payment couponBumpedForward = COUPON_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {CURVES_NAMES[0], bumpedCurveName});
    final double[] nodeTimesForward = new double[] {COUPON.getFixingPeriodStartTime(), COUPON.getFixingPeriodEndTime()};
    final double[] sensiForwardMethod = SensitivityFiniteDifference.curveSensitivity(couponBumpedForward, CURVES_BUNDLE, CURVES_NAMES[1], bumpedCurveName, nodeTimesForward, deltaShift, METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 2, sensiForwardMethod.length);
    final List<DoublesPair> sensiPvForward = pvsFuture.getSensitivities().get(CURVES_NAMES[1]);
    for (int loopnode = 0; loopnode < sensiForwardMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvForward.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesForward[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiForwardMethod[loopnode], deltaTolerancePrice);
    }
    // 2. Discounting curve sensitivity
    final Payment couponBumpedDisc = COUPON_DEFINITION.toDerivative(REFERENCE_DATE, new String[] {bumpedCurveName, CURVES_NAMES[1]});
    final double[] nodeTimesDisc = new double[] {COUPON.getPaymentTime()};
    final double[] sensiDiscMethod = SensitivityFiniteDifference.curveSensitivity(couponBumpedDisc, CURVES_BUNDLE, CURVES_NAMES[0], bumpedCurveName, nodeTimesDisc, deltaShift, METHOD);
    assertEquals("Sensitivity finite difference method: number of node", 1, sensiDiscMethod.length);
    final List<DoublesPair> sensiPvDisc = pvsFuture.getSensitivities().get(CURVES_NAMES[0]);
    for (int loopnode = 0; loopnode < sensiDiscMethod.length; loopnode++) {
      final DoublesPair pairPv = sensiPvDisc.get(loopnode);
      assertEquals("Sensitivity coupon pv to forward curve: Node " + loopnode, nodeTimesDisc[loopnode], pairPv.getFirst(), 1E-8);
      assertEquals("Sensitivity finite difference method: node sensitivity", pairPv.second, sensiDiscMethod[loopnode], deltaTolerancePrice);
    }
  }

  @Test
  /**
   * Tests the present value curve sensitivity in method vs calculator.
   */
  public void presentValueCurveSensitivityMethodVsCalculator() {
    PresentValueSensitivity pvcsMethod = METHOD.presentValueCurveSensitivity(COUPON, CURVES_BUNDLE);
    Map<String, List<DoublesPair>> pvcsCalculator = PVCSC.visit(COUPON, CURVES_BUNDLE);
    assertEquals("Coupon with gearing and spread - present value curve sensitivity: Method vs Calculator", pvcsMethod.getSensitivities(), pvcsCalculator);
  }

}
