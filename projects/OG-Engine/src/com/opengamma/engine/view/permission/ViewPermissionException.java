/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Used to indicate that a user does not have a necessary permission.
 */
public class ViewPermissionException extends OpenGammaRuntimeException {
  
  public ViewPermissionException(String message) {
    super(message);
  }
  
  public ViewPermissionException(String message, Throwable cause) {
    super(message, cause);
  }
  
}