/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.option.pricing.analytic;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.option.definition.FixedStrikeLookbackOptionDefinition;
import com.opengamma.financial.model.option.definition.StandardOptionWithSpotTimeSeriesDataBundle;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.statistics.distribution.NormalDistribution;
import com.opengamma.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.timeseries.DoubleTimeSeries;

/**
 * 
 */
public class FixedStrikeLookbackOptionModel extends AnalyticOptionModel<FixedStrikeLookbackOptionDefinition, StandardOptionWithSpotTimeSeriesDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public Function1D<StandardOptionWithSpotTimeSeriesDataBundle, Double> getPricingFunction(final FixedStrikeLookbackOptionDefinition definition) {
    Validate.notNull(definition, "definition");
    return new Function1D<StandardOptionWithSpotTimeSeriesDataBundle, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final StandardOptionWithSpotTimeSeriesDataBundle data) {
        Validate.notNull(data, "data");
        final DoubleTimeSeries<?> ts = data.getSpotTimeSeries();
        final double s = data.getSpot();
        final ZonedDateTime date = data.getDate();
        final double t = definition.getTimeToExpiry(date);
        final boolean isCall = definition.isCall();
        final double k = definition.getStrike();
        final double sCritical = isCall ? ts.maxValue() : ts.minValue();
        final double sigma = data.getVolatility(t, k);
        final double r = data.getInterestRate(t);
        final double b = data.getCostOfCarry();
        final double df1 = Math.exp(t * (b - r));
        final double df2 = Math.exp(-r * t);
        double x = k;
        if ((isCall && x <= sCritical) || (!isCall && x >= sCritical)) {
          x = sCritical;
        }
        final int sign = isCall ? 1 : -1;
        final double d1 = getD1(s, x, t, sigma, b);
        final double d2 = getD2(d1, sigma, t);
        final double cdf1 = NORMAL.getCDF(sign * d1);
        return sign
            * (df2 * (x - k) + s * df1 * cdf1 - x * df2 * NORMAL.getCDF(sign * d2) - s * df2 * sigma * sigma
                * (Math.pow(s / x, -2 * b / sigma / sigma) * NORMAL.getCDF(sign * (d1 - 2 * b * Math.sqrt(t) / sigma)) - Math.exp(b * t) * cdf1) / 2 / b);
      }

    };
  }
}
