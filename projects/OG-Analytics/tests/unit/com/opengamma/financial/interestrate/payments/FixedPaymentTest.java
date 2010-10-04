/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.payments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * 
 */
public class FixedPaymentTest {

  private static final double PAYMENT_TIME = 0.67;
  private static final double AMOUNT = 45.6;
  private static final String CURVE_NAME = "vfsmngsdjkflsadfk";
  private static final FixedPayment PAYMENT = new FixedPayment(PAYMENT_TIME, AMOUNT, CURVE_NAME);

  @Test(expected = IllegalArgumentException.class)
  public void testNegativePaymentTime() {
    new FixedPayment(-1, AMOUNT, CURVE_NAME);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCurveName() {
    new FixedPayment(PAYMENT_TIME, AMOUNT, null);
  }

  @Test
  public void testHashCodeAndEquals() {
    FixedPayment other = new FixedPayment(PAYMENT_TIME, AMOUNT, CURVE_NAME);
    assertEquals(other, PAYMENT);
    assertEquals(other.hashCode(), PAYMENT.hashCode());
    other = new FixedPayment(PAYMENT_TIME - 0.01, AMOUNT, CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new FixedPayment(PAYMENT_TIME, AMOUNT + 0.01, CURVE_NAME);
    assertFalse(other.equals(PAYMENT));
    other = new FixedPayment(PAYMENT_TIME, AMOUNT, "hklhkldf");
    assertFalse(other.equals(PAYMENT));
  }

  @Test
  public void testGetters() {
    assertEquals(PAYMENT.getPaymentTime(), PAYMENT_TIME, 0);
    assertEquals(PAYMENT.getAmount(), AMOUNT, 0);
    assertEquals(PAYMENT.getFundingCurveName(), CURVE_NAME);
  }

}