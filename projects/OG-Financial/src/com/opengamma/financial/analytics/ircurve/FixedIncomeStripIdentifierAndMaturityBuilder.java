/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.time.calendar.LocalDate;
import javax.time.calendar.LocalTime;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.financial.convention.ConventionBundle;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.DefaultConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.financial.security.DateTimeWithZone;
import com.opengamma.financial.security.cash.CashSecurity;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.swap.FixedInterestRateLeg;
import com.opengamma.financial.security.swap.FloatingInterestRateLeg;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.security.swap.SwapSecurity;
import com.opengamma.financial.world.region.InMemoryRegionMaster;
import com.opengamma.financial.world.region.Region;
import com.opengamma.financial.world.region.RegionSource;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.time.DateUtil;

/**
 * Converts specifications into fully resolved security definitions 
 */
public class FixedIncomeStripIdentifierAndMaturityBuilder {
  private static final LocalTime CASH_EXPIRY_TIME = LocalTime.of(11, 00);
  private RegionSource _regionSource;
  private ConventionBundleSource _conventionBundleSource;
  private SecuritySource _secSource;

  public FixedIncomeStripIdentifierAndMaturityBuilder(RegionSource regionSource, ConventionBundleSource conventionBundleSource, SecuritySource secSource) {
    _regionSource = regionSource;
    _conventionBundleSource = conventionBundleSource;
    _secSource = secSource;
  }
  
