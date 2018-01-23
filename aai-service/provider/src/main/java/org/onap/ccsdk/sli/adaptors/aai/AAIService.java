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


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.ws.rs.HttpMethod;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.onap.ccsdk.sli.adaptors.aai.data.ErrorResponse;
import org.onap.ccsdk.sli.adaptors.aai.data.notify.NotifyEvent;
import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.onap.ccsdk.sli.core.sli.MetricLogger;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.openecomp.aai.inventory.v11.GenericVnf;
import org.openecomp.aai.inventory.v11.PhysicalLink;
import org.openecomp.aai.inventory.v11.ResultData;
import org.openecomp.aai.inventory.v11.SearchResults;
import org.openecomp.aai.inventory.v11.Vserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;


public class AAIService extends AAIDeclarations implements AAIClient, SvcLogicResource {

    public static final String AAICLIENT_PROPERTIES = "/aaiclient.properties";
    public static final String PATH_PROPERTIES = "/aai-path.properties";

    private static final Logger LOG = LoggerFactory.getLogger(AAIService.class);

    private final String truststorePath;
    private final String truststorePassword;
    private final String keystorePath;
    private final String keystorePassword;
    private final Boolean ignoreCertificateHostError;

    private final String targetUri;
    private final String queryPath;

    private final String networkVserverPath;

    private final String svcInstancePath;
    private final String svc_inst_qry_path;

    private final String vnf_image_query_path;

    private final String param_service_type;            //= "service-type";

    private final String ubb_notify_path;
    private final String selflinkAvpn;
    private final String selflinkFqdn;

    private final String pInterfacePath;

    private final String servicePath;
    private final String sitePairSetPath;

    private final int connectionTimeout;
    private final int readTimeout;

    // 1602
    private final String queryNodesPath;
    private final String updatePath;

    private final String applicationId;

    // authentication credentials
    private String userName;
    private String userPassword;

    // runtime
    private final boolean runtimeOSGI;

    private SSLContext CTX;

    private final MetricLogger ml = new MetricLogger();

    private AAIExecutorInterface executor;

    public AAIService(final UtilsProvider configuration) {
        this(configuration.getProperties());
    }
    
    public AAIService(final URL url) {
        this(getProperties(url));
    }

    private static Properties getProperties(URL url) {
		Properties properties = new Properties();
		try {
			properties.load(url.openStream());
		} catch (IOException exc) {
			LOG.error("getProperties", exc);
		}
		return properties;
	}

