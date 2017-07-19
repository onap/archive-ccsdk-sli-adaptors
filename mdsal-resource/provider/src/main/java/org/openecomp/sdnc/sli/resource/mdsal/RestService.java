/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All rights
 * 						reserved.
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

package org.openecomp.sdnc.sli.resource.mdsal;

import java.io.BufferedReader;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;




public class RestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ConfigResource.class);
	
	public enum PayloadType {
		XML,
		JSON
	}
	
	private class SdncAuthenticator extends Authenticator {
		
		private String user;
		private String passwd;

		SdncAuthenticator(String user, String passwd) {
			this.user = user;
			this.passwd = passwd;
		}
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(user, passwd.toCharArray());
		}
		
	}
	
	private String user;
	private String passwd;
	private PayloadType payloadType;

	private String protocol;
	private String host;
	private String port;
	
	public RestService(String protocol, String host, String port, String user, String passwd, PayloadType payloadType) {
		this.protocol = protocol;
		this.host = host;
		this.port = port;
		this.user = user;
		this.passwd = passwd;
		this.payloadType = payloadType;
	}
	
	private HttpURLConnection getRestConnection(String urlString, String method) throws IOException
	{
		
		URL sdncUrl = new URL(urlString);
		Authenticator.setDefault(new SdncAuthenticator(user, passwd));
		
		HttpURLConnection conn = (HttpURLConnection) sdncUrl.openConnection();
		
		String authStr = user+":"+passwd;
		String encodedAuthStr = new String(Base64.encodeBase64(authStr.getBytes()));
		
		conn.addRequestProperty("Authentication", "Basic "+encodedAuthStr);
		
		conn.setRequestMethod(method);
		
		if (payloadType == PayloadType.XML) {
			conn.setRequestProperty("Content-Type", "application/xml");
			conn.setRequestProperty("Accept", "application/xml");
		} else {

			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
		}
		
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setUseCaches(false);
		
		return(conn);
		
	}
	

	private Document send(String urlString, byte[] msgBytes, String method) {
		Document response = null;
		String fullUrl = protocol + "://" + host + ":" + port + "/" + urlString;
		LOG.info("Sending REST "+method +" to "+fullUrl);
		
		if (msgBytes != null) {
			LOG.info("Message body:\n"+msgBytes);
		}
		
		try {
			HttpURLConnection conn = getRestConnection(fullUrl, method);

			if (conn instanceof HttpsURLConnection) {
				HostnameVerifier hostnameVerifier = new HostnameVerifier() {
					@Override
					public boolean verify(String hostname, SSLSession session) {
						return true;
					}
				};
				((HttpsURLConnection)conn).setHostnameVerifier(hostnameVerifier);
			}

			// Write message
			if (msgBytes != null) {
				conn.setRequestProperty("Content-Length", ""+msgBytes.length);
				DataOutputStream outStr = new DataOutputStream(conn.getOutputStream());
				outStr.write(msgBytes);
				outStr.close();
			} else {
				conn.setRequestProperty("Content-Length", "0");
			}


			// Read response
			BufferedReader respRdr;
			
			LOG.info("Response: "+conn.getResponseCode()+" "+conn.getResponseMessage());
			

			if (conn.getResponseCode() < 300) {

				respRdr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			} else {
				respRdr = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			}

			StringBuffer respBuff = new StringBuffer();

			String respLn;

			while ((respLn = respRdr.readLine()) != null) {
				respBuff.append(respLn+"\n");
			}
			respRdr.close();

			String respString = respBuff.toString();

			LOG.info("Response body :\n"+respString);

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();


			response = db.parse(new ByteArrayInputStream(respString.getBytes()));

		} catch (Exception e) {

			LOG.error("Caught exception executing REST command", e);
		}
		
		return (response);
	}

	
	public Document get(String urlString) {
		return(send(urlString, null, "GET"));
	}
	
	public Document delete(String urlString) {
		return(send(urlString, null, "DELETE"));
	}
	
	public Document post(String urlString, byte[] msgBytes) {
		return(send(urlString, msgBytes, "POST"));
	}

	public Document put(String urlString, byte[] msgBytes) {
		return(send(urlString, msgBytes, "PUT"));
	}
}
