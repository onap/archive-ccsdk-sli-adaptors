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

package org.openecomp.sdnc.ra.rule.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class VpeLockDaoImpl implements VpeLockDao {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(VpeLockDaoImpl.class);

    private final static String GET_SQL = "SELECT vpn_lock FROM VPE_LOCK WHERE vpe_name = ?";

    private JdbcTemplate jdbcTemplate;

    @Override
    public String getVpeLock(String vpeName) {
        List<String> ll = jdbcTemplate.queryForList(GET_SQL, String.class, vpeName);
        return ll != null && !ll.isEmpty() ? ll.get(0) : null;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
