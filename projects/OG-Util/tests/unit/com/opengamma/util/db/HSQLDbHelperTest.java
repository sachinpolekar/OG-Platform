/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * Test HSQLDbHelper.
 */
@Test
public class HSQLDbHelperTest extends DbHelperTest {

  public HSQLDbHelperTest() {
    _helper = HSQLDbHelper.INSTANCE;
  }

  //-------------------------------------------------------------------------
  public void test_getJDBCDriver() {
    assertEquals(org.hsqldb.jdbcDriver.class, _helper.getJDBCDriverClass());
  }

  public void test_getHibernateDialect() {
    assertEquals(org.hibernate.dialect.HSQLDialect.class, _helper.getHibernateDialect().getClass());
  }

  public void test_getName() {
    assertEquals("HSQL", _helper.getName());
  }

  //-------------------------------------------------------------------------
  public void test_sqlNextSequenceValueSelect() {
    assertEquals("CALL NEXT VALUE FOR MySeq", _helper.sqlNextSequenceValueSelect("MySeq"));
  }

}
