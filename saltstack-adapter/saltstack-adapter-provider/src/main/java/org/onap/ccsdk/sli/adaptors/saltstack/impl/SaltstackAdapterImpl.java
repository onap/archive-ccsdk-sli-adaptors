/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics. All rights reserved.
 * ================================================================================
 *
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
 *
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.saltstack.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

/**
 * This class implements the {@link SaltstackAdapter} interface. This interface defines the behaviors
 * that our service provides.
 */
public class SaltstackAdapterImpl implements SaltstackAdapter {

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
    public static final String CONNECTION_RETRY_DELAY = "retryDelay";
    public static final String CONNECTION_RETRY_COUNT = "retryCount";
    private static final String APPC_EXCEPTION_CAUGHT = "APPCException caught";
    /**
     * Adapter Name
     */
    private static final String ADAPTER_NAME = "Saltstack Adapter";
    private static final String RESULT_CODE_ATTRIBUTE_NAME = "org.onap.appc.adapter.saltstack.result.code";
    private static final String MESSAGE_ATTRIBUTE_NAME = "org.onap.appc.adapter.saltstack.message";
    private static final String ID_ATTRIBUTE_NAME = "org.onap.appc.adapter.saltstack.Id";
    private static final String CLIENT_TYPE_PROPERTY_NAME = "org.onap.appc.adapter.saltstack.clientType";
    private static final String SS_SERVER_HOSTNAME = "org.onap.appc.adapter.saltstack.host";
    private static final String SS_SERVER_PORT = "org.onap.appc.adapter.saltstack.port";
    private static final String SS_SERVER_USERNAME = "org.onap.appc.adapter.saltstack.userName";
    private static final String SS_SERVER_PASSWD = "org.onap.appc.adapter.saltstack.userPasswd";
    private static final String SS_SERVER_SSH_KEY = "org.onap.appc.adapter.saltstack.sshKey";

    private static final String COMMAND_IN_JSON_OUT = " --out=json --static ";
    private static final String COMMAND_CHANGE_DEFAULT_DIR = " cd /srv/salt/ ;";

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
    public SaltstackAdapterImpl() throws SvcLogicException {
        initialize(new SaltstackAdapterPropertiesProviderImpl());
    }

    public SaltstackAdapterImpl(SaltstackAdapterPropertiesProvider propProvider) throws SvcLogicException {
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
     * @see SaltstackAdapter#getAdapterName()
     */
    @Override
    public String getAdapterName() {
        return ADAPTER_NAME;
    }

    /**
     * Method posts info to Context memory in case of an error and throws a
     * SvcLogicException causing SLI to register this as a failure
     */
    @SuppressWarnings("static-method")
    private void doFailure(SvcLogicContext svcLogic, int code, String message) throws SvcLogicException {
        logger.error(APPC_EXCEPTION_CAUGHT, message);
        svcLogic.setStatus(OUTCOME_FAILURE);
        svcLogic.setAttribute(RESULT_CODE_ATTRIBUTE_NAME, Integer.toString(code));
        svcLogic.setAttribute(MESSAGE_ATTRIBUTE_NAME, message);
        throw new SvcLogicException("Saltstack Adapter Error = " + message);
    }

    /**
     * initialize the Saltstack adapter based on default and over-ride configuration data
     */
    private void initialize(SaltstackAdapterPropertiesProvider propProvider) throws SvcLogicException {


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

            if ("BASIC".equalsIgnoreCase(clientType)) {
                logger.info("Creating ssh client connection");
                // set path to keystore file
                String sshHost = props.getProperty(SS_SERVER_HOSTNAME);
                String sshPort = reqServerPort(props);
                String sshUserName = props.getProperty(SS_SERVER_USERNAME);
                String sshPassword = props.getProperty(SS_SERVER_PASSWD);
                sshClient = new ConnectionBuilder(sshHost, sshPort, sshUserName, sshPassword);
            } else if ("SSH_CERT".equalsIgnoreCase(clientType)) {
                // set path to keystore file
                String sshKey = props.getProperty(SS_SERVER_SSH_KEY);
                String sshHost = props.getProperty(SS_SERVER_HOSTNAME);
                String sshPort = reqServerPort(props);
                logger.info("Creating ssh client with ssh KEY from " + sshKey);
                sshClient = new ConnectionBuilder(sshHost, sshPort, sshKey);
            } else if ("BOTH".equalsIgnoreCase(clientType)) {
                // set path to keystore file
                String sshKey = props.getProperty(SS_SERVER_SSH_KEY);
                String sshHost = props.getProperty(SS_SERVER_HOSTNAME);
                String sshUserName = props.getProperty(SS_SERVER_USERNAME);
                String sshPassword = props.getProperty(SS_SERVER_PASSWD);
                String sshPort = reqServerPort(props);
                logger.info("Creating ssh client with ssh KEY from " + sshKey);
                sshClient = new ConnectionBuilder(sshHost, sshPort, sshUserName, sshPassword, sshKey);
            } else {
                logger.info("No saltstack-adapter.properties defined so reading from DG props");
                sshClient = null;
            }
        } catch (NumberFormatException e) {
            logger.error("Error Initializing Saltstack Adapter due to Unknown Exception", e);
            throw new SvcLogicException("Saltstack Adapter Property file parsing Error = port in property file has to be an integer.");
        } catch (Exception e) {
            logger.error("Error Initializing Saltstack Adapter due to Exception", e);
            throw new SvcLogicException("Saltstack Adapter Property file parsing Error = " + e.getMessage());
        }
        logger.info("Initialized Saltstack Adapter");
    }

