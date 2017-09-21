/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.resource.sql;

import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.core.dblib.DBResourceManager;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

public class SqlResource implements SvcLogicResource, SvcLogicJavaPlugin {

	private static final Logger LOG = LoggerFactory.getLogger(SqlResource.class);

	private static final String DBLIB_SERVICE = "org.onap.ccsdk.sli.adaptors.resource.dblib.DBResourceManager";

	private static String CRYPT_KEY = "";

	public SqlResource() {
	}

	// For sql-resource, is-available is the same as exists
	@Override
	public QueryStatus isAvailable(String resource, String key, String prefix, SvcLogicContext ctx)
			throws SvcLogicException {

		return (exists(resource, key, prefix, ctx));

	}

	@Override
	public QueryStatus exists(String resource, String key, String prefix, SvcLogicContext ctx)
			throws SvcLogicException {

		DbLibService dblibSvc = getDbLibService();
		if (dblibSvc == null) {
			return (QueryStatus.FAILURE);
		}

		String theStmt = resolveCtxVars(key, ctx);

		try {
			CachedRowSet results = dblibSvc.getData(theStmt, null, null);

			if (!results.next()) {
				return (QueryStatus.NOT_FOUND);
			}

			int numRows = results.getInt(1);

			if (numRows > 0) {
				return (QueryStatus.SUCCESS);
			} else {
				return (QueryStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error("Caught SQL exception", e);
			return (QueryStatus.FAILURE);
		}
	}

	// @Override
	public QueryStatus query(String resource, boolean localOnly, String select, String key, String prefix,
			String orderBy, SvcLogicContext ctx) throws SvcLogicException {

		DbLibService dblibSvc = getDbLibService();

		if (dblibSvc == null) {
			return (QueryStatus.FAILURE);
		}

		String sqlQuery = resolveCtxVars(key, ctx);

		try {

			CachedRowSet results = dblibSvc.getData(sqlQuery, null, null);

			QueryStatus retval = QueryStatus.SUCCESS;

			if (!results.next()) {
				retval = QueryStatus.NOT_FOUND;
				LOG.debug("No data found");
			} else {
				saveCachedRowSetToCtx(results, ctx, prefix, dblibSvc);
			}
			return (retval);
		} catch (Exception e) {
			LOG.error("Caught SQL exception", e);
			return (QueryStatus.FAILURE);
		}
	}

	public void saveCachedRowSetToCtx(CachedRowSet results, SvcLogicContext ctx, String prefix, DbLibService dblibSvc)
			throws SQLException {
		if (ctx != null) {
			if ((prefix != null) && prefix.endsWith("[]")) {
				// Return an array.
				String pfx = prefix.substring(0, prefix.length() - 2);
				int idx = 0;
				do {
					ResultSetMetaData rsMeta = results.getMetaData();
					int numCols = rsMeta.getColumnCount();

					for (int i = 0; i < numCols; i++) {
						String colValue = null;
						String tableName = rsMeta.getTableName(i + 1);
						if (rsMeta.getColumnType(i + 1) == java.sql.Types.VARBINARY) {
							colValue = decryptColumn(tableName, rsMeta.getColumnName(i + 1), results.getBytes(i + 1),
									dblibSvc);
						} else {
							colValue = results.getString(i + 1);
						}
						LOG.debug("Setting " + pfx + "[" + idx + "]."
								+ rsMeta.getColumnLabel(i + 1).replaceAll("_", "-") + " = " + colValue);
						ctx.setAttribute(pfx + "[" + idx + "]." + rsMeta.getColumnLabel(i + 1).replaceAll("_", "-"),
								colValue);
					}
					idx++;
				} while (results.next());
				LOG.debug("Setting " + pfx + "_length = " + idx);
				ctx.setAttribute(pfx + "_length", "" + idx);
			} else {
				ResultSetMetaData rsMeta = results.getMetaData();
				int numCols = rsMeta.getColumnCount();

				for (int i = 0; i < numCols; i++) {
					String colValue = null;
					String tableName = rsMeta.getTableName(i + 1);
					if ("VARBINARY".equalsIgnoreCase(rsMeta.getColumnTypeName(i + 1))) {
						colValue = decryptColumn(tableName, rsMeta.getColumnName(i + 1), results.getBytes(i + 1),
								dblibSvc);
					} else {
						colValue = results.getString(i + 1);
					}
					if (prefix != null) {
						LOG.debug("Setting " + prefix + "." + rsMeta.getColumnLabel(i + 1).replaceAll("_", "-") + " = "
								+ colValue);
						ctx.setAttribute(prefix + "." + rsMeta.getColumnLabel(i + 1).replaceAll("_", "-"), colValue);
					} else {
						LOG.debug("Setting " + rsMeta.getColumnLabel(i + 1).replaceAll("_", "-") + " = " + colValue);
						ctx.setAttribute(rsMeta.getColumnLabel(i + 1).replaceAll("_", "-"), colValue);
					}
				}
			}
		}
	}

	// reserve is no-op
	@Override
	public QueryStatus reserve(String resource, String select, String key, String prefix, SvcLogicContext ctx)
			throws SvcLogicException {
		return (QueryStatus.SUCCESS);
	}

	// release is no-op
	@Override
	public QueryStatus release(String resource, String key, SvcLogicContext ctx) throws SvcLogicException {
		return (QueryStatus.SUCCESS);
	}

	private QueryStatus executeSqlWrite(String key, SvcLogicContext ctx) throws SvcLogicException {
		QueryStatus retval = QueryStatus.SUCCESS;

		DbLibService dblibSvc = getDbLibService();

		if (dblibSvc == null) {
			return (QueryStatus.FAILURE);
		}

		String sqlStmt = resolveCtxVars(key, ctx);

		LOG.debug("key = [" + key + "]; sqlStmt = [" + sqlStmt + "]");
		try {

			if (!dblibSvc.writeData(sqlStmt, null, null)) {
				retval = QueryStatus.FAILURE;
			}
		} catch (Exception e) {
			LOG.error("Caught SQL exception", e);
			retval = QueryStatus.FAILURE;
		}

		return (retval);

	}

	private String resolveCtxVars(String key, SvcLogicContext ctx) {
		if (key == null) {
			return (null);
		}

		if (key.startsWith("'") && key.endsWith("'")) {
			key = key.substring(1, key.length() - 1);
			LOG.debug("Stripped outer single quotes - key is now [" + key + "]");
		}

		String[] keyTerms = key.split("\\s+");

		StringBuffer sqlBuffer = new StringBuffer();

		for (int i = 0; i < keyTerms.length; i++) {
			sqlBuffer.append(resolveTerm(keyTerms[i], ctx));
			sqlBuffer.append(" ");
		}

		return (sqlBuffer.toString());
	}

	private String resolveTerm(String term, SvcLogicContext ctx) {
		if (term == null) {
			return (null);
		}

		LOG.trace("resolveTerm: term is " + term);

		if (term.startsWith("$") && (ctx != null)) {
			// Resolve any index variables.
			term = resolveCtxVariable(term.substring(1), ctx);
			// Escape single quote
			if (term != null) {
				term = term.replaceAll("'", "''");
			}
			return ("'" + term + "'");
		} else {
			return (term);
		}

	}

	private String resolveCtxVariable(String ctxVarName, SvcLogicContext ctx) {

		if (ctxVarName.indexOf('[') == -1) {
			// Ctx variable contains no arrays
			if ("CRYPT_KEY".equals(ctxVarName)) {
				// Handle crypt key as special case. If it's set as a context
				// variable, use it. Otherwise, use
				// configured crypt key.
				String cryptKey = ctx.getAttribute(ctxVarName);
				if ((cryptKey != null) && (cryptKey.length() > 0)) {
					return (cryptKey);
				} else {
					return (CRYPT_KEY);
				}
			}
			return (ctx.getAttribute(ctxVarName));
		}

		// Resolve any array references
		StringBuffer sbuff = new StringBuffer();
		String[] ctxVarParts = ctxVarName.split("\\[");
		sbuff.append(ctxVarParts[0]);
		for (int i = 1; i < ctxVarParts.length; i++) {
			if (ctxVarParts[i].startsWith("$")) {
				int endBracketLoc = ctxVarParts[i].indexOf("]");
				if (endBracketLoc == -1) {
					// Missing end bracket ... give up parsing
					LOG.warn("Variable reference " + ctxVarName + " seems to be missing a ']'");
					return (ctx.getAttribute(ctxVarName));
				}

				String idxVarName = ctxVarParts[i].substring(1, endBracketLoc);
				String remainder = ctxVarParts[i].substring(endBracketLoc);

				sbuff.append("[");
				sbuff.append(ctx.getAttribute(idxVarName));
				sbuff.append(remainder);

			} else {
				// Index is not a variable reference
				sbuff.append("[");
				sbuff.append(ctxVarParts[i]);
			}
		}

		return (ctx.getAttribute(sbuff.toString()));
	}

	@Override
	public QueryStatus save(String resource, boolean force, boolean localOnly, String key, Map<String, String> parms,
			String prefix, SvcLogicContext ctx) throws SvcLogicException {
		return (executeSqlWrite(key, ctx));
	}

	private DbLibService getDbLibService() {
		// Try to get dblib as an OSGI service
		DbLibService dblibSvc = null;
		BundleContext bctx = null;
		ServiceReference sref = null;

		Bundle bundle = FrameworkUtil.getBundle(SqlResource.class);

		if (bundle != null) {
			bctx = bundle.getBundleContext();
		}

		if (bctx != null) {
			sref = bctx.getServiceReference(DBLIB_SERVICE);
		}

		if (sref == null) {
			LOG.warn("Could not find service reference for DBLIB service (" + DBLIB_SERVICE + ")");
		} else {
			dblibSvc = (DbLibService) bctx.getService(sref);
			if (dblibSvc == null) {
				LOG.warn("Could not find service reference for DBLIB service (" + DBLIB_SERVICE + ")");
			}
		}

		if (dblibSvc == null) {
			// Must not be running in an OSGI container. See if you can load it
			// as a
			// a POJO then.
			try {
				dblibSvc = new DBResourceManager(System.getProperties());
			} catch (Exception e) {
				LOG.error("Caught exception trying to create dblib service", e);
			}

			if (dblibSvc == null) {
				LOG.warn("Could not create new DBResourceManager");
			}
		}

		return (dblibSvc);
	}

	@Override
	public QueryStatus notify(String resource, String action, String key, SvcLogicContext ctx)
			throws SvcLogicException {
		if (LOG.isDebugEnabled()) {
			LOG.debug("SqlResource.notify called with resource=" + resource + ", action=" + action);
		}
		return QueryStatus.SUCCESS;
	}

	@Override
	public QueryStatus delete(String resource, String key, SvcLogicContext ctx) throws SvcLogicException {
		return (executeSqlWrite(key, ctx));
	}

	public QueryStatus update(String resource, String key, Map<String, String> parms, String prefix,
			SvcLogicContext ctx) throws SvcLogicException {
		return (executeSqlWrite(key, ctx));
	}

    private String decryptColumn(String tableName, String colName, byte[] colValue, DbLibService dblibSvc) {
        String strValue = new String(colValue);

        if (StringUtils.isAsciiPrintable(strValue)) {

            // If printable, not encrypted
            return (strValue);
        } else {
            ResultSet results = null;
            try (Connection conn = ((DBResourceManager) dblibSvc).getConnection();
               PreparedStatement stmt = conn.prepareStatement("SELECT CAST(AES_DECRYPT(?, ?) AS CHAR(50)) FROM DUAL")) {

                stmt.setBytes(1, colValue);
                stmt.setString(2, getCryptKey());
                results = stmt.executeQuery();

                if ((results != null) && results.next()) {
                    strValue = results.getString(1);
                    LOG.debug("Decrypted value is " + strValue);
                } else {
                    LOG.warn("Cannot decrypt " + tableName + "." + colName);
                }
            } catch (Exception e) {
                if (results != null) {
                    try {
                        results.close();
                    } catch (SQLException ignored) {

                    }
                }
                LOG.error("Caught exception trying to decrypt " + tableName + "." + colName, e);
            }
        }
        return (strValue);
    }

	public static String getCryptKey() {
		return (CRYPT_KEY);
	}

	public static String setCryptKey(String key) {
		CRYPT_KEY = key;
		return (CRYPT_KEY);
	}

	public String parameterizedQuery(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
		DbLibService dblibSvc = getDbLibService();
		String prefix = parameters.get("prefix");
		String query = parameters.get("query");

		ArrayList<String> arguments = new ArrayList<String>();
		for (Entry<String, String> a : parameters.entrySet()) {
			if (a.getKey().startsWith("param")) {
				arguments.add(a.getValue());
			}
		}

		try {
			if (dblibSvc == null) {
				return mapQueryStatus(QueryStatus.FAILURE);
			}
			if (query.contains("count") || query.contains("COUNT")) {
				CachedRowSet results = dblibSvc.getData(query, arguments, null);

				if (!results.next()) {
					return mapQueryStatus(QueryStatus.FAILURE);
				}

				int numRows = results.getInt(1);
				ctx.setAttribute(prefix + ".count", String.valueOf(numRows));
				if (numRows > 0) {
					return "true";
				} else {
					return "false";
				}
			} else if (query.startsWith("select") || query.startsWith("SELECT")) {
				CachedRowSet results = dblibSvc.getData(query, arguments, null);
				if (!results.next()) {
					return mapQueryStatus(QueryStatus.NOT_FOUND);
				} else {
					saveCachedRowSetToCtx(results, ctx, prefix, dblibSvc);
				}
			} else {
				if (!dblibSvc.writeData(query, arguments, null)) {
					return mapQueryStatus(QueryStatus.FAILURE);
				}
			}
			return mapQueryStatus(QueryStatus.SUCCESS);
		} catch (SQLException e) {
			LOG.error("Caught SQL exception", e);
			return mapQueryStatus(QueryStatus.FAILURE);
		}
	}

	protected String mapQueryStatus(QueryStatus status) {
		String str = status.toString();
		str = str.toLowerCase();
		str = str.replaceAll("_", "-");
		return str;
	}
}
