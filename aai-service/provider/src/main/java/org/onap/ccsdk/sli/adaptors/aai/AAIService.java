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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import java.util.ArrayList;
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
import org.onap.ccsdk.sli.core.sli.ConfigurationException;
import org.onap.ccsdk.sli.core.sli.MetricLogger;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.openecomp.aai.inventory.v11.AvailabilityZone;
import org.openecomp.aai.inventory.v11.Complex;
import org.openecomp.aai.inventory.v11.CtagPool;
import org.openecomp.aai.inventory.v11.DvsSwitch;
import org.openecomp.aai.inventory.v11.GenericVnf;
import org.openecomp.aai.inventory.v11.L3Network;
import org.openecomp.aai.inventory.v11.OamNetwork;
import org.openecomp.aai.inventory.v11.PInterface;
import org.openecomp.aai.inventory.v11.PhysicalLink;
import org.openecomp.aai.inventory.v11.Pserver;
import org.openecomp.aai.inventory.v11.ResultData;
import org.openecomp.aai.inventory.v11.SearchResults;
import org.openecomp.aai.inventory.v11.Service;
import org.openecomp.aai.inventory.v11.ServiceInstance;
import org.openecomp.aai.inventory.v11.SitePairSet;
import org.openecomp.aai.inventory.v11.Tenant;
import org.openecomp.aai.inventory.v11.Vce;
import org.openecomp.aai.inventory.v11.VnfImage;
import org.openecomp.aai.inventory.v11.VnfImages;
import org.openecomp.aai.inventory.v11.VplsPe;
import org.openecomp.aai.inventory.v11.VpnBinding;
import org.openecomp.aai.inventory.v11.Vserver;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.onap.ccsdk.sli.adaptors.aai.data.ErrorResponse;
import org.onap.ccsdk.sli.adaptors.aai.data.RequestError;
import org.onap.ccsdk.sli.adaptors.aai.data.ResourceVersion;
import org.onap.ccsdk.sli.adaptors.aai.data.ServiceException;
import org.onap.ccsdk.sli.adaptors.aai.data.notify.NotifyEvent;
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

    private final String target_uri;
    private final String queryPath;

    private final String networkVserverPath;

    private final String svcInstancePath;
    private final String svc_inst_qry_path;

    private final String vnf_image_query_path;

    private final String param_service_type;            //= "service-type";

    private final String ubb_notify_path;
    private final String selflink_avpn;
    private final String selflink_fqdn;

    private final String p_interface_path;

    private final String service_path;
    private final String site_pair_set_path;

    private final int connection_timeout;
    private final int read_timeout;

    // 1602
    private final String query_nodes_path;
    private final String update_path;

    private final String application_id;

    // authentication credentials
    private String user_name;
    private String user_password;

    // runtime
    private final boolean runtimeOSGI;

    private SSLContext CTX;

    private final MetricLogger ml = new MetricLogger();

    private final AAIRequestExecutor executor;

    public AAIService(URL propURL) {
        LOG.info("Entered AAIService.ctor");

        String runtime = System.getProperty("aaiclient.runtime");
        if("OSGI".equals(runtime)) {
            runtimeOSGI = true;
        } else {
            runtimeOSGI = false;
        }

        Properties props = null;
        try {
            props = initialize(propURL);
            AAIRequest.setProperties(props, this);

        } catch(Exception exc){
            LOG.error("AicAAIResource.static", exc);
        }

        executor = new AAIRequestExecutor();

        user_name            = props.getProperty(CLIENT_NAME);
        user_password        = props.getProperty(CLIENT_PWWD);

        if(user_name == null || user_name.isEmpty()){
            LOG.debug("Basic user name is not set");
        }
        if(user_password == null || user_password.isEmpty()) {
            LOG.debug("Basic password is not set");
        }

        truststorePath     = props.getProperty(TRUSTSTORE_PATH);
        truststorePassword = props.getProperty(TRUSTSTORE_PSSWD);
        keystorePath         = props.getProperty(KEYSTORE_PATH);
        keystorePassword     = props.getProperty(KEYSTORE_PSSWD);

        target_uri             = props.getProperty(TARGET_URI);
        queryPath             = props.getProperty(QUERY_PATH);
        update_path         = props.getProperty(UPDATE_PATH);

        String applicationId =props.getProperty(APPLICATION_ID);
        if(applicationId == null || applicationId.isEmpty()) {
            applicationId = "SDNC";
        }
        application_id = applicationId;

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
        connection_timeout = tmpConnectionTimeout;
        read_timeout = tmpReadTimeout;

        networkVserverPath =props.getProperty(NETWORK_VSERVER_PATH);

        svcInstancePath    = props.getProperty(SVC_INSTANCE_PATH);
        svc_inst_qry_path    = props.getProperty(SVC_INST_QRY_PATH);
        param_service_type     = props.getProperty(PARAM_SERVICE_TYPE, "service-type");

        // P-Interfaces
        p_interface_path   = props.getProperty(P_INTERFACE_PATH);

        vnf_image_query_path    = props.getProperty(VNF_IMAGE_QUERY_PATH);

        ubb_notify_path = props.getProperty(UBB_NOTIFY_PATH);
        selflink_avpn = props.getProperty(SELFLINK_AVPN);
        selflink_fqdn = props.getProperty(SELFLINK_FQDN);

        service_path  = props.getProperty(SERVICE_PATH);

        site_pair_set_path  = props.getProperty(SITE_PAIR_SET_PATH);

        query_nodes_path = props.getProperty(QUERY_NODES_PATH);

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
        con.setConnectTimeout( connection_timeout );
        con.setReadTimeout( read_timeout );
        con.setRequestMethod( method );
        con.setRequestProperty( "Accept", "application/json" );
        con.setRequestProperty( "Content-Type",  "PATCH".equalsIgnoreCase(method) ? "application/merge-patch+json" : "application/json" );
        con.setRequestProperty("X-FromAppId", application_id);
        con.setRequestProperty("X-TransactionId",TransactionIdTracker.getNextTransactionId());
        String mlId = ml.getRequestID();
        if(mlId != null && !mlId.isEmpty()) {
            LOG.debug(String.format("MetricLogger requestId = %s", mlId));
            con.setRequestProperty(MetricLogger.REQUEST_ID, mlId);
        } else {
            LOG.debug("MetricLogger requestId is null");
        }

        if(user_name != null && !user_name.isEmpty() && user_password != null && !user_password.isEmpty()) {
            String basicAuth = "Basic " + new String(Base64.encodeBase64((user_name + ":" + user_password).getBytes()));
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
    public boolean deleteGenericVnfData(String vnf_id, String resourceVersion) throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("generic-vnf");
            request.addRequestProperty("generic-vnf.vnf-id", vnf_id);
            response = executor.delete(request, resourceVersion);
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deleteGenericVnfData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.adaptors.resource.aic.AnAIClient#requestSdnZoneQuery(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public Vce requestNetworkVceData(String vnf_id) throws AAIServiceException {
        Vce response = null;
        try {
            AAIRequest request = AAIRequest.getRequestFromResource("vce");
            request.addRequestProperty("vce.vnf-id", vnf_id);
            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, Vce.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn(Object.class.getClass().getEnclosingMethod().getName(), exc);
            throw new AAIServiceException(exc);
        }

        return response;
    }


    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.adaptors.resource.aic.AnAIClient#requestSdnZoneQuery(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean deleteNetworkVceData(String vnf_id, String resourceVersion) throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("vce");
            request.addRequestProperty("vce.vnf-id", vnf_id);
            response = executor.delete(request, resourceVersion);
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deleteNetworkVceData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    /* (non-Javadoc)
     * @see org.onap.ccsdk.sli.adaptors.resource.aic.AnAIClient#postNetworkVceData(java.lang.String, org.onap.ccsdk.sli.adaptors.resource.aic.aai.VCERequest)
     */
    @Override
    public boolean postNetworkVceData(String vnf_id, Vce data) throws AAIServiceException {
        try {
            AAIRequest request = AAIRequest.getRequestFromResource("vce");
            request.addRequestProperty("vce.vnf-id", vnf_id);
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

            String request_url = target_uri+path;
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

    @Override
    public ServiceInstance requestServiceInterfaceData(String customer_id, String service_type, String svc_instance_id) throws AAIServiceException {
        ServiceInstance response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("service-instance");
            request.addRequestProperty("customer.global-customer-id", customer_id);
            request.addRequestProperty("service-subscription.service-type", service_type);
            request.addRequestProperty("service-instance.service-instance-id", svc_instance_id);

            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, ServiceInstance.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestServiceInterfaceData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    @Override
    public boolean postServiceInterfaceData(String customer_id, String service_type, String svc_instance_id, ServiceInstance data) throws AAIServiceException {
        try {
            AAIRequest request = AAIRequest.getRequestFromResource("service-instance");
            request.addRequestProperty("customer.global-customer-id", customer_id);
            request.addRequestProperty("service-subscription.service-type", service_type);
            request.addRequestProperty("service-instance.service-instance-id", svc_instance_id);
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

    @Override
        public VplsPe requestNetworkVplsPeData(String equipment_name)throws AAIServiceException {
            VplsPe response = null;

            try {
                AAIRequest request = AAIRequest.getRequestFromResource("vpls-pe");
                request.addRequestProperty("vpls-pe.equipment-name", equipment_name);

                String rv = executor.get(request);
                if(rv != null) {
                    ObjectMapper mapper = getObjectMapper();
                    response = mapper.readValue(rv, VplsPe.class);
                }
            } catch(AAIServiceException aaiexc) {
                throw aaiexc;
            } catch (Exception exc) {
                LOG.warn(Object.class.getClass().getEnclosingMethod().getName(),
                        exc);
                throw new AAIServiceException(exc);
            }
            return response;
        }

    @Override
    public boolean postNetworkVplsPeData(String equipment_name, VplsPe data) throws AAIServiceException {
        try {
            AAIRequest request = AAIRequest.getRequestFromResource("vpls-pe");
            request.addRequestProperty("vpls-pe.equipment-name", equipment_name);
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
    public boolean deleteNetworkVplsPeData(String vnf_id, String resourceVersion)    throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("vpls-pe");
            request.addRequestProperty("vpls-pe.equipment-name", vnf_id);
            response = executor.delete(request, resourceVersion);
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deleteNetworkVplsPeData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    @Override
    public Complex  requestNetworkComplexData(String pLocId) throws AAIServiceException {
        Complex response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("complex");
            request.addRequestProperty("complex.physical-location-id", pLocId);

            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, Complex.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestNetworkComplexData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    @Override
    public boolean postNetworkComplexData(String vnf_id, Complex data) throws AAIServiceException {
        try {
            AAIRequest request = AAIRequest.getRequestFromResource("complex");
            request.addRequestProperty("complex.physical-location-id", vnf_id);
            request.setRequestObject(data);
            Object response = executor.post(request);
            return true;
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("postNetworkComplexData", exc);
            throw new AAIServiceException(exc);
        }
    }

    @Override
    public boolean deleteNetworkComplexData(String pLocId, String resourceVersion) throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("complex");
            request.addRequestProperty("complex.physical-location-id", pLocId);

            response = executor.delete(request, resourceVersion);

        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deleteNetworkComplexData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
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


    @Override
    public boolean postVServerData(String tenantId, String vserverId, String cloudOwner, String cloudRegionId, Vserver data) throws AAIServiceException {
        try {
            AAIRequest request = AAIRequest.getRequestFromResource("vserver");
            request.addRequestProperty("cloud-region.cloud-owner", cloudOwner);
            request.addRequestProperty("cloud-region.cloud-region-id", cloudRegionId);
            request.addRequestProperty("tenant.tenant-id", tenantId);
            request.addRequestProperty("vserver.vserver-id", vserverId);
            request.setRequestObject(data);
            Object response = executor.post(request);
            return true;
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("postNetworkComplexData", exc);
            throw new AAIServiceException(exc);
        }
    }

    @Override
    public boolean deleteVServerData(String tenant_id, String vserver_id, String cloudOwner, String cloudRegionId, String resourceVersion) throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("vserver");

            request.addRequestProperty("vserver.vserver-id", vserver_id);
            request.addRequestProperty("tenant.tenant-id", tenant_id);
            request.addRequestProperty("cloud-region.cloud-owner", cloudOwner);
            request.addRequestProperty("cloud-region.cloud-region-id",cloudRegionId);

            response = executor.delete(request, resourceVersion);

        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deleteVServerData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }


    /*
     * (non-Javadoc)
     * @see org.onap.ccsdk.sli.adaptors.aai.AAIClient#requestCtagPoolData(String)
     */
    @Override
    public CtagPool requestCtagPoolData(String physical_location_id, String target_pe, String availability_zone_name)    throws AAIServiceException {
        CtagPool response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("ctag-pool");

            request.addRequestProperty("ctag-pool.target-pe", target_pe);
            request.addRequestProperty("ctag-pool.availability-zone-name", availability_zone_name);
            request.addRequestProperty("complex.physical-location-id", physical_location_id);

            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, CtagPool.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestNetworkVceData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    //==================== DvsSwitch ======================
    @Override
    public DvsSwitch  requestDvsSwitchData(String vnf_id) throws AAIServiceException {
        DvsSwitch response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("dvs-switch");
            request.addRequestProperty("dvs-switch.switch-name", vnf_id);

            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, DvsSwitch.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestDvsSwitchData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    @Override
    public boolean postDvsSwitchData(String switch_name, DvsSwitch data) throws AAIServiceException {
        try {
            AAIRequest request = AAIRequest.getRequestFromResource("dvs-switch");
            request.addRequestProperty("dvs-switch.switch-name", switch_name);
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
    public boolean deleteDvsSwitchData(String vnf_id, String resourceVersion) throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("dvs-switch");
            request.addRequestProperty("dvs-switch.switch-name", vnf_id);
            response = executor.delete(request, resourceVersion);
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deleteDvsSwitchData", exc);
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
    //================== End of PhysicalLink =================
    //==================== PInterface ======================
    @Override
    public PInterface  requestPInterfaceData(String hostname, String interfaceName) throws AAIServiceException {
        PInterface response = null;

        try {
            AAIRequest request =  AAIRequest.getRequestFromResource("p-interface");
            request.addRequestProperty("p-interface.interface-name", interfaceName);
            request.addRequestProperty("pserver.hostname", hostname);
            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, PInterface.class);
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
    public boolean postPInterfaceData(String hostname, String interfaceName, PInterface request) throws AAIServiceException {
        InputStream inputStream = null;

        try {

            ObjectMapper mapper = getObjectMapper();
            String json_text = mapper.writeValueAsString(request);

            SSLSocketFactory sockFact = CTX.getSocketFactory();

            String request_url = target_uri+p_interface_path;
            String encoded_vnf = encodeQuery(hostname);
            request_url = request_url.replace("{hostname}", encoded_vnf) ;
            encoded_vnf = encodeQuery(interfaceName);
            request_url = request_url.replace("{interface-name}", encoded_vnf) ;
            URL http_req_url =    new URL(request_url);

            HttpURLConnection con = getConfiguredConnection(http_req_url, HttpMethod.PUT);

            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
            osw.write(json_text);
            osw.flush();
            osw.close();


            LOGwriteFirstTrace("PUT", request_url);
            LOGwriteDateTrace("hostname", hostname);
            LOGwriteDateTrace("interface-name", interfaceName);
            LOGwriteDateTrace("PInterface", json_text);

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
                LOGwriteEndingTrace(responseCode, "SUCCESS", (stringBuilder.length() > 0) ? stringBuilder.toString() : "{no-data}");
                return true;
            } else {
                ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, "FAILURE", mapper.writeValueAsString(errorresponse));

                throw new AAIServiceException(responseCode, errorresponse);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("postPInterfaceData", exc);
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
    public boolean deletePInterfaceData(String hostname, String interfaceName, String resourceVersion) throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("p-interface");
            request.addRequestProperty("p-interface.interface-name", interfaceName);
            request.addRequestProperty("pserver.hostname", hostname);
            response = executor.delete(request, resourceVersion);
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deletePInterfaceData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }
    //================== End of PInterface =================
    //==================== SitePairSet ======================
    @Override
    public SitePairSet requestSitePairSetData(String sitePairSetId) throws AAIServiceException {
        SitePairSet response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("site-pair-set");
            request.addRequestProperty("site-pair-set.site-pair-set-id", sitePairSetId);
            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, SitePairSet.class);
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
    public boolean postSitePairSetData(String linkName, SitePairSet request) throws AAIServiceException {
        InputStream inputStream = null;

        try {

            ObjectMapper mapper = getObjectMapper();
            String json_text = mapper.writeValueAsString(request);

            SSLSocketFactory sockFact = CTX.getSocketFactory();

            String request_url = target_uri+site_pair_set_path;
            String encoded_vnf = encodeQuery(linkName);
            request_url = request_url.replace("{site-pair-set-id}", encoded_vnf) ;
            URL http_req_url =    new URL(request_url);

            HttpURLConnection con = getConfiguredConnection(http_req_url, HttpMethod.PUT);

            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
            osw.write(json_text);
            osw.flush();
            osw.close();


            LOGwriteFirstTrace("PUT", request_url);
            LOGwriteDateTrace("link-name", linkName);
            LOGwriteDateTrace("SitePairSet", json_text);

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
                LOGwriteEndingTrace(responseCode, "SUCCESS", (stringBuilder.length() > 0) ? stringBuilder.toString() : "{no-data}");
                return true;
            } else {
                ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, "FAILURE", mapper.writeValueAsString(errorresponse));

                throw new AAIServiceException(responseCode, errorresponse);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("postSitePairSetData", exc);
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
    public boolean deleteSitePairSetData(String linkName, String resourceVersion) throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("site-pair-set");
            request.addRequestProperty("site-pair-set.site-pair-set-id", linkName);
            response = executor.delete(request, resourceVersion);
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deleteSitePairSetData", exc);
            throw new AAIServiceException(exc);
        }

        return response;
    }
    //================== End of SitePairSet =================
    //==================== Service ======================
    @Override
    public Service requestServiceData(String serviceId) throws AAIServiceException {
        Service response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("service");
            request.addRequestProperty("service.service-id", serviceId);


            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, Service.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestServiceData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    @Override
    public boolean postServiceData(String linkName, Service request) throws AAIServiceException {
        InputStream inputStream = null;

        try {

            ObjectMapper mapper = getObjectMapper();
            String json_text = mapper.writeValueAsString(request);

            SSLSocketFactory sockFact = CTX.getSocketFactory();

            String request_url = target_uri+service_path;
            String encoded_vnf = encodeQuery(linkName);
            request_url = request_url.replace("{service-id}", encoded_vnf) ;
            URL http_req_url =    new URL(request_url);

            HttpURLConnection con = getConfiguredConnection(http_req_url, HttpMethod.PUT);

            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
            osw.write(json_text);
            osw.flush();
            osw.close();


            LOGwriteFirstTrace("PUT", request_url);
            LOGwriteDateTrace("service-id", linkName);
            LOGwriteDateTrace("Service", json_text);

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
                LOGwriteEndingTrace(responseCode, "SUCCESS", (stringBuilder.length() > 0) ? stringBuilder.toString() : "{no-data}");
                return true;
            } else {
                ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
                LOGwriteEndingTrace(responseCode, "FAILURE", mapper.writeValueAsString(errorresponse));

                throw new AAIServiceException(responseCode, errorresponse);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("postServiceData", exc);
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
    public boolean deleteServiceData(String service_id, String resourceVersion) throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("service");
            request.addRequestProperty("service.service-id", service_id);
            response = executor.delete(request, resourceVersion);
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deleteServiceData", exc);
            throw new AAIServiceException(exc);
        }

        return response;
    }
    //================== End of Service =================


    @Override
    public Vserver dataChangeRequestVServerData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return this.getResource(url.toString(), Vserver.class);
    }

    @Override
    public Pserver dataChangeRequestPServerData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return this.getResource(url.toString(), Pserver.class);
    }

    @Override
    public CtagPool dataChangeRequestCtagPoolData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return this.getResource(url.toString(), CtagPool.class);
    }

    @Override
    public VplsPe dataChangeRequestVplsPeData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return this.getResource(url.toString(), VplsPe.class);
    }

    @Override
    public DvsSwitch dataChangeRequestDvsSwitchData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return this.getResource(url.toString(), DvsSwitch.class);
    }

    @Override
    public OamNetwork dataChangeRequestOAMNetworkData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return this.getResource(url.toString(), OamNetwork.class);
    }

    @Override
    public AvailabilityZone dataChangeRequestAvailabilityZoneData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return this.getResource(url.toString(), AvailabilityZone.class);
    }

    @Override
    public Complex dataChangeRequestComplexData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return this.getResource(url.toString(), Complex.class);
    }

    /* DELETE */
    @Override
    public boolean dataChangeDeleteVServerData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return deleteAAIEntity(url, Object.class.getClass().getEnclosingMethod()
                .getName());
    }

    @Override
    public boolean dataChangeDeleteCtagPoolData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return deleteAAIEntity(url, Object.class.getClass().getEnclosingMethod()
                .getName());
    }

    @Override
    public boolean dataChangeDeleteVplsPeData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return deleteAAIEntity(url, Object.class.getClass().getEnclosingMethod()
                .getName());
    }

    @Override
    public boolean dataChangeDeleteVpeData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return deleteAAIEntity(url, Object.class.getClass().getEnclosingMethod()
                .getName());
    }

    @Override
    public boolean dataChangeDeleteDvsSwitchData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return deleteAAIEntity(url, Object.class.getClass().getEnclosingMethod()
                .getName());
    }
    //OAM-Network:
    @Override
    public boolean dataChangeDeleteOAMNetworkData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return deleteAAIEntity(url, Object.class.getClass().getEnclosingMethod()
                .getName());
    }
    //Availability-Zone:
    @Override
    public boolean dataChangeDeleteAvailabilityZoneData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return deleteAAIEntity(url, Object.class.getClass().getEnclosingMethod()
                .getName());
    }
    //Complex:
    @Override
    public boolean dataChangeDeleteComplexData(URL url) throws AAIServiceException {

        if(url ==  null) {
            throw new NullPointerException();
        }

        return deleteAAIEntity(url, Object.class.getClass().getEnclosingMethod()
                .getName());
    }

    private boolean deleteAAIEntity(URL url, String caller) throws AAIServiceException {

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

    @Override
    public Pserver requestPServerData(String hostname) throws AAIServiceException {
        Pserver response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("pserver");
            request.addRequestProperty("pserver.hostname", hostname);


            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, Pserver.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestPServerData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    @Override
    public boolean postPServerData(String hostname, Pserver data) throws AAIServiceException {
        try {
            AAIRequest request = AAIRequest.getRequestFromResource("pserver");
            request.addRequestProperty("pserver.hostname", hostname);
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
    public boolean deletePServerData(String hostname, String resourceVersion) throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("pserver");
            request.addRequestProperty("pserver.hostname", hostname);
            response = executor.delete(request, resourceVersion);
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deletePServerData", exc);
            throw new AAIServiceException(exc);
        }

        return response;
    }


    @Override
    public L3Network requestL3NetworkData(String networkId) throws AAIServiceException {
        L3Network response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("l3-network");
            request.addRequestProperty("l3-network.network-id", networkId);

            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, L3Network.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestL3NetworkData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    @Override
    public L3Network requestL3NetworkQueryByName(String networkName) throws AAIServiceException {
        L3Network response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("l3-network");
            request.addRequestProperty("l3-network.network-name", networkName);

            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, L3Network.class);
            }

        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestL3NetworkQueryByName", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    @Override
    public boolean postL3NetworkData(String networkId, L3Network data) throws AAIServiceException {
        try {
            AAIRequest request = AAIRequest.getRequestFromResource("l3-network");
            request.addRequestProperty("l3-network.network-id", networkId);
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
    public boolean deleteL3NetworkData(String networkId, String resourceVersion) throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("l3-network");
            request.addRequestProperty("l3-network.network-id", networkId);
            response = executor.delete(request, resourceVersion);
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deleteL3NetworkData", exc);
            throw new AAIServiceException(exc);
        }

        return response;
    }


    @Override
    public VpnBinding requestVpnBindingData(String vpnId) throws AAIServiceException {
        VpnBinding response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("vpn-binding");
            request.addRequestProperty("vpn-binding.vpn-id", vpnId);

            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, VpnBinding.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestVpnBindingData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    @Override
    public boolean deleteVpnBindingData(String vpnId, String resourceVersion) throws AAIServiceException {
        boolean response = false;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("vpn-binding");
            request.addRequestProperty("vpn-binding.vpn-id", vpnId);
            response = executor.delete(request, resourceVersion);
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("deleteVpnBindingData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }


    @Override
    public VnfImage requestVnfImageData(String vnf_image_uuid) throws AAIServiceException {
        VnfImage response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("vnf-image");
            request.addRequestProperty("vnf-image.vnf-image-uuid", vnf_image_uuid);

            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, VnfImage.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestVnfImageData", exc);
            throw new AAIServiceException(exc);
        }
        return response;
    }

    @Override
    public VnfImage requestVnfImageDataByVendorModel(String vendor, String model) throws AAIServiceException {
        return requestVnfImageDataByVendorModelVersion(vendor, model, null);
    }

    @Override
    public VnfImage requestVnfImageDataByVendorModelVersion(String vendor, String model, String version) throws AAIServiceException
    {
        List<VnfImage> responseList = new ArrayList<VnfImage>();
        VnfImage response = null;
        InputStream inputStream = null;

        try {
            String request_url = target_uri+vnf_image_query_path + (version==null? "": "&application-version={application_version}");
            request_url = request_url.replace("{application_vendor}", encodeQuery(vendor)) ;
            request_url = request_url.replace("{application_model}", encodeQuery(model)) ;
            if(version != null) {
                request_url = request_url.replace("{application_version}", encodeQuery(version)) ;
            }
            URL http_req_url =    new URL(request_url);

            HttpURLConnection con = getConfiguredConnection(http_req_url, HttpMethod.GET);

            LOGwriteFirstTrace(HttpMethod.GET, http_req_url.toString());
            LOGwriteDateTrace("application_vendor", vendor);
            LOGwriteDateTrace("application_model", model);
            if(version != null) {
                LOGwriteDateTrace("application_version", version);
            }

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
                response = mapper.readValue(reader, VnfImage.class);
                String original_buffer = mapper.writeValueAsString(response);
                LOGwriteEndingTrace(HttpURLConnection.HTTP_OK, "SUCCESS", original_buffer);
                if(response.getApplicationVendor() == null  /*&& response.getAdditionalProperties() != null && !response.getAdditionalProperties().isEmpty()*/){
                    LOG.warn("A List of multiple VNF-IMAGE entries has been returned");
                    VnfImages listOfObjects = mapper.readValue(original_buffer, VnfImages.class);
                    if(!listOfObjects.getVnfImage().isEmpty()) {
                        response = listOfObjects.getVnfImage().get(0);
                    }
                }
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
            LOG.warn("requestVnfImageData", exc);
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


    public boolean sendNotify(NotifyEvent event, String serviceInstanceId, String pathCode) throws AAIServiceException {
        InputStream inputStream = null;

        try {

            String selfLink = selflink_fqdn;
            if(SELFLINK_AVPN != null && SELFLINK_AVPN.equals(pathCode)) {
                selfLink = selflink_avpn;
            }
            selfLink = selfLink.replace("{service-instance-id}", encodeQuery(serviceInstanceId));
            event.setSelflink(selfLink);

            ObjectMapper mapper = getObjectMapper();
            String json_text = mapper.writeValueAsString(event);

            SSLSocketFactory sockFact = CTX.getSocketFactory();

            String request_url = target_uri+ubb_notify_path;
            URL http_req_url =    new URL(request_url);

            HttpURLConnection con = getConfiguredConnection(http_req_url, HttpMethod.PUT);

            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
            osw.write(json_text);
            osw.flush();
            osw.close();


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
            String request_url = target_uri+query_nodes_path;
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

    class AAIRequestExecutor implements AAIExecutorInterface {

        @Override
        public String get(AAIRequest request) throws AAIServiceException {
            String response = null;
            InputStream inputStream = null;
            HttpURLConnection con = null;
            URL requestUrl = null;

            String requestId = UUID.randomUUID().toString();
            StringBuilder errorStringBuilder = new StringBuilder();

            try {

                if(request.getRequestObject() != null) {
                    requestUrl = request.getRequestUrl(HttpMethod.POST, null);
                    requestUrl = appendDepth(requestUrl, request);
                    con = getConfiguredConnection(requestUrl, HttpMethod.POST);
                    String json_text = request.toJSONString();
                    LOGwriteDateTrace("data", json_text);
                    logMetricRequest(requestId, "POST "+requestUrl.getPath(), json_text, requestUrl.getPath());
                    OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
                    osw.write(json_text);
                    osw.flush();
                } else {
                    requestUrl = request.getRequestUrl(HttpMethod.GET, null);
                    requestUrl = appendDepth(requestUrl, request);
                    con = getConfiguredConnection(requestUrl, HttpMethod.GET);
                    logMetricRequest(requestId, "GET "+requestUrl.getPath(), "", requestUrl.getPath());
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
                logMetricResponse(requestId, responseCode, responseMessage);

                if(inputStream == null) inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
                BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );

                ObjectMapper mapper = getObjectMapper();

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
//                    StringBuilder errorStringBuilder = new StringBuilder();
                    String line = null;
                    while( ( line = reader.readLine() ) != null ) {
                        errorStringBuilder.append("\n").append( line );
                    }

                    ErrorResponse errorresponse = mapper.readValue(errorStringBuilder.toString(), ErrorResponse.class);
//                    ErrorResponse errorresponse = mapper.readValue(reader, ErrorResponse.class);
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

                    }
                }
            }
            return response;
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

        @Override
        public String post(AAIRequest request) throws AAIServiceException {
            InputStream inputStream = null;
            String requestId = UUID.randomUUID().toString();

            try {
                String resourceVersion = null;
                AAIDatum instance = request.getRequestObject();

                Method getResourceVersionMethod = instance.getClass().getMethod("getResourceVersion");
                if(getResourceVersionMethod != null){
                    try {
                        getResourceVersionMethod.setAccessible(true);
                        Object object = getResourceVersionMethod.invoke(instance);
                        if(object != null)
                            resourceVersion = object.toString();
                    } catch (InvocationTargetException x) {
                        Throwable cause = x.getCause();
                    }
                }

                URL requestUrl = null;
                HttpURLConnection con = getConfiguredConnection(requestUrl = request.getRequestUrl(HttpMethod.PUT, resourceVersion), HttpMethod.PUT);
                ObjectMapper mapper = getObjectMapper();
                String json_text = request.toJSONString();

                LOGwriteDateTrace("data", json_text);
                logMetricRequest(requestId, "PUT "+requestUrl.getPath(), json_text, requestUrl.getPath());

                OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
                osw.write(json_text);
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
                logMetricResponse(requestId,responseCode, responseMessage);

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
                    LOGwriteEndingTrace(responseCode, responseMessage, (stringBuilder != null) ? stringBuilder.toString() : "{no-data}");
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

                }
            }
        }

        @Override
        public Boolean delete(AAIRequest request, String resourceVersion) throws AAIServiceException {
            Boolean response = null;
            InputStream inputStream = null;
            String requestId = UUID.randomUUID().toString();

            if(resourceVersion == null) {
                throw new AAIServiceException("resource-version is required for DELETE request");
            }

            try {
                URL requestUrl = null;
                HttpURLConnection conn = getConfiguredConnection(requestUrl = request.getRequestUrl(HttpMethod.DELETE, resourceVersion), HttpMethod.DELETE);
                logMetricRequest(requestId, "DELETE "+requestUrl.getPath(), "", requestUrl.getPath());
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
                logMetricResponse(requestId,responseCode, responseMessage);

                if(inputStream == null) inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
                BufferedReader reader = new BufferedReader( new InputStreamReader( inputStream ) );
                String line = null;

                ObjectMapper mapper = getObjectMapper();

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

                    }
                }
            }
            return response;
        }

        @Override
        public Object query(AAIRequest request, Class clas) throws AAIServiceException {
            Object response = null;
            InputStream inputStream = null;
            HttpURLConnection con = null;
            URL requestUrl = null;
            String requestId = UUID.randomUUID().toString();

            try {
                requestUrl = request.getRequestQueryUrl(HttpMethod.GET);
                con = getConfiguredConnection(requestUrl , HttpMethod.GET);
                logMetricRequest(requestId, "GET "+requestUrl.getPath(), "", requestUrl.getPath());

                // Check for errors
                String responseMessage = con.getResponseMessage();
                int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    inputStream = con.getInputStream();
                } else {
                    inputStream = con.getErrorStream();
                }

                logMetricResponse(requestId,responseCode, responseMessage);
                ObjectMapper mapper = getObjectMapper();

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

                    }
                }
                con = null;
            }
            return response;
        }

        @Override
        public Boolean patch(AAIRequest request, String resourceVersion) throws AAIServiceException {
            InputStream inputStream = null;
            String requestId = UUID.randomUUID().toString();

            try {
                AAIDatum instance = request.getRequestObject();
                if(instance instanceof ResourceVersion) {
                    resourceVersion = ((ResourceVersion)instance).getResourceVersion();
                }

                URL requestUrl = null;
                HttpURLConnection con = getConfiguredConnection(requestUrl = request.getRequestUrl("PATCH", resourceVersion), "PATCH");
                ObjectMapper mapper = getObjectMapper();
                String json_text = request.toJSONString();

                LOGwriteDateTrace("data", json_text);
                logMetricRequest(requestId, "PATCH "+requestUrl.getPath(), json_text, requestUrl.getPath());

                OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
                osw.write(json_text);
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
                logMetricResponse(requestId,responseCode, responseMessage);

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
                    LOGwriteEndingTrace(responseCode, responseMessage,
                                               (stringBuilder.length() > 0) ? stringBuilder.toString() : "{no-data}");
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

                }
            }
        }
    }

    @Override
    public Tenant requestTenantData(String tenant_id, String cloudOwner, String cloudRegionId) throws AAIServiceException {
        Tenant response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("tenant");
            request.addRequestProperty("tenant.tenant-id", tenant_id);
            request.addRequestProperty("cloud-region.cloud-owner", cloudOwner);
            request.addRequestProperty("cloud-region.cloud-region-id", cloudRegionId);

            String rv = executor.get(request);
            if(rv != null) {
                ObjectMapper mapper = getObjectMapper();
                response = mapper.readValue(rv, Tenant.class);
            }
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestTenantData", exc);
            throw new AAIServiceException(exc);
        }

        return response;
    }

    @Override
    public Tenant requestTenantDataByName(String tenant_name, String cloudOwner, String cloudRegionId) throws AAIServiceException {
        Tenant response = null;

        try {
            AAIRequest request = AAIRequest.getRequestFromResource("tenant");
            request.addRequestProperty("tenant.tenant-name", tenant_name);
            request.addRequestProperty("cloud-region.cloud-owner", cloudOwner);
            request.addRequestProperty("cloud-region.cloud-region-id", cloudRegionId);
            Object rv = executor.query(request, Tenant.class);
            if(rv == null)
                return (Tenant)null;
            else
                response = (Tenant)rv;
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("requestTenantDataByName", exc);
            throw new AAIServiceException(exc);
        }

        return response;
    }


    @Override
    public boolean postTenantData(String tenant_id, String cloudOwner, String cloudRegionId, Tenant tenannt) throws AAIServiceException {
        try {
            AAIRequest request = AAIRequest.getRequestFromResource("tenant");
            request.addRequestProperty("tenant.tenant-id", tenant_id);
            request.addRequestProperty("cloud-region.cloud-owner", cloudOwner);
            request.addRequestProperty("cloud-region.cloud-region-id", cloudRegionId);
            request.setRequestObject(tenannt);
            Object response = executor.post(request);
            return true;
        } catch(AAIServiceException aaiexc) {
            throw aaiexc;
        } catch (Exception exc) {
            LOG.warn("postTenantData", exc);
            throw new AAIServiceException(exc);
        }
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
    public AAIRequestExecutor getExecutor() {
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


    protected boolean deleteRelationshipList(URL httpReqUrl, String json_text) throws AAIServiceException {
        if(httpReqUrl ==  null) {
            throw new NullPointerException();
        }

        boolean response = false;
        InputStream inputStream = null;

        try {
            HttpURLConnection con = getConfiguredConnection(httpReqUrl, HttpMethod.DELETE);

//            SSLSocketFactory sockFact = CTX.getSocketFactory();
//            con.setSSLSocketFactory( sockFact );
            OutputStreamWriter osw = new OutputStreamWriter(con.getOutputStream());
            osw.write(json_text);
            osw.flush();
            osw.close();


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
            LOG.warn("deleteRelationshipList", exc);
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
        return mapper;
    }

    public void logMetricRequest(String requestId, String targetServiceName, String msg, String path){
        String svcInstanceId = "";
        String svcName = null;
        String partnerName = null;
        String targetEntity = "A&AI";
        String targetVirtualEntity = null;

        targetServiceName = "";

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
     * @see org.openecomp.sdnc.sli.aai.haha#query(org.openecomp.sdnc.sli.aai.AAIRequest)
     */
    @Override
    public String query(AAIRequest request) throws AAIServiceException {
        return executor.get(request);
    }

    /* (non-Javadoc)
     * @see org.openecomp.sdnc.sli.aai.haha#save(org.openecomp.sdnc.sli.aai.AAIRequest)
     */
    @Override
    public String save(AAIRequest request) throws AAIServiceException {
        return executor.post(request);
    }

    public boolean update(AAIRequest request, String resourceVersion) throws AAIServiceException {
        return executor.patch(request, resourceVersion);
    }

    /* (non-Javadoc)
     * @see org.openecomp.sdnc.sli.aai.haha#delete(org.openecomp.sdnc.sli.aai.AAIRequest, java.lang.String)
     */
    @Override
    public boolean delete(AAIRequest request, String resourceVersion) throws AAIServiceException {
        return executor.patch(request, resourceVersion);
    }

}
