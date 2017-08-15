/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

package org.openecomp.sdnc.ra.service.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.openecomp.sdnc.ra.service.data.ServiceResource;
import org.openecomp.sdnc.ra.service.data.ServiceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

public class ServiceResourceDaoImpl implements ServiceResourceDao {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ServiceResourceDaoImpl.class);

    private static final String GET_SQL =
            "SELECT * FROM SERVICE_RESOURCE WHERE service_instance_id = ? AND service_status = ?";

    private static final String ADD_SQL = "INSERT INTO SERVICE_RESOURCE (\n"
            + "  service_instance_id, service_status, service_change_number, resource_set_id, resource_union_id)\n"
            + "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE SERVICE_RESOURCE SET service_change_number = ?, resource_set_id = ?\n"
                    + "WHERE service_instance_id = ? AND service_status = ?";

    private static final String DELETE_SQL =
            "DELETE FROM SERVICE_RESOURCE WHERE service_instance_id = ? AND service_status = ?";

    private static final String UPDATE_STATUS_SQL =
            "UPDATE SERVICE_RESOURCE SET service_status = ? WHERE service_instance_id = ? AND service_status = ?";

    private JdbcTemplate jdbcTemplate;

    @Override
    public ServiceResource getServiceResource(final String serviceInstanceId, final ServiceStatus serviceStatus) {
        List<ServiceResource> serviceResourceList =
                jdbcTemplate.query(GET_SQL, new Object[] { serviceInstanceId, serviceStatus.toString() },
                        new RowMapper<ServiceResource>() {

                            @Override
                            public ServiceResource mapRow(ResultSet rs, int rowNum) throws SQLException {
                                ServiceResource sr = new ServiceResource();
                                sr.id = rs.getLong("service_resource_id");
                                sr.serviceInstanceId = serviceInstanceId;
                                sr.serviceStatus = serviceStatus;
                                sr.serviceChangeNumber = rs.getInt("service_change_number");
                                sr.resourceSetId = rs.getString("resource_set_id");
                                sr.resourceUnionId = rs.getString("resource_union_id");
                                return sr;
                            }
                        });
        if (serviceResourceList.isEmpty())
            return null;
        return serviceResourceList.get(0);
    }

    @Override
    public void addServiceResource(ServiceResource serviceResource) {
        jdbcTemplate.update(ADD_SQL, serviceResource.serviceInstanceId, serviceResource.serviceStatus.toString(),
                serviceResource.serviceChangeNumber, serviceResource.resourceSetId, serviceResource.resourceUnionId);
    }

    @Override
    public void updateServiceResource(ServiceResource serviceResource) {
        jdbcTemplate.update(UPDATE_SQL, serviceResource.serviceChangeNumber, serviceResource.resourceSetId,
                serviceResource.serviceInstanceId, serviceResource.serviceStatus.toString());
    }

    @Override
    public void deleteServiceResource(String serviceInstanceId, ServiceStatus serviceStatus) {
        jdbcTemplate.update(DELETE_SQL, serviceInstanceId, serviceStatus.toString());
    }

    @Override
    public void updateServiceStatus(
            String serviceInstanceId,
            ServiceStatus serviceStatus,
            ServiceStatus newServiceStatus) {
        jdbcTemplate.update(UPDATE_STATUS_SQL, newServiceStatus.toString(), serviceInstanceId, serviceStatus.toString());
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
