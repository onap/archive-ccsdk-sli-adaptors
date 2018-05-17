/*-
 * ============LICENSE_START=======================================================
 * onap
 * ================================================================================
 * Copyright (C) 2016 - 2017 ONAP
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

package org.onap.ccsdk.sli.adaptors.resource.mdsal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.Vector;

import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.onap.ccsdk.sli.core.utils.JREFileResolver;
import org.onap.ccsdk.sli.core.utils.KarafRootFileResolver;
import org.onap.ccsdk.sli.core.utils.PropertiesFileResolver;
import org.onap.ccsdk.sli.core.utils.common.CoreDefaultFileResolver;
import org.onap.ccsdk.sli.core.utils.common.SdncConfigEnvVarFileResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for determining the properties file to use and instantiating the
 * <code>SqlResource</code> Service. The priority for properties file
 * resolution is as follows:
 *
 * <ol>
 * <li>A directory identified by the system environment variable
 * <code>SDNC_CONFIG_DIR</code></li>
 * <li>The default directory <code>DEFAULT_DBLIB_PROP_DIR</code></li>
 * <li>A directory identified by the JRE argument
 * <code>sql-resource.properties</code></li>
 * <li>A <code>sql-resource.properties</code> file located in the karaf root
 * directory</li>
 * </ol>
 */
public class MdsalResourcePropertiesProviderImpl implements MdsalResourcePropertiesProvider {

    private static final Logger LOG = LoggerFactory.getLogger(MdsalResourcePropertiesProviderImpl.class);

    /**
     * The name of the properties file for database configuration
     */
    private static final String MDSALRESOURCE_PROP_FILE_NAME = "mdsal-resource.properties";

    /**
     * A prioritized list of strategies for resolving sql-resource properties files.
     */
    private Vector<PropertiesFileResolver> mdsalResourcePropertiesFileResolvers = new Vector<>();

    /**
     * The configuration properties for the db connection.
     */
    private Properties properties;

    /**
     * Set up the prioritized list of strategies for resolving dblib properties
     * files.
     */
    public MdsalResourcePropertiesProviderImpl() {
        mdsalResourcePropertiesFileResolvers
                .add(new SdncConfigEnvVarFileResolver("Using property file (1) from environment variable"));
        mdsalResourcePropertiesFileResolvers.add(new CoreDefaultFileResolver("Using property file (2) from default directory"));

        mdsalResourcePropertiesFileResolvers.add(
                new JREFileResolver("Using property file (3) from JRE argument", MdsalResourcePropertiesProviderImpl.class));
        mdsalResourcePropertiesFileResolvers.add(new KarafRootFileResolver("Using property file (4) from karaf root", this));

        // determines properties file as according to the priority described in the
        // class header comment
        final File propertiesFile = determinePropertiesFile(this);
        if (propertiesFile != null) {
            try (FileInputStream fileInputStream = new FileInputStream(propertiesFile)) {
                properties = new Properties();
                properties.load(fileInputStream);
            } catch (final IOException e) {
                LOG.error("Failed to load properties for file: {}", propertiesFile.toString(),
                        new ConfigurationException("Failed to load properties for file: " + propertiesFile.toString(),
                                e));
            }
        } else {
            // Try to read properties as resource

            InputStream propStr = getClass().getResourceAsStream("/" + MDSALRESOURCE_PROP_FILE_NAME);
            if (propStr != null) {
                properties = new Properties();
                try {
                    properties.load(propStr);
                    propStr.close();
                } catch (IOException e) {
                    properties = null;
                }
            }

        }

        if (properties == null) {
            reportFailure("Missing configuration properties resource(3)", new ConfigurationException(
                    "Missing configuration properties resource(3): " + MDSALRESOURCE_PROP_FILE_NAME));
        }
    }

    /**
     * Extract svclogic config properties.
     *
     * @return the svclogic config properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Reports the method chosen for properties resolution to the
     * <code>Logger</code>.
     *
     * @param message
     *            Some user friendly message
     * @param fileOptional
     *            The file location of the chosen properties file
     * @return the file location of the chosen properties file
     */
    private static File reportSuccess(final String message, final Optional<File> fileOptional) {
        if (fileOptional.isPresent()) {
            final File file = fileOptional.get();
            LOG.info("{} {}", message, file.getPath());
            return file;
        }
        return null;
    }

    /**
     * Reports fatal errors. This is the case in which no properties file could be
     * found.
     *
     * @param message
     *            An appropriate fatal error message
     * @param configurationException
     *            An exception describing what went wrong during resolution
     */
    private static void reportFailure(final String message, final ConfigurationException configurationException) {

        LOG.error("{}", message, configurationException);
    }

    /**
     * Determines the sql-resource properties file to use based on the following priority:
     * <ol>
     * <li>A directory identified by the system environment variable
     * <code>SDNC_CONFIG_DIR</code></li>
     * <li>The default directory <code>DEFAULT_DBLIB_PROP_DIR</code></li>
     * <li>A directory identified by the JRE argument
     * <code>mdsal-resource.properties</code></li>
     * <li>A <code>sql-resource.properties</code> file located in the karaf root
     * directory</li>
     * </ol>
     */
    File determinePropertiesFile(final MdsalResourcePropertiesProviderImpl resourceProvider) {

        for (final PropertiesFileResolver sliPropertiesFileResolver : mdsalResourcePropertiesFileResolvers) {
            final Optional<File> fileOptional = sliPropertiesFileResolver.resolveFile(MDSALRESOURCE_PROP_FILE_NAME);
            if (fileOptional.isPresent()) {
                return reportSuccess(sliPropertiesFileResolver.getSuccessfulResolutionMessage(), fileOptional);
            }
        }

        return null;
    }
}
