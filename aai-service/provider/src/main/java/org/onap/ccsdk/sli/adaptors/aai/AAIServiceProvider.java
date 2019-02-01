/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights
 *                         reserved.
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
/**
 * @author Rich Tabedzki
 *
 */
package org.onap.ccsdk.sli.adaptors.aai;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Properties;
import java.util.Vector;

import org.onap.ccsdk.sli.core.utils.JREFileResolver;
import org.onap.ccsdk.sli.core.utils.KarafRootFileResolver;
import org.onap.ccsdk.sli.core.utils.PropertiesFileResolver;
import org.onap.ccsdk.sli.core.utils.common.BundleContextFileResolver;
import org.onap.ccsdk.sli.core.utils.common.CoreDefaultFileResolver;
import org.onap.ccsdk.sli.core.utils.common.SdncConfigEnvVarFileResolver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for determining the properties file to use and instantiating the <code>DBResourceManager</code>
 * Service.  The priority for properties file resolution is as follows:
 *
 * <ol>
 *     <li>A directory identified by the system environment variable <code>SDNC_CONFIG_DIR</code></li>
 *     <li>The default directory <code>DEFAULT_DBLIB_PROP_DIR</code></li>
 *     <li>A directory identified by the JRE argument <code>dblib.properties</code></li>
 *     <li>A <code>dblib.properties</code> file located in the karaf root directory</li>
 * </ol>
 *
 * Encryption Support
 * <ol>
 *    <li>Uses ecryption provided by <code>AAAEncryptionService</code></li>
 *    <li>AAA Configuration file is <code>aaa-cert-config.xml</code></li>
 * </ol>
 *
 */
public class AAIServiceProvider implements UtilsProvider {

    private static final Logger LOG = LoggerFactory.getLogger(AAIServiceProvider.class);

    /**
     * The name of the properties file for database configuration
     */
    private static final String AAISEERVICE_PROP_FILE_NAME = "aaiclient.properties";

    /**
     * The name of the pwd key
     */
    private static final String AAICLIENT_PROPERTY_NAME = "org.onap.ccsdk.sli.adaptors.aai.client.psswd";

    /**
     * A prioritized list of strategies for resolving dblib properties files.
     */
    private Vector<PropertiesFileResolver> dblibPropertiesFileResolvers = new Vector<>();

    /**
     * The configuration properties for the db connection.
     */
    private Properties properties;

    /**
     * Set up the prioritized list of strategies for resolving dblib properties files.
     */
    public AAIServiceProvider() {
        dblibPropertiesFileResolvers.add(new SdncConfigEnvVarFileResolver(
                "Using property file (1) from environment variable"
            ));
        dblibPropertiesFileResolvers.add(new JREFileResolver(
            "Using property file (2) from JRE argument", AAIServiceProvider.class
        ));
        dblibPropertiesFileResolvers.add(new BundleContextFileResolver(
            "Using property file (3) from JRE argument", AAIServiceProvider.class
        ));
        dblibPropertiesFileResolvers.add(new KarafRootFileResolver(
            "Using property file (4) from karaf root", this
        ));
        dblibPropertiesFileResolvers.add(new CoreDefaultFileResolver(
            "Using property file (5) from default directory"
        ));

        // determines properties file as according to the priority described in the class header comment
        final File propertiesFile = determinePropertiesFile();
        if (propertiesFile != null) {
            try(FileInputStream fileInputStream = new FileInputStream(propertiesFile)) {
                properties = new Properties();
                properties.load(fileInputStream);

                if(properties.containsKey(AAICLIENT_PROPERTY_NAME)) {
                    String sensitive = properties.getProperty(AAICLIENT_PROPERTY_NAME);
                    if(sensitive != null && sensitive.startsWith("ENC:")) {
                        try {
                            sensitive = sensitive.substring(4);
                            String postsense = decrypt(sensitive);
                            properties.setProperty(AAICLIENT_PROPERTY_NAME, postsense);
                        } catch(Exception exc) {
                            LOG.error("Failed to translate property", exc);
                        }
                    }
                }
            } catch (final IOException e) {
                LOG.error("Failed to load properties for file: {}", propertiesFile.toString(),
                        new AAIServiceException("Failed to load properties for file: "
                                + propertiesFile.toString(), e));
            }
        }
    }

    /**
     *
     * @param value
     * @return decrypted string if successful or the original value if unsuccessful
     */
    private String decrypt(String value) {
        try {
            BundleContext bctx = FrameworkUtil.getBundle(this.getClass()).getBundleContext();

            ServiceReference sref = bctx.getServiceReference("org.opendaylight.aaa.encrypt.AAAEncryptionService");
            if(sref == null) {
                LOG.warn("Could not acquire service reference for 'org.opendaylight.aaa.encrypt.AAAEncryptionService'");
                return value;
            }
            Object encrSvc = bctx.getService(sref);
            if(encrSvc == null) {
                LOG.warn("Could not access service for 'org.opendaylight.aaa.encrypt.AAAEncryptionService'");
                return value;
            }

            Method gs2Method = encrSvc.getClass().getMethod("decrypt", new Class[] { "".getClass() });
            Object unmasked = gs2Method.invoke(encrSvc, new Object[] { value });
            return unmasked.toString();

        } catch (Exception exc) {
            LOG.error("Failure", exc);
            return value;
        }
    }

    /**
     * Extract db config properties.
     *
     * @return the db config properties
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Reports the method chosen for properties resolution to the <code>Logger</code>.
     *
     * @param message Some user friendly message
     * @param fileOptional The file location of the chosen properties file
     * @return the file location of the chosen properties file
     */
    private static File reportSuccess(final String message, final Optional<File> fileOptional) {
        if(fileOptional.isPresent()) {
            final File file = fileOptional.get();
            LOG.info("{} {}", message, file.getPath());
            return file;
        }
        return null;
    }

    /**
     * Reports fatal errors.  This is the case in which no properties file could be found.
     *
     * @param message An appropriate fatal error message
     * @param dblibConfigurationException An exception describing what went wrong during resolution
     */
    private static void reportFailure(final String message,
                                      final AAIServiceException dblibConfigurationException) {

        LOG.error("{}", message, dblibConfigurationException);
    }

    /**
     * Determines the dblib properties file to use based on the following priority:
     * <ol>
     *     <li>A directory identified by the system environment variable <code>SDNC_CONFIG_DIR</code></li>
     *     <li>The default directory <code>DEFAULT_DBLIB_PROP_DIR</code></li>
     *     <li>A directory identified by the JRE argument <code>dblib.properties</code></li>
     *     <li>A <code>dblib.properties</code> file located in the karaf root directory</li>
     * </ol>
     */
    File determinePropertiesFile() {

        for (final PropertiesFileResolver dblibPropertiesFileResolver : dblibPropertiesFileResolvers) {
            final Optional<File> fileOptional = dblibPropertiesFileResolver.resolveFile(AAISEERVICE_PROP_FILE_NAME);
            if (fileOptional.isPresent()) {
                return reportSuccess(dblibPropertiesFileResolver.getSuccessfulResolutionMessage(), fileOptional);
            }
        }

        reportFailure("Missing configuration properties resource(3)",
                new AAIServiceException("Missing configuration properties resource(3): "
                        + AAISEERVICE_PROP_FILE_NAME));
        return null;
    }
}
