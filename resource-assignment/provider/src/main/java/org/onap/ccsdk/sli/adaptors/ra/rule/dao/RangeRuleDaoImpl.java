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

package org.onap.ccsdk.sli.adaptors.ra.rule.dao;

import java.util.ArrayList;
import java.util.List;
import org.onap.ccsdk.sli.adaptors.ra.rule.data.RangeRule;
import org.onap.ccsdk.sli.adaptors.rm.data.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class RangeRuleDaoImpl implements RangeRuleDao {

    private static final Logger log = LoggerFactory.getLogger(RangeRuleDaoImpl.class);

    private static final String GET_SQL = "SELECT * FROM RANGE_RULE WHERE service_model = ? AND equipment_level = ?";

    private JdbcTemplate jdbcTemplate;

    @Override
    public List<RangeRule> getRangeRules(String serviceModel, String equipLevel) {
        return jdbcTemplate.query(GET_SQL, new Object[] { serviceModel, equipLevel },
                (RowMapper<RangeRule>) (rs, rowNum) -> {
                    RangeRule rl = new RangeRule();
                    rl.id = rs.getLong("range_rule_id");
                    rl.rangeName = rs.getString("range_name");
                    rl.serviceModel = rs.getString("service_model");
                    rl.endPointPosition = rs.getString("end_point_position");
                    rl.equipmentLevel = rs.getString("equipment_level");
                    rl.equipmentExpression = rs.getString("equipment_expression");

                    String rangesStr = rs.getString("ranges");
                    String[] ranges = rangesStr.split(",");
                    rl.rangeList = new ArrayList<>();
                    for (String rangeStr : ranges) {
                        Range range = new Range();
                        String[] nn = rangeStr.split("-");
                        if (nn.length >= 1) {
                            try {
                                range.min = range.max = Integer.parseInt(nn[0]);
                            } catch (NumberFormatException e) {
                                log.warn("Invalid value found in DB for range: " + rangeStr, e);
                            }
                        }
                        if (nn.length >= 2) {
                            try {
                                range.max = Integer.parseInt(nn[1]);
                            } catch (NumberFormatException e) {
                                log.warn("Invalid value found in DB for range: " + rangeStr, e);
                            }
                        }
                        if (nn.length > 2) {
                            log.warn("Invalid value found in DB for range: {}", rangeStr);
                        }
                        rl.rangeList.add(range);
                    }

                    return rl;
                });

    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}