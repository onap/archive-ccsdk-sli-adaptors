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

package org.onap.ccsdk.sli.adaptors.saltstack.impl;

import java.util.Map;
import java.util.Properties;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.ccsdk.sli.adaptors.saltstack.SaltstackAdapter;
import org.onap.ccsdk.sli.adaptors.saltstack.SaltstackAdapterPropertiesProvider;
import org.onap.ccsdk.sli.adaptors.saltstack.model.SaltstackMessageParser;
import org.onap.ccsdk.sli.adaptors.saltstack.model.SaltstackResult;
import org.onap.ccsdk.sli.adaptors.saltstack.model.SaltstackResultCodes;
import org.onap.ccsdk.sli.adaptors.saltstack.model.SaltstackServerEmulator;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * This class implements the {@link SaltstackAdapter} interface. This interface defines the behaviors
 * that our service provides.
 */
public class SaltstackAdapterImpl implements SaltstackAdapter {

    private static final long EXEC_TIMEOUT = 120000;
    private long timeout = EXEC_TIMEOUT;

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
    private static final String ADAPTER_NAME = "Saltstack Adapter";
    private static final String APPC_EXCEPTION_CAUGHT = "APPCException caught";

    private static final String RESULT_CODE_ATTRIBUTE_NAME = "org.onap.appc.adapter.saltstack.result.code";
    private static final String MESSAGE_ATTRIBUTE_NAME = "org.onap.appc.adapter.saltstack.message";
    private static final String RESULTS_ATTRIBUTE_NAME = "org.onap.appc.adapter.saltstack.results";
    private static final String ID_ATTRIBUTE_NAME = "org.onap.appc.adapter.saltstack.Id";
    private static final String LOG_ATTRIBUTE_NAME = "org.onap.appc.adapter.saltstack.log";

    private static final String CLIENT_TYPE_PROPERTY_NAME = "org.onap.appc.adapter.saltstack.clientType";
    private static final String SS_SERVER_USERNAME = "org.onap.appc.adapter.saltstack.userName";
    private static final String SS_SERVER_PASSWORD = "org.onap.appc.adapter.saltstack.userPasswd";
    private static final String SS_SERVER_SSH_KEY = "org.onap.appc.adapter.saltstack.sshKey";


    /**
     * The logger to be used
     */
    private static final EELFLogger logger = EELFManager.getInstance().getLogger(SaltstackAdapterImpl.class);


    /**
     * Connection object
     **/
    private ConnectionBuilder sshClient;

    /**
     * Saltstack API Message Handlers
     **/
    private SaltstackMessageParser messageProcessor;

    /**
     * indicator whether in test mode
     **/
    private boolean testMode = false;

    /**
     * server emulator object to be used if in test mode
     **/
    private SaltstackServerEmulator testServer;

    /**
     * This default constructor is used as a work around because the activator wasn't getting called
     */
    public SaltstackAdapterImpl() {
        initialize(new SaltstackAdapterPropertiesProviderImpl());
    }
    public SaltstackAdapterImpl(SaltstackAdapterPropertiesProvider propProvider) {
        initialize(propProvider);
    }

    /**
     * Used for jUnit test and testing interface
     */
    public SaltstackAdapterImpl(boolean mode) {
        testMode = mode;
        testServer = new SaltstackServerEmulator();
        messageProcessor = new SaltstackMessageParser();
    }

    /**
     * Returns the symbolic name of the adapter
     *
     * @return The adapter name
     * @see org.onap.appc.adapter.rest.SaltstackAdapter#getAdapterName()
     */
    @Override
    public String getAdapterName() {
        return ADAPTER_NAME;
    }

    @Override
    public void setExecTimeout(long timeout) {
        this.timeout = timeout;
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

        throw new SvcLogicException("Saltstack Adapter Error = " + message);
    }

    /**
     * initialize the Saltstack adapter based on default and over-ride configuration data
     */
    private void initialize(SaltstackAdapterPropertiesProvider propProvider) {


        Properties props = propProvider.getProperties();

        // Create the message processor instance
        messageProcessor = new SaltstackMessageParser();

        // Create the ssh client instance
        // type of client is extracted from the property file parameter
        // org.onap.appc.adapter.saltstack.clientType
        // It can be :
        // 1. BASIC. SSH Connection using username and password
        // 2. SSH_CERT (trust only those whose certificates have been stored in the SSH KEY file)
        // 3. DEFAULT SSH Connection without any authentication

        try {
            String clientType = props.getProperty(CLIENT_TYPE_PROPERTY_NAME);
            logger.info("Saltstack ssh client type set to " + clientType);

            if ("BASIC".equals(clientType)) {
                logger.info("Creating ssh client connection");
                // set path to keystore file
                String trustStoreFile = props.getProperty(SS_SERVER_USERNAME);
                String key = props.getProperty(SS_SERVER_PASSWORD);
                //TODO: Connect to SSH Saltstack server (using username and password) and return client to execute command
                sshClient = null;
            } else if ("SSH_CERT".equals(clientType)) {
                // set path to keystore file
                String key = props.getProperty(SS_SERVER_SSH_KEY);
                logger.info("Creating ssh client with ssh KEY from " + key);
                //TODO: Connect to SSH Saltstack server (using SSH Key) and return client to execute command
                sshClient = null;
            } else {
                logger.info("Creating ssh client without any Auth");
                //TODO: Connect to SSH Saltstack server without any Auth
                sshClient = null;
            }
        } catch (Exception e) {
            logger.error("Error Initializing Saltstack Adapter due to Unknown Exception", e);
        }

        logger.info("Initialized Saltstack Adapter");
    }

    // Public Method to post single command request to execute saltState. Posts the following back
    // to Svc context memory
    //  org.onap.appc.adapter.saltstack.req.code : 100 if successful
    //  org.onap.appc.adapter.saltstack.req.messge : any message
    //  org.onap.appc.adapter.saltstack.req.Id : a unique uuid to reference the request
    @Override
    public void reqExecCommand(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        //TODO: to implement
    }

    /**
     * Public Method to post SLS file request to execute saltState. Posts the following back
     * to Svc context memory
     *
     * org.onap.appc.adapter.saltstack.req.code : 100 if successful
     * org.onap.appc.adapter.saltstack.req.messge : any message
     * org.onap.appc.adapter.saltstack.req.Id : a unique uuid to reference the request
     */
    @Override
    public void reqExecSLS(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        //TODO: to implement

    }

    /**
     * Public method to get logs from saltState execution for a specific request Posts the following back
     * to Svc context memory
     *
     * It blocks till the Saltstack Server responds or the session times out very similar to
     * reqExecResult logs are returned in the DG context variable org.onap.appc.adapter.saltstack.log
     */
    @Override
    public void reqExecLog(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        //TODO: to implement

    }
}
