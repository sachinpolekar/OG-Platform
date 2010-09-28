/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.master.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.financial.security.master.SecurityDocument;
import com.opengamma.financial.security.master.SecuritySearchHistoricRequest;
import com.opengamma.financial.security.master.SecuritySearchHistoricResult;
import com.opengamma.financial.security.master.SecuritySearchRequest;
import com.opengamma.financial.security.master.SecuritySearchResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.DbDateUtils;
import com.opengamma.util.db.DbMapSqlParameterSource;
import com.opengamma.util.db.Paging;

/**
 * Security master worker to get the security.
 */
public class QuerySecurityDbSecurityMasterWorker extends DbSecurityMasterWorker {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(QuerySecurityDbSecurityMasterWorker.class);
  /**
   * SQL select.
   */
  protected static final String SELECT =
      "SELECT " +
        "s.id AS security_id, " +
        "s.oid AS security_oid, " +
        "s.ver_from_instant AS ver_from_instant, " +
        "s.ver_to_instant AS ver_to_instant, " +
        "s.corr_from_instant AS corr_from_instant, " +
        "s.corr_to_instant AS corr_to_instant, " +
        "s.name AS name, " +
        "s.sec_type AS sec_type, " +
        "i.key_scheme AS key_scheme, " +
        "i.key_value AS key_value ";
  /**
   * SQL from.
   */
  protected static final String FROM =
      "FROM sec_security s LEFT JOIN sec_security2idkey si ON (si.security_id = s.id) LEFT JOIN sec_idkey i ON (si.idkey_id = i.id) ";

  /**
   * Creates an instance.
   */
  public QuerySecurityDbSecurityMasterWorker() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  protected SecurityDocument get(final UniqueIdentifier uid) {
    if (uid.isVersioned()) {
      return getById(uid);
    } else {
      return getByLatest(uid);
    }
  }

  /**
   * Gets a security by searching for the latest version of an object identifier.
   * @param uid  the unique identifier
   * @return the security document, null if not found
   */
  protected SecurityDocument getByLatest(final UniqueIdentifier uid) {
    s_logger.debug("getSecurityByLatest: {}", uid);
    final Instant now = Instant.now(getTimeSource());
    final SecuritySearchHistoricRequest request = new SecuritySearchHistoricRequest(uid, now, now);
    request.setFullDetail(true);
    final SecuritySearchHistoricResult result = getMaster().searchHistoric(request);
    if (result.getDocuments().size() != 1) {
      throw new DataNotFoundException("Security not found: " + uid);
    }
    return result.getFirstDocument();
  }

  /**
   * Gets a security by identifier.
   * @param uid  the unique identifier
   * @return the security document, null if not found
   */
  @SuppressWarnings("unchecked")
  protected SecurityDocument getById(final UniqueIdentifier uid) {
    s_logger.debug("getSecurityById {}", uid);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("security_id", extractRowId(uid));
    
    final SecurityDocumentExtractor extractor = new SecurityDocumentExtractor();
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final List<SecurityDocument> docs = (List<SecurityDocument>) namedJdbc.query(sqlGetSecurityById(), args, extractor);
    if (docs.isEmpty()) {
      throw new DataNotFoundException("Security not found: " + uid);
    }
    loadDetail(docs);
    return docs.get(0);
  }

