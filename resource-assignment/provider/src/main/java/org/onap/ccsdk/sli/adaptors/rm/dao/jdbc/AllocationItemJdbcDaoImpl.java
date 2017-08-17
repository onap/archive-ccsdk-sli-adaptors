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

package org.onap.ccsdk.sli.adaptors.rm.dao.jdbc;

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

public class AllocationItemJdbcDaoImpl implements AllocationItemJdbcDao {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ResourceJdbcDaoImpl.class);

    private static final String INSERT_SQL = "INSERT INTO ALLOCATION_ITEM (\n"
            + "  resource_id, application_id, resource_set_id, resource_union_id, resource_share_group_list,\n"
            + "  lt_used, ll_label, rr_used, allocation_time)\nVALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL = "UPDATE ALLOCATION_ITEM SET\n"
            + "  resource_share_group_list = ?, lt_used = ?, ll_label = ?, rr_used = ?, allocation_time = ?\n"
            + "WHERE allocation_item_id = ?";

    private static final String DELETE_SQL = "DELETE FROM ALLOCATION_ITEM WHERE allocation_item_id = ?";

    private static final String GET_SQL = "SELECT * FROM ALLOCATION_ITEM WHERE resource_id = ?";

    private JdbcTemplate jdbcTemplate;
    private AllocationItemRowMapper allocationItemRowMapper = new AllocationItemRowMapper();

    @Override
    public void add(final AllocationItem ai) {
        PreparedStatementCreator psc = new PreparedStatementCreator() {

            @Override
            public PreparedStatement createPreparedStatement(Connection dbc) throws SQLException {
                PreparedStatement ps = dbc.prepareStatement(INSERT_SQL, new String[] { "allocation_item_id" });
                ps.setLong(1, ai.resourceId);
                ps.setString(2, ai.applicationId);
                ps.setString(3, ai.resourceSetId);
                ps.setString(4, ai.resourceUnionId);
                ps.setString(5, ai.resourceShareGroupList);
                ps.setLong(6, ai.ltUsed);
                ps.setString(7, ai.llLabel);
                ps.setString(8, ai.rrUsed);
                ps.setTimestamp(9, new Timestamp(ai.allocationTime.getTime()));
                return ps;
            }
        };
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(psc, keyHolder);
        ai.id = keyHolder.getKey().longValue();
    }

    @Override
    public void update(AllocationItem ai) {
        Long ltUsed = ai.ltUsed <= 0 ? null : ai.ltUsed;
        jdbcTemplate.update(UPDATE_SQL, ai.resourceShareGroupList, ltUsed, ai.llLabel, ai.rrUsed, ai.allocationTime,
                ai.id);
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update(DELETE_SQL, id);
    }

    @Override
    public List<AllocationItem> getAllocationItems(long resourceId) {
        if (resourceId <= 0)
            return Collections.emptyList();

        return jdbcTemplate.query(GET_SQL, new Object[] { resourceId }, allocationItemRowMapper);
    }

    private static class AllocationItemRowMapper implements RowMapper<AllocationItem> {

        @Override
        public AllocationItem mapRow(ResultSet rs, int n) throws SQLException {
            AllocationItem ai = new AllocationItem();
            ai.id = rs.getLong("allocation_item_id");
            ai.resourceId = rs.getLong("resource_id");
            ai.applicationId = rs.getString("application_id");
            ai.resourceSetId = rs.getString("resource_set_id");
            ai.resourceUnionId = rs.getString("resource_union_id");
            ai.resourceShareGroupList = rs.getString("resource_share_group_list");
            ai.ltUsed = rs.getLong("lt_used");
            ai.llLabel = rs.getString("ll_label");
            ai.rrUsed = rs.getString("rr_used");
            ai.allocationTime = rs.getTimestamp("allocation_time");
            return ai;
        }
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
