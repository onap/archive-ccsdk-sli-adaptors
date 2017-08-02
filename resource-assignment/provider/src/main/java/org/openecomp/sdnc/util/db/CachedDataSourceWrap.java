/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All rights
 * reserved.
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

package org.openecomp.sdnc.util.db;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedDataSourceWrap implements DataSource {

    private static final Logger log = LoggerFactory.getLogger(CachedDataSourceWrap.class);

    private ThreadLocal<ConnectionWrap> con = new ThreadLocal<>();

    private DataSource dataSource;

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return dataSource.getLoginTimeout();
    }

    @Override
    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return dataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return dataSource.isWrapperFor(iface);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (con.get() == null) {
            Connection c = dataSource.getConnection();
            ConnectionWrap cc = new ConnectionWrap(c);
            con.set(cc);

            log.info("Got new DB connection: " + c);
        } else
            log.info("Using thread DB connection: " + con.get().getCon());

        return con.get();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        if (con.get() == null) {
            Connection c = dataSource.getConnection(username, password);
            ConnectionWrap cc = new ConnectionWrap(c);
            con.set(cc);

            log.info("Got new DB connection: " + c);
        } else
            log.info("Using thread DB connection: " + con.get().getCon());

        return con.get();
    }

    public void releaseConnection() {
        if (con.get() != null) {
            try {
                con.get().realClose();

                log.info("DB Connection released: " + con.get().getCon());
            } catch (SQLException e) {
                log.warn("Failed to release DB connection", e);
            } finally {
                con.remove();
            }
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