  /**
   * Gets the SQL for getting a security by unique row identifier.
   * @return the SQL, not null
   */
  protected String sqlGetSecurityById() {
    return SELECT + FROM + "WHERE s.id = :security_id ";
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  protected SecuritySearchResult search(SecuritySearchRequest request) {
    s_logger.debug("searchSecurities: {}", request);
    final SecuritySearchResult result = new SecuritySearchResult();
    if (IdentifierBundle.EMPTY.equals(request.getIdentityKey())) {
      return result;  // containsAny with empty bundle always returns nothing
    }
    final Instant now = Instant.now(getTimeSource());
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addTimestamp("version_as_of_instant", Objects.firstNonNull(request.getVersionAsOfInstant(), now))
      .addTimestamp("corrected_to_instant", Objects.firstNonNull(request.getCorrectedToInstant(), now))
      .addValueNullIgnored("name", getDbHelper().sqlWildcardAdjustValue(request.getName()))
      .addValueNullIgnored("sec_type", request.getSecurityType());
    if (request.getIdentityKey() != null) {
      int i = 0;
      for (Identifier idKey : request.getIdentityKey().getIdentifiers()) {
        args.addValue("key_scheme" + i, idKey.getScheme().getName());
        args.addValue("key_value" + i, idKey.getValue());
        i++;
      }
    }
    final String[] sql = sqlSearchSecurities(request);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final int count = namedJdbc.queryForInt(sql[1], args);
    result.setPaging(new Paging(request.getPagingRequest(), count));
    if (count > 0) {
      final SecurityDocumentExtractor extractor = new SecurityDocumentExtractor();
      result.getDocuments().addAll((List<SecurityDocument>) namedJdbc.query(sql[0], args, extractor));
      if (request.isFullDetail()) {
        loadDetail(result.getDocuments());
      }
    }
    return result;
  }

  /**
   * Gets the SQL to search for securities.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchSecurities(final SecuritySearchRequest request) {
    String where = "WHERE ver_from_instant <= :version_as_of_instant AND ver_to_instant > :version_as_of_instant " +
                "AND corr_from_instant <= :corrected_to_instant AND corr_to_instant > :corrected_to_instant ";
    if (request.getName() != null) {
      where += getDbHelper().sqlWildcardQuery("AND UPPER(name) ", "UPPER(:name)", request.getName());
    }
    if (request.getSecurityType() != null) {
      where += "AND sec_type = :sec_type ";
    }
    if (request.getIdentityKey() != null) {
      where += "AND id IN (SELECT DISTINCT security_id FROM sec_security2idkey WHERE idkey_id IN (" + sqlSelectIdKeys(request) + ")) ";
    }
    String selectFromWhereInner = "SELECT id FROM sec_security " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY id ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE s.id IN (" + inner + ") ORDER BY s.id";
    String count = "SELECT COUNT(*) FROM sec_security " + where;
    return new String[] {search, count};
  }

  /**
   * Gets the SQL to search for idkeys.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String sqlSelectIdKeys(final SecuritySearchRequest request) {
    String select = "SELECT id FROM sec_idkey ";
    for (int i = 0; i < request.getIdentityKey().getIdentifiers().size(); i++) {
      select += (i == 0 ? "WHERE " : "OR ");
      select += "(key_scheme = :key_scheme" + i + " AND key_value = :key_value" + i + ") ";
    }
    return select;
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  protected SecuritySearchHistoricResult searchHistoric(final SecuritySearchHistoricRequest request) {
    s_logger.debug("searchSecurityHistoric: {}", request);
    final DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue("security_oid", extractOid(request.getSecurityId()))
      .addTimestampNullIgnored("versions_from_instant", request.getVersionsFromInstant())
      .addTimestampNullIgnored("versions_to_instant", request.getVersionsToInstant())
      .addTimestampNullIgnored("corrections_from_instant", request.getCorrectionsFromInstant())
      .addTimestampNullIgnored("corrections_to_instant", request.getCorrectionsToInstant());
    final String[] sql = sqlSearchSecurityHistoric(request);
    final NamedParameterJdbcOperations namedJdbc = getJdbcTemplate().getNamedParameterJdbcOperations();
    final int count = namedJdbc.queryForInt(sql[1], args);
    final SecuritySearchHistoricResult result = new SecuritySearchHistoricResult();
    result.setPaging(new Paging(request.getPagingRequest(), count));
    if (count > 0) {
      final SecurityDocumentExtractor extractor = new SecurityDocumentExtractor();
      result.getDocuments().addAll((List<SecurityDocument>) namedJdbc.query(sql[0], args, extractor));
      if (request.isFullDetail()) {
        loadDetail(result.getDocuments());
      }
    }
    return result;
  }

  /**
   * Gets the SQL for searching the history of a security.
   * @param request  the request, not null
   * @return the SQL search and count, not null
   */
  protected String[] sqlSearchSecurityHistoric(final SecuritySearchHistoricRequest request) {
    String where = "WHERE oid = :security_oid ";
    if (request.getVersionsFromInstant() != null && request.getVersionsFromInstant().equals(request.getVersionsToInstant())) {
      where += "AND ver_from_instant <= :versions_from_instant AND ver_to_instant > :versions_from_instant ";
    } else {
      if (request.getVersionsFromInstant() != null) {
        where += "AND ((ver_from_instant <= :versions_from_instant AND ver_to_instant > :versions_from_instant) " +
                            "OR ver_from_instant >= :versions_from_instant) ";
      }
      if (request.getVersionsToInstant() != null) {
        where += "AND ((ver_from_instant <= :versions_to_instant AND ver_to_instant > :versions_to_instant) " +
                            "OR ver_to_instant < :versions_to_instant) ";
      }
    }
    if (request.getCorrectionsFromInstant() != null && request.getCorrectionsFromInstant().equals(request.getCorrectionsToInstant())) {
      where += "AND corr_from_instant <= :corrections_from_instant AND corr_to_instant > :corrections_from_instant ";
    } else {
      if (request.getCorrectionsFromInstant() != null) {
        where += "AND ((corr_from_instant <= :corrections_from_instant AND corr_to_instant > :corrections_from_instant) " +
                            "OR corr_from_instant >= :corrections_from_instant) ";
      }
      if (request.getCorrectionsToInstant() != null) {
        where += "AND ((corr_from_instant <= :corrections_to_instant AND ver_to_instant > :corrections_to_instant) " +
                            "OR corr_to_instant < :corrections_to_instant) ";
      }
    }
    String selectFromWhereInner = "SELECT id FROM sec_security " + where;
    String inner = getDbHelper().sqlApplyPaging(selectFromWhereInner, "ORDER BY ver_from_instant DESC, corr_from_instant DESC ", request.getPagingRequest());
    String search = SELECT + FROM + "WHERE s.id IN (" + inner + ") ORDER BY s.ver_from_instant DESC, s.corr_from_instant DESC";
    String count = "SELECT COUNT(*) FROM sec_security " + where;
    return new String[] {search, count};
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the detail of the security for the document.
   * @param docs  the documents to load detail for, not null
   */
  protected void loadDetail(final List<SecurityDocument> docs) {
    SecurityMasterDetailProvider detailProvider = getMaster().getWorkers().getDetailProvider();
    if (detailProvider != null) {
      for (SecurityDocument doc : docs) {
        doc.setSecurity(detailProvider.loadSecurityDetail(doc.getSecurity()));
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Mapper from SQL rows to a SecurityDocument.
   */
  protected final class SecurityDocumentExtractor implements ResultSetExtractor {
    private long _lastSecurityId = -1;
    private DefaultSecurity _security;
    private List<SecurityDocument> _documents = new ArrayList<SecurityDocument>();
    private Map<UniqueIdentifier, UniqueIdentifier> _deduplicate = Maps.newHashMap();

    @Override
    public Object extractData(final ResultSet rs) throws SQLException, DataAccessException {
      while (rs.next()) {
        final long securityId = rs.getLong("SECURITY_ID");
        if (_lastSecurityId != securityId) {
          _lastSecurityId = securityId;
          buildSecurity(rs, securityId);
        }
        final String idScheme = rs.getString("KEY_SCHEME");
        final String idValue = rs.getString("KEY_VALUE");
        if (idScheme != null && idValue != null) {
          Identifier id = Identifier.of(idScheme, idValue);
          _security.setIdentifiers(_security.getIdentifiers().withIdentifier(id));
        }
      }
      return _documents;
    }

    private void buildSecurity(final ResultSet rs, final long securityId) throws SQLException {
      final long securityOid = rs.getLong("SECURITY_OID");
      final Timestamp versionFrom = rs.getTimestamp("VER_FROM_INSTANT");
      final Timestamp versionTo = rs.getTimestamp("VER_TO_INSTANT");
      final Timestamp correctionFrom = rs.getTimestamp("CORR_FROM_INSTANT");
      final Timestamp correctionTo = rs.getTimestamp("CORR_TO_INSTANT");
      final String name = rs.getString("NAME");
      final String type = rs.getString("SEC_TYPE");
      UniqueIdentifier uid = createUniqueIdentifier(securityOid, securityId, _deduplicate);
      _security = new DefaultSecurity(uid, name, type, IdentifierBundle.EMPTY);
      SecurityDocument doc = new SecurityDocument(_security);
      doc.setVersionFromInstant(DbDateUtils.fromSqlTimestamp(versionFrom));
      doc.setVersionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(versionTo));
      doc.setCorrectionFromInstant(DbDateUtils.fromSqlTimestamp(correctionFrom));
      doc.setCorrectionToInstant(DbDateUtils.fromSqlTimestampNullFarFuture(correctionTo));
      doc.setSecurityId(uid);
      _documents.add(doc);
    }
  }

}