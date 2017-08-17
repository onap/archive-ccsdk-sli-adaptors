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

package org.onap.ccsdk.sli.adaptors.resource.sql;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlResourceActivator implements BundleActivator {

	private static final String SQLRESOURCE_PROP_PATH = "/sql-resource.properties";

	private ServiceRegistration registration = null;

	private static final Logger LOG = LoggerFactory
			.getLogger(SqlResourceActivator.class);

	@Override
	public void start(BundleContext ctx) throws Exception {

		String cfgDir = System.getenv("SDNC_CONFIG_DIR");

		if ((cfgDir == null) || (cfgDir.length() == 0)) {
			cfgDir = "/opt/sdnc/data/properties";
			LOG.warn("SDNC_CONFIG_DIR unset - defaulting to "+cfgDir);
		}

		String cryptKey = "";

		File sqlResourcePropFile = new File(cfgDir+SQLRESOURCE_PROP_PATH);
		Properties sqlResourceProps = new Properties();
		if (sqlResourcePropFile.exists()) {
			try {

				sqlResourceProps.load(new FileInputStream(sqlResourcePropFile));

				cryptKey = sqlResourceProps.getProperty("org.openecomp.sdnc.resource.sql.cryptkey");
			} catch (Exception e) {
				LOG.warn(
						"Could not load properties file " + sqlResourcePropFile.getAbsolutePath(), e);
			}
		} else {
			LOG.warn("Cannot read "+sqlResourcePropFile.getAbsolutePath()+" to find encryption key - using default");
		}

		SqlResource.setCryptKey(cryptKey);

		// Advertise Sql resource adaptor
		SvcLogicResource impl = new SqlResource();
		String regName = impl.getClass().getName();

		if (registration == null)
		{
			LOG.debug("Registering SqlResource service "+regName);
			registration =ctx.registerService(regName, impl, null);
		}

	}

	@Override
	public void stop(BundleContext ctx) throws Exception {

		if (registration != null)
		{
			registration.unregister();
			registration = null;
		}
	}

}