    private String reqServerPort(Properties props) {
        // use default port if null
        if (props.getProperty(SS_SERVER_PORT) == null) {
            return "22";
        }
        return props.getProperty(SS_SERVER_PORT);
    }

    private void setSSHClient(Map<String, String> params) throws SvcLogicException {
        if (sshClient == null) {
            logger.info("saltstack-adapter.properties not defined so reading saltstack host and " +
                                "auth details from DG's parameters");
            String sshHost = messageProcessor.reqHostNameResult(params);
            String sshPort = messageProcessor.reqPortResult(params);
            String sshUserName = messageProcessor.reqUserNameResult(params);
            String sshPassword = messageProcessor.reqPasswordResult(params);
            logger.info("Creating ssh client with BASIC Auth");
            if (!testMode) {
                sshClient = new ConnectionBuilder(sshHost, sshPort, sshUserName, sshPassword);
            }
        }
    }

    private String parseEnvParam(JSONObject envParams) {
        StringBuilder envParamBuilder = new StringBuilder();
        if (envParams != null) {
            for (Object key : envParams.keySet()) {
                if (envParamBuilder.length() > 0) {
                    envParamBuilder.append(", ");
                }
                envParamBuilder.append(key + "=" + envParams.get((String) key));
                logger.info("EnvParameters : " + envParamBuilder);
            }
        }
        return envParamBuilder.toString();
    }

    private String parseFileParam(JSONObject fileParams) {
        StringBuilder fileParamBuilder = new StringBuilder();
        if (fileParams != null) {
            for (Object key : fileParams.keySet()) {
                fileParamBuilder.append("echo -e \"" + fileParams.get((String) key) + "\" > /srv/salt/" + key).append("; ");
                logger.info("FileParameters : " + fileParamBuilder);
            }
        }
        return fileParamBuilder.toString();
    }

