/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                         reserved.
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

package jtest.util.org.onap.ccsdk.sli.adaptors.ra;

import java.sql.ResultSetMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

public class TestTable {

	private String tableName;
	private String[] columnList;
	private String idName;

	private String insertSql;

	private JdbcTemplate jdbcTemplate;
	
	private static final Logger log = LoggerFactory.getLogger(TestTable.class);

	public TestTable(JdbcTemplate jdbcTemplate, String tableName, String idName, String... columnList) {
		this.jdbcTemplate = jdbcTemplate;
		this.tableName = tableName;
		this.idName = idName;
		this.columnList = columnList;
		createInsertSql();
	}
	
	public TestTable(JdbcTemplate jdbcTemplate, String tableName) {
		this.jdbcTemplate = jdbcTemplate;
		this.tableName = tableName;
	}

	private void createInsertSql() {
		StringBuilder ss = new StringBuilder();
		ss.append("INSERT INTO ").append(tableName).append(" (");
		for (String s : columnList)
			ss.append(s).append(", ");
		ss.setLength(ss.length() - 2);
		ss.append(") VALUES (");
		for (int i = 0; i < columnList.length; i++)
			ss.append("?, ");
		ss.setLength(ss.length() - 2);
		ss.append(")");
		insertSql = ss.toString();
	}

	public void add(Object... values) {
		jdbcTemplate.update(insertSql, values);
	}
	
	public void update(String updateSql, Object... values) {
		jdbcTemplate.update(updateSql, values);
	}

	public long getLastId() {
		return jdbcTemplate.queryForObject("SELECT max(" + idName + ") FROM " + tableName, Long.class);
	}

	public Long getId(String where) {
		String selectSql = "SELECT " + idName + " FROM " + tableName + " WHERE " + where;
		SqlRowSet rs = jdbcTemplate.queryForRowSet(selectSql);
		if (rs.first())
			return rs.getLong(idName);
		return null;
	}
	
	public Object getColumn(String columnName, String where) {
		String selectSql = "SELECT " + columnName + " FROM " + tableName + " WHERE " + where;
		SqlRowSet rs = jdbcTemplate.queryForRowSet(selectSql);
		if (rs.first())
			return rs.getObject(columnName);
		return null;
	}

	public boolean exists(String where) {
		String selectSql = "SELECT * FROM " + tableName + " WHERE " + where;
		SqlRowSet rs = jdbcTemplate.queryForRowSet(selectSql);
		return rs.first();
	}

	public void delete(String where) {
		jdbcTemplate.update("DELETE FROM " + tableName + " WHERE " + where);
	}
	
	public void print() {
		
		jdbcTemplate.query("SELECT * FROM " + tableName, 
								(rs, rowNum) -> {
									String row = "Table Data for " + tableName +"\n";
									String col = "";
									
									final ResultSetMetaData meta = rs.getMetaData();
									final int columnCount = meta.getColumnCount();
									
									do {
										col = "";
								
										for (int column = 1; column <= columnCount; ++column) {
											Object obj = rs.getObject(column);
											if(!rs.wasNull()) {
												col = col + obj + ",";
											} 
										}
										col = col.trim().length() == 0 ? "" : (col.trim().substring(0, col.trim().length() - 1));
										row = row + col + "\n";
									} while (rs.next());
									
									return row;
								}).forEach(row -> {
														log.info(row);
													});
	}
}