  public InterpolatedYieldCurveSpecificationWithSecurities resolveToSecurity(InterpolatedYieldCurveSpecification curveSpecification, Map<Identifier, Double> marketValues) {
    //Currency currency = curveSpecification.getCurrency();
    LocalDate curveDate = curveSpecification.getCurveDate();
    Collection<FixedIncomeStripWithSecurity> securityStrips = new ArrayList<FixedIncomeStripWithSecurity>();
    for (FixedIncomeStripWithIdentifier strip : curveSpecification.getStrips()) {
      Security security;
      ZonedDateTime maturity;
      switch (strip.getInstrumentType()) {
        case CASH:
          CashSecurity cashSecurity = getCash(curveSpecification, strip);
          if (cashSecurity == null) { 
            throw new OpenGammaRuntimeException("Could not resolve cash curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification); 
          }
          Region region = _regionSource.getHighestLevelRegion(cashSecurity.getRegion());
          TimeZone timeZone = region.getTimeZone();
          timeZone = ensureZone(timeZone);
          maturity = curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone);
          security = cashSecurity;
          break;
        case FRA:
          FRASecurity fraSecurity = getFRA(curveSpecification, strip);
          if (fraSecurity == null) { 
            throw new OpenGammaRuntimeException("Could not resolve FRA curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification); 
          }
          maturity = fraSecurity.getEndDate().toZonedDateTime();
          security = fraSecurity;
          break;
        case FUTURE:
          // TODO: jim 17-Aug-2010 -- we need to sort out the zoned date time related to the expiry.
          FutureSecurity futureSecurity = getFuture(curveSpecification, strip);
          if (futureSecurity == null) { 
            throw new OpenGammaRuntimeException("Could not resolve future curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification); 
          }
          maturity = futureSecurity.getExpiry().getExpiry();
          security = futureSecurity;
          break;
        case LIBOR:
          CashSecurity rateSecurity = getCash(curveSpecification, strip);
          if (rateSecurity == null) { 
            throw new OpenGammaRuntimeException("Could not resolve future curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification); 
          }
          Region region2 = _regionSource.getHighestLevelRegion(rateSecurity.getRegion());
          TimeZone timeZone2 = region2.getTimeZone();
          timeZone2 = ensureZone(timeZone2);
          maturity = curveDate.plus(strip.getMaturity().getPeriod()).atTime(CASH_EXPIRY_TIME).atZone(timeZone2);
          security = rateSecurity;
          break;
        case SWAP:
          SwapSecurity swapSecurity = getSwap(curveSpecification, strip, marketValues);
          if (swapSecurity == null) { 
            throw new OpenGammaRuntimeException("Could not resolve swap curve instrument " + strip.getSecurity() + " from strip " + strip + " in " + curveSpecification); 
          }
          maturity = swapSecurity.getMaturityDate().toZonedDateTime();
          security = swapSecurity;
          break;
        default:
          throw new OpenGammaRuntimeException("Unhandled type of instrument in curve definition " + strip.getInstrumentType());
      }
      securityStrips.add(new FixedIncomeStripWithSecurity(strip.getInstrumentType(), maturity, strip.getSecurity(), security));
    }
    return new InterpolatedYieldCurveSpecificationWithSecurities(curveDate, curveSpecification.getName(), curveSpecification.getCurrency(), curveSpecification.getInterpolator(), securityStrips);
  }
  
  private CashSecurity getCash(InterpolatedYieldCurveSpecification spec, FixedIncomeStripWithIdentifier strip) {
    CashSecurity sec = new CashSecurity(spec.getCurrency(), Identifier.of(InMemoryRegionMaster.ISO_COUNTRY_2, "US"), 
                                        new DateTimeWithZone(spec.getCurveDate().plus(strip.getMaturity().getPeriod()).atTime(11, 00)));
    sec.setIdentifiers(IdentifierBundle.of(strip.getSecurity()));
    return sec;
  }
  
  private FRASecurity getFRA(InterpolatedYieldCurveSpecification spec, FixedIncomeStripWithIdentifier strip) {
    LocalDate curveDate = spec.getCurveDate(); // quick hack
    LocalDate startDate = curveDate.plus(strip.getMaturity().getPeriod());
    LocalDate endDate = startDate.plusMonths(3); // quick hack, needs to be sorted.
    return new FRASecurity(spec.getCurrency(), Identifier.of(InMemoryRegionMaster.ISO_COUNTRY_2, "US"), 
                           new DateTimeWithZone(startDate.atTime(11, 00)), new DateTimeWithZone(endDate.atTime(11, 00)));
  }
  
  private FutureSecurity getFuture(InterpolatedYieldCurveSpecification spec, FixedIncomeStripWithIdentifier strip) {
    return (FutureSecurity) _secSource.getSecurity(IdentifierBundle.of(strip.getSecurity()));
  }
  
  private SwapSecurity getSwap(InterpolatedYieldCurveSpecification spec, FixedIncomeStripWithIdentifier strip, Map<Identifier, Double> marketValues) {
    Identifier swapIdentifier = strip.getSecurity();
    Double rate = marketValues.get(swapIdentifier);
    LocalDate curveDate = spec.getCurveDate();
    InMemoryConventionBundleMaster refRateRepo = new InMemoryConventionBundleMaster();
    ConventionBundleSource source = new DefaultConventionBundleSource(refRateRepo);
    DateTimeWithZone tradeDate = new DateTimeWithZone(curveDate.atTime(11, 00).atZone(TimeZone.UTC));
    DateTimeWithZone effectiveDate = new DateTimeWithZone(DateUtil.previousWeekDay(curveDate.plusDays(3)).atTime(11, 00).atZone(TimeZone.UTC));
    DateTimeWithZone maturityDate = new DateTimeWithZone(curveDate.plus(strip.getMaturity().getPeriod()).atTime(11, 00).atZone(TimeZone.UTC));
    ConventionBundle convention = _conventionBundleSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, spec.getCurrency().getISOCode() + "_SWAP"));
    String counterparty = "";
    Identifier region = Identifier.of(InMemoryRegionMaster.ISO_COUNTRY_2, "US");
    // REVIEW: jim 25-Aug-2010 -- change this to pass the identifier from the convention straight in instead of resolving and using a unique ID
    ConventionBundle floatRateConvention = source.getConventionBundle(convention.getSwapFloatingLegInitialRate());
    Double initialRate = null; 
    for (Identifier identifier :  floatRateConvention.getIdentifiers()) {
      if (marketValues.containsKey(identifier)) {
        initialRate = marketValues.get(identifier); // get the initial rate.
        break;
      }
    }
    if (initialRate == null) {
      throw new OpenGammaRuntimeException("Could not get initial rate");
    }
    double spread = 0;
    double fixedRate = rate;
    // REVIEW: jim 25-Aug-2010 -- we need to change the swap to take settlement days.
    SwapSecurity swap =  new SwapSecurity(tradeDate, 
                                          effectiveDate, 
                                          maturityDate,
                                          counterparty, 
                                            new FloatingInterestRateLeg(
                                                convention.getSwapFloatingLegDayCount(),
                                                convention.getSwapFloatingLegFrequency(),
                                                region,
                                                convention.getSwapFloatingLegBusinessDayConvention(),
                                                new InterestRateNotional(spec.getCurrency(), 1),
                                                floatRateConvention.getUniqueIdentifier(), 
                                                initialRate, 
                                                spread),
                                            new FixedInterestRateLeg(
                                                convention.getSwapFixedLegDayCount(), 
                                                convention.getSwapFixedLegFrequency(),
                                                region, 
                                                convention.getSwapFixedLegBusinessDayConvention(),
                                                new InterestRateNotional(spec.getCurrency(), 1),
                                                fixedRate)
                                          );
    swap.setIdentifiers(IdentifierBundle.of(swapIdentifier));
    return swap;
  }
  
  private TimeZone ensureZone(TimeZone zone) {
    if (zone != null) {
      return zone;
    } else {
      return TimeZone.UTC;
    }
  }
}