    private String putToCommands(SvcLogicContext ctx, String slsFileName,
                                 String applyTo, JSONObject envParams, JSONObject fileParams) throws SvcLogicException {

        StringBuilder constructedCommand = new StringBuilder();
        try {
            File file = new File(slsFileName);
            String slsFile = file.getName();
            if (!slsFile.substring(slsFile.lastIndexOf("."),
                                   slsFile.length()).equalsIgnoreCase(".sls")) {
                doFailure(ctx, SaltstackResultCodes.IO_EXCEPTION.getValue(), "Input file " +
                        "is not of type .sls");
            }
            InputStream in = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            in.read(data);
            String str = new String(data, "UTF-8");
            in.close();
            String slsWithoutExtn = stripExtension(slsFile);
            constructedCommand.append(parseFileParam(fileParams)).append("echo -e \"").append(str).append("\" > /srv/salt/").
                    append(slsFile).append("; ").append(COMMAND_CHANGE_DEFAULT_DIR).append(" salt '").
                    append(applyTo).append("' state.apply ").append(slsWithoutExtn).append(" ").append(parseEnvParam(envParams)).append(COMMAND_IN_JSON_OUT);

            logger.info("Command to be executed on server : " + constructedCommand.toString());

        } catch (FileNotFoundException e) {
            doFailure(ctx, SaltstackResultCodes.IO_EXCEPTION.getValue(), "Input SLS file " +
                    "not found in path : " + slsFileName + ". " + e.getMessage());
        } catch (IOException e) {
            doFailure(ctx, SaltstackResultCodes.IO_EXCEPTION.getValue(), "Input SLS file " +
                    "error in path : " + slsFileName + ". " + e.getMessage());
        } catch (StringIndexOutOfBoundsException e) {
            doFailure(ctx, SaltstackResultCodes.IO_EXCEPTION.getValue(), "Input file " +
                    "is not of type .sls");
        }
        return constructedCommand.toString();
    }

    private String stripExtension(String str) {
        if (str == null) {
            return null;
        }
        int pos = str.lastIndexOf(".");
        if (pos == -1) {
            return str;
        }
        return str.substring(0, pos);
    }

    private String putToCommands(String slsName, String applyTo, JSONObject envParams, JSONObject fileParams) {

        StringBuilder constructedCommand = new StringBuilder();

        constructedCommand.append(parseFileParam(fileParams)).append(COMMAND_CHANGE_DEFAULT_DIR).append(" salt '").append(applyTo)
                .append("' state.apply ").append(slsName).append(" ").append(parseEnvParam(envParams)).append(COMMAND_IN_JSON_OUT);

        logger.info("Command to be executed on server : " + constructedCommand.toString());
        return constructedCommand.toString();
    }

    private void checkResponseStatus(SaltstackResult testResult, SvcLogicContext ctx, String reqID, boolean slsExec)
            throws SvcLogicException {

        // Check status of test request returned by Agent
        if (testResult.getStatusCode() != SaltstackResultCodes.FINAL_SUCCESS.getValue()) {
            ctx.setAttribute(ID_ATTRIBUTE_NAME, reqID);
            doFailure(ctx, testResult.getStatusCode(), "Request for execution of command failed. Reason = " + testResult.getStatusMessage());
        } else {
            logger.info(String.format("Execution of request : successful."));
            ctx.setAttribute(RESULT_CODE_ATTRIBUTE_NAME, Integer.toString(testResult.getStatusCode()));
            ctx.setAttribute(MESSAGE_ATTRIBUTE_NAME, OUTCOME_SUCCESS);
            ctx.setAttribute(ID_ATTRIBUTE_NAME, reqID);
        }
    }

    // Public Method to post single command request to execute saltState. Posts the following back
    // to Svc context memory
    //  org.onap.appc.adapter.saltstack.req.code : 100 if successful
    //  org.onap.appc.adapter.saltstack.req.messge : any message
    //  org.onap.appc.adapter.saltstack.req.Id : a unique uuid to reference the request
    @Override
    public void reqExecCommand(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        String reqID;
        boolean slsExec;
        SaltstackResult testResult;
        setSSHClient(params);
        try {
            reqID = messageProcessor.reqId(params);
            String commandToExecute = messageProcessor.reqCmd(params);
            slsExec = messageProcessor.reqIsSLSExec(params);
            long execTimeout = messageProcessor.reqExecTimeout(params);
            testResult = execCommand(ctx, params, commandToExecute, execTimeout);
            testResult = messageProcessor.parseResponse(ctx, reqID, testResult, slsExec);
            checkResponseStatus(testResult, ctx, reqID, slsExec);
        } catch (IOException e) {
            doFailure(ctx, SaltstackResultCodes.IO_EXCEPTION.getValue(),
                      "IOException in file stream : " + e.getMessage());
        }
    }

