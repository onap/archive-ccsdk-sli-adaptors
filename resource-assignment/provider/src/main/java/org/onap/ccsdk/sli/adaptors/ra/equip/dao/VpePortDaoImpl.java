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

package org.onap.ccsdk.sli.adaptors.ra.equip.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class VpePortDaoImpl implements VpePortDao {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(VpePortDaoImpl.class);

    private static final String GET_SQL = "SELECT * FROM VPE_POOL WHERE aic_site_id = ?";
    private static final String GET2_SQL = "SELECT * FROM VPE_POOL WHERE vpe_name = ?";
    private static final String GET3_SQL =
            "SELECT * FROM VPE_POOL WHERE substring(aic_site_id, 1, 8) = substring(?, 1, 8)";

    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getVpePortData(String aicSiteId) {
        return jdbcTemplate.query(GET_SQL, new Object[] { aicSiteId }, new VpePortRowMapper());
    }

    @Override
    public List<Map<String, Object>> getVpePortData(String aicSiteId, String vpeName) {
        String sql = vpeName != null ? GET2_SQL : GET3_SQL;
        Object[] param = new Object[] { vpeName != null ? vpeName : aicSiteId };

        return jdbcTemplate.query(sql, param, new VpePortRowMapper());
    }

    private static class VpePortRowMapper implements RowMapper<Map<String, Object>> {

        @Override
        public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Map<String, Object> mm = new HashMap<String, Object>();
            mm.put("vpe-id", rs.getString("vpe_name"));
            mm.put("aic-site-id", rs.getString("aic_site_id"));
            mm.put("availability-zone", rs.getString("availability_zone"));
            mm.put("image-file-name", rs.getString("image_filename"));
            mm.put("vendor", rs.getString("vendor"));
            mm.put("provisioning-status", rs.getString("provisioning_status"));
            mm.put("physical-interface-name", rs.getString("physical_intf_name"));
            mm.put("physical-interface-speed", rs.getLong("physical_intf_speed"));
            mm.put("physical-interface-speed-unit", rs.getString("physical_intf_units"));
            return mm;
        }
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
