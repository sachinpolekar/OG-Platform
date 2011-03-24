/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.swaption;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalDateTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinition;
import com.opengamma.financial.instrument.FixedIncomeInstrumentDefinitionVisitor;
import com.opengamma.financial.instrument.swap.ZZZSwapFixedIborDefinition;
import com.opengamma.financial.interestrate.payments.Payment;
import com.opengamma.financial.interestrate.swap.definition.FixedCouponSwap;
import com.opengamma.financial.interestrate.swaption.SwaptionPhysicalFixedIbor;
import com.opengamma.financial.model.option.definition.EuropeanVanillaOptionDefinition;
import com.opengamma.util.time.Expiry;

/**
 * Class describing a European swaption on a vanilla swap with physical delivery.
 */
public final class SwaptionPhysicalFixedIborDefinition extends EuropeanVanillaOptionDefinition implements FixedIncomeInstrumentDefinition<SwaptionPhysicalFixedIbor> {

  /**
   * Swap underlying the swaption.
   */
  private final ZZZSwapFixedIborDefinition _underlyingSwap;
  /**
   * Flag indicating if the option is long (true) or short (false).
   */
  private final boolean _isLong;

  /**
   * Constructor from the expiry date, the underlying swap and the long/short flqg.
   * @param expiryDate The expiry date.
   * @param strike The strike
   * @param underlyingSwap The underlying swap.
   * @param isCall Call.
   * @param isLong The long (true) / short (false) flag.
   */
  private SwaptionPhysicalFixedIborDefinition(ZonedDateTime expiryDate, double strike, ZZZSwapFixedIborDefinition underlyingSwap, boolean isCall, boolean isLong) {
    super(strike, new Expiry(expiryDate), isCall);
    // A swaption payer can be consider as a call on the swap rate.
    Validate.notNull(expiryDate, "expiry date");
    Validate.notNull(underlyingSwap, "underlying swap");
    Validate.isTrue(isCall == underlyingSwap.getFixedLeg().isPayer(), "Call flag not in line with underlying");
    _underlyingSwap = underlyingSwap;
    _isLong = isLong;
  }

  /**
   * Builder from the expiry date, the underlying swap and the long/short flqg.
   * @param expiryDate The expiry date.
   * @param underlyingSwap The underlying swap.
   * @param isLong The long (true) / short (false) flag.
   * @return The swaption.
   */
  public static SwaptionPhysicalFixedIborDefinition from(ZonedDateTime expiryDate, ZZZSwapFixedIborDefinition underlyingSwap, boolean isLong) {
    Validate.notNull(expiryDate, "expiry date");
    Validate.notNull(underlyingSwap, "underlying swap");
    // A swaption payer can be consider as a call on the swap rate.
    double strike = underlyingSwap.getFixedLeg().getNthPayment(0).getRate();
    // Is working only for swap with same rate on all coupons and standard conventions.
    return new SwaptionPhysicalFixedIborDefinition(expiryDate, strike, underlyingSwap, underlyingSwap.getFixedLeg().isPayer(), isLong);
  }

  /**
   * Gets the _underlyingSwap field.
   * @return The underlying swap.
   */
  public ZZZSwapFixedIborDefinition getUnderlyingSwap() {
    return _underlyingSwap;
  }

  /**
   * Gets the _isLong field.
   * @return The Long (true)/Short (flase) flag.
   */
  public boolean isLong() {
    return _isLong;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _underlyingSwap.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    SwaptionPhysicalFixedIborDefinition other = (SwaptionPhysicalFixedIborDefinition) obj;
    if (!ObjectUtils.equals(_underlyingSwap, other._underlyingSwap)) {
      return false;
    }
    return true;
  }

  @Override
  public SwaptionPhysicalFixedIbor toDerivative(LocalDate date, String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    final ZonedDateTime zonedDate = ZonedDateTime.of(LocalDateTime.ofMidnight(date), TimeZone.UTC);
    final double expiryTime = actAct.getDayCountFraction(zonedDate, getExpiry().getExpiry());
    final FixedCouponSwap<? extends Payment> underlyingSwap = _underlyingSwap.toDerivative(date, yieldCurveNames);
    return SwaptionPhysicalFixedIbor.from(expiryTime, underlyingSwap, _isLong);
  }

  @Override
  public <U, V> V accept(FixedIncomeInstrumentDefinitionVisitor<U, V> visitor, U data) {
    return null;
  }

  @Override
  public <V> V accept(FixedIncomeInstrumentDefinitionVisitor<?, V> visitor) {
    return null;
  }

}