    /**
     * Public Method to post SLS command request to execute saltState on server. Posts the following back
     * to Svc context memory
     * <p>
     * org.onap.appc.adapter.saltstack.req.code : 200 if successful
     * org.onap.appc.adapter.saltstack.req.messge : any message
     * org.onap.appc.adapter.saltstack.req.Id : a unique uuid to reference the request
     */
    @Override
    public void reqExecSLS(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        String reqID;
        SaltstackResult testResult;
        setSSHClient(params);
        try {
            reqID = messageProcessor.reqId(params);
            String slsName = messageProcessor.reqSlsName(params);
            String applyTo = messageProcessor.reqApplyToDevices(params);
            long execTimeout = messageProcessor.reqExecTimeout(params);
            JSONObject envParams = messageProcessor.reqEnvParameters(params);
            JSONObject fileParams = messageProcessor.reqFileParameters(params);

            String commandToExecute = putToCommands(slsName, applyTo, envParams, fileParams);
            testResult = execCommand(ctx, params, commandToExecute, execTimeout);
            testResult = messageProcessor.parseResponse(ctx, reqID, testResult, true);
            checkResponseStatus(testResult, ctx, reqID, true);
        } catch (IOException e) {
            doFailure(ctx, SaltstackResultCodes.IO_EXCEPTION.getValue(),
                      "IOException in file stream : " + e.getMessage());
        } catch (JSONException e) {
            doFailure(ctx, SaltstackResultCodes.INVALID_COMMAND.getValue(), e.getMessage());
        }
    }

    /**
     * Public Method to post SLS file request to execute saltState. Posts the following back
     * to Svc context memory
     * <p>
     * org.onap.appc.adapter.saltstack.req.code : 100 if successful
     * org.onap.appc.adapter.saltstack.req.messge : any message
     * org.onap.appc.adapter.saltstack.req.Id : a unique uuid to reference the request
     */
    @Override
    public void reqExecSLSFile(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException {
        String reqID;
        SaltstackResult testResult;
        setSSHClient(params);
        try {
            reqID = messageProcessor.reqId(params);
            String slsFile = messageProcessor.reqSlsFile(params);
            String applyTo = messageProcessor.reqApplyToDevices(params);
            long execTimeout = messageProcessor.reqExecTimeout(params);
            JSONObject envParams = messageProcessor.reqEnvParameters(params);
            JSONObject fileParams = messageProcessor.reqFileParameters(params);

            String commandToExecute = putToCommands(ctx, slsFile, applyTo, envParams, fileParams);
            testResult = execCommand(ctx, params, commandToExecute, execTimeout);
            testResult = messageProcessor.parseResponse(ctx, reqID, testResult, true);
            checkResponseStatus(testResult, ctx, reqID, true);
        } catch (IOException e) {
            doFailure(ctx, SaltstackResultCodes.IO_EXCEPTION.getValue(),
                      "IOException in file stream : " + e.getMessage());
        }
    }

    public SaltstackResult execCommand(SvcLogicContext ctx, Map<String, String> params, String commandToExecute,
                                       long execTimeout)
            throws SvcLogicException {

        SaltstackResult testResult = new SaltstackResult();
        try {
            if (params.get(CONNECTION_RETRY_DELAY) != null && params.get(CONNECTION_RETRY_COUNT) != null) {
                int retryDelay = Integer.parseInt(params.get(CONNECTION_RETRY_DELAY));
                int retryCount = Integer.parseInt(params.get(CONNECTION_RETRY_COUNT));
                if (!testMode) {
                    testResult = sshClient.connectNExecute(commandToExecute, retryCount, retryDelay, execTimeout);
                } else {
                    testResult = testServer.mockReqExec(params);
                }
            } else {
                if (!testMode) {
                    testResult = sshClient.connectNExecute(commandToExecute, execTimeout);
                } else {
                    testResult = testServer.mockReqExec(params);
                }
            }
        } catch (IOException e) {
            doFailure(ctx, SaltstackResultCodes.IO_EXCEPTION.getValue(),
                      "IOException in file stream : " + e.getMessage());
        }
        return testResult;
    }
}
