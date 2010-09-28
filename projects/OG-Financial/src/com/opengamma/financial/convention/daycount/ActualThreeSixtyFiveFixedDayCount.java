/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.time.DateUtil;

/**
 * The Actual/365 (Fixed) day count convention.
 * <p>
 * The day count fraction is defined as the actual number of days in the period
 * divided by 365.
 * <p>
 * This convention is also known as "Act/365 (Fixed)", "A/365 (Fixed)" or "A/365F".
 */
public class ActualThreeSixtyFiveFixedDayCount extends StatelessDayCount {

  @Override
  public double getBasis(final ZonedDateTime date) {
    return 365;
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    return DateUtil.getDaysBetween(firstDate, false, secondDate, true) / getBasis(firstDate);
  }

  @Override
  public String getConventionName() {
    return "Actual/365 (Fixed)";
  }

}