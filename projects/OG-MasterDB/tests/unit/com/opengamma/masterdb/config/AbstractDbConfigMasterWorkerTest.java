/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.config;

import static com.opengamma.util.db.DbDateUtils.MAX_SQL_TIMESTAMP;
import static com.opengamma.util.db.DbDateUtils.toSqlTimestamp;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.sql.Types;
import java.util.TimeZone;

import javax.time.Instant;
import javax.time.TimeSource;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.masterdb.DbMasterTestUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.DBTest;

/**
 * Base tests for DbConfigMasterWorker via DbConfigMaster.
 */
public abstract class AbstractDbConfigMasterWorkerTest extends DBTest {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractDbConfigMasterWorkerTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  protected DbConfigMaster _cfgMaster;
  protected Instant _version1aInstant;
  protected Instant _version1bInstant;
  protected Instant _version1cInstant;
  protected Instant _version2Instant;
  protected int _totalConfigs;
  protected int _totalIdentifiers;
  protected int _totalIdentifierBundles;
 

  public AbstractDbConfigMasterWorkerTest(String databaseType, String databaseVersion) {
    super(databaseType, databaseVersion);
    s_logger.info("running testcases for {}", databaseType);
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }

  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    ConfigurableApplicationContext context = DbMasterTestUtils.getContext(getDatabaseType());
    _cfgMaster = (DbConfigMaster) context.getBean(getDatabaseType() + "DbConfigMaster");
  
    Instant now = Instant.now();
    _cfgMaster.setTimeSource(TimeSource.fixed(now));
    _version1aInstant = now.minusSeconds(102);
    _version1bInstant = now.minusSeconds(101);
    _version1cInstant = now.minusSeconds(100);
    _version2Instant = now.minusSeconds(50);
    addIdentifiers();
    addIdentifierBundles();
    _totalConfigs = 6;
  }

  private void addIdentifiers() {
    FudgeMsgEnvelope env = s_fudgeContext.toFudgeMsg(Identifier.of("A", "B"));
    byte[] bytes = s_fudgeContext.toByteArray(env.getMessage());
    String cls = Identifier.class.getName();
    LobHandler lobHandler = new DefaultLobHandler();
    final SimpleJdbcTemplate template = _cfgMaster.getDbSource().getJdbcTemplate();
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        101, 101, toSqlTimestamp(_version1aInstant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1aInstant), MAX_SQL_TIMESTAMP, "TestConfig101", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        102, 102, toSqlTimestamp(_version1bInstant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1bInstant), MAX_SQL_TIMESTAMP, "TestConfig102", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        201, 201, toSqlTimestamp(_version1cInstant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1cInstant), MAX_SQL_TIMESTAMP, "TestConfig201", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        202, 201, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, "TestConfig202", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    _totalIdentifiers = 3;
  }
  
  private void addIdentifierBundles() {
    FudgeMsgEnvelope env = s_fudgeContext.toFudgeMsg(IdentifierBundle.of(Identifier.of("C", "D"), Identifier.of("E", "F")));
    byte[] bytes = s_fudgeContext.toByteArray(env.getMessage());
    String cls = IdentifierBundle.class.getName();
    LobHandler lobHandler = new DefaultLobHandler();
    final SimpleJdbcTemplate template = _cfgMaster.getDbSource().getJdbcTemplate();
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        301, 301, toSqlTimestamp(_version1aInstant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1aInstant), MAX_SQL_TIMESTAMP, "TestConfig301", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        302, 302, toSqlTimestamp(_version1bInstant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version1bInstant), MAX_SQL_TIMESTAMP, "TestConfig302", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        401, 401, toSqlTimestamp(_version1cInstant), toSqlTimestamp(_version2Instant), toSqlTimestamp(_version1cInstant), MAX_SQL_TIMESTAMP, "TestConfig401", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    template.update("INSERT INTO cfg_config VALUES (?,?,?,?,?, ?,?,?,?)",
        402, 401, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, toSqlTimestamp(_version2Instant), MAX_SQL_TIMESTAMP, "TestConfig402", cls,
        new SqlParameterValue(Types.BLOB, new SqlLobValue(bytes, lobHandler)));
    _totalIdentifierBundles = 3;
  }
  
  @AfterMethod
  public void tearDown() throws Exception {
    _cfgMaster = null;
    super.tearDown();
  }

  //-------------------------------------------------------------------------
  protected void assert101(final ConfigDocument<Identifier> test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "101", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1aInstant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1aInstant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals("TestConfig101", test.getName());
    assertEquals(Identifier.of("A", "B"), test.getValue());
  }

  protected void assert102(final ConfigDocument<Identifier> test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "102", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1bInstant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version1bInstant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals("TestConfig102", test.getName());
    assertEquals(Identifier.of("A", "B"), test.getValue());
  }

  protected void assert201(final ConfigDocument<Identifier> test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "201", "0");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version1cInstant, test.getVersionFromInstant());
    assertEquals(_version2Instant, test.getVersionToInstant());
    assertEquals(_version1cInstant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals("TestConfig201", test.getName());
    assertEquals(Identifier.of("A", "B"), test.getValue());
  }

  protected void assert202(final ConfigDocument<Identifier> test) {
    UniqueIdentifier uid = UniqueIdentifier.of("DbCfg", "201", "1");
    assertNotNull(test);
    assertEquals(uid, test.getUniqueId());
    assertEquals(_version2Instant, test.getVersionFromInstant());
    assertEquals(null, test.getVersionToInstant());
    assertEquals(_version2Instant, test.getCorrectionFromInstant());
    assertEquals(null, test.getCorrectionToInstant());
    assertEquals(Identifier.of("A", "B"), test.getValue());
  }

}
