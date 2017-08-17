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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.onap.ccsdk.sli.adaptors.ra.equip.data.EquipmentLevel;
import org.onap.ccsdk.sli.adaptors.ra.rule.data.ResourceRule;
import org.onap.ccsdk.sli.adaptors.ra.rule.data.ResourceThreshold;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class ResourceRuleDaoImpl implements ResourceRuleDao {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ResourceRuleDaoImpl.class);

    private static final String GET1_SQL =
            "SELECT * FROM RESOURCE_RULE WHERE service_model = ? AND end_point_position = ? AND equipment_level = ?";
    private static final String GET2_SQL =
            "SELECT * FROM RESOURCE_RULE WHERE service_model = ? AND end_point_position = ? AND equipment_level = ? AND resource_name = ?";
    private static final String THRESHOLD_SQL = "SELECT * FROM RESOURCE_THRESHOLD WHERE resource_rule_id = ?";

    private JdbcTemplate jdbcTemplate;
    ResourceRuleRowMapper resourceRuleRowMapper = new ResourceRuleRowMapper();
    ResourceThresholdRowMapper resourceThresholdRowMapper = new ResourceThresholdRowMapper();

    @Override
    public List<ResourceRule> getResourceRules(
            String serviceModel,
            String endPointPosition,
            EquipmentLevel equipLevel) {
        List<ResourceRule> resourceRuleList = jdbcTemplate.query(GET1_SQL,
                new Object[] { serviceModel, endPointPosition, equipLevel.toString() }, resourceRuleRowMapper);

        for (ResourceRule rr : resourceRuleList)
            rr.thresholdList = jdbcTemplate.query(THRESHOLD_SQL, new Object[] { rr.id }, resourceThresholdRowMapper);

        return resourceRuleList;
    }

    @Override
    public ResourceRule getResourceRule(
            String serviceModel,
            String endPointPosition,
            EquipmentLevel equipLevel,
            String resourceName) {
        List<ResourceRule> resourceRuleList = jdbcTemplate.query(GET2_SQL,
                new Object[] { serviceModel, endPointPosition, equipLevel.toString(), resourceName },
                resourceRuleRowMapper);

        if (resourceRuleList == null || resourceRuleList.isEmpty())
            return null;

        ResourceRule rr = resourceRuleList.get(0);
        rr.thresholdList = jdbcTemplate.query(THRESHOLD_SQL, new Object[] { rr.id }, resourceThresholdRowMapper);

        return rr;
    }

    private static class ResourceRuleRowMapper implements RowMapper<ResourceRule> {

        @Override
        public ResourceRule mapRow(ResultSet rs, int rowNum) throws SQLException {
            ResourceRule rl = new ResourceRule();
            rl.id = rs.getLong("resource_rule_id");
            rl.resourceName = rs.getString("resource_name");
            rl.serviceModel = rs.getString("service_model");
            rl.endPointPosition = rs.getString("end_point_position");
            rl.serviceExpression = rs.getString("service_expression");
            rl.equipmentLevel = rs.getString("equipment_level");
            rl.equipmentExpression = rs.getString("equipment_expression");
            rl.allocationExpression = rs.getString("allocation_expression");
            rl.softLimitExpression = rs.getString("soft_limit_expression");
            rl.hardLimitExpression = rs.getString("hard_limit_expression");
            return rl;
        }
    }

    private static class ResourceThresholdRowMapper implements RowMapper<ResourceThreshold> {

        @Override
        public ResourceThreshold mapRow(ResultSet rs, int rowNum) throws SQLException {
            ResourceThreshold th = new ResourceThreshold();
            th.expression = rs.getString("threshold_expression");
            th.message = rs.getString("threshold_message");
            return th;
        }
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
