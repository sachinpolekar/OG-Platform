/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.timeseries.sampling;

import java.util.ArrayList;
import java.util.List;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * 
 */
public class NoPaddingTimeSeriesSamplingFunction implements TimeSeriesSamplingFunction {

  @Override
  public DoubleTimeSeries<?> getSampledTimeSeries(final DoubleTimeSeries<?> ts, final LocalDate[] schedule) {
    Validate.notNull(ts, "time series");
    Validate.notNull(schedule, "schedule");
    final LocalDateDoubleTimeSeries localDateTS = ts.toLocalDateDoubleTimeSeries();
    final List<LocalDate> tsDates = localDateTS.times();
    final List<LocalDate> scheduledDates = new ArrayList<LocalDate>();
    final List<Double> scheduledData = new ArrayList<Double>();
    for (final LocalDate date : schedule) {
      if (tsDates.contains(date)) {
        scheduledDates.add(date);
        scheduledData.add(localDateTS.getValue(date));
      }
    }
    return new ArrayLocalDateDoubleTimeSeries(scheduledDates, scheduledData);
  }

}