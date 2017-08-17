/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
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

package org.onap.ccsdk.sli.adaptors.ra.rule.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.onap.ccsdk.sli.adaptors.util.speed.SpeedUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class MaxServerSpeedDaoImpl implements MaxServerSpeedDao {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(MaxServerSpeedDaoImpl.class);

    private final static String GET_SQL =
            "SELECT * FROM MAX_SERVER_SPEED\n" +
            "WHERE (server_model = ? OR server_model = 'ALL') AND evc_count >= ?\n" +
            "ORDER BY evc_count";

    private JdbcTemplate jdbcTemplate;
    private long defaultMaxServerSpeed = 1600000;
    private SpeedUtil speedUtil;

    @Override
    public long getMaxServerSpeed(String serverModel, int evcCount) {
        List<MaxServerSpeed> maxServerSpeedList =
                jdbcTemplate.query(GET_SQL, new Object[] { serverModel, evcCount }, new RowMapper<MaxServerSpeed>() {

                    @Override
                    public MaxServerSpeed mapRow(ResultSet rs, int rowNum) throws SQLException {
                        MaxServerSpeed mps = new MaxServerSpeed();
                        mps.maxSpeed = rs.getLong("max_speed");
                        mps.unit = rs.getString("unit");
                        return mps;
                    }
                });

        if (maxServerSpeedList.isEmpty())
            return defaultMaxServerSpeed;

        MaxServerSpeed mps = maxServerSpeedList.get(0);
        return speedUtil.convertToKbps(mps.maxSpeed, mps.unit);
    }

    private static class MaxServerSpeed {

        public long maxSpeed;
        public String unit;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setDefaultMaxServerSpeed(long defaultMaxServerSpeed) {
        this.defaultMaxServerSpeed = defaultMaxServerSpeed;
    }

    public void setSpeedUtil(SpeedUtil speedUtil) {
        this.speedUtil = speedUtil;
    }
}
