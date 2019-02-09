/*
 * Copyright (C) 2018 Bell Canada.
 *
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
 */
package org.onap.ccsdk.sli.adaptors.netbox.property;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetboxProperties {

    private static final Logger LOG = LoggerFactory.getLogger(NetboxProperties.class);

    private static final String NETBOX_PROPERTY_FILE_NAME = "netbox.properties";
    private static final String DEFAULT_PROPERTIES_DIR = "/opt/onap/ccsdk/data/properties";
    private static final String PROPERTIES_DIR_KEY = "SDNC_CONFIG_DIR";

    private static final String NETBOX_URL_PROP = "org.onap.ccsdk.sli.adaptors.netbox.url";
    private static final String NETBOX_API_KEY_PROP = "org.onap.ccsdk.sli.adaptors.netbox.apikey";

    private Properties properties;

    public NetboxProperties() {
        loadProps();
    }

    public String getHost() {
        return properties.getProperty(NETBOX_URL_PROP);
    }

    public String getApiKey() {
        return properties.getProperty(NETBOX_API_KEY_PROP);
    }

    private void loadProps() {
        properties = new Properties();
        // Try to load config from dir
        final String ccsdkConfigDir =
            System.getProperty(PROPERTIES_DIR_KEY, DEFAULT_PROPERTIES_DIR) + "/" + NETBOX_PROPERTY_FILE_NAME;
        try (FileInputStream in = new FileInputStream(ccsdkConfigDir)) {
            properties.load(in);
            LOG.info("Loaded {} properties from file {}", properties.size(), ccsdkConfigDir);
        } catch (Exception e) {
            // Try to load config from jar
            final Bundle bundle = FrameworkUtil.getBundle(NetboxProperties.class);
            final BundleContext ctx = bundle.getBundleContext();
            final URL url = ctx.getBundle().getResource(NETBOX_PROPERTY_FILE_NAME);

            try (InputStream inputStream = url.openStream()) {
                properties.load(inputStream);
                LOG.info("Loaded {} properties from file {}", properties.size(), NETBOX_PROPERTY_FILE_NAME);
            } catch (IOException e1) {
                LOG.error("Failed to load properties for file: {} " + NETBOX_PROPERTY_FILE_NAME, e1);
            }
        }
    }
}
