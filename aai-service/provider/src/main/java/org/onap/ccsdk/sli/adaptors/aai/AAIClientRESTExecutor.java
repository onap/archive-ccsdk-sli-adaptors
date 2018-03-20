/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
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

package org.onap.ccsdk.sli.adaptors.aai;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.ws.rs.HttpMethod;

import org.apache.commons.codec.binary.Base64;
import org.onap.ccsdk.sli.adaptors.aai.AAIService.TransactionIdTracker;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.onap.ccsdk.sli.adaptors.aai.data.ErrorResponse;
import org.onap.ccsdk.sli.adaptors.aai.data.RequestError;
import org.onap.ccsdk.sli.adaptors.aai.data.ResourceVersion;
import org.onap.ccsdk.sli.adaptors.aai.data.ServiceException;
import org.onap.ccsdk.sli.core.sli.MetricLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

/**
 * The AAIClientRESTExecutor class provides CRUD API for AAI Client service.
 * @author  richtabedzki
 */
public     class AAIClientRESTExecutor implements AAIExecutorInterface {

    private final String truststorePath;
    private final String truststorePassword;
    private final String keystorePath;
    private final String keystorePassword;
    private final Boolean ignoreCertificateHostError;
    // authentication credentials
    private String userName;
    private String userPassword;
    private final String applicationId;

    /**
     * class Constructor
     * @param props - properties to initialize an instance.
     */
    public AAIClientRESTExecutor(Properties props) {
        super();

        userName            = props.getProperty(AAIService.CLIENT_NAME);
        userPassword        = props.getProperty(AAIService.CLIENT_PWWD);

        if(userName == null || userName.isEmpty()){
            LOG.debug("Basic user name is not set");
        }
        if(userPassword == null || userPassword.isEmpty()) {
            LOG.debug("Basic password is not set");
        }

        truststorePath     = props.getProperty(AAIService.TRUSTSTORE_PATH);
        truststorePassword = props.getProperty(AAIService.TRUSTSTORE_PSSWD);
        keystorePath         = props.getProperty(AAIService.KEYSTORE_PATH);
        keystorePassword     = props.getProperty(AAIService.KEYSTORE_PSSWD);

        String tmpApplicationId =props.getProperty(AAIService.APPLICATION_ID);
        if(tmpApplicationId == null || tmpApplicationId.isEmpty()) {
            tmpApplicationId = "SDNC";
        }
        applicationId = tmpApplicationId;

        String iche = props.getProperty(AAIService.CERTIFICATE_HOST_ERROR);
        boolean host_error = false;
        if(iche != null && !iche.isEmpty()) {
            host_error = Boolean.valueOf(iche);
        }

        ignoreCertificateHostError = host_error;

        HttpsURLConnection.setDefaultHostnameVerifier( new HostnameVerifier(){
            public boolean verify(String string,SSLSession ssls) {
                return ignoreCertificateHostError;
            }
        });

        if(truststorePath != null && truststorePassword != null && (new File(truststorePath)).exists()) {
            System.setProperty("javax.net.ssl.trustStore", truststorePath);
            System.setProperty("javax.net.ssl.trustStorePassword", truststorePassword);
        }

        if(keystorePath != null && keystorePassword != null && (new File(keystorePath)).exists())
        {
            DefaultClientConfig config = new DefaultClientConfig();
            //both jersey and HttpURLConnection can use this
            SSLContext ctx = null;
            try {
                ctx = SSLContext.getInstance("TLS");

                KeyManagerFactory kmf = null;
                try {
                    String storeType = "PKCS12";
                    String def = KeyStore.getDefaultType();
                    kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                    FileInputStream fin = new FileInputStream(keystorePath);

                    String extension = keystorePath.substring(keystorePath.lastIndexOf(".") + 1);

                    if(extension != null && !extension.isEmpty() && extension.equalsIgnoreCase("JKS")) {
                        storeType = "JKS";
                    }
                    KeyStore ks = KeyStore.getInstance(storeType);

                    char[] pwd = keystorePassword.toCharArray();
                    ks.load(fin, pwd);
                    kmf.init(ks, pwd);
                } catch (Exception ex) {
                    LOG.error("AAIResource", ex);
                }

                ctx.init(kmf.getKeyManagers(), null, null);
                config.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, new HTTPSProperties( new HostnameVerifier() {
                        @Override
                        public boolean verify( String s, SSLSession sslSession ) {
                            return ignoreCertificateHostError;
                        }
                }, ctx));

                CTX = ctx;
                LOG.debug("SSLContext created");

            } catch (KeyManagementException | NoSuchAlgorithmException exc) {
                LOG.error("AAIResource", exc);
            }
        }

