/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fixedincome;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.historicaldata.HistoricalTimeSeries;
import com.opengamma.core.historicaldata.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityUtils;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.instrument.FixedIncomeInstrumentConverter;
import com.opengamma.financial.instrument.future.InterestRateFutureSecurityDefinition;
import com.opengamma.financial.instrument.future.InterestRateFutureTransactionDefinition;
import com.opengamma.financial.instrument.swap.SwapDefinition;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.time.DateUtil;
import com.opengamma.util.timeseries.DoubleTimeSeries;
import com.opengamma.util.timeseries.FastBackedDoubleTimeSeries;
import com.opengamma.util.timeseries.fast.DateTimeNumericEncoding;
import com.opengamma.util.timeseries.fast.longint.FastLongDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ArrayZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.timeseries.zoneddatetime.ZonedDateTimeEpochMillisConverter;

/**
 * 
 */
public class FixedIncomeConverterDataProvider {
  private final String _dataSourceName;
  private final String _fieldName;
  private final String _dataProvider = "CMPL"; // TODO: totally fix this.
  private final ConventionBundleSource _conventionSource;

  public FixedIncomeConverterDataProvider(final String dataSourceName, final String fieldName, final ConventionBundleSource conventionSource) {
    _dataSourceName = dataSourceName;
    _fieldName = fieldName;
    _conventionSource = conventionSource;
  }

  public InterestRateDerivative convert(final Security security, final FixedIncomeInstrumentConverter<?> definition,
      final ZonedDateTime now, final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {
    if (definition == null) {
      throw new OpenGammaRuntimeException("Definition to convert was null for security " + security);
    }
    if (security instanceof SwapSecurity) {
      return convert((SwapSecurity) security, (SwapDefinition) definition, now, curveNames, dataSource);
    }
    //TODO this only applies for those futures formed at now (i.e. those used in curves) - interest rate future trades should be converted differently
    if (security instanceof InterestRateFutureSecurity) {
      return convert((InterestRateFutureSecurity) security, (InterestRateFutureSecurityDefinition) definition, now, curveNames, dataSource);
    }
    return definition.toDerivative(now, curveNames);
  }

  public InterestRateDerivative convert(final InterestRateFutureSecurity security, final InterestRateFutureSecurityDefinition definition, final ZonedDateTime now,
      final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {
    final IdentifierBundle id = security.getIdentifiers();
    final LocalDate startDate = DateUtil.previousWeekDay(now.toLocalDate().minusDays(7));
    final HistoricalTimeSeries ts = dataSource
          .getHistoricalTimeSeries(id, _dataSourceName, _dataProvider, _fieldName, startDate, true, now.toLocalDate(), true);
    if (ts == null) {
      throw new OpenGammaRuntimeException("Could not get price time series for " + security);
    }
    final int length = ts.getTimeSeries().size();
    final double lastMarginPrice = ts.getTimeSeries().getValueAt(length - 2);
    final double price = ts.getTimeSeries().getValueAt(length - 1); //TODO this is wrong need margin data and previous close for lastMarginPrice
    final InterestRateFutureTransactionDefinition transactionDefinition = new InterestRateFutureTransactionDefinition(definition, 1, now, price);
    return transactionDefinition.toDerivative(now, lastMarginPrice, curveNames);
  }

  @SuppressWarnings("unchecked")
  public InterestRateDerivative convert(final SwapSecurity security, final SwapDefinition definition, final ZonedDateTime now,
      final String[] curveNames, final HistoricalTimeSeriesSource dataSource) {
    Validate.notNull(security, "security");
    final SwapLeg payLeg = security.getPayLeg();
    final SwapLeg receiveLeg = security.getReceiveLeg();
    final ZonedDateTime swapStartDate = security.getEffectiveDate();
    final DoubleTimeSeries<ZonedDateTime> payLegTS = getIndexTimeSeries(
        InterestRateInstrumentType.getInstrumentTypeFromSecurity(security), payLeg, swapStartDate, now, dataSource);
    final DoubleTimeSeries<ZonedDateTime> receiveLegTS = getIndexTimeSeries(
        InterestRateInstrumentType.getInstrumentTypeFromSecurity(security), receiveLeg, swapStartDate, now, dataSource);
    if (payLegTS != null) {
      if (receiveLegTS != null) {
        return definition.toDerivative(now, new DoubleTimeSeries[] {payLegTS, receiveLegTS}, curveNames);
      }
      return definition.toDerivative(now, new DoubleTimeSeries[] {payLegTS}, curveNames);
    }
    if (receiveLegTS != null) {
      return definition.toDerivative(now, new DoubleTimeSeries[] {receiveLegTS}, curveNames);
    }
    throw new OpenGammaRuntimeException("Could not get fixing series for either the pay or receive leg");
  }

  private DoubleTimeSeries<ZonedDateTime> getIndexTimeSeries(final InterestRateInstrumentType type, final SwapLeg leg,
      final ZonedDateTime swapStartDate, final ZonedDateTime now, final HistoricalTimeSeriesSource dataSource) {
    if (leg instanceof FloatingInterestRateLeg) {
      final FloatingInterestRateLeg floatingLeg = (FloatingInterestRateLeg) leg;

      Identifier indexID = floatingLeg.getFloatingReferenceRateIdentifier();
      if (!indexID.getScheme().equals(SecurityUtils.BLOOMBERG_TICKER)) {
        ConventionBundle indexConvention = _conventionSource.getConventionBundle(floatingLeg.getFloatingReferenceRateIdentifier());
        if (indexConvention == null) {
          //TODO remove this immediately
          indexConvention = _conventionSource.getConventionBundle(Identifier.of(SecurityUtils.BLOOMBERG_TICKER, indexID.getValue()));
        }
        indexID = indexConvention.getIdentifiers().getIdentifier(SecurityUtils.BLOOMBERG_TICKER);
      } 

      final IdentifierBundle id = indexID.toBundle();
      final LocalDate startDate = swapStartDate.isBefore(now) ? swapStartDate.toLocalDate().minusDays(7) : now.toLocalDate()
          .minusDays(7); 
      final HistoricalTimeSeries ts = dataSource
          .getHistoricalTimeSeries(id, _dataSourceName, _dataProvider, _fieldName, startDate, true, now.toLocalDate(), true);
      if (ts == null) {        
        throw new OpenGammaRuntimeException("Could not get time series of underlying index " + indexID.toString());
      }
      FastBackedDoubleTimeSeries<LocalDate> localDateTS = ts.getTimeSeries();
      //TODO this normalization should not be done here
      if (type == InterestRateInstrumentType.SWAP_FIXED_IBOR) {
        localDateTS = localDateTS.divide(100);
      } else if (type == InterestRateInstrumentType.SWAP_IBOR_IBOR) { //TODO not really - valid for tenor swaps but we really need to normalize the time series rather than doing it here
        localDateTS = localDateTS.divide(10000);
      } else {
        throw new OpenGammaRuntimeException("Couldn't identify swap type");
      }
      final FastLongDoubleTimeSeries convertedTS = localDateTS
          .toFastLongDoubleTimeSeries(DateTimeNumericEncoding.TIME_EPOCH_MILLIS);
      final LocalTime fixingTime = LocalTime.of(11, 0);
      return new ArrayZonedDateTimeDoubleTimeSeries(new ZonedDateTimeEpochMillisConverter(now.getZone(), fixingTime),
          convertedTS);
    }
    return null;
  }
}
