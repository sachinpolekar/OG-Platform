/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;

/**
 * The platform (in particular the Spring configuration files) makes use of certain system properties which are assumed
 * to exist. These allow the platform to be configured differently depending on the OS (affecting external file
 * references) and database type.
 * <p>
 * This is a helper for anything that launches the platform in any way, to ensure that the necessary properties have
 * been set.
 */
public final class PlatformConfigUtils {
  
  private static final Logger s_logger = LoggerFactory.getLogger(PlatformConfigUtils.class);
  private static final String PROPERTY_PREFIX = "opengamma.platform.";
   
  /**
   * Enumerates the valid OS types
   */
  public static enum OsType {
    /**
     * Windows
     */
    WIN,
    /**
     * POSIX-compliant
     */
    POSIX;
    
    /**
     * System property used to specify the OS type
     */
    public static final String PROPERTY_NAME = PROPERTY_PREFIX + "os";
  }
  
  /**
   * Enumerates the valid database types
   */
  public static enum RunMode {    
    /**
     * Standalone
     */
    STANDALONE,
    /**
     * Shared dev
     */
    SHAREDDEV;
    
    /**
     * System property used to specify the database type
     */
    public static final String PROPERTY_NAME = PROPERTY_PREFIX + "runmode";
  }
  
  /**
   * Hidden constructor
   */
  private PlatformConfigUtils() {
  }
   
  /**
   * Sets and/or validates the system properties generally necessary for the platform to run. 
   * 
   * @throws OpenGammaRuntimeException  if any property is missing or invalid and cannot be set automatically
   */
  public static void configureSystemProperties() {
    configureSystemProperties((String) null);
  }
  
  /**
   * Sets and/or validates the system properties generally necessary for the platform to run. 
   * 
   * @param runMode  the database type, possibly null. Allows this to be set from an external source such as the
   *                 command line. Only used if the database type has not been set directly.
   * @throws OpenGammaRuntimeException  if any property is missing or invalid and cannot be set automatically
   */
  public static void configureSystemProperties(RunMode runMode) {
    configureSystemProperties(toPropertyValue(runMode));
  }
  
  /**
   * Sets and/or validates the system properties generally necessary for the platform to run. 
   * 
   * @param runMode  the database type, possibly null. Allows this to be set from an external source such as the
   *                 command line. Only used if the database type has not been set directly.
   * @throws OpenGammaRuntimeException  if any property is missing or invalid and cannot be set automatically
   */
  public static void configureSystemProperties(String runMode) {
    if (System.getProperty(OsType.PROPERTY_NAME) == null) {
      String os = System.getProperty("os.name").toLowerCase();
      System.setProperty(OsType.PROPERTY_NAME, toPropertyValue(os.startsWith("win") ? OsType.WIN : OsType.POSIX));
    } else {
      validateProperty(OsType.class, OsType.PROPERTY_NAME);
    }
    
    if (System.getProperty(RunMode.PROPERTY_NAME) == null && runMode != null) {
      setFromEnumValue(RunMode.class, RunMode.PROPERTY_NAME, runMode);
    } else {
      validateProperty(RunMode.class, RunMode.PROPERTY_NAME);
    }
    
    logPlatformConfiguration();
  }
  
  private static <T extends Enum<T>> String toPropertyValue(Enum<T> enumValue) {
    return enumValue.name().toLowerCase();
  }
  
  private static <T extends Enum<T>> void setFromEnumValue(Class<T> enumType, String propertyName, String propertyValue) {
    validatePropertyValue(enumType, propertyName, propertyValue);
    System.setProperty(propertyName, propertyValue);
  }
  
  private static <T extends Enum<T>> void validateProperty(Class<T> enumType, String propertyName) {
    String propertyValue = System.getProperty(propertyName);
    validatePropertyValue(enumType, propertyName, propertyValue);
  }
  
  private static <T extends Enum<T>> void validatePropertyValue(Class<T> enumType, String propertyName, String propertyValue) {
    if (propertyValue == null) {
      throw new OpenGammaRuntimeException("System property '" + propertyName + "' is required, but has not been set");
    }
    try {
      Enum.valueOf(enumType, propertyValue.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new OpenGammaRuntimeException("The value '" + propertyValue + "' is not valid for system property '" + propertyName + "'");
    }
  }
  
  private static void logPlatformConfiguration() {
    if (!s_logger.isInfoEnabled()) {
      return;
    }
    StringBuilder sb = new StringBuilder("\nOpenGamma platform configuration: \n");
    for (String propertyName : System.getProperties().stringPropertyNames()) {
      if (propertyName.startsWith(PROPERTY_PREFIX)) {
        sb.append("\t").append(propertyName).append(" = ").append(System.getProperty(propertyName)).append("\n");
      }
    }
    s_logger.info(sb.toString());
  }
  
}