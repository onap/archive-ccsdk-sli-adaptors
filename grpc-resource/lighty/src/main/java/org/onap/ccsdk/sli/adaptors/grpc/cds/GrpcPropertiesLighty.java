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
package org.onap.ccsdk.sli.adaptors.grpc.cds;

import java.io.FileInputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcPropertiesLighty {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcPropertiesLighty.class);

    private static final String GRPC_PROPERTY_FILE_NAME = "grpc.properties";
    private static final String DEFAULT_PROPERTIES_DIR = "/opt/onap/ccsdk/data/properties";
    private static final String PROPERTIES_DIR_KEY = "SDNC_CONFIG_DIR";

    private static final String BLUEPRINT_PROCESSOR_URL_PROP = "org.onap.ccsdk.sli.adaptors.grpc.blueprint.processor.url";
    private static final String BLUEPRINT_PROCESSOR_PORT_PROP = "org.onap.ccsdk.sli.adaptors.grpc.blueprint.processor.port";
    private static final String BLUEPRINT_PROCESSOR_AUTH_PROP = "org.onap.ccsdk.sli.adaptors.grpc.blueprint.processor.auth";

    private Properties properties;

    public GrpcPropertiesLighty() {
        loadProps();
    }

    public String getUrl() {
        return properties.getProperty(BLUEPRINT_PROCESSOR_URL_PROP);
    }

    public String getAuth() {
        return properties.getProperty(BLUEPRINT_PROCESSOR_AUTH_PROP);
    }

    public int getPort() {
        return Integer.parseInt(properties.getProperty(BLUEPRINT_PROCESSOR_PORT_PROP));
    }

    private void loadProps() {
        properties = new Properties();
        // Try to load config from dir
        final String ccsdkConfigDir =
            System.getProperty(PROPERTIES_DIR_KEY, DEFAULT_PROPERTIES_DIR) + "/" + GRPC_PROPERTY_FILE_NAME;
        LOG.info("Loading properties from file {}", ccsdkConfigDir);
        try (FileInputStream in = new FileInputStream(ccsdkConfigDir)) {
            properties.load(in);
            LOG.info("Loaded {} properties from file {}", properties.size(), ccsdkConfigDir);
        } catch (Exception e) {
            LOG.error("Failed to load properties for file: {} " + GRPC_PROPERTY_FILE_NAME, e);
        }
    }
}
