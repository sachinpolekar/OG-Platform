/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.livedata.UserPrincipal;

public class TestComputationResultListener extends AbstractTestResultListener<ViewComputationResultModel>
    implements ComputationResultListener {

  @Override
  public void computationResultAvailable(ViewComputationResultModel resultModel) {
    resultReceived(resultModel);
  }

  @Override
  public UserPrincipal getUser() {
    return UserPrincipal.getLocalUser();
  }
  
}