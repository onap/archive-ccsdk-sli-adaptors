/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

public class RestService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigResource.class);
    private String user;
    private String passwd;
    private String contentType;
    private String accept;
    private String protocol;
    private String host;
    private String port;

    public RestService(String protocol, String host, String port, String user, String passwd, String accept, String contentType) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.user = user;
        this.passwd = passwd;
        this.accept = accept;
        this.contentType = contentType;
    }

    private HttpURLConnection getRestConnection(String urlString, String method) throws IOException {
        URL sdncUrl = new URL(urlString);
        Authenticator.setDefault(new SdncAuthenticator(user, passwd));

        String authStr = user + ":" + passwd;
        String encodedAuthStr = new String(Base64.encodeBase64(authStr.getBytes()));

        HttpURLConnection conn = (HttpURLConnection) sdncUrl.openConnection();
        conn.addRequestProperty("Authentication", "Basic " + encodedAuthStr);
        conn.setRequestMethod(method);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);

        //Setting Accept header (doesn't dependent on Msg Body if present or not)
        if ("XML".equalsIgnoreCase(accept)) {
            conn.setRequestProperty("Accept", "application/xml");
        } else {
            conn.setRequestProperty("Accept", "application/json");
        }

        return (conn);
    }

    private Document send(String urlString, byte[] msgBytes, String method) {
        Document response = null;
        String fullUrl = protocol + "://" + host + ":" + port + "/" + urlString;
        LOG.info("Sending REST {} to {}", method, fullUrl);

        try {
            HttpURLConnection conn = getRestConnection(fullUrl, method);
            if (conn instanceof HttpsURLConnection) {
                HostnameVerifier hostnameVerifier = (hostname, session) -> true;
                ((HttpsURLConnection) conn).setHostnameVerifier(hostnameVerifier);
            }

            // Write message
            if (msgBytes != null) {
                LOG.info("Message body:\n{}", msgBytes);
                conn.setRequestProperty("Content-Length", "" + msgBytes.length);

                // Setting Content-Type header only if Msg Body is present
                if ("XML".equalsIgnoreCase(contentType)) {
                    conn.setRequestProperty("Content-Type", "application/xml");
                } else {
                    conn.setRequestProperty("Content-Type", "application/json");
                }

                DataOutputStream outStr = new DataOutputStream(conn.getOutputStream());
                outStr.write(msgBytes);
                outStr.close();
            } else {
                conn.setRequestProperty("Content-Length", "0");
            }

            // Read response
            LOG.info("Response: {} {}", conn.getResponseCode(), conn.getResponseMessage());

            BufferedReader respRdr;
            if (conn.getResponseCode() < 300) {
                respRdr = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                respRdr = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuffer respBuff = new StringBuffer();
            String respLn;
            while ((respLn = respRdr.readLine()) != null) {
                respBuff.append(respLn + "\n");
            }
            respRdr.close();

            String respString = respBuff.toString();
            LOG.info("Response body :\n{}", respString);

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);  
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false); 
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            DocumentBuilder db = dbf.newDocumentBuilder();

            response = db.parse(new ByteArrayInputStream(respString.getBytes()));

        } catch (Exception e) {
            LOG.error("Caught exception executing REST command", e);
        }

        return (response);
    }

    public Document get(String urlString) {
        return (send(urlString, null, "GET"));
    }

    public Document delete(String urlString) {
        return (send(urlString, null, "DELETE"));
    }

    public Document post(String urlString, byte[] msgBytes) {
        return (send(urlString, msgBytes, "POST"));
    }

    public Document put(String urlString, byte[] msgBytes) {
        return (send(urlString, msgBytes, "PUT"));
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

}
