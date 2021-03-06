/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.region;

import java.util.Collections;
import java.util.Comparator;

/**
 * Region comparator based on the region classification, then name.
 */
public final class RegionComparator implements Comparator<Region> {

  /**
   * A singleton region comparator sorting in ascending order.
   */
  public static final Comparator<Region> ASC = new RegionComparator();
  /**
   * A singleton region comparator sorting in descending order.
   */
  public static final Comparator<Region> DESC = Collections.reverseOrder(ASC);

  /**
   * Restrictive constructor.
   */
  private RegionComparator() {
  }

  //-------------------------------------------------------------------------
  @Override
  public int compare(Region r1, Region r2) {
    int type = r1.getClassification().compareTo(r2.getClassification());
    if (type != 0) {
      return type;
    }
    return r1.getName().compareTo(r2.getName());
  }

}
