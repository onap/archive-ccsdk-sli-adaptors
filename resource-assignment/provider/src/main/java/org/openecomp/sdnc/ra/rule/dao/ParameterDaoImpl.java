/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All rights
 * 						reserved.
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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

public class ParameterDaoImpl implements ParameterDao {

	private static final Logger log = LoggerFactory.getLogger(ParameterDaoImpl.class);

	private final static String GET_SQL = "SELECT * FROM PARAMETERS WHERE name = ?";

	private JdbcTemplate jdbcTemplate;

	@Override
	public String getParameter(String name) {
		List<Map<String, Object>> ll = jdbcTemplate.queryForList(GET_SQL, name);

		if (ll == null || ll.isEmpty()) {
			log.info("Parameter: " + name + " not found in DB");
			return null;
		}

		String v = (String) ll.get(0).get("value");
		log.info("Parameter from DB: " + name + "='" + v + "'");

		return v;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
}
