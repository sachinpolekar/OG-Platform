/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.transport.EndPointDescriptionProvider;

/**
 * An implementation of {@code EndPointDescriptionProvider} that operates over a REST call.
 */
public class RemoteEndPointDescriptionProvider implements EndPointDescriptionProvider {

  private RestClient _client;
  private RestTarget _target;

  public void setRestClient(final RestClient client) {
    _client = client;
  }

  public RestClient getRestClient() {
    return _client;
  }

  public void setRestTarget(final RestTarget target) {
    _target = target;
  }

  public RestTarget getRestTarget() {
    return _target;
  }

  public void setUri(final String uri) {
    setRestTarget(new RestTarget(uri));
  }

  @Override
  public FudgeMsg getEndPointDescription(final FudgeContext fudgeContext) {
    RestClient client = getRestClient();
    if (client == null) {
      client = RestClient.getInstance(fudgeContext, null);
    }
    return client.getMsg(getRestTarget());
  }

}
