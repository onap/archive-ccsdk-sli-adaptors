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

package org.onap.ccsdk.sli.adaptors.util.db;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ConnectionWrap implements Connection {

    private Connection cc;

    public ConnectionWrap(Connection cc) {
        super();
        this.cc = cc;
    }

    public Connection getCon() {
        return cc;
    }

    public void realClose() throws SQLException {
        cc.close();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return cc.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return cc.isWrapperFor(iface);
    }

    @Override
    public Statement createStatement() throws SQLException {
        return cc.createStatement();
    }

    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return cc.prepareStatement(sql);
    }

    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        return cc.prepareCall(sql);
    }

    @Override
    public String nativeSQL(String sql) throws SQLException {
        return cc.nativeSQL(sql);
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        cc.setAutoCommit(autoCommit);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return cc.getAutoCommit();
    }

    @Override
    public void commit() throws SQLException {
        cc.commit();
    }

    @Override
    public void rollback() throws SQLException {
        cc.rollback();
    }

    @Override
    public void close() throws SQLException {
    }

    @Override
    public boolean isClosed() throws SQLException {
        return cc.isClosed();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return cc.getMetaData();
    }

    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        cc.setReadOnly(readOnly);
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return cc.isReadOnly();
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        cc.setCatalog(catalog);
    }

    @Override
    public String getCatalog() throws SQLException {
        return cc.getCatalog();
    }

    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        cc.setTransactionIsolation(level);
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return cc.getTransactionIsolation();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return cc.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        cc.clearWarnings();
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return cc.createStatement(resultSetType, resultSetConcurrency);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
            throws SQLException {
        return cc.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return cc.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return cc.getTypeMap();
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        cc.setTypeMap(map);
    }

    @Override
    public void setHoldability(int holdability) throws SQLException {
        cc.setHoldability(holdability);
    }

    @Override
    public int getHoldability() throws SQLException {
        return cc.getHoldability();
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return cc.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        return cc.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        cc.rollback(savepoint);
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        cc.releaseSavepoint(savepoint);
    }

    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
            throws SQLException {
        return cc.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(
            String sql,
            int resultSetType,
            int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return cc.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public CallableStatement prepareCall(
            String sql,
            int resultSetType,
            int resultSetConcurrency,
            int resultSetHoldability) throws SQLException {
        return cc.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return cc.prepareStatement(sql, autoGeneratedKeys);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return cc.prepareStatement(sql, columnIndexes);
    }

    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return cc.prepareStatement(sql, columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return cc.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return cc.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return cc.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return cc.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return cc.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        cc.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        cc.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return cc.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return cc.getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return cc.createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return cc.createStruct(typeName, attributes);
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        cc.setSchema(schema);
    }

    @Override
    public String getSchema() throws SQLException {
        return cc.getSchema();
    }

    @Override
    public void abort(Executor executor) throws SQLException {
        cc.abort(executor);
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        cc.setNetworkTimeout(executor, milliseconds);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return cc.getNetworkTimeout();
    }
}
