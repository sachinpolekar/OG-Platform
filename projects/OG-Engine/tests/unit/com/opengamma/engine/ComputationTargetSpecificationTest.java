/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import java.math.BigDecimal;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.core.security.Security;
import com.opengamma.engine.test.MockSecurity;
import com.opengamma.id.Identifiable;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test ComputationTargetSpecification.
 */
@Test
public class ComputationTargetSpecificationTest {

  private static final Identifier ID = Identifier.of("Test", "0");
  private static final UniqueIdentifier UID = UniqueIdentifier.of("Test", "1");
  private static final UniqueIdentifier UID2 = UniqueIdentifier.of("Test", "2");
  private static final Identifiable IDENTIFIABLE = new Identifiable() {
    @Override
    public Identifier getIdentityKey() {
      return Identifier.of("Test", "3");
    }
  };
  private static final UniqueIdentifiable UNIQUE_IDENTIFIABLE = new UniqueIdentifiable() {
    @Override
    public UniqueIdentifier getUniqueId() {
      return UniqueIdentifier.of("Test", "4");
    }
  };
  private static final Portfolio PORTFOLIO = new PortfolioImpl(UID, "Name");
  private static final PortfolioNodeImpl NODE = new PortfolioNodeImpl(UID, "Name");
  private static final Position POSITION = new PositionImpl(UID, new BigDecimal(1), IdentifierBundle.EMPTY);
  private static final Security SECURITY = new MockSecurity(UID, "", "", IdentifierBundle.EMPTY);

  public void test_constructor_Object_Portfolio() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(PORTFOLIO);
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, test.getType());
    assertEquals(PORTFOLIO.getUniqueId(), test.getUniqueId());
  }

  public void test_constructor_Object_Node() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(NODE);
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, test.getType());
    assertEquals(NODE.getUniqueId(), test.getUniqueId());
  }

  public void test_constructor_Object_Position() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(POSITION);
    assertEquals(ComputationTargetType.POSITION, test.getType());
    assertEquals(POSITION.getUniqueId(), test.getUniqueId());
  }

  public void test_constructor_Object_Security() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(SECURITY);
    assertEquals(ComputationTargetType.SECURITY, test.getType());
    assertEquals(SECURITY.getUniqueId(), test.getUniqueId());
  }

  public void test_constructor_Object_null() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(null);
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals(null, test.getUniqueId());
  }
  
  public void test_constructor_Object_ID() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(ID);
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals(UniqueIdentifier.of("Test", "0"), test.getUniqueId());
  }

  public void test_constructor_Object_Identifiable() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(IDENTIFIABLE);
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals(UniqueIdentifier.of("Test", "3"), test.getUniqueId());
  }

  public void test_constructor_Object_UniqueIdentifiable() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(UNIQUE_IDENTIFIABLE);
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals(UNIQUE_IDENTIFIABLE.getUniqueId(), test.getUniqueId());
  }

  //-------------------------------------------------------------------------
  public void test_constructor_Type_UniqueIdentifier_ok() {
    new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UID);
    new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    new ComputationTargetSpecification(ComputationTargetType.SECURITY, UID);
    new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, (UniqueIdentifier) null);
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_constructor_Type_UniqueIdentifier_nullType() {
    new ComputationTargetSpecification(null, UID);
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_constructor_Type_UniqueIdentifier_nullId() {
    new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, (UniqueIdentifier) null);
  }

  //-------------------------------------------------------------------------
  public void test_getters_PortfolioNode() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UID);
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, test.getType());
    assertEquals(UID.getScheme(), test.getIdentifier().getScheme().getName());
    assertEquals(UID.getValue(), test.getIdentifier().getValue());
  }

  //-------------------------------------------------------------------------
  public void test_toSpecification() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    assertEquals(true, test.toString().contains("POSITION"));
    assertEquals(true, test.toString().contains(UID.toString()));
  }

  //-------------------------------------------------------------------------
  public void test_equals_similar() {
    ComputationTargetSpecification a1 = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    ComputationTargetSpecification a2 = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  public void test_equals_different() {
    ComputationTargetSpecification a = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    ComputationTargetSpecification b = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID2);
    ComputationTargetSpecification c = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UID2);
    
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, a.equals(c));
    
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
    assertEquals(false, b.equals(c));
    
    assertEquals(false, c.equals(a));
    assertEquals(false, c.equals(b));
    assertEquals(true, c.equals(c));
  }

  public void test_equals_other() {
    ComputationTargetSpecification a = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    assertEquals(false, a.equals(null));
    assertEquals(false, a.equals("Rubbish"));
  }

  public void test_hashCode() {
    ComputationTargetSpecification a = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    ComputationTargetSpecification b = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    assertEquals(true, a.equals(b));
  }

}
