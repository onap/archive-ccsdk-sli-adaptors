/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All
 *                         rights
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

package org.onap.ccsdk.sli.adaptors.rm.dao.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

public class ResourceJdbcDaoImpl implements ResourceJdbcDao {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ResourceJdbcDaoImpl.class);

    private static final String RESOURCE_SQL = "SELECT * FROM RESOURCE WHERE asset_id = ? AND resource_name = ?";

    private static final String RESOURCE_SET_SQL = "SELECT * FROM RESOURCE WHERE resource_id IN (\n"
            + "SELECT DISTINCT resource_id FROM ALLOCATION_ITEM WHERE resource_set_id = ?)";

    private static final String RESOURCE_UNION_SQL = "SELECT * FROM RESOURCE WHERE resource_id IN (\n"
            + "SELECT DISTINCT resource_id FROM ALLOCATION_ITEM WHERE resource_union_id = ?)";

    private static final String INSERT_SQL = "INSERT INTO RESOURCE (\n"
            + "  asset_id, resource_name, resource_type, lt_used, ll_label, ll_reference_count, rr_used)\n"
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL = "UPDATE RESOURCE SET\n"
            + "  lt_used = ?, ll_label = ?, ll_reference_count = ?, rr_used = ?\nWHERE resource_id = ?";

    private static final String DELETE_SQL = "DELETE FROM RESOURCE WHERE resource_id = ?";

    private JdbcTemplate jdbcTemplate;
    private ResourceRowMapper resourceRowMapper = new ResourceRowMapper();

    @Override
    public Resource getResource(String assetId, String resourceName) {
        if (assetId == null || assetId.trim().length() == 0 || resourceName == null ||
                resourceName.trim().length() == 0)
            return null;

        List<Resource> ll = jdbcTemplate.query(RESOURCE_SQL, new Object[] { assetId, resourceName }, resourceRowMapper);
        return ll.isEmpty() ? null : ll.get(0);
    }

    @Override
    public List<Resource> getResourceSet(String resourceSetId) {
        if (resourceSetId == null)
            return Collections.emptyList();

        return jdbcTemplate.query(RESOURCE_SET_SQL, new Object[] { resourceSetId }, resourceRowMapper);
    }

    @Override
    public List<Resource> getResourceUnion(String resourceUnionId) {
        if (resourceUnionId == null)
            return Collections.emptyList();

        return jdbcTemplate.query(RESOURCE_UNION_SQL, new Object[] { resourceUnionId }, resourceRowMapper);
    }

    @Override
    public void add(final Resource r) {
        PreparedStatementCreator psc = new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection dbc) throws SQLException {
                PreparedStatement ps = dbc.prepareStatement(INSERT_SQL, new String[] { "resource_id" });
                ps.setString(1, r.assetId);
                ps.setString(2, r.name);
                ps.setString(3, r.type);
                ps.setLong(4, r.ltUsed);
                ps.setString(5, r.llLabel);
                ps.setInt(6, r.llReferenceCount);
                ps.setString(7, r.rrUsed);
                return ps;
            }
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(psc, keyHolder);
        r.id = keyHolder.getKey().longValue();
    }

    @Override
    public void update(Resource r) {
        Long ltUsed = r.ltUsed <= 0 ? null : r.ltUsed;
        Integer llRefCount = r.llReferenceCount <= 0 ? null : r.llReferenceCount;
        jdbcTemplate.update(UPDATE_SQL, ltUsed, r.llLabel, llRefCount, r.rrUsed, r.id);
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }

    private static class ResourceRowMapper implements RowMapper<Resource> {

        @Override
        public Resource mapRow(ResultSet rs, int arg1) throws SQLException {
            Resource r = new Resource();
            r.id = rs.getLong("resource_id");
            r.assetId = rs.getString("asset_id");
            r.name = rs.getString("resource_name");
            r.type = rs.getString("resource_type");
            r.ltUsed = rs.getLong("lt_used");
            r.llLabel = rs.getString("ll_label");
            r.llReferenceCount = rs.getInt("ll_reference_count");
            r.rrUsed = rs.getString("rr_used");
            return r;
        }
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
