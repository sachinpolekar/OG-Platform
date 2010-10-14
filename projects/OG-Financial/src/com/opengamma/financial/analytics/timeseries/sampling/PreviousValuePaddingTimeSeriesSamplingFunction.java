/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class PreviousValuePaddingTimeSeriesSamplingFunction implements TimeSeriesSamplingFunction {

  @Override
  public DoubleTimeSeries<?> getPaddedTimeSeries(final DoubleTimeSeries<?> ts, final LocalDate[] schedule) {
    Validate.notNull(ts, "time series");
    Validate.notNull(schedule, "schedule");
    final LocalDateDoubleTimeSeries localDateTS = ts.toLocalDateDoubleTimeSeries();
    final List<LocalDate> tsDates = localDateTS.times();
    final List<LocalDate> scheduledDates = Arrays.asList(schedule);
    final List<Double> scheduledData = new ArrayList<Double>();
    for (final LocalDate date : schedule) {
      if (tsDates.contains(date)) {
        scheduledData.add(localDateTS.getValue(date));
      } else {
        if (localDateTS.getEarliestTime().isAfter(date)) {
          throw new IllegalArgumentException("Could not get any data for date " + date);
        } else {
          LocalDate temp = date.minusDays(1);
          while (!tsDates.contains(temp)) {
            temp = temp.minusDays(1);
            if (temp.isBefore(schedule[0]) || temp.isBefore(tsDates.get(0))) {
              throw new IllegalArgumentException("Could not get any data for date " + date);
            }
          }
          scheduledData.add(localDateTS.getValue(temp));
        }
      }
    }
    return new ArrayLocalDateDoubleTimeSeries(scheduledDates, scheduledData);
  }
}