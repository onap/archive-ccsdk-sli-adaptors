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

package org.onap.ccsdk.sli.adaptors.ra.rule.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.onap.ccsdk.sli.adaptors.util.speed.SpeedUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class MaxPortSpeedDaoImpl implements MaxPortSpeedDao {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(MaxPortSpeedDaoImpl.class);

    private final static String GET_SQL =
            "SELECT * FROM MAX_PORT_SPEED WHERE image_file_name = ? AND end_point_position = ? AND interface_name = ?";

    private JdbcTemplate jdbcTemplate;
    private long defaultMaxPortSpeed = 5000000;
    private SpeedUtil speedUtil;

    @Override
    public long getMaxPortSpeed(String imageFile, String endPointPosition, String interfaceName) {
        List<MaxPortSpeed> maxPortSpeedList =
                jdbcTemplate.query(GET_SQL, new Object[] { imageFile, endPointPosition, interfaceName },
                        new RowMapper<MaxPortSpeed>() {

                            @Override
                            public MaxPortSpeed mapRow(ResultSet rs, int rowNum) throws SQLException {
                                MaxPortSpeed mps = new MaxPortSpeed();
                                mps.maxSpeed = rs.getLong("max_speed");
                                mps.unit = rs.getString("unit");
                                return mps;
                            }
                        });

        if (maxPortSpeedList.isEmpty())
            return defaultMaxPortSpeed;

        MaxPortSpeed mps = maxPortSpeedList.get(0);
        return speedUtil.convertToKbps(mps.maxSpeed, mps.unit);
    }

    private static class MaxPortSpeed {

        public long maxSpeed;
        public String unit;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setDefaultMaxPortSpeed(long defaultMaxPortSpeed) {
        this.defaultMaxPortSpeed = defaultMaxPortSpeed;
    }

    public void setSpeedUtil(SpeedUtil speedUtil) {
        this.speedUtil = speedUtil;
    }
}
