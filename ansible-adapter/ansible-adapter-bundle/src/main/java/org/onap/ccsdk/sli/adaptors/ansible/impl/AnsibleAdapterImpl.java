/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.ansible.impl;

import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.ccsdk.sli.adaptors.ansible.AnsibleAdapter;
import org.onap.ccsdk.sli.adaptors.ansible.AnsibleAdapterPropertiesProvider;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleMessageParser;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResult;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResultCodes;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleServerEmulator;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * This class implements the {@link AnsibleAdapter} interface. This interface defines the behaviors
 * that our service provides.
 */
public class AnsibleAdapterImpl implements AnsibleAdapter {


    /**
     * The constant used to define the service name in the mapped diagnostic context
     */
    @SuppressWarnings("nls")
    public static final String MDC_SERVICE = "service";

    /**
     * The constant for the status code for a failed outcome
     */
    @SuppressWarnings("nls")
    public static final String OUTCOME_FAILURE = "failure";

    /**
     * The constant for the status code for a successful outcome
     */
    @SuppressWarnings("nls")
    public static final String OUTCOME_SUCCESS = "success";

    /**
     * Adapter Name
     */
    private static final String ADAPTER_NAME = "Ansible Adapter";
    private static final String APPC_EXCEPTION_CAUGHT = "APPCException caught";

    private static final String RESULT_CODE_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.result.code";
    private static final String MESSAGE_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.message";
    private static final String RESULTS_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.results";
    private static final String ID_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.Id";
    private static final String LOG_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.log";
    private static final String OUTPUT_ATTRIBUTE_NAME = "org.onap.appc.adapter.ansible.output";

    private static final String CLIENT_TYPE_PROPERTY_NAME = "org.onap.appc.adapter.ansible.clientType";
    private static final String TRUSTSTORE_PROPERTY_NAME = "org.onap.appc.adapter.ansible.trustStore";
    private static final String TRUSTPASSD_PROPERTY_NAME = "org.onap.appc.adapter.ansible.trustStore.trustPasswd";

    private static final String PASSD = "Password";

    /**
     * The logger to be used
     */
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(AnsibleAdapterImpl.class);


    /**
     * Connection object
     **/
    private ConnectionBuilder httpClient;

    /**
     * Ansible API Message Handlers
     **/
    private AnsibleMessageParser messageProcessor;

    /**
     * indicator whether in test mode
     **/
    private boolean testMode = false;

    /**
     * server emulator object to be used if in test mode
     **/
    private AnsibleServerEmulator testServer;

    /**
     * This default constructor is used as a work around because the activator wasn't getting called
     */
    public AnsibleAdapterImpl() {
        initialize(new AnsibleAdapterPropertiesProviderImpl());
    }
    public AnsibleAdapterImpl(AnsibleAdapterPropertiesProvider propProvider) {
        initialize(propProvider);
    }

    /**
     * Used for jUnit test and testing interface
     */
    public AnsibleAdapterImpl(boolean mode) {
        testMode = mode;
        testServer = new AnsibleServerEmulator();
        messageProcessor = new AnsibleMessageParser();
    }

    /**
     * Returns the symbolic name of the adapter
     *
     * @return The adapter name
     * @see org.onap.appc.adapter.rest.AnsibleAdapter#getAdapterName()
     */
    @Override
    public String getAdapterName() {
        return ADAPTER_NAME;
    }

    /**
     * @param rc Method posts info to Context memory in case of an error and throws a
     *        SvcLogicException causing SLI to register this as a failure
     */
    @SuppressWarnings("static-method")
    private void doFailure(SvcLogicContext svcLogic, int code, String message) throws SvcLogicException {

        svcLogic.setStatus(OUTCOME_FAILURE);
        svcLogic.setAttribute(RESULT_CODE_ATTRIBUTE_NAME, Integer.toString(code));
        svcLogic.setAttribute(MESSAGE_ATTRIBUTE_NAME, message);

        throw new SvcLogicException("Ansible Adapter Error = " + message);
    }

    /**
     * initialize the Ansible adapter based on default and over-ride configuration data
     */
    private void initialize(AnsibleAdapterPropertiesProvider propProvider) {


        Properties props = propProvider.getProperties();

        // Create the message processor instance
        messageProcessor = new AnsibleMessageParser();

        // Create the http client instance
        // type of client is extracted from the property file parameter
        // org.onap.appc.adapter.ansible.clientType
        // It can be :
        // 1. TRUST_ALL (trust all SSL certs). To be used ONLY in dev
        // 2. TRUST_CERT (trust only those whose certificates have been stored in the trustStore file)
        // 3. DEFAULT (trust only well known certificates). This is standard behavior to which it will
        // revert. To be used in PROD

        try {
            String clientType = props.getProperty(CLIENT_TYPE_PROPERTY_NAME);
            logger.info("Ansible http client type set to " + clientType);

            if ("TRUST_ALL".equals(clientType)) {
                logger.info(
                        "Creating http client to trust ALL ssl certificates. WARNING. This should be done only in dev environments");
                httpClient = new ConnectionBuilder(1);
            } else if ("TRUST_CERT".equals(clientType)) {
                // set path to keystore file
                String trustStoreFile = props.getProperty(TRUSTSTORE_PROPERTY_NAME);
                String key = props.getProperty(TRUSTPASSD_PROPERTY_NAME);
                char[] trustStorePasswd = key.toCharArray();
                logger.info("Creating http client with trustmanager from " + trustStoreFile);
                httpClient = new ConnectionBuilder(trustStoreFile, trustStorePasswd);
            } else {
                logger.info("Creating http client with default behaviour");
                httpClient = new ConnectionBuilder(0);
            }
        } catch (Exception e) {
            logger.error("Error Initializing Ansible Adapter due to Unknown Exception", e);
        }

        logger.info("Initialized Ansible Adapter");
    }

    // Public Method to post request to execute playbook. Posts the following back
    // to Svc context memory
    //  org.onap.appc.adapter.ansible.req.code : 100 if successful
    //  org.onap.appc.adapter.ansible.req.messge : any message
    //  org.onap.appc.adapter.ansible.req.Id : a unique uuid to reference the request
    @Override
    public void reqExec(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {

        String playbookName = StringUtils.EMPTY;
        String payload = StringUtils.EMPTY;
        String agentUrl = StringUtils.EMPTY;
        String user = StringUtils.EMPTY;
        String password = StringUtils.EMPTY;
        String id = StringUtils.EMPTY;

        JSONObject jsonPayload;

        try {
            // create json object to send request
            jsonPayload = messageProcessor.reqMessage(params);

            agentUrl = (String) jsonPayload.remove("AgentUrl");
            user = (String) jsonPayload.remove("User");
            password = (String) jsonPayload.remove(PASSD);
            id = jsonPayload.getString("Id");
            payload = jsonPayload.toString();
            logger.info("Updated Payload  = " + payload);
        } catch (SvcLogicException e) {
            logger.error(APPC_EXCEPTION_CAUGHT, e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request for execution of playbook due to missing mandatory parameters. Reason = "
                            + e.getMessage());
        } catch (JSONException e) {
            logger.error("JSONException caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request for execution of playbook due to invalid JSON block. Reason = "
                            + e.getMessage());
        } catch (NumberFormatException e) {
            logger.error("NumberFormatException caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request for execution of playbook due to invalid parameter values. Reason = "
                            + e.getMessage());
        }

        int code = -1;
        String message = StringUtils.EMPTY;

        try {
            // post the test request
            logger.info("Posting request = " + payload + " to url = " + agentUrl);
            AnsibleResult testResult = postExecRequest(agentUrl, payload, user, password);

            // Process if HTTP was successful
            if (testResult.getStatusCode() == 200) {
                testResult = messageProcessor.parsePostResponse(testResult.getStatusMessage());
            } else {
                doFailure(ctx, testResult.getStatusCode(),
                        "Error posting request. Reason = " + testResult.getStatusMessage());
            }

            code = testResult.getStatusCode();
            message = testResult.getStatusMessage();

            // Check status of test request returned by Agent
            if (code == AnsibleResultCodes.PENDING.getValue()) {
                logger.info(String.format("Submission of Test %s successful.", playbookName));
                // test request accepted. We are in asynchronous case
            } else {
                doFailure(ctx, code, "Request for execution of playbook rejected. Reason = " + message);
            }
        } catch (SvcLogicException e) {
            logger.error(APPC_EXCEPTION_CAUGHT, e);
            doFailure(ctx, AnsibleResultCodes.UNKNOWN_EXCEPTION.getValue(),
                    "Exception encountered when posting request for execution of playbook. Reason = " + e.getMessage());
        }

        ctx.setAttribute(RESULT_CODE_ATTRIBUTE_NAME, Integer.toString(code));
        ctx.setAttribute(MESSAGE_ATTRIBUTE_NAME, message);
        ctx.setAttribute(ID_ATTRIBUTE_NAME, id);
    }

    /**
     * Public method to query status of a specific request It blocks till the Ansible Server
     * responds or the session times out (non-Javadoc)
     *
     * @see org.onap.ccsdk.sli.adaptors.ansible.AnsibleAdapter#reqExecResult(java.util.Map,
     *      org.onap.ccsdk.sli.core.sli.SvcLogicContext)
     */
    @Override
    public void reqExecResult(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {

        // Get URI
        String reqUri = StringUtils.EMPTY;

        try {
            reqUri = messageProcessor.reqUriResult(params);
            logger.info("Got uri ", reqUri );
        } catch (SvcLogicException e) {
            logger.error(APPC_EXCEPTION_CAUGHT, e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request to retrieve result due to missing parameters. Reason = "
                            + e.getMessage());
            return;
        } catch (NumberFormatException e) {
            logger.error("NumberFormatException caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(),
                    "Error constructing request to retrieve result due to invalid parameters value. Reason = "
                            + e.getMessage());
            return;
        }

        int code = -1;
        String message = StringUtils.EMPTY;
        String results = StringUtils.EMPTY;

        try {
            // Try to retrieve the test results (modify the URL for that)
            AnsibleResult testResult = queryServer(reqUri, params.get("User"), params.get(PASSD));
            code = testResult.getStatusCode();
            message = testResult.getStatusMessage();

            if (code == 200) {
                logger.info("Parsing response from Server = " + message);
                // Valid HTTP. process the Ansible message
                testResult = messageProcessor.parseGetResponse(message);
                code = testResult.getStatusCode();
                message = testResult.getStatusMessage();
                results = testResult.getResults();
            }

            logger.info("Request response = " + message);
        } catch (SvcLogicException e) {
            logger.error(APPC_EXCEPTION_CAUGHT, e);
            doFailure(ctx, AnsibleResultCodes.UNKNOWN_EXCEPTION.getValue(),
                    "Exception encountered retrieving result : " + e.getMessage());
            return;
        }

        // We were able to get and process the results. Determine if playbook succeeded

        if (code == AnsibleResultCodes.FINAL_SUCCESS.getValue()) {
            message = String.format("Ansible Request  %s finished with Result = %s, Message = %s", params.get("Id"),
                    OUTCOME_SUCCESS, message);
            logger.info(message);
        } else {
            logger.info(String.format("Ansible Request  %s finished with Result %s, Message = %s", params.get("Id"),
                    OUTCOME_FAILURE, message));
            ctx.setAttribute(RESULTS_ATTRIBUTE_NAME, results);
            doFailure(ctx, code, message);
            return;
        }

        ctx.setAttribute(RESULT_CODE_ATTRIBUTE_NAME, Integer.toString(400));
        ctx.setAttribute(MESSAGE_ATTRIBUTE_NAME, message);
        ctx.setAttribute(RESULTS_ATTRIBUTE_NAME, results);
        ctx.setStatus(OUTCOME_SUCCESS);
    }

    /**
     * Public method to get logs from playbook execution for a specific request
     *
     * It blocks till the Ansible Server responds or the session times out very similar to
     * reqExecResult logs are returned in the DG context variable org.onap.appc.adapter.ansible.log
     */
    @Override
    public void reqExecLog(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {

        String reqUri = StringUtils.EMPTY;
        try {
            reqUri = messageProcessor.reqUriLog(params);
            logger.info("Retrieving results from " + reqUri);
        } catch (Exception e) {
            logger.error("Exception caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(), e.getMessage());
        }

        String message = StringUtils.EMPTY;
        try {
            // Try to retrieve the test results (modify the url for that)
            AnsibleResult testResult = queryServer(reqUri, params.get("User"), params.get(PASSD));
            message = testResult.getStatusMessage();
            logger.info("Request output = " + message);
            ctx.setAttribute(LOG_ATTRIBUTE_NAME, message);
            ctx.setStatus(OUTCOME_SUCCESS);
        } catch (Exception e) {
            logger.error("Exception caught", e);
            doFailure(ctx, AnsibleResultCodes.UNKNOWN_EXCEPTION.getValue(),
                    "Exception encountered retreiving output : " + e.getMessage());
        }
    }

    /**
     * Public method to get output from playbook execution for a specific request
     *
     * It blocks till the Ansible Server responds or the session times out very similar to
     * reqExecResult and output is returned in the DG context variable org.onap.appc.adapter.ansible.output
     */
    @Override
    public void reqExecOutput(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {

        String reqUri = StringUtils.EMPTY;
        try {
            reqUri = messageProcessor.reqUriOutput(params);
            logger.info("Retrieving results from " + reqUri);
        } catch (Exception e) {
            logger.error("Exception caught", e);
            doFailure(ctx, AnsibleResultCodes.INVALID_PAYLOAD.getValue(), e.getMessage());
        }

        String message = StringUtils.EMPTY;
        try {
            // Try to retrieve the test results (modify the url for that)
            AnsibleResult testResult = queryServer(reqUri, params.get("User"), params.get(PASSD));
            message = testResult.getStatusMessage();
            logger.info("Request output = " + message);
            ctx.setAttribute(OUTPUT_ATTRIBUTE_NAME, message);
            ctx.setStatus(OUTCOME_SUCCESS);
        } catch (Exception e) {
            logger.error("Exception caught", e);
            doFailure(ctx, AnsibleResultCodes.UNKNOWN_EXCEPTION.getValue(),
                    "Exception encountered retreiving output : " + e.getMessage());
        }
    }

    /**
     * Method that posts the request
     */
    private AnsibleResult postExecRequest(String agentUrl, String payload, String user, String password) {

        AnsibleResult testResult;

        if (!testMode) {
            httpClient.setHttpContext(user, password);
            testResult = httpClient.post(agentUrl, payload);
        } else {
            testResult = testServer.Post(agentUrl, payload);
        }
        return testResult;
    }

    /**
     * Method to query Ansible server
     */
    private AnsibleResult queryServer(String agentUrl, String user, String password) {

        AnsibleResult testResult;

        logger.info("Querying url = " + agentUrl);

        if (!testMode) {
            testResult = httpClient.get(agentUrl);
        } else {
            testResult = testServer.Get(agentUrl);
        }

        return testResult;
    }
}
