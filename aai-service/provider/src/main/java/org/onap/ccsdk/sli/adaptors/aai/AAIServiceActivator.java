/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *             reserved.
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

package org.onap.ccsdk.sli.adaptors.aai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAIServiceActivator implements BundleActivator {

    private static final String DEFAULT_CONFIG_FILE_NAME = "aaiclient.config";
    private static final String DEFAULT_PROPERTY_FILE_NAME = "aaiclient.properties";
    private static final String DEFAULT_KEYWORD = "default";

    private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";

    private static final String BVC_PROPERTY_FILE = "/opt/bvc/controller/configuration/aaiclient.properties";
    private static final String DEFAULT_SDNC_PROPERTY_FILE = "/opt/sdnc/data/properties/aaiclient.properties";

    private Set<ServiceRegistration> registrationSet = new HashSet<ServiceRegistration>();

    private static final Logger LOG = LoggerFactory.getLogger(AAIServiceActivator.class);

    @Override
    public void start(BundleContext ctx) throws Exception {

        System.setProperty("aaiclient.runtime", "OSGI");

        String sdnConfigDirectory = System.getenv(SDNC_CONFIG_DIR);

        // check SDNC CONFIG DIR system property
        if(sdnConfigDirectory == null ) {
            LOG.error("System property SDNC_CONFIG_DIR is not defined.");
            LOG.info("Defaulting SDNC_CONFIG_DIR to '/opt/sdnc/data/properties/'");
            sdnConfigDirectory = "/opt/sdnc/data/properties/";
        }

        LOG.debug("Configuration directory used : " + sdnConfigDirectory);

        // check existance of properties directory
        File configDirectory = new File(sdnConfigDirectory);
        if(!configDirectory.exists() || !configDirectory.isDirectory()){
            LOG.error("System property SDNC_CONFIG_DIR = '" + sdnConfigDirectory + "' does not point to a valid directory. AAIService will not be initialized.");
            return;
        }

        Properties properties = new Properties();
        InputStream input = null;

        // find aaiclient config file
        File[] files = findFiles(configDirectory, DEFAULT_CONFIG_FILE_NAME);

        // read the aai config data
        if(files != null && files.length > 0) {
            LOG.debug("AAIService config file exists and it is named :" + files[0].getAbsolutePath() );
            try {
                input = new FileInputStream(files[0]);
                properties.load(input);
                LOG.debug("Loaded AAI Client properties from " + files[0].getAbsolutePath());
            } catch (IOException exc) {
                LOG.warn("Problem loading AAI Client properties from " + files[0].getAbsolutePath(), exc);
            } finally {
                if(input != null ) {
                    try {
                        input.close();
                    } catch(Exception exc) {
                        LOG.debug(exc.getMessage());
                    }
                }
                int size = properties.keySet().size() ;
                if(size == 0) {
                    LOG.debug(files[0].getAbsolutePath() + " contained no entries. Adding the default entry");
                    properties.put(DEFAULT_KEYWORD, DEFAULT_PROPERTY_FILE_NAME);
                }
            }
        } else {
            LOG.debug("No configuration entries were found. Adding the default entry");
            properties.put(DEFAULT_KEYWORD, DEFAULT_PROPERTY_FILE_NAME);
        }

        Set<String> entrySet = properties. stringPropertyNames();
        String value = null;

        // initialize AAI Service for each aai client property files
        for(String entry : entrySet) {
            value = properties.getProperty(entry);
            if(value != null && !value.isEmpty()) {

                final String fileName = value;

                File[] propertyFileList = findFiles(configDirectory, fileName);

                for(File propertiesFile : propertyFileList) {
                    LOG.info(propertiesFile.getName());
                    // Advertise AAI resource adaptor
                    AAIClient impl = null;
                    switch(entry) {
                    case DEFAULT_KEYWORD:
                        impl = new AAIService(propertiesFile.toURI().toURL());
                        break;
                    case "trinity":
                        impl = new AAITrinityService(propertiesFile.toURI().toURL());
                        break;
                    default:
                        LOG.error("Invalid configuration keyword '"+entry+"' detected in aaiclient.config. Aborting initialization");
                        continue;
                    }
                    String regName = impl.getClass().getName();

                    LOG.debug("Registering AAIService service "+regName);
                    ServiceRegistration registration = ctx.registerService(regName, impl, null);
                    registrationSet.add(registration);

                }
            }
        }
    }

//    @Override
    @Deprecated
    public void start1(BundleContext ctx) throws Exception {

        String sdnConfigDirectory = System.getenv(SDNC_CONFIG_DIR);
        String propertiesPath = null;

        if (sdnConfigDirectory == null || sdnConfigDirectory.isEmpty()) {
            String filename = DEFAULT_SDNC_PROPERTY_FILE;
            File file = new File(filename);
            if(file.exists()) {
                propertiesPath = filename;
                LOG.info("Using property file (1): " + propertiesPath);
            } else {
                filename = BVC_PROPERTY_FILE;
                file = new File(filename);
                if(file.exists()) {
                    propertiesPath = filename;
                    LOG.info("Using property file (1): " + propertiesPath);
                } else {
                    throw new ConfigurationException("Cannot find config file - "+filename+" and "+SDNC_CONFIG_DIR+" is unset");
                }
            }
        } else {
            propertiesPath = sdnConfigDirectory + "/aaiclient.properties";
            LOG.info("Environment variable " + SDNC_CONFIG_DIR + " set, - calculated path " + propertiesPath);
        }

        File propFile = new File(propertiesPath);
        if(!propFile.exists()) {
            String filename = DEFAULT_SDNC_PROPERTY_FILE;
            File file = new File(filename);
            if(file.exists()) {
                propertiesPath = filename;
                LOG.info("Using property file (1): " + propertiesPath);
            } else {
                filename = BVC_PROPERTY_FILE;
                file = new File(filename);
                if(file.exists()) {
                    propertiesPath = filename;
                    LOG.info("Using property file (1): " + propertiesPath);
                } else {
                    LOG.error("AnAI Service Property file " + propertiesPath + "does not exist.");
                    throw new ConfigurationException("Cannot find config file - "+propertiesPath+" and " + SDNC_CONFIG_DIR + " is unset.");
                }
            }
        }

        // Advertise AAI resource adaptor
        AAIClient impl = new AAIService(propFile.toURI().toURL());
        String regName = impl.getClass().getName();

        LOG.debug("Registering AAIService service "+regName);
        ServiceRegistration registration = ctx.registerService(regName, impl, null);
        registrationSet.add(registration);
    }

    @Override
    public void stop(BundleContext ctx) throws Exception {

        Set<ServiceRegistration> localRegistrationSet = new HashSet<ServiceRegistration>();
        localRegistrationSet.addAll(registrationSet);

        for(ServiceRegistration registration : localRegistrationSet) {
            if (registration != null) {
                try {
                    AAIService aaiService = (AAIService)ctx.getService(registration.getReference());
                registration.unregister();
                registrationSet.remove(registration);
                    if(aaiService != null) {
                        aaiService.cleanUp();
                    }
                } catch(Exception exc) {
                    if(LOG.isDebugEnabled())
                        LOG.debug(exc.getMessage());
                }
            }
        }
    }

    private File[] findFiles(File configDirectory, final String filter) {
        File[] files = configDirectory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.equalsIgnoreCase(filter);
            }
        });

        return files;
    }
}
