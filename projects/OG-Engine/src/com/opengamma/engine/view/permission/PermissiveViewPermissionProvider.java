/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.permission;

import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.livedata.UserPrincipal;

/**
 * Implementation of {@link ViewPermissionProvider} that always responds positively.
 */
public class PermissiveViewPermissionProvider implements ViewPermissionProvider {

  @Override
  public boolean canAccessCompiledViewDefinition(UserPrincipal user, CompiledViewDefinition viewEvaluationModel) {
    return true;
  }

  @Override
  public boolean canAccessComputationResults(UserPrincipal user, CompiledViewDefinition viewEvaluationModel) {
    return true;
  }

}