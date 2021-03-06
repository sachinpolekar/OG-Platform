/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.resolver;

import java.util.Collection;
import java.util.Map;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;

/**
 * Transforms a set of IDs into a unique ID.
 */
public interface IdResolver extends Resolver<IdentifierBundle, Identifier> { 
  
  /**
   * Transforms a set of IDs into a unique ID. For example, a Bloomberg ticker is 
   * transformed into a Bloomberg unique ID.
   * <p>
   * If the input is already a unique ID, it is returned as it is: no validation is performed.
   * 
   * @param ids input IDs
   * @return the unique ID. Null if it was not found.
   */
  Identifier resolve(IdentifierBundle ids);
  
  /**
   * Same as calling {@link #resolve(IdentifierBundle)} for each ID bundle
   * individually, but since it works in bulk, may be more efficient. 
   * 
   * @param ids input IDs
   * @return map from requested identifier bundle to result. 
   * For each input identifier bundle, there must be an entry in the map.
   * However, the value of the entry can be null if no unique ID was 
   * found for the given identifier bundle.
   */
  Map<IdentifierBundle, Identifier> resolve(Collection<IdentifierBundle> ids);
  
  

}