	public AAIService(Properties props) {
        LOG.info("Entered AAIService.ctor");

        String runtime = System.getProperty("aaiclient.runtime");
        if("OSGI".equals(runtime)) {
            runtimeOSGI = true;
        } else {
            runtimeOSGI = false;
        }

        try {
            AAIRequest.setProperties(props, this);

        } catch(Exception exc){
            LOG.error("AicAAIResource.static", exc);
        }

        executor = new AAIClientRESTExecutor(props);

        userName            = props.getProperty(CLIENT_NAME);
        userPassword        = props.getProperty(CLIENT_PWWD);

        if(userName == null || userName.isEmpty()){
            LOG.debug("Basic user name is not set");
        }
        if(userPassword == null || userPassword.isEmpty()) {
            LOG.debug("Basic password is not set");
        }

        truststorePath     = props.getProperty(TRUSTSTORE_PATH);
        truststorePassword = props.getProperty(TRUSTSTORE_PSSWD);
        keystorePath         = props.getProperty(KEYSTORE_PATH);
        keystorePassword     = props.getProperty(KEYSTORE_PSSWD);

        targetUri             = props.getProperty(TARGET_URI);
        queryPath             = props.getProperty(QUERY_PATH);
        updatePath         = props.getProperty(UPDATE_PATH);

        String tmpApplicationId = props.getProperty(APPLICATION_ID);
        if(tmpApplicationId == null || tmpApplicationId.isEmpty()) {
        	tmpApplicationId = "SDNC";
        }
        this.applicationId = tmpApplicationId;

        // connection timeout
        int tmpConnectionTimeout = 30000;
        int tmpReadTimeout = 30000;

        try {
            String tmpValue = null;
            tmpValue = props.getProperty(CONNECTION_TIMEOUT, "30000");
            tmpConnectionTimeout = Integer.parseInt(tmpValue);
            tmpValue = props.getProperty(READ_TIMEOUT, "30000");
            tmpReadTimeout = Integer.parseInt(tmpValue);
        } catch(Exception exc) {
            LOG.error("Failed setting connection timeout", exc);
            tmpConnectionTimeout = 30000;
            tmpReadTimeout = 30000;
        }
        connectionTimeout = tmpConnectionTimeout;
        readTimeout = tmpReadTimeout;

        networkVserverPath =props.getProperty(NETWORK_VSERVER_PATH);

        svcInstancePath    = props.getProperty(SVC_INSTANCE_PATH);
        svc_inst_qry_path    = props.getProperty(SVC_INST_QRY_PATH);
        param_service_type     = props.getProperty(PARAM_SERVICE_TYPE, "service-type");

        // P-Interfaces
        pInterfacePath   = props.getProperty(P_INTERFACE_PATH);

        vnf_image_query_path    = props.getProperty(VNF_IMAGE_QUERY_PATH);

        ubb_notify_path = props.getProperty(UBB_NOTIFY_PATH);
        selflinkAvpn = props.getProperty(SELFLINK_AVPN);
        selflinkFqdn = props.getProperty(SELFLINK_FQDN);

        servicePath  = props.getProperty(SERVICE_PATH);

        sitePairSetPath  = props.getProperty(SITE_PAIR_SET_PATH);

        queryNodesPath = props.getProperty(QUERY_NODES_PATH);

        String iche = props.getProperty(CERTIFICATE_HOST_ERROR);
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

        if(keystorePath != null && keystorePassword != null && (new File(keystorePath)).exists()) {
        DefaultClientConfig config = new DefaultClientConfig();
        //both jersey and HttpURLConnection can use this
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("TLS");

            KeyManagerFactory kmf = null;
            try (FileInputStream fin = new FileInputStream(keystorePath)){
                String def = "SunX509";
                String storeType = "PKCS12";
                def = KeyStore.getDefaultType();
                kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());

                String extension = keystorePath.substring(keystorePath.lastIndexOf(".") + 1);
                if("JKS".equalsIgnoreCase(extension)) {
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

        LOG.info("AAIResource.ctor initialized.");

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
         e.printStackTrace();
        }

    }

    public void setExecutor(AAIExecutorInterface executor) {
		this.executor = executor;
	}

	public void cleanUp() {

    }

    /**
     *
     * @param http_req_url
     * @param method
     * @return
     * @throws Exception
     */
    protected HttpURLConnection getConfiguredConnection(URL http_req_url, String method) throws Exception {
        HttpURLConnection con = (HttpURLConnection) http_req_url.openConnection();

        // Set up the connection properties
        con.setRequestProperty( "Connection", "close" );
        con.setDoInput(true);
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setConnectTimeout( connectionTimeout );
        con.setReadTimeout( readTimeout );
        con.setRequestMethod( method );
        con.setRequestProperty( "Accept", "application/json" );
        con.setRequestProperty( "Content-Type",  "PATCH".equalsIgnoreCase(method) ? "application/merge-patch+json" : "application/json" );
        con.setRequestProperty("X-FromAppId", applicationId);
        con.setRequestProperty("X-TransactionId",TransactionIdTracker.getNextTransactionId());
        String mlId = ml.getRequestID();
        if(mlId != null && !mlId.isEmpty()) {
            LOG.debug(String.format("MetricLogger requestId = %s", mlId));
            con.setRequestProperty(MetricLogger.REQUEST_ID, mlId);
        } else {
            LOG.debug("MetricLogger requestId is null");
        }
        con.setRequestProperty("Transfer-Encoding","chunked");

        if(userName != null && !userName.isEmpty() && userPassword != null && !userPassword.isEmpty()) {
            String basicAuth = "Basic " + new String(Base64.encodeBase64((userName + ":" + userPassword).getBytes()));
            con.setRequestProperty ("Authorization", basicAuth);
        }

        if(con instanceof HttpsURLConnection && CTX != null) {
            SSLSocketFactory sockFact = CTX.getSocketFactory();
            HttpsURLConnection.class.cast(con).setSSLSocketFactory( sockFact );
        }
        return con;
    }


    @Override
    public GenericVnf requestGenericVnfData(String vnf_id) throws AAIServiceException {
        GenericVnf response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("generic-vnf");
            request.addRequestProperty("generic-vnf.vnf-id", vnf_id);
            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, GenericVnf.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn(Object.class.getClass().getEnclosingMethod().getName(), exc);
            throw new AAIServiceException(exc);
        }

        return response;

    }

    @Override
    public boolean postGenericVnfData(String vnf_id, GenericVnf data) throws AAIServiceException {
        try {
            AAIRequest request = AAIRequest.getRequestFromResource("generic-vnf");
            request.addRequestProperty("generic-vnf.vnf-id", vnf_id);
            request.setRequestObject(data);
            Object response = executor.post(request);
            return true;
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestGenericVnfData", exc);
            throw new AAIServiceException(exc);
        }
    }

