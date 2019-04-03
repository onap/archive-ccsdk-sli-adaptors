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
package org.onap.ccsdk.sli.adaptors.netbox.lighty;

import java.io.FileInputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetboxPropertiesLighty {

    private static final Logger LOG = LoggerFactory.getLogger(NetboxPropertiesLighty.class);

    private static final String NETBOX_PROPERTY_FILE_NAME = "netbox.properties";
    private static final String DEFAULT_PROPERTIES_DIR = "/opt/onap/ccsdk/data/properties";
    private static final String PROPERTIES_DIR_KEY = "SDNC_CONFIG_DIR";

    private static final String NETBOX_URL_PROP = "org.onap.ccsdk.sli.adaptors.netbox.url";
    private static final String NETBOX_API_KEY_PROP = "org.onap.ccsdk.sli.adaptors.netbox.apikey";

    private Properties properties;

    public NetboxPropertiesLighty() {
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
        LOG.info("Loading properties from file {}", ccsdkConfigDir);
        try (FileInputStream in = new FileInputStream(ccsdkConfigDir)) {
            properties.load(in);
            LOG.info("Loaded {} properties from file {}", properties.size(), ccsdkConfigDir);
        } catch (Exception e) {
            LOG.error("Failed to load properties for file: {} " + NETBOX_PROPERTY_FILE_NAME, e);
        }
    }
}