        try {
            Field methodsField = HttpURLConnection.class.getDeclaredField("methods");
            methodsField.setAccessible(true);
            // get the methods field modifiers
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            // bypass the "private" modifier
            modifiersField.setAccessible(true);

            // remove the "final" modifier
            modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

            /* valid HTTP methods */
            String[] methods = {
                       "GET", "POST", "HEAD", "OPTIONS", "PUT", "DELETE", "TRACE", "PATCH"
            };
            // set the new methods - including patch
            methodsField.set(null, methods);

        } catch (SecurityException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException e) {
            LOG.warn("Adding PATCH method", e);
        }
        LOG.info("AAIResource.ctor initialized.");

    }

    private static final Logger LOG = LoggerFactory.getLogger(AAIService.class);
    private final MetricLogger ml = new MetricLogger();

    private SSLContext CTX;


    private int connection_timeout = 300000;

    private int read_timeout = 300000;

    /**
     * Returns an String that contains JSON data returned from the AAI Server.
     * <p>
     * This method always returns immediately, whether or not the
     * data exists.
     *
     * @param  request  an instance of AAIRequiest representing
     *                 the request made by DirectedGraph node.
     * @return      the JSON based representation of data instance requested.
     * @see         String
     */
    @Override
    public String get(AAIRequest request) throws AAIServiceException {
        String response = null;
        InputStream inputStream = null;
        HttpURLConnection con = null;
        URL requestUrl = null;

        StringBuilder errorStringBuilder = new StringBuilder();

        try {

            if(request.getRequestObject() != null) {
                requestUrl = request.getRequestUrl(HttpMethod.POST, null);
                requestUrl = appendDepth(requestUrl, request);
                con = getConfiguredConnection(requestUrl, HttpMethod.POST);
                String json_text = request.toJSONString();
                LOGwriteDateTrace("data", json_text);
                logMetricRequest("POST "+requestUrl.getPath(), json_text, requestUrl.getPath());
                OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
                osw.write(json_text);
                osw.flush();
            } else {
                requestUrl = request.getRequestUrl(HttpMethod.GET, null);
                requestUrl = appendDepth(requestUrl, request);
                con = getConfiguredConnection(requestUrl, HttpMethod.GET);
                logMetricRequest("GET "+requestUrl.getPath(), "", requestUrl.getPath());
            }

            // Check for errors
            String responseMessage = con.getResponseMessage();
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            // Process the response
            LOG.debug("HttpURLConnection result:" + responseCode + " : " + responseMessage);
            logMetricResponse(responseCode, responseMessage);

            if(inputStream == null) inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );

            ObjectMapper mapper = AAIService.getObjectMapper();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                while( ( line = reader.readLine() ) != null ) {
                    stringBuilder.append( line );
                }
                response = stringBuilder.toString();
                try {
                    Object object = mapper.readValue(response, Object.class);
                    LOGwriteEndingTrace(HttpURLConnection.HTTP_OK, responseMessage, mapper.writeValueAsString(object));
                } catch(Exception exc) {
                    LOGwriteEndingTrace(HttpURLConnection.HTTP_OK, responseMessage, mapper.writeValueAsString(response));
                }
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                LOGwriteEndingTrace(responseCode, responseMessage, "Entry does not exist.");
                ErrorResponse errorresponse = null;
                try {
                    errorresponse = mapper.readValue(reader, ErrorResponse.class);
                } catch(Exception exc) {
                    errorresponse = new ErrorResponse();
                    RequestError requestError = new RequestError();
                    ServiceException serviceException = new ServiceException();
                    serviceException.setText("Entry does not exist.");
                    requestError.setServiceException(serviceException);
                    errorresponse.setRequestError(requestError );
                }
                throw new AAIServiceException(responseCode, errorresponse);
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                StringBuilder stringBuilder = new StringBuilder();
                String line = null;
                while( ( line = reader.readLine() ) != null ) {
                    stringBuilder.append( line );
                }
                LOGwriteEndingTrace(responseCode, responseMessage, stringBuilder.toString());
                ServiceException serviceException = new ServiceException();
                serviceException.setMessageId("HTTP_UNAUTHORIZED");
                serviceException.setText(stringBuilder.toString());
                RequestError requestError = new RequestError();
                requestError.setServiceException(serviceException);
                ErrorResponse errorresponse = new ErrorResponse();
                errorresponse.setRequestError(requestError);
                throw new AAIServiceException(responseCode, errorresponse);
            } else {
                String line = null;
                while( ( line = reader.readLine() ) != null ) {
                    errorStringBuilder.append("\n").append( line );
                }

                ErrorResponse errorresponse = mapper.readValue(errorStringBuilder.toString(), ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, responseMessage, mapper.writeValueAsString(errorresponse));
                throw new AAIServiceException(responseCode, errorresponse);
            }

        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn(errorStringBuilder.toString(), exc);
            throw new AAIServiceException(exc);
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch(Exception exc) {
                    LOG.warn("", exc);
                }
            }
        }
        return response;
    }

    /**
     * Returns an String that contains JSON data returned from the AAI Server.
     * <p>
     * This method always returns immediately, whether or not the
     * data exists.
     *
     * @param  request  an instance of AAIRequiest representing
     *                 the request made by DirectedGraph node.
     * @return      the JSON based representation of data instance requested.
     * @see         String
     */
    @Override
    public String post(AAIRequest request) throws AAIServiceException {
        InputStream inputStream = null;

        try {
            String resourceVersion = null;
            AAIDatum instance = request.getRequestObject();

            try {
                Method getResourceVersionMethod = instance.getClass().getMethod("getResourceVersion");
                if(getResourceVersionMethod != null){
                    try {
                        Object object = getResourceVersionMethod.invoke(instance);
                        if(object != null)
                            resourceVersion = object.toString();
                    } catch (InvocationTargetException exc) {
                        LOG.warn("", exc);
                    }
                }
            } catch(Exception exc) {
                LOG.error("", exc);
            }

            URL requestUrl = request.getRequestUrl(HttpMethod.PUT, resourceVersion);
            HttpURLConnection con = getConfiguredConnection(requestUrl, HttpMethod.PUT);
            ObjectMapper mapper = AAIService.getObjectMapper();
            String jsonText = request.toJSONString();

            LOGwriteDateTrace("data", jsonText);
            logMetricRequest("PUT "+requestUrl.getPath(), jsonText, requestUrl.getPath());

            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
            osw.write(jsonText);
            osw.flush();

            // Check for errors
            String responseMessage = con.getResponseMessage();
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            LOG.debug("HttpURLConnection result:" + responseCode + " : " + responseMessage);
            logMetricResponse(responseCode, responseMessage);

            // Process the response
            BufferedReader reader;
            String line = null;
            reader = new BufferedReader( new InputStreamReader( inputStream ) );
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                StringBuilder stringBuilder = new StringBuilder();

                while( ( line = reader.readLine() ) != null ) {
                    stringBuilder.append( line );
                }
                LOGwriteEndingTrace(responseCode, responseMessage, (stringBuilder.length() > 0) ? stringBuilder.toString() : "{no-data}");
                return stringBuilder.toString();
            } else {
                ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, responseMessage, mapper.writeValueAsString(errorresponse));

                throw new AAIServiceException(responseCode, errorresponse);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("AAIRequestExecutor.post", exc);
            throw new AAIServiceException(exc);
        } finally {
            try {
                if(inputStream != null)
                inputStream.close();
            } catch (Exception exc) {
                LOG.warn("AAIRequestExecutor.post", exc);
            }
        }
    }

    /**
     * Returns Boolean that contains completion state of the command executed.
     * <p>
     * This method always returns immediately, whether or not the
     * data exists.
     *
     * @param  request  an instance of AAIRequiest representing
     * @param  resourceVersion  a resource version of the data instacne to be deleted.
     *                 the request made by DirectedGraph node.
     * @return      completion state of the command.
     * @see         String
     */
    @Override
    public Boolean delete(AAIRequest request, String resourceVersion) throws AAIServiceException {
        Boolean response = null;
        InputStream inputStream = null;

        if(resourceVersion == null) {
            throw new AAIServiceException("resource-version is required for DELETE request");
        }

        try {
            URL requestUrl = request.getRequestUrl(HttpMethod.DELETE, resourceVersion);
            HttpURLConnection conn = getConfiguredConnection(requestUrl, HttpMethod.DELETE);
            logMetricRequest("DELETE "+requestUrl.getPath(), "", requestUrl.getPath());
            conn.setDoOutput(true);

            // Check for errors
            String responseMessage = conn.getResponseMessage();
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                inputStream = conn.getInputStream();
            } else {
                inputStream = conn.getErrorStream();
            }

            // Process the response
            LOG.debug("HttpURLConnection result:" + responseCode + " : " + responseMessage);
            logMetricResponse(responseCode, responseMessage);

            if(inputStream == null) inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );
            String line = null;

            ObjectMapper mapper = AAIService.getObjectMapper();

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                StringBuilder stringBuilder = new StringBuilder();

                while( ( line = reader.readLine() ) != null ) {
                    stringBuilder.append( line );
                }
                LOGwriteEndingTrace(responseCode, responseMessage, stringBuilder.toString());
                response = true;
            } else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND ) {
                LOGwriteEndingTrace(responseCode, responseMessage, "Entry does not exist.");
                response = false;
            } else {
                ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, responseMessage, mapper.writeValueAsString(errorresponse));
                throw new AAIServiceException(responseCode, errorresponse);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("delete", exc);
            throw new AAIServiceException(exc);
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch(Exception exc) {
                    LOG.warn("delete", exc);
                }
            }
        }
        return response;
    }

    /**
     * Returns an String that contains JSON data returned from the AAI Server.
     * <p>
     * This method always returns immediately, whether or not the
     * data exists.
     *
     * @param  request  an instance of AAIRequiest representing
     *                 the request made by DirectedGraph node.
     * @param clas   an definition of the class for which data will be returned
     * @return      the instance of the class with data.
     * @see         String
     */
    @Override
    public Object query(AAIRequest request, Class clas) throws AAIServiceException {
        Object response = null;
        InputStream inputStream = null;

        try {
            URL requestUrl = request.getRequestQueryUrl(HttpMethod.GET);
            HttpURLConnection con = getConfiguredConnection(requestUrl, HttpMethod.GET);
            logMetricRequest("GET "+requestUrl.getPath(), "", requestUrl.getPath());

            // Check for errors
            String responseMessage = con.getResponseMessage();
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            logMetricResponse(responseCode, responseMessage);
            ObjectMapper mapper = AAIService.getObjectMapper();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Process the response
                BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );
                response = mapper.readValue(reader, clas);
                LOGwriteEndingTrace(HttpURLConnection.HTTP_OK, "SUCCESS", mapper.writeValueAsString(response));
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                LOGwriteEndingTrace(responseCode, "HTTP_NOT_FOUND", "Entry does not exist.");
                return response;
            } else {
                BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );
                ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, "FAILURE", mapper.writeValueAsString(errorresponse));
                throw new AAIServiceException(responseCode, errorresponse);
            }

        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("GET", exc);
            throw new AAIServiceException(exc);
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch(Exception exc) {
                    LOG.warn("GET", exc);
                }
            }
        }
        return response;
    }

    @Override
    public Boolean patch(AAIRequest request, String resourceVersion) throws AAIServiceException {
        InputStream inputStream = null;

        try {
            AAIDatum instance = request.getRequestObject();
            if(instance instanceof ResourceVersion) {
                resourceVersion = ((ResourceVersion)instance).getResourceVersion();
            }

            URL requestUrl = null;
            HttpURLConnection con = getConfiguredConnection(requestUrl = request.getRequestUrl("PATCH", resourceVersion), "PATCH");
            ObjectMapper mapper = AAIService.getObjectMapper();
            String jsonText = request.toJSONString();

            LOGwriteDateTrace("data", jsonText);
            logMetricRequest("PATCH "+requestUrl.getPath(), jsonText, requestUrl.getPath());

            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
            osw.write(jsonText);
            osw.flush();

            // Check for errors
            String responseMessage = con.getResponseMessage();
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            LOG.info("HttpURLConnection result: " + responseCode + " : " + responseMessage);
            logMetricResponse(responseCode, responseMessage);

            // Process the response
            BufferedReader reader;
            String line = null;
            reader = new BufferedReader( new InputStreamReader( inputStream ) );
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                StringBuilder stringBuilder = new StringBuilder();

                while( ( line = reader.readLine() ) != null ) {
                    stringBuilder.append( line );
                }
                LOGwriteEndingTrace(responseCode, responseMessage, (stringBuilder.length() > 0) ? stringBuilder.toString() : "{no-data}");
                return true;
            } else {
                StringBuilder stringBuilder = new StringBuilder();

                while( ( line = reader.readLine() ) != null ) {
                    stringBuilder.append("\n").append( line );
                }
                LOG.info(stringBuilder.toString());


                ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, responseMessage, mapper.writeValueAsString(errorresponse));

                throw new AAIServiceException(responseCode, errorresponse);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("AAIRequestExecutor.patch", exc);
            throw new AAIServiceException(exc);
        } finally {
            try {
                if(inputStream != null)
                inputStream.close();
            } catch (Exception exc) {
                LOG.warn("AAIRequestExecutor.patch", exc);
            }
        }
    }

    /**
     *
     * @param httpReqUrl
     * @param method
     * @return
     * @throws Exception
     */
    protected HttpURLConnection getConfiguredConnection(URL httpReqUrl, String method) throws Exception {
        HttpURLConnection con = (HttpURLConnection) httpReqUrl.openConnection();

        // Set up the connection properties
        con.setRequestProperty("Connection", "close");
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setConnectTimeout(connection_timeout);
        con.setReadTimeout(read_timeout);
        con.setRequestMethod(method);
        con.setRequestProperty("Accept", "application/json");
        con.setRequestProperty("Transfer-Encoding","chunked");
        con.setRequestProperty("Content-Type",
                "PATCH".equalsIgnoreCase(method) ? "application/merge-patch+json" : "application/json");
        con.setRequestProperty("X-FromAppId", applicationId);
        con.setRequestProperty("X-TransactionId", TransactionIdTracker.getNextTransactionId());
        String mlId = ml.getRequestID();
        if (mlId != null && !mlId.isEmpty()) {
            LOG.debug(String.format("MetricLogger requestId = %s", mlId));
            con.setRequestProperty(MetricLogger.REQUEST_ID, mlId);
        } else {
            LOG.debug("MetricLogger requestId is null");
        }

        if (userName != null && !userName.isEmpty() && userPassword != null && !userPassword.isEmpty()) {
            String basicAuth = "Basic " + new String(Base64.encodeBase64((userName + ":" + userPassword).getBytes()));
            con.setRequestProperty("Authorization", basicAuth);
        }

        if (con instanceof HttpsURLConnection && CTX != null) {
            SSLSocketFactory sockFact = CTX.getSocketFactory();
            HttpsURLConnection.class.cast(con).setSSLSocketFactory(sockFact);
        }
        return con;
    }

    private URL appendDepth(URL requestUrl, AAIRequest request) throws MalformedURLException {

        String depth = request.requestProperties.getProperty("depth", "1");
        String path = requestUrl.toString();
        if(path.contains("?depth=") || path.contains("&depth=")) {
            return requestUrl;
        } else {
            if(path.contains("?")) {
                path = String.format("%s&depth=%s", path, depth);
            } else {
                path = String.format("%s?depth=%s", path, depth);
            }
            return new URL(path);
        }
    }

    public void logMetricRequest(String targetServiceName, String msg, String path){
        String svcInstanceId = "";
        String svcName = null;
        String partnerName = null;
        String targetEntity = "A&AI";
        String targetVirtualEntity = null;

        ml.logRequest(svcInstanceId, svcName, partnerName, targetEntity, targetServiceName, targetVirtualEntity, msg);
    }

    public void logMetricResponse(int responseCode, String responseDescription){
        ml.logResponse(responseCode < 400 ? "COMPLETE" : "ERROR", Integer.toString(responseCode), responseDescription);
    }

    protected void LOGwriteFirstTrace(String method, String url) {
        String time = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(System.currentTimeMillis());
        LOG.info("A&AI transaction :");
        LOG.info("Request Time : " + time + ", Method : " + method);
        LOG.info("Request URL : "+ url);
    }

    protected void LOGwriteDateTrace(String name, String data) {
        LOG.info("Input - " + name  + " : " + data);
    }

    protected void LOGwriteEndingTrace(int response_code, String comment, String data) {
        LOG.info("Response code : " + response_code +", " + comment);
        LOG.info(String.format("Response data : %s", data));
    }

}