    @Override
    public SearchResults requestServiceInstanceURL(String svc_instance_id) throws AAIServiceException {
        SearchResults response = null;
        InputStream inputStream = null;

        try {
            String path = svc_inst_qry_path;
            path = path.replace("{svc-instance-id}", encodeQuery(svc_instance_id));

            String request_url = targetUri+path;
            URL http_req_url =    new URL(request_url);

            HttpURLConnection con = getConfiguredConnection(http_req_url, HttpMethod.GET);

            LOGwriteFirstTrace(HttpMethod.GET, http_req_url.toString());
            LOGwriteDateTrace("svc_instance_id", svc_instance_id);

            // Check for errors
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            // Process the response
            LOG.debug("HttpURLConnection result:" + responseCode);
            if(inputStream == null) inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );

            ObjectMapper mapper = getObjectMapper();

    if (responseCode == HttpURLConnection.HTTP_OK) {
                response = mapper.readValue(reader, SearchResults.class);
                LOGwriteEndingTrace(HttpURLConnection.HTTP_OK, "SUCCESS", mapper.writeValueAsString(response));
            } else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND ) {
                LOGwriteEndingTrace(responseCode, "HTTP_NOT_FOUND", "Entry does not exist.");
                return response;
            } else {
                ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, "FAILURE", mapper.writeValueAsString(errorresponse));
                throw new AAIServiceException(responseCode, errorresponse);
            }

        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestServiceInstanceURL", exc);
            throw new AAIServiceException(exc);
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch(Exception exc) {
                }
            }
        }
        return response;
    }


    private static Properties initialize(URL url ) throws ConfigurationException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        InputStream is = null;
        Properties props = new Properties();

        try {
            if(LOG.isDebugEnabled())
                LOG.info("Property file is: " + url.toString());

            is = url.openStream();

            props.load(is);
            if(LOG.isDebugEnabled()) {
                LOG.info("Properties loaded: " + props.size());
                Enumeration<Object> en = props.keys();

                while(en.hasMoreElements()) {
                    String key = (String)en.nextElement();
                    String property = props.getProperty(key);
                    LOG.debug(key + " : " + property);
                }
            }
        } catch (Exception e) {
            throw new ConfigurationException("Could not load properties file.", e);
        }
        return props;
    }

    static class TransactionIdTracker {
//        protected static AtomicLong tracker = new AtomicLong();

        public static String getNextTransactionId() {
            // Check if RequestId exists as MDC. If not, create new.
            String transactionId = MDC.get("RequestId");
            if ("".equals(transactionId) || transactionId == null) {
                transactionId = UUID.randomUUID().toString();
                LOG.info("Missing requestID. Assigned " + transactionId);
                MDC.put("RequestId", transactionId);
            }
            return transactionId;
        }

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

    protected String encodeQuery(String param) throws UnsupportedEncodingException {
        return URLEncoder.encode(param, "UTF-8").replace("+", "%20");
    }

    private String encodeCustomerURL(final String selection)
    {
        String encrypted_url = selection;
        String apnpattern =
                "/aai/v11/business/customers/customer/(.+)/service-subscriptions/service-subscription/(.+)/service-instances/service-instance/(.+)/";
        Pattern pattern = Pattern.compile(apnpattern);

        try {
            URL url =    new URL(selection);
            String path = url.getPath();

            LOG.info("Trying to match apn to <" + path + ">");

            Matcher matcher = pattern.matcher(path);

            while(matcher.find()) {
                String customer = matcher.group(1);
                String subscription = matcher.group(2);
                String service = matcher.group(3);

                encrypted_url = selection.replace(customer, encodeQuery(customer));
                encrypted_url = encrypted_url.replace(subscription, encodeQuery(subscription));
                encrypted_url = encrypted_url.replace(service, encodeQuery(service));
            }
        } catch (Exception e) {
            LOG.warn("", e);
        }

        return encrypted_url;
    }



    /*
     * (non-Javadoc)
     * @see org.openecomp.sdnct.sli.aai.AAIClient#requestVServersData(java.lang.String, java.lang.String)
     */
    @Override
    public Vserver requestVServerData(String tenantId, String vserverId, String cloudOwner, String cloudRegionId)    throws AAIServiceException {
        Vserver response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("vserver");
            request.addRequestProperty("cloud-region.cloud-owner", cloudOwner);
            request.addRequestProperty("cloud-region.cloud-region-id", cloudRegionId);
            request.addRequestProperty("tenant.tenant-id", tenantId);
            request.addRequestProperty("vserver.vserver-id", vserverId);

            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, Vserver.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn(Object.class.getClass().getEnclosingMethod().getName(), exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }


    //================== End of DvsSwitch =================
    //==================== PhysicalLink ======================
    @Override
    public PhysicalLink  requestPhysicalLinkData(String linkName) throws AAIServiceException {
        PhysicalLink response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("physical-link");
            request.addRequestProperty("physical-link.link-name", linkName);

            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, PhysicalLink.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestPhysicalLinkData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    @Override
    public boolean postPhysicalLinkData(String linkName, PhysicalLink data) throws AAIServiceException {
        try {
            AAIRequest request = AAIRequest.getRequestFromResource("physical-link");
            request.addRequestProperty("physical-link.link-name", linkName);
            request.setRequestObject(data);
            Object response = executor.post(request);
            return true;
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn(Object.class.getClass().getEnclosingMethod().getName(), exc);
            throw new AAIServiceException(exc);
        }
    }

    @Override
    public boolean deletePhysicalLinkData(String linkName, String resourceVersion) throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("physical-link");
            request.addRequestProperty("physical-link.link-name", linkName);
            response = executor.delete(request, resourceVersion);
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deletePhysicalLinkData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    public boolean deleteAAIEntity(URL url, String caller) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        boolean response = false;
        InputStream inputStream = null;

        try {
            URL http_req_url =    url;

            HttpURLConnection con = getConfiguredConnection(http_req_url, HttpMethod.DELETE);

            LOGwriteFirstTrace("DELETE", http_req_url.toString());


            // Check for errors
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            // Process the response
            LOG.debug("HttpURLConnection result:" + responseCode);
            if(inputStream == null) inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );
            String line = null;

            ObjectMapper mapper = getObjectMapper();

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                StringBuilder stringBuilder = new StringBuilder();

                while( ( line = reader.readLine() ) != null ) {
                    stringBuilder.append( line );
                }
                LOGwriteEndingTrace(responseCode, "SUCCESS", stringBuilder.toString());
                response = true;
            } else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND ) {
                LOGwriteEndingTrace(responseCode, "HTTP_NOT_FOUND", "Entry does not exist.");
                response = false;
            } else {
                ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, "FAILURE", mapper.writeValueAsString(errorresponse));
                throw new AAIServiceException(responseCode, errorresponse);
            }

        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn(caller, exc);
            throw new AAIServiceException(exc);
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch(Exception exc) {

                }
            }
        }
        return response;
    }

    /**
     * Generic method to GET json data from an A&AI callback URL.
     * Then convert that json to an Object.
     * If successful the Object is attempted to be cast to the type parameter.
     *
     * @param key
     *            callback url for A&AI
     * @param type
     *            the class of object that A&AI will return
     * @return the object created from json or null if the response code is not 200
     *
     * @throws AAIServiceException
     *             if empty or null key and or type or there's an error with processing
     */
    public <T> T dataChangeRequestAaiData(String key, Class<T> type) throws AAIServiceException {
        if (StringUtils.isEmpty(key) || type == null) {
            throw new AAIServiceException("Key is empty or null and or type is null");
        }

        T response = null;

        SvcLogicContext ctx = new SvcLogicContext();
        if(!key.contains(" = ") && isValidURL(key)) {
            key = String.format("selflink = '%s'", key);
        } else
        if(!key.contains(" = ") && isValidURI(key)) {
            key = String.format("resource-path = '%s'", key);
        }

        HashMap<String, String> nameValues = AAIServiceUtils.keyToHashMap(key, ctx);

        SelfLinkRequest request = new SelfLinkRequest(type);
        request.processRequestPathValues(nameValues);
        Object obj = this.getExecutor().query(request, type);
        response = type.cast(obj);

        return response != null ? type.cast(response) : response;
    }


    public boolean sendNotify(NotifyEvent event, String serviceInstanceId, String pathCode) throws AAIServiceException {
        InputStream inputStream = null;

        try {

            String selfLink = selflinkFqdn;
            if(SELFLINK_AVPN != null && SELFLINK_AVPN.equals(pathCode)) {
                selfLink = selflinkAvpn;
            }
            selfLink = selfLink.replace("{service-instance-id}", encodeQuery(serviceInstanceId));
            event.setSelflink(selfLink);

            ObjectMapper mapper = getObjectMapper();
            String json_text = mapper.writeValueAsString(event);

            SSLSocketFactory sockFact = CTX.getSocketFactory();

            String request_url = targetUri+ubb_notify_path;
            URL http_req_url =    new URL(request_url);

            HttpURLConnection con = getConfiguredConnection(http_req_url, HttpMethod.PUT);

            if (json_text != null) {
                OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
                osw.write(json_text);
                osw.flush();
                osw.close();
            }

            LOGwriteFirstTrace("PUT", request_url);
            LOGwriteDateTrace("NotifyEvent", json_text);

            // Check for errors
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            // Process the response
            BufferedReader reader;
            String line = null;
            reader = new BufferedReader( new InputStreamReader( inputStream ) );

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED || responseCode == HttpURLConnection.HTTP_ACCEPTED || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                StringBuilder stringBuilder = new StringBuilder();

                while( ( line = reader.readLine() ) != null ) {
                    stringBuilder.append( line );
                }
                LOGwriteEndingTrace(responseCode, "SUCCESS", (stringBuilder.length() > 0) ? stringBuilder.toString() :
                                                                                            "{no-data}");
                return true;
            } else {
                ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, "FAILURE", mapper.writeValueAsString(errorresponse));

                throw new AAIServiceException(responseCode, errorresponse);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("sendNotify", exc);
            throw new AAIServiceException(exc);
        } finally {
            try {
                if(inputStream != null)
                inputStream.close();
            } catch (Exception exc) {

            }
        }
    }

    @Override
    public SearchResults requestNodeQuery(String node_type, String entityIdentifier, String entityName) throws AAIServiceException {
        SearchResults response = null;
        InputStream inputStream = null;

        try {
            String request_url = targetUri+queryNodesPath;
            request_url = request_url.replace("{node-type}", encodeQuery(node_type)) ;
            request_url = request_url.replace("{entity-identifier}", entityIdentifier) ;
            request_url = request_url.replace("{entity-name}", encodeQuery(entityName)) ;
            URL http_req_url =    new URL(request_url);

            HttpURLConnection con = getConfiguredConnection(http_req_url, HttpMethod.GET);

            LOGwriteFirstTrace(HttpMethod.GET, http_req_url.toString());
            LOGwriteDateTrace("node_type", node_type);
            LOGwriteDateTrace("vnf_name", entityName);

            // Check for errors
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            // Process the response
            LOG.debug("HttpURLConnection result:" + responseCode);
            if(inputStream == null) inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );

            ObjectMapper mapper = getObjectMapper();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                response = mapper.readValue(reader, SearchResults.class);
                LOGwriteEndingTrace(HttpURLConnection.HTTP_OK, "SUCCESS", mapper.writeValueAsString(response));
            } else if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                LOGwriteEndingTrace(responseCode, "HTTP_NOT_FOUND", "Entry does not exist.");
                return response;
            } else {
                ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, "FAILURE", mapper.writeValueAsString(errorresponse));
                throw new AAIServiceException(responseCode, errorresponse);
            }

        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestNodeQuery", exc);
            throw new AAIServiceException(exc);
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch(Exception exc) {

                }
            }
        }
        return response;

    }


    @Override
    public String requestDataByURL(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        String response = null;
        InputStream inputStream = null;

        try {
            URL http_req_url = url;

            HttpURLConnection con = getConfiguredConnection(http_req_url, HttpMethod.GET);

            LOGwriteFirstTrace(HttpMethod.GET, http_req_url.toString());

            // Check for errors
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            // Process the response
            LOG.debug("HttpURLConnection result:" + responseCode);
            if(inputStream == null) inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );

            ObjectMapper mapper = getObjectMapper();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                StringBuilder stringBuilder = new StringBuilder("\n");
                String line = null;
                while( ( line = reader.readLine() ) != null ) {
                    stringBuilder.append( line );
                }
                LOG.info(stringBuilder.toString());
//                response = mapper.readValue(reader, String.class);
                response = stringBuilder.toString();
                LOGwriteEndingTrace(HttpURLConnection.HTTP_OK, "SUCCESS", mapper.writeValueAsString(response));
            } else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND ) {
                LOGwriteEndingTrace(responseCode, "HTTP_NOT_FOUND", "Entry does not exist.");
                response = null;
            } else {
                ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, "FAILURE", mapper.writeValueAsString(errorresponse));
                throw new AAIServiceException(responseCode, errorresponse);
            }

        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestNetworkVceData", exc);
            throw new AAIServiceException(exc);
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch(Exception exc) {

                }
            }
        }
        return response;
    }


    @Override
    public GenericVnf requestGenericVnfeNodeQuery(String vnf_name) throws AAIServiceException {

        if(vnf_name == null) {
            throw new NullPointerException();
        }

        GenericVnf entity = null;
        SearchResults resp = this.requestNodeQuery("generic-vnf", "vnf-name", vnf_name);

        List<ResultData> resultDataList = resp.getResultData();

        try {
            for (ResultData datum : resultDataList) {
                String data_type = datum.getResourceType();
                URL url = new URL(datum.getResourceLink());
                entity = this.getResource(url.toString(), GenericVnf.class);
            }
        }
        catch (Exception e)
        {
            LOG.error("Caught exception", e);
        }
        return entity;
    }

    @Override
    public Vserver requestVServerDataByURL(URL url) throws AAIServiceException {

        if(url == null) {
            throw new NullPointerException();
        }

        Vserver entity = null;

        try {
                entity = this.getResource(url.toString(), Vserver.class);
        } catch (AAIServiceException exc) {
            throw exc;
        } catch (Exception e) {
            throw new AAIServiceException(e);
        }
        return entity;
    }

    @Override
    public URL requestVserverURLNodeQuery(String vserver_name) throws AAIServiceException {

        if(vserver_name == null) {
            throw new NullPointerException();
        }

        URL entity = null;
        SearchResults resp = this.requestNodeQuery("vserver", "vserver-name", vserver_name);

        List<ResultData> resultDataList = resp.getResultData();

        try {
            for (ResultData datum : resultDataList) {
                String data_type = datum.getResourceType();
                String resourceLink = datum.getResourceLink();
                if(!resourceLink.isEmpty() && !resourceLink.toLowerCase().startsWith("http")) {
                    resourceLink = (new EchoRequest()).targetUri + resourceLink;
                }
                entity = new URL(resourceLink);
            }
        } catch (Exception e) {
            throw new AAIServiceException(e);
        }
        return entity;
    }

    @Override
    public String getTenantIdFromVserverUrl(URL url) {

        String path = url.getPath();

        String[] split = path.split("/tenants/tenant/");
        if(split.length > 1) {
            split = split[1].split("/");
            return split[0];
        } else {
            return null;
        }
    }

    @Override
    public String getCloudOwnerFromVserverUrl(URL url) {

        String path = url.getPath();

        String[] split = path.split("/cloud-regions/cloud-region/");
        if(split.length > 1) {
            split = split[1].split("/");
            return split[0];
        } else {
            return null;
        }
    }

    @Override
    public String getCloudRegionFromVserverUrl(URL url) {

        String path = url.getPath();

        String[] split = path.split("/cloud-regions/cloud-region/");
        if(split.length > 1) {
            split = split[1].split("/");
            return split[1];
        } else {
            return null;
        }
    }

    @Override
    public String getVServerIdFromVserverUrl(URL url, String tenantId) {
        String pattern =  networkVserverPath;
        pattern = pattern.replace("{tenant-id}", tenantId);

        int end = pattern.indexOf("{vserver-id}");
        String prefix = pattern.substring(0, end);

        String path = url.getPath();

        if(path.startsWith(prefix)) {
            path = path.substring(prefix.length());
        }

        return path;
    }

    protected  Logger getLogger(){
        return LOG;
    }


    @Override
    public AAIExecutorInterface getExecutor() {
        return executor;
    }

    /**
     * Creates a current time stamp in UTC i.e. 2016-03-08T22:15:13.343Z.
     * If there are any parameters the values are appended to the time stamp.
     *
     * @param parameters
     *            values to be appended to current time stamp
     * @param ctx
     *            used to set an attribute for a DG
     * @throws SvcLogicException
     */
    public void setStatusMethod(Map<String, String> parameters, SvcLogicContext ctx) throws SvcLogicException {
        if (ctx == null) {
            throw new SvcLogicException("SvcLogicContext is null.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%tFT%<tTZ", Calendar.getInstance(TimeZone.getTimeZone("Z")))).append(" - ");

        for (Entry<String, String> entry : parameters.entrySet()) {
            sb.append(entry.getValue()).append("  ");
        }

        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }

        ctx.setAttribute("aai-summary-status-message", sb.toString());
        LOG.info("aai-summary-status-message: " + sb.toString());
    }

    /**
     * Generic method to GET json data from an A&AI using key structure.
     * Then convert that json to an Object.
     * If successful the Object is attempted to be cast to the type parameter.
     *
     * @param key
     *            key identifying the resource to be retrieved from AAI
     * @param type
     *            the class of object that A&AI will return
     * @return the object created from json or null if the response code is not 200
     *
     * @throws AAIServiceException
     *             if empty or null key and or type or there's an error with processing
     */

    public <T> T getResource(String key, Class<T> type) throws AAIServiceException {
            if (StringUtils.isEmpty(key) || type == null) {
                throw new AAIServiceException("Key is empty or null and or type is null");
            }

            T response = null;

            SvcLogicContext ctx = new SvcLogicContext();
            if(!key.contains(" = ")) {
                if(isValidURL(key)) {
                    key = String.format("selflink = '%s'", key);
                } else if(isValidURI(key)) {
                    key = String.format("resource-path = '%s'", key);
                } else {
                    return response;
                }
            }

            HashMap<String, String> nameValues = AAIServiceUtils.keyToHashMap(key, ctx);

            AAIRequest request = new SelfLinkRequest(type);
            if(nameValues.containsKey(PathRequest.RESOURCE_PATH.replaceAll("-", "_"))) {
                request = new PathRequest(type);
            }

            request.processRequestPathValues(nameValues);
            Object obj = this.getExecutor().query(request, type);
            response = type.cast(obj);

            return response != null ? type.cast(response) : response;
     }

     public boolean isValidURL(String url) {

            URL u = null;

            try {
                u = new URL(url);
            } catch (MalformedURLException e) {
                return false;
            }

            try {
                u.toURI();
            } catch (URISyntaxException e) {
                return false;
            }

            return true;
        }


    public boolean isValidURI(String url) {

        URI u = null;

        try {
            u = new URI(url);
        } catch (URISyntaxException e) {
            return false;
        }

        return true;
    }


    protected boolean deleteList(URL httpReqUrl, String json_text) throws AAIServiceException {
        if(httpReqUrl ==  null) {
            throw new NullPointerException();
        }

        boolean response = false;
        InputStream inputStream = null;

        try {
            HttpURLConnection con = getConfiguredConnection(httpReqUrl, HttpMethod.DELETE);

//            SSLSocketFactory sockFact = CTX.getSocketFactory();
//            con.setSSLSocketFactory( sockFact );
            if (json_text != null) {
                OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
                osw.write(json_text);
                osw.flush();
                osw.close();
            }

            LOGwriteFirstTrace("DELETE", httpReqUrl.toString());
            LOGwriteDateTrace("data", json_text);

            // Check for errors
            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                inputStream = con.getInputStream();
            } else {
                inputStream = con.getErrorStream();
            }

            // Process the response
            LOG.debug("HttpURLConnection result:" + responseCode);
            if(inputStream == null) inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
            BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );
            String line = null;

            ObjectMapper mapper = getObjectMapper();

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                StringBuilder stringBuilder = new StringBuilder();

                while( ( line = reader.readLine() ) != null ) {
                    stringBuilder.append( line );
                }
                LOGwriteEndingTrace(responseCode, "SUCCESS", stringBuilder.toString());
                response = true;
            } else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND ) {
                LOGwriteEndingTrace(responseCode, "HTTP_NOT_FOUND", "Entry does not exist.");
                response = false;
            } else {
                ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, "FAILURE", mapper.writeValueAsString(errorresponse));
                throw new AAIServiceException(responseCode, errorresponse);
            }

        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deleteList", exc);
            throw new AAIServiceException(exc);
        } finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch(Exception exc) {

                }
            }
        }
        return response;
    }

    public static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(TypeFactory.defaultInstance());
        AnnotationIntrospector secondary = new JacksonAnnotationIntrospector();
        mapper.setAnnotationIntrospector(AnnotationIntrospector.pair(introspector, secondary));
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(Include.NON_NULL);
        mapper.setSerializationInclusion(Include.NON_EMPTY);
        return mapper;
    }

    public void logMetricRequest(String requestId, String targetServiceName, String msg, String path){
        String svcInstanceId = "";
        String svcName = null;
        String partnerName = null;
        String targetEntity = "A&AI";
        String targetVirtualEntity = null;

        ml.logRequest(svcInstanceId, svcName, partnerName, targetEntity, targetServiceName, targetVirtualEntity, msg);
    }

    public void logMetricResponse(String requestId, int responseCode, String responseDescription){
        ml.logResponse(responseCode < 400 ? "SUCCESS" : "FAILURE", Integer.toString(responseCode), responseDescription);
    }

    public void logKeyError(String keys){
        LOG.error("Atleast one of the keys [" + keys + "] should have been populated. This will cause a NPE.");
    }


    /**
     * Retrofit code
     */
    @Override
    public QueryStatus save(String resource, boolean force, boolean localOnly, String key, Map<String, String> params, String prefix, SvcLogicContext ctx)
            throws SvcLogicException {
        String normResource = resource.split(":")[0];

        switch(normResource){
        case "custom-query":
        case "formatted-query":
        case "generic-query":
        case "named-query":
        case "nodes-query":
        case "linterface":
        case "l2-bridge-sbg":
        case "l2-bridge-bgf":
        case "echo":
        case "test":
            break;

        default:
            if(key.contains("selflink =")) {
                break;
            }
            if(!key.contains(String.format("%s.", normResource))) {
                key = rewriteKey(resource, key, ctx);
            }
        }
        return super.save(resource, force, localOnly, key, params, prefix, ctx);
    }

    @Override
    public QueryStatus query(String resource, boolean localOnly, String select, String key, String prefix, String orderBy, SvcLogicContext ctx)
        throws SvcLogicException {
        String normResource = resource.split(":")[0];

        switch(normResource){
		case "custom-query":
        case "formatted-query":
        case "generic-query":
        case "named-query":
        case "nodes-query":
        case "linterface":
        case "l2-bridge-sbg":
        case "l2-bridge-bgf":
        case "echo":
        case "test":
            break;

        default:
            if(key.contains("selflink =")) {
                break;
            }
            if(!key.contains(String.format("%s.", normResource))) {
                key = rewriteKey(resource, key, ctx);
            }
        }

        return super.query(resource, localOnly, select, key, prefix, orderBy, ctx);
    }

    @Override
    public QueryStatus delete(String resource, String key, SvcLogicContext ctx) throws SvcLogicException {
        String normResource = resource.split(":")[0];

        switch(normResource){
		case "custom-query":
        case "formatted-query":
        case "generic-query":
        case "named-query":
        case "nodes-query":
        case "linterface":
        case "l2-bridge-sbg":
        case "l2-bridge-bgf":
        case "echo":
        case "test":
            break;

        default:
            if(key.contains("selflink =")) {
                break;
            }
            if(!key.contains(String.format("%s.", normResource))) {
                key = rewriteKey(resource, key, ctx);
            }
        }

        return super.delete(resource, key, ctx);
    }

    @Override
    public QueryStatus update(String resource, String key, Map<String, String> params, String prefix, SvcLogicContext ctx) throws SvcLogicException {
        String normResource = resource.split(":")[0];

        switch(normResource){
		case "custom-query":
        case "formatted-query":
        case "generic-query":
        case "named-query":
        case "nodes-query":
        case "linterface":
        case "l2-bridge-sbg":
        case "l2-bridge-bgf":
        case "echo":
        case "test":
            break;

        default:
            if(key.contains("selflink =")) {
                break;
            }
            if(!key.contains(String.format("%s.", normResource))) {
                key = rewriteKey(resource, key, ctx);
            }
        }

        return super.update(resource, key, params, prefix, ctx);
    }

    private String rewriteKey(String resource, String key, SvcLogicContext ctx) {
        LOG.info("AAI Deprecation - the format of request key is no longer supported. Please rewrite this key : " + key);

        String normResource = resource.split(":")[0];
        Class<? extends AAIDatum> clazz = null;
        try {
            clazz = AAIRequest.getClassFromResource(normResource) ;
        } catch (ClassNotFoundException e) {
            LOG.warn("AAIRequest does not support class: " + e.getMessage());
            return key;
        }
        if(clazz == null)
            return key;

        List<String> fieldAnnotatedNames = new LinkedList<>();

        Field[] fields = clazz.getDeclaredFields();
        for(Field field : fields) {
            String fieldName = field.getName();
            XmlElement annotation = field.getAnnotation(XmlElement.class);
            if(annotation == null)
                continue;
            String primaryId = annotation.name();
            if("##default".equals(primaryId)) {
                primaryId = fieldName;
            }
            fieldAnnotatedNames.add(primaryId);
        }

        HashMap<String, String> nameValues = AAIServiceUtils.keyToHashMap(key, ctx);
        Set<String> keyset = nameValues.keySet();
        for(String keyName : keyset) {
            if(keyName.contains("."))
                continue;
            else {
                String tmpKeyName = keyName.replaceAll("_", "-");
                String valueToSubstitute = String.format("%s =", tmpKeyName);
                if(fieldAnnotatedNames.contains(tmpKeyName) && key.contains(valueToSubstitute)) {
                    key = key.replace(valueToSubstitute, String.format("%s.%s =", normResource, tmpKeyName));
                }
            }
        }


        return key;
    }

    @Override
    public String getPathTemplateForResource(String resoourceName, String keys, SvcLogicContext ctx) throws MalformedURLException {
        return AAIServiceUtils.getPathForResource(resoourceName, StringUtils.join(keys, " AND "), ctx);
    }

    @Override
    public boolean isDeprecatedFormat(String resource, HashMap<String, String> nameValues) {
        return !AAIServiceUtils.isValidFormat(resource, nameValues);
    }

    public AAIRequest getRequestFromResource(String resoourceName) {
        return AAIRequest.getRequestFromResource(resoourceName);
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.aai.haha#query(org.onap.ccsdk.sli.core.sli.aai.AAIRequest)
     */
    @Override
    public String query(AAIRequest request) throws AAIServiceException {
        return executor.get(request);
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.aai.haha#save(org.onap.ccsdk.sli.core.sli.aai.AAIRequest)
     */
    @Override
    public String save(AAIRequest request) throws AAIServiceException {
        return executor.post(request);
    }

    public boolean update(AAIRequest request, String resourceVersion) throws AAIServiceException {
        return executor.patch(request, resourceVersion);
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.core.sli.aai.haha#delete(org.onap.ccsdk.sli.core.sli.aai.AAIRequest, java.lang.String)
     */
    @Override
    public boolean delete(AAIRequest request, String resourceVersion) throws AAIServiceException {
        return executor.delete(request, resourceVersion);
    }

}
