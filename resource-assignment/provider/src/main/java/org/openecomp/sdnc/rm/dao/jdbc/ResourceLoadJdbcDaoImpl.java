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

package org.openecomp.sdnc.rm.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class ResourceLoadJdbcDaoImpl implements ResourceLoadJdbcDao {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ResourceJdbcDaoImpl.class);

    private static final String INSERT_SQL = "INSERT INTO RESOURCE_LOAD (\n"
            + "  resource_id, application_id, resource_load_time, resource_expiration_time)\nVALUES (?, ?, ?, ?)";

    private static final String UPDATE_SQL = "UPDATE RESOURCE_LOAD SET\n"
            + "  resource_load_time = ?, resource_expiration_time = ?\nWHERE resource_id = ?";

    private static final String DELETE_SQL = "DELETE FROM RESOURCE_LOAD WHERE resource_load_id = ?";

    private static final String GET_SQL = "SELECT * FROM RESOURCE_LOAD WHERE resource_id = ?";

    private JdbcTemplate jdbcTemplate;
    private ResourceLoadRowMapper resourceLoadRowMapper = new ResourceLoadRowMapper();

    @Override
    public void add(final ResourceLoad rl) {
        PreparedStatementCreator psc = new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection dbc) throws SQLException {
                PreparedStatement ps = dbc.prepareStatement(INSERT_SQL, new String[] { "resource_load_id" });
                ps.setLong(1, rl.resourceId);
                ps.setString(2, rl.applicationId);
                ps.setTimestamp(3, new Timestamp(rl.loadTime.getTime()));
                ps.setTimestamp(4, new Timestamp(rl.expirationTime.getTime()));
                return ps;
            }
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(psc, keyHolder);
        rl.id = keyHolder.getKey().longValue();
    }

    @Override
    public void update(ResourceLoad rl) {
        jdbcTemplate.update(UPDATE_SQL, rl.loadTime, rl.expirationTime, rl.id);
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }

    @Override
    public List<ResourceLoad> getResourceLoads(long resourceId) {
        if (resourceId <= 0)
            return Collections.emptyList();

        return jdbcTemplate.query(GET_SQL, new Object[] { resourceId }, resourceLoadRowMapper);
    }

    private static class ResourceLoadRowMapper implements RowMapper<ResourceLoad> {

        @Override
        public ResourceLoad mapRow(ResultSet rs, int n) throws SQLException {
            ResourceLoad rl = new ResourceLoad();
            rl.id = rs.getLong("allocation_item_id");
            rl.resourceId = rs.getLong("resource_id");
            rl.applicationId = rs.getString("application_id");
            rl.loadTime = rs.getTimestamp("resource_load_time");
            rl.expirationTime = rs.getTimestamp("resource_expiration_time");
            return rl;
        }
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
