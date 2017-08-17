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

import org.onap.ccsdk.sli.adaptors.ra.equip.data.EquipmentLevel;
import org.onap.ccsdk.sli.adaptors.ra.rule.data.RangeRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class RangeRuleDaoImpl implements RangeRuleDao {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(RangeRuleDaoImpl.class);

    private static final String GET_SQL =
            "SELECT * FROM RANGE_RULE WHERE service_model = ? AND end_point_position = ? AND equipment_level = ?";

    private JdbcTemplate jdbcTemplate;

    @Override
    public List<RangeRule> getRangeRules(String serviceModel, String endPointPosition, EquipmentLevel equipLevel) {
        List<RangeRule> rangeRuleList =
                jdbcTemplate.query(GET_SQL, new Object[] { serviceModel, endPointPosition, equipLevel.toString() },
                        new RowMapper<RangeRule>() {

                            @Override
                            public RangeRule mapRow(ResultSet rs, int rowNum) throws SQLException {
                                RangeRule rl = new RangeRule();
                                rl.id = rs.getLong("range_rule_id");
                                rl.rangeName = rs.getString("range_name");
                                rl.serviceModel = rs.getString("service_model");
                                rl.endPointPosition = rs.getString("end_point_position");
                                rl.equipmentLevel = rs.getString("equipment_level");
                                rl.minValue = rs.getInt("min_value");
                                rl.maxValue = rs.getInt("max_value");
                                return rl;
                            }
                        });
        return rangeRuleList;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
