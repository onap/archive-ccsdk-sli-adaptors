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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.onap.ccsdk.sli.adaptors.netbox.api.IpamException;
import org.onap.ccsdk.sli.core.utils.JREFileResolver;
import org.onap.ccsdk.sli.core.utils.KarafRootFileResolver;
import org.onap.ccsdk.sli.core.utils.PropertiesFileResolver;
import org.onap.ccsdk.sli.core.utils.common.BundleContextFileResolver;
import org.onap.ccsdk.sli.core.utils.common.SdncConfigEnvVarFileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for determining the properties file to use.
 *
 * <ol>
 * <li>A directory identified by the system environment variable <code>SDNC_CONFIG_DIR</code></li>
 * <li>A directory identified by the JRE argument <code>netbox.properties</code></li>
 * <li>A <code>netbox.properties</code> file located in the karaf root directory</li>
 * </ol>
 *
 * Partial copy and adaptation of org.onap.ccsdk.sli.adaptors.aai.AAIServiceProvider
 */
public class NetboxProperties {

    private static final Logger LOG = LoggerFactory.getLogger(NetboxProperties.class);

    private static final String NETBOX_PROPERTY_FILE_NAME = "netbox.properties";
    private static final String MISSING_PROPERTY_FILE =
        "Missing configuration properties resource for Netbox: " + NETBOX_PROPERTY_FILE_NAME;
    private static final String NETBOX_URL_PROP = "org.onap.ccsdk.sli.adaptors.netbox.url";
    private static final String NETBOX_API_KEY_PROP = "org.onap.ccsdk.sli.adaptors.netbox.apikey";

    private Set<PropertiesFileResolver> fileResolvers = new HashSet<>();
    private Properties properties;

    public NetboxProperties() {
        fileResolvers.add(new SdncConfigEnvVarFileResolver("Using property file (1) from environment variable"));
        fileResolvers.add(new BundleContextFileResolver("Using property file (2) from BundleContext property",
            NetboxProperties.class));
        fileResolvers.add(new JREFileResolver("Using property file (3) from JRE argument", NetboxProperties.class));
        fileResolvers.add(new KarafRootFileResolver("Using property file (4) from karaf root", this));

        loadProps();
    }

    public String getHost() {
        checkArgument(properties != null);
        return properties.getProperty(NETBOX_URL_PROP);
    }

    public String getApiKey() {
        checkArgument(properties != null);
        return properties.getProperty(NETBOX_API_KEY_PROP);
    }

    private void checkArgument(final boolean argument) {
        if (!argument) {
            LOG.info("Propety file {} was missing, trying to reload it", NETBOX_PROPERTY_FILE_NAME);
            loadProps();
            if (properties == null) {
                throw new IllegalArgumentException(MISSING_PROPERTY_FILE);
            }
        }
    }

    private void loadProps() {
        // determines properties file as according to the priority described in the class header comment
        final File propertiesFile = determinePropertiesFile();
        if (propertiesFile != null) {
            try (FileInputStream fileInputStream = new FileInputStream(propertiesFile)) {
                properties = new Properties();
                properties.load(fileInputStream);
            } catch (final IOException e) {
                String errorMsg = "Failed to load properties for file: " + propertiesFile.toString();
                LOG.error(errorMsg, new IpamException(errorMsg));
            }
        }
    }

    private File determinePropertiesFile() {
        for (final PropertiesFileResolver resolver : fileResolvers) {
            final Optional<File> fileOptional = resolver.resolveFile(NETBOX_PROPERTY_FILE_NAME);
            if (fileOptional.isPresent()) {
                final File file = fileOptional.get();
                LOG.info("{} {}", resolver.getSuccessfulResolutionMessage(), file.getPath());
                return file;
            }
        }

        LOG.error(MISSING_PROPERTY_FILE, new IpamException(MISSING_PROPERTY_FILE));
        return null;
    }
}
