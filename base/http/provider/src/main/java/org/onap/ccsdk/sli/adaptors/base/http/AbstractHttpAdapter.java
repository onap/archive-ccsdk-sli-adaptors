package org.onap.ccsdk.sli.adaptors.base.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Base64;
import java.util.Properties;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import javax.ws.rs.client.ClientBuilder;
import org.onap.logging.filter.base.MetricLogClientFilter;
import org.onap.logging.filter.base.PayloadLoggingClientFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractHttpAdapter {
    protected static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";
    private static final String SDNC_CONFIG_DIR_DEFAULT = "/opt/sdnc/data/properties";
    protected static final int DEFAULT_HTTP_CONNECT_TIMEOUT_MS = 60000; // 1 minute
    protected static final int DEFAULT_HTTP_READ_TIMEOUT_MS = 1800000; // 30 minutes
    protected ClientBuilder clientBuilder;

    private static final Logger logger = LoggerFactory.getLogger(AbstractHttpAdapter.class);

    public AbstractHttpAdapter() {
        clientBuilder = ClientBuilder.newBuilder();
        setTimeouts();
        defaultHostNameVerifier();
    }
    
    private void defaultHostNameVerifier() {
        clientBuilder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
    }

    protected void enableMetricLogging() {
        clientBuilder.register(new MetricLogClientFilter());
    }

    protected void enablePayloadLogging() {
        clientBuilder.register(new PayloadLoggingClientFilter());
    }

    private void setTimeouts() {
        Integer httpReadTimeout = readOptionalInteger("HTTP_READ_TIMEOUT_MS", DEFAULT_HTTP_READ_TIMEOUT_MS);
        Integer httpConnectTimeout = readOptionalInteger("HTTP_CONNECT_TIMEOUT_MS", DEFAULT_HTTP_CONNECT_TIMEOUT_MS);

        // restore once we migrate to once we migrate to javax.ws.rs-api 2.1
        // clientBuilder.connectTimeout(30, TimeUnit.SECONDS);
        // clientBuilder.readTimeout(30, TimeUnit.SECONDS);

        // Setting jersey specific properties is ugly, such behavior should be removed
        // once we migrate to javax.ws.rs-api 2.1
        clientBuilder.property("jersey.config.client.readTimeout", httpReadTimeout);
        clientBuilder.property("jersey.config.client.connectTimeout", httpConnectTimeout);
    }

    public Properties getProperties(String propertiesFileName) throws FileNotFoundException, IOException {
        // Check System property, then environment variable then default if null
        String propDir = System.getProperty(SDNC_CONFIG_DIR);
        if (propDir == null || propDir.length() < 1) {
            propDir = System.getenv(SDNC_CONFIG_DIR);
        }
        if (propDir == null || propDir.length() < 1) {
            propDir = SDNC_CONFIG_DIR_DEFAULT;
        }
        Properties properties = new Properties();
        // forward slash is checked to support path src/test/resources on windows machine
        if (!propDir.endsWith(File.separator) && !propDir.endsWith("/")) {
            propDir = propDir + File.separator;
        }
        String path = propDir + propertiesFileName;
        properties.load(new FileInputStream(path));
        logger.trace("Initialized properties from ({}) properties ({})", path, properties);
        return properties;
    }

    protected void addBasicAuthCredentials(String username, String password) {
        String basicAuthValue = getBasicAuthValue(username,password);
        clientBuilder.register(new BasicAuthFilter(basicAuthValue));
    }

    protected String getBasicAuthValue(String userName, String password) {
        String token = userName + ":" + password;
        try {
            return "Basic " + Base64.getEncoder().encodeToString(token.getBytes());
        } catch (Exception e) {
            logger.error("getBasicAuthValue threw an exception, credentials will be null", e);
        }
        return null;
    }

    public ClientBuilder getClientBuilder() {
        return clientBuilder;
    }

    private Integer readOptionalInteger(String propertyName, Integer defaultValue) {
        String stringValue = System.getProperty(propertyName);
        if (stringValue != null && stringValue.length() > 0) {
            try {
                return Integer.valueOf(stringValue);
            } catch (NumberFormatException e) {
                logger.warn("property " + propertyName + " had the value " + stringValue + " that could not be converted to an Integer, default " + defaultValue + " will be used instead", e);
            }
        }
        return defaultValue;
    }

}
