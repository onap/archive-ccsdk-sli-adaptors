package org.onap.ccsdk.sli.adaptors.messagerouter.consumer.provider.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ConsumerFactory.class);

    // Default values to minimize required configuration
    private static final int DEFAULT_FETCH_PAUSE = 5000;
    private static final int DEFAULT_CONNECT_TIMEOUT = 30000;
    private static final int DEFAULT_READ_TIMEOUT = 180000;
    private static final int DEFAULT_LIMIT = 5; // Limits the number of messages pulled in a single GET request
    private static final int DEFAULT_TIMEOUT_QUERY_PARAM_VALUE = 15000;
    private static final String DEFAULT_AUTH_METHOD = "basic";

    // Required properties
    protected final String username;
    protected final String password;
    protected final String host;
    private final String group;
    private final String id;

    // Optional properties
    protected Integer connectTimeout;
    protected Integer readTimeout;
    private Integer fetchPause;
    private Integer limit;
    private Integer timeoutQueryParamValue;
    private String filter;
    protected String auth;

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
    
    public Integer getConnectTimeout() {
	return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
	this.connectTimeout = connectTimeout;
    }

    public Integer getReadTimeout() {
	return readTimeout;
    }

    public void setReadTimeout(Integer readTimeout) {
	this.readTimeout = readTimeout;
    }

    public Integer getFetchPause() {
	return fetchPause;
    }

    public void setFetchPause(Integer fetchPause) {
	this.fetchPause = fetchPause;
    }

    public Integer getLimit() {
	return limit;
    }

    public void setLimit(Integer limit) {
	this.limit = limit;
    }

    public Integer getTimeoutQueryParamValue() {
	return timeoutQueryParamValue;
    }

    public void setTimeoutQueryParamValue(Integer timeoutQueryParamValue) {
	this.timeoutQueryParamValue = timeoutQueryParamValue;
    }

    public String getFilter() {
	return filter;
    }

    public void setFilter(String filter) {
	processFilter(filter);
    }

    public ConsumerFactory(String username, String password, String host, String group, String id, Integer connectTimeout, Integer readTimeout) {
	this.username = username;
	this.password = password;
	this.host = host;
	this.group = group;
	this.id = id;
	setDefaults();
    }

    public ConsumerFactory(Properties properties) {
	// Required properties
	username = properties.getProperty("username");
	password = properties.getProperty("password");
	host = properties.getProperty("host");
	auth = properties.getProperty("auth");
	group = properties.getProperty("group");
	id = properties.getProperty("id");

	// Optional properties
	connectTimeout = readOptionalInteger(properties, "connectTimeoutSeconds");
	readTimeout = readOptionalInteger(properties, "readTimeoutMinutes");
	fetchPause = readOptionalInteger(properties, "fetchPause");
	limit = readOptionalInteger(properties, "limit");
	timeoutQueryParamValue = readOptionalInteger(properties, "timeout");
	processFilter(properties.getProperty("filter"));

	setDefaults();
    }

    private Integer readOptionalInteger(Properties properties, String propertyName) {
	String stringValue = properties.getProperty(propertyName);
	if (stringValue != null && stringValue.length() > 0) {
	    try {
		return Integer.valueOf(stringValue);
	    } catch (NumberFormatException e) {
		LOG.error("property " + propertyName + " had the value " + stringValue + " that could not be converted to an Integer", e);
	    }
	}
	return null;
    }

    public ConsumerImpl createClient() {
	return new ConsumerImpl(username, password, host, auth, connectTimeout, readTimeout, fetchPause, group, id, filter, limit, timeoutQueryParamValue);
    }

    private void processFilter(String filterString) {
	if (filterString != null) {
	    if (filterString.length() > 0) {
		try {
		    filter = URLEncoder.encode(filterString, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
		    LOG.warn("Couldn't encode filter string. Filter will be ignored.", e);
		    filter = null;
		}
	    } else {
		filter = null;
	    }
	}
    }

    private void setDefaults() {
	if (connectTimeout == null) {
	    connectTimeout = DEFAULT_CONNECT_TIMEOUT;
	}
	if (readTimeout == null) {
	    readTimeout = DEFAULT_READ_TIMEOUT;
	}
	if (fetchPause == null) {
	    fetchPause = DEFAULT_FETCH_PAUSE;
	}
	if (limit == null) {
	    limit = DEFAULT_LIMIT;
	}
	if (timeoutQueryParamValue == null) {
	    timeoutQueryParamValue = DEFAULT_TIMEOUT_QUERY_PARAM_VALUE;
	}
	if (auth == null) {
	    auth = DEFAULT_AUTH_METHOD;
	}
    }

}
