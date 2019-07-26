package org.onap.ccsdk.sli.adaptors.messagerouter.publisher.provider.impl;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.util.Base64;

import org.onap.ccsdk.sli.adaptors.messagerouter.publisher.api.PublisherApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublisherApiImpl implements PublisherApi {
	private static final Logger logger = LoggerFactory.getLogger(PublisherApiImpl.class);
	protected final Integer DEFAULT_CONNECT_TIMEOUT = 30000; // will be treated as 30 seconds
	protected final Integer DEFAULT_READ_TIMEOUT = 180000; // will be treated as 3 minutes
	private String authorizationString;
	protected Integer connectTimeout;
	protected Integer readTimeout;
	protected String baseUrl;
	protected String username;
	protected String[] hosts;
	private String password;

	public void setUsername(String username) {
		this.username = username;
		setAuthorizationString();
	}

	public void setPassword(String password) {
		this.password = password;
		setAuthorizationString();
	}

	public void setHost(String hostString) {
		// a comma separated list of hosts can be passed in or a single host may be used
		if (!hostString.contains(",")) {
			this.hosts = new String[] { hostString };
		} else {
			this.hosts = hostString.split(",");
		}
	}

	public PublisherApiImpl() {
		connectTimeout = DEFAULT_CONNECT_TIMEOUT;
		readTimeout = DEFAULT_READ_TIMEOUT;
	}

	public void init() {
		setAuthorizationString();
	}

	protected String buildUrlString(Integer hostIndex, String topic) {
		return hosts[hostIndex] + "/events/" + topic;
	}

	protected void configureHttpURLConnection(HttpURLConnection httpUrlConnection) {
		httpUrlConnection.setRequestProperty("Content-Type", "application/json");
	}

	public Boolean publish(String topic, String body) {
		for (int hostIndex = 0; hostIndex < hosts.length; hostIndex++) {
			HttpURLConnection httpUrlConnection = null;
			URL url = null;
			try {
				url = new URL(buildUrlString(hostIndex, topic));
				logger.info("Publishing body to topic {} using the url {}", topic, url);
				logger.info("Message to publish is\n{}", body);
				httpUrlConnection = buildHttpURLConnection(url);
				httpUrlConnection.setDoInput(true);
				httpUrlConnection.setDoOutput(true);
				httpUrlConnection.setUseCaches(false);
				httpUrlConnection.setRequestMethod("POST");

				// Write message
				httpUrlConnection.setRequestProperty("Content-Length", Integer.toString(body.length()));
				DataOutputStream outStr = new DataOutputStream(httpUrlConnection.getOutputStream());
				outStr.write(body.getBytes());
				outStr.close();

				int status = httpUrlConnection.getResponseCode();
				logger.info("Publishing body for topic {} using  url {} returned status {}.", topic, url, status);
				if (status < 300) {
					String responseFromDMaaP = readFromStream(httpUrlConnection.getInputStream());
					logger.info("Message router response is\n{}", responseFromDMaaP);
					return true;
				} else {
					if (httpUrlConnection.getErrorStream() != null) {
						String responseFromDMaaP = readFromStream(httpUrlConnection.getErrorStream());
						logger.warn("Publishing body for topic {} using  url {} failed." + " Error message is\n{}",
								topic, url, responseFromDMaaP);
					}
					return false;
				}

			} catch (SocketException socketException) {
				logger.error("SocketException was thrown during publishing message to DMaaP on url {}.", url,
						socketException);
				if (hostIndex < hosts.length) {
					logger.info("Message sent to {} failed with a SocketException, but will be tried on {}",
							hosts[hostIndex], hosts[hostIndex + 1]);
				}
			} catch (Exception e) {
				logger.warn("Exception was thrown during publishing message to DMaaP on url {}.", url, e);
				return false;
			} finally {
				if (httpUrlConnection != null) {
					httpUrlConnection.disconnect();
				}
			}
		}
		return false;
	}

	protected void setAuthorizationString() {
	    String str = buildAuthorizationString(this.username, this.password);
		this.authorizationString = str;
		//System.out.println(this.authorizationString);
	}
	
         protected String buildAuthorizationString(String username, String password) {
             String basicAuthString = username + ":" + password;
             basicAuthString = Base64.getEncoder().encodeToString(basicAuthString.getBytes());
             return "Basic " + basicAuthString;
        }

	protected HttpURLConnection buildHttpURLConnection(URL url) throws IOException {
		HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();
		if (authorizationString != null) {
		    System.out.println(authorizationString);
			httpUrlConnection.setRequestProperty("Authorization", authorizationString);
		}
		httpUrlConnection.setRequestProperty("Accept", "application/json");
		httpUrlConnection.setUseCaches(false);
		httpUrlConnection.setConnectTimeout(connectTimeout);
		httpUrlConnection.setReadTimeout(readTimeout);
		configureHttpURLConnection(httpUrlConnection);
		return httpUrlConnection;
	}

	protected String readFromStream(InputStream stream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		return sb.toString();
	}

}