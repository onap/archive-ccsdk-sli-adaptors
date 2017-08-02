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

package org.openecomp.sdnc.sli.resource.mdsal;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.Properties;

import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MdsalResourceActivator implements BundleActivator {



    private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";

    public LinkedList<ServiceRegistration> registrations = new LinkedList<ServiceRegistration>();

    private static final Logger LOG = LoggerFactory
            .getLogger(MdsalResourceActivator.class);

    @Override
    public void start(BundleContext ctx) throws Exception {

        // Read properties
        Properties props = new Properties();

        String propDir = System.getenv(SDNC_CONFIG_DIR);
        if (propDir == null) {

            propDir = "/opt/sdnc/data/properties";
        }
        String propPath = propDir + "/mdsal-resource.properties";


        File propFile = new File(propPath);

        if (!propFile.exists()) {

            throw new ConfigurationException(
                    "Missing configuration properties file : "
                            + propFile);
        }
        try {

            props.load(new FileInputStream(propFile));
        } catch (Exception e) {
            throw new ConfigurationException(
                    "Could not load properties file " + propPath, e);

        }

        String sdncUser = props.getProperty("org.openecomp.sdnc.sli.resource.mdsal.sdnc-user", "admin");
        String sdncPasswd = props.getProperty("org.openecomp.sdnc.sli.resource.mdsal.sdnc-passwd", "admin");
        String sdncHost = props.getProperty("org.openecomp.sdnc.sli.resource.mdsal.sdnc-host", "localhost");
        String sdncProtocol = props.getProperty("org.openecomp.sdnc.sli.resource.mdsal.sdnc-protocol", "https");
        String sdncPort = props.getProperty("org.openecomp.sdnc.sli.resource.mdsal.sdnc-port", "8443");

        // Advertise MD-SAL resource adaptors
        SvcLogicResource impl = new ConfigResource(sdncProtocol, sdncHost, sdncPort, sdncUser, sdncPasswd);

        LOG.debug("Registering MdsalResource service "+impl.getClass().getName());
        registrations.add(ctx.registerService(impl.getClass().getName(), impl, null));

        impl = new OperationalResource(sdncProtocol, sdncHost, sdncPort, sdncUser, sdncPasswd);

        LOG.debug("Registering MdsalResource service "+impl.getClass().getName());
        registrations.add(ctx.registerService(impl.getClass().getName(), impl, null));
    }

    @Override
    public void stop(BundleContext ctx) throws Exception {

        for (ServiceRegistration registration : registrations)
        {
            registration.unregister();
        }
    }

}
