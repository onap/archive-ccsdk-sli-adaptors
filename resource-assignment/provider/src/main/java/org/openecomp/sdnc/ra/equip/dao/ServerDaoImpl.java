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

package org.openecomp.sdnc.ra.equip.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class ServerDaoImpl implements ServerDao {

    private static final Logger log = LoggerFactory.getLogger(ServerDaoImpl.class);

    private static final String GET_SERVER_COUNT_SQL = "SELECT count(*) FROM PSERVER WHERE aic_site_id = ?";

    private JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getServerData(String aicSiteId) {
        List<Map<String, Object>> ll = new ArrayList<Map<String, Object>>();
        Map<String, Object> sd = new HashMap<String, Object>();
        sd.put("aic-site-id", aicSiteId);
        sd.put("server-id", aicSiteId + "/Server1");
        sd.put("server-model", "Unknown");
        sd.put("server-count", getServerCount(aicSiteId));
        ll.add(sd);
        return ll;
    }

    private int getServerCount(String aicSiteId) {
        int n = jdbcTemplate.queryForInt(GET_SERVER_COUNT_SQL, aicSiteId);

        log.info("Number of servers in " + aicSiteId + ": " + n);

        return n;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
