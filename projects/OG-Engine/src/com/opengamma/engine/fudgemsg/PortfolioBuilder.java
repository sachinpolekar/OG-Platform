/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.id.UniqueIdentifier;

/**
 * Fudge message builder for {@code Portfolio}.
 */
@GenericFudgeBuilderFor(Portfolio.class)
public class PortfolioBuilder implements FudgeBuilder<Portfolio> {

  private static final String FIELD_IDENTIFIER = "identifier";
  private static final String FIELD_NAME = "name";
  private static final String FIELD_ROOT = "root";

  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializationContext context, Portfolio portfolio) {
    final MutableFudgeMsg message = context.newMessage();
    context.addToMessage(message, FIELD_IDENTIFIER, null, portfolio.getUniqueId());
    message.add(FIELD_NAME, portfolio.getName());
    context.addToMessage(message, FIELD_ROOT, null, portfolio.getRootNode());
    return message;
  }

  @Override
  public Portfolio buildObject(FudgeDeserializationContext context, FudgeMsg message) {
    FudgeField idField = message.getByName(FIELD_IDENTIFIER);
    final UniqueIdentifier id = idField != null ? context.fieldValueToObject(UniqueIdentifier.class, idField) : null;
    final String name = message.getFieldValue(String.class, message.getByName(FIELD_NAME));
    final PortfolioNode node = context.fieldValueToObject(PortfolioNode.class, message.getByName(FIELD_ROOT));
    
    PortfolioImpl portfolio = new PortfolioImpl(name);
    if (id != null) {
      portfolio.setUniqueId(id);
    }
    portfolio.setRootNode((PortfolioNodeImpl) node);
    return portfolio;
  }

}
