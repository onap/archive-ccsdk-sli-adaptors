/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights
 *                      reserved.
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

package org.onap.ccsdk.sli.adaptors.messagerouter.consumer.provider.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.onap.ccsdk.sli.adaptors.messagerouter.consumer.api.ConsumerApi;
import org.onap.ccsdk.sli.adaptors.messagerouter.consumer.api.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/*
 * java.net based client to build message router consumers
 */
public class ConsumerImpl implements Runnable, ConsumerApi {
    private static final Logger LOG = LoggerFactory.getLogger(ConsumerImpl.class);
    private volatile Thread t;
    private static final String REQUEST_METHOD = "GET";

    private final String host;
    private final Integer connectTimeout;
    private final Integer readTimeout;
    private final Integer fetchPause;
    private final String group;
    private final String id;
    private final String filter;
    private final Integer limit;
    private final Integer timeoutQueryParamValue;
    private final String authorizationString;

    private RequestHandler requestHandler;
    private URL url;
    private Boolean subscribed;

    public ConsumerImpl(String username, String password, String host, String authentication, Integer connectTimeout, Integer readTimeout, Integer fetchPause, String group, String id, String filter, Integer limit, Integer timeoutQueryParamValue) {
	this.host = host;
	this.connectTimeout = connectTimeout;
	this.readTimeout = readTimeout;
	this.fetchPause = fetchPause;
	this.group = group;
	this.id = id;
	this.filter = filter;
	this.limit = limit;
	this.timeoutQueryParamValue = timeoutQueryParamValue;
	this.subscribed = false;

	if ("basic".equals(authentication)) {
	    if (username != null && password != null && username.length() > 0 && password.length() > 0) {
		authorizationString = buildAuthorizationString(username, password);
	    } else {
		throw new IllegalStateException("Authentication is set to basic but username or password is missing");
	    }
	} else if ("noauth".equals(authentication)) {
	    authorizationString = null;
	} else {
	    throw new IllegalStateException("Unknown authentication method: " + authentication);
	}
    }

    private void start() {
	t = new Thread(this);
	t.start();
	LOG.info("ConsumerImpl started. Fetch period is {} ms.", fetchPause);
    }

    public void stop() {
	t = null;
	LOG.info("ConsumerImpl stopped.");
    }

    @Override
    public void run() {
	if (this.url != null) {
	    Thread thisThread = Thread.currentThread();
	    while (t == thisThread) {
		String responseBody = performHttpOperation();
		if (responseBody != null && !responseBody.startsWith("[]")) {
		    LOG.info("New message was fetched from MessageRouter.");
		    LOG.trace("Fetched message is\n{}", responseBody);
		    try {
			String[] requests = new Gson().fromJson(responseBody, String[].class);
			if (requests != null) {
			    for (String request : requests) {
				if (request != null) {
				    requestHandler.handleRequest(request);
				}
			    }
			} else {
			    LOG.warn("Deserialization of received message results in null array.", responseBody);
			}
		    } catch (JsonParseException e) {
			LOG.warn("Received message has bad format. Expected format is JSON.");
		    }
		} else {
		    LOG.trace("No new message was fetched from MessageRouter.");
		}

		try {
		    LOG.trace("Next fetch from MessageRouter url {} after {} milliseconds.", url, fetchPause);
		    Thread.sleep(fetchPause);
		} catch (InterruptedException e) {
		    LOG.warn("Thread sleep was interrupted.", e);
		}
	    }
	} else {
	    LOG.error("URL is null, can't listen for messages");
	}
    }

    private String buildlUrlString(String topic) {
	StringBuilder sb = new StringBuilder();
	sb.append(host + "/events/" + topic + "/" + group + "/" + id);
	sb.append("?timeout=" + timeoutQueryParamValue);

	if (limit != null) {
	    sb.append("&limit=" + limit);
	}
	if (filter != null) {
	    sb.append("&filter=" + filter);
	}
	return sb.toString();
    }

    private String performHttpOperation() {
	HttpURLConnection httpUrlConnection = null;
	try {
	    httpUrlConnection = buildHttpURLConnection(url);
	    httpUrlConnection.setRequestMethod(REQUEST_METHOD);
	    httpUrlConnection.connect();
	    int status = httpUrlConnection.getResponseCode();
	    if (status < 300) {
		return readFromStream(httpUrlConnection.getInputStream());
	    } else {
		String response = readFromStream(httpUrlConnection.getErrorStream());
		LOG.warn("Fetching message from MessageRouter on url {} failed with http status {}. Error message is\n{}.", url, status, response);
	    }
	} catch (Exception e) {
	    LOG.warn("Exception was thrown during fetching message from MessageRouter on url {}.", url, e);
	} finally {
	    if (httpUrlConnection != null) {
		httpUrlConnection.disconnect();
	    }
	}
	return null;
    }

    private String buildAuthorizationString(String userName, String password) {
	String basicAuthString = userName + ":" + password;
	basicAuthString = Base64.getEncoder().encodeToString(basicAuthString.getBytes());
	return "Basic " + basicAuthString;
    }

    protected HttpURLConnection buildHttpURLConnection(URL url) throws IOException {
	HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
	if (authorizationString != null) {
	    httpUrlConnection.setRequestProperty("Authorization", authorizationString);
	}
	httpUrlConnection.setRequestProperty("Accept", "application/json");
	httpUrlConnection.setUseCaches(false);
	httpUrlConnection.setConnectTimeout(connectTimeout);
	httpUrlConnection.setReadTimeout(readTimeout);
	
        //ignore hostname errors when dealing with HTTPS connections
	if(httpUrlConnection instanceof  HttpsURLConnection) {
	   HttpsURLConnection conn = (HttpsURLConnection) httpUrlConnection;
	   conn.setHostnameVerifier(new HostnameVerifier() {
	    @Override
	    public boolean verify(String arg0, SSLSession arg1) {
		return true;
	    }});
	}
	return httpUrlConnection;
    }

    protected String readFromStream(InputStream stream) throws IOException {
	BufferedReader br = new BufferedReader(new InputStreamReader(stream));
	StringBuilder sb = new StringBuilder();
	String line;
	while ((line = br.readLine()) != null) {
	    sb.append(line);
	    sb.append("\n");
	}
	br.close();
	return sb.toString();
    }

    @Override
    public void subscribe(String topic, RequestHandler requestHandler) {
	if (!subscribed) {
	    subscribed = true;
	    try {
		this.url = new URL(buildlUrlString(topic));
	    } catch (MalformedURLException e) {
		LOG.error("Topic " + topic + " resulted in MalformedURLException", e);
	    }
	    this.requestHandler = requestHandler;
	    start();
	} else {
	    LOG.error("This client can only subscribe to a single topic, the call to subscribe with topic " + topic + " will be ignored.");
	}
    }

    @Override
    public void close() throws Exception {
	stop();
    }
}
