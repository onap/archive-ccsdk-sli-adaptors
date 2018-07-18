/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.saltstack.model;

/**
 * This module implements the APP-C/Saltstack Server interface
 * based on the REST API specifications
 */

import com.google.common.base.Strings;
import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Class that validates and constructs requests sent/received from
 * Saltstack Server
 */
public class SaltstackMessageParser {

    private static final String SS_AGENT_HOSTNAME_KEY = "HostName";
    private static final String SS_AGENT_PORT_KEY = "Port";
    private static final String PASS_KEY = "Password";
    private static final String USER_KEY = "User";
    private static final String CMD_EXEC = "Cmd"; //cmd
    private static final String IS_SLS_EXEC = "SlsExec"; //slsExec
    private static final String SS_REQ_ID = "Id";
    private static final String SLS_FILE_LOCATION = "SlsFile"; //slsFile
    private static final String SLS_NAME = "SlsName"; //slsName
    private static final String MINION_TO_APPLY = "NodeList"; //applyTo
    private static final String EXEC_TIMEOUT_TO_APPLY = "Timeout"; //execTimeout
    private static final String FILE_PARAMETERS_OPT_KEY = "FileParameters";
    private static final String ENV_PARAMETERS_OPT_KEY = "EnvParameters";

    private static final Logger LOGGER = LoggerFactory.getLogger(SaltstackMessageParser.class);

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate PORT number.
     */
    public String reqPortResult(Map<String, String> params) throws SvcLogicException {

        final String[] mandatoryTestParams = {SS_AGENT_HOSTNAME_KEY, SS_AGENT_PORT_KEY, USER_KEY,
                PASS_KEY};

        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
        }
        return params.get(SS_AGENT_PORT_KEY);
    }

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate HOST name.
     */
    public String reqHostNameResult(Map<String, String> params) throws SvcLogicException {

        final String[] mandatoryTestParams = {SS_AGENT_HOSTNAME_KEY, SS_AGENT_PORT_KEY, USER_KEY,
                PASS_KEY};

        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
        }
        return params.get(SS_AGENT_HOSTNAME_KEY);
    }

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate request ID.
     */
    public String reqId(Map<String, String> params) {

        if (params.get(SaltstackMessageParser.SS_REQ_ID) == null) {
            return UUID.randomUUID().toString();
        } else if (params.get(SaltstackMessageParser.SS_REQ_ID).equalsIgnoreCase("")) {
            return UUID.randomUUID().toString();
        }
        return params.get(SaltstackMessageParser.SS_REQ_ID);
    }

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate command to execute.
     */
    public String reqCmd(Map<String, String> params) throws SvcLogicException {

        final String[] mandatoryTestParams = {CMD_EXEC, IS_SLS_EXEC};

        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
        }

        return params.get(SaltstackMessageParser.CMD_EXEC);
    }

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate SLS file location to execute.
     */
    public String reqSlsFile(Map<String, String> params) throws SvcLogicException {

        final String[] mandatoryTestParams = {SLS_FILE_LOCATION};

        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
        }

        return params.get(SaltstackMessageParser.SLS_FILE_LOCATION);
    }

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate SLS file location to execute.
     */
    public String reqSlsName(Map<String, String> params) throws SvcLogicException {

        final String[] mandatoryTestParams = {SLS_NAME};

        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
        }
        String slsName = params.get(SaltstackMessageParser.SLS_NAME);
        try {
            if (slsName.substring(slsName.lastIndexOf("."), slsName.length()).equalsIgnoreCase(".sls")) {
                return stripExtension(slsName);
            }
        } catch (StringIndexOutOfBoundsException e) {
            return slsName;
        }
        return slsName;
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

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate minions/vnfc to execute the SLS file.
     */
    public String reqApplyToDevices(Map<String, String> params) {

        if (params.get(SaltstackMessageParser.MINION_TO_APPLY) == null) {
            return "*";
        } else if (params.get(SaltstackMessageParser.MINION_TO_APPLY).equalsIgnoreCase("")) {
            return "*";
        }
        return params.get(SaltstackMessageParser.MINION_TO_APPLY);
    }

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate minions/vnfc to execute the SLS file.
     */
    public long reqExecTimeout(Map<String, String> params) {

        if (params.get(SaltstackMessageParser.EXEC_TIMEOUT_TO_APPLY) == null) {
            return -1;
        } else if (params.get(SaltstackMessageParser.EXEC_TIMEOUT_TO_APPLY).equalsIgnoreCase("")) {
            return -1;
        }
        return Long.parseLong(params.get(SaltstackMessageParser.EXEC_TIMEOUT_TO_APPLY));
    }

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate EnvParameters to execute the SLS file.
     */
    public JSONObject reqEnvParameters(Map<String, String> params) throws JSONException {

        JSONObject jsonPayload = new JSONObject();
        final String[] optionalTestParam = { SaltstackMessageParser.ENV_PARAMETERS_OPT_KEY };
        parseParam(params, optionalTestParam, jsonPayload);

        return (JSONObject) jsonPayload.remove(SaltstackMessageParser.ENV_PARAMETERS_OPT_KEY);
    }

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate EnvParameters to execute the SLS file.
     */
    public JSONObject reqFileParameters(Map<String, String> params) throws JSONException {

        JSONObject jsonPayload = new JSONObject();
        final String[] optionalTestParam = { SaltstackMessageParser.FILE_PARAMETERS_OPT_KEY };
        parseParam(params, optionalTestParam, jsonPayload);

        return (JSONObject) jsonPayload.remove(SaltstackMessageParser.FILE_PARAMETERS_OPT_KEY);
    }

    private void parseParam(Map<String, String> params, String[] optionalTestParams, JSONObject jsonPayload)
            throws JSONException {

        Set<String> optionalParamsSet = new HashSet<>();
        Collections.addAll(optionalParamsSet, optionalTestParams);

        //@formatter:off
        params.entrySet()
                .stream()
                .filter(entry -> optionalParamsSet.contains(entry.getKey()))
                .filter(entry -> !Strings.isNullOrEmpty(entry.getValue()))
                .forEach(entry -> parseParam(entry, jsonPayload));
        //@formatter:on
    }

    private void parseParam(Map.Entry<String, String> params, JSONObject jsonPayload)
            throws JSONException {
        String key = params.getKey();
        String payload = params.getValue();

        switch (key) {
            case ENV_PARAMETERS_OPT_KEY:
                JSONObject paramsJson = new JSONObject(payload);
                jsonPayload.put(key, paramsJson);
                break;

            case FILE_PARAMETERS_OPT_KEY:
                jsonPayload.put(key, getFilePayload(payload));
                break;

            default:
                break;
        }
    }

    /**
     * Return payload with escaped newlines
     */
    private JSONObject getFilePayload(String payload) {
        String formattedPayload = payload.replace("\n", "\\n").replace("\r", "\\r");
        return new JSONObject(formattedPayload);
    }

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate IsSLSExec true or false.
     */
    public boolean reqIsSLSExec(Map<String, String> params) throws SvcLogicException {

        final String[] mandatoryTestParams = {CMD_EXEC, IS_SLS_EXEC};

        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
        }

        return params.get(SaltstackMessageParser.IS_SLS_EXEC).equalsIgnoreCase("true");
    }

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate Saltstack server login user name.
     */
    public String reqUserNameResult(Map<String, String> params) throws SvcLogicException {

        final String[] mandatoryTestParams = {SS_AGENT_HOSTNAME_KEY, SS_AGENT_PORT_KEY, USER_KEY,
                PASS_KEY};

        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
        }
        return params.get(USER_KEY);
    }

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate Saltstack server login password.
     */
    public String reqPasswordResult(Map<String, String> params) throws SvcLogicException {

        final String[] mandatoryTestParams = {SS_AGENT_HOSTNAME_KEY, SS_AGENT_PORT_KEY, USER_KEY,
                PASS_KEY};

        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
        }
        return params.get(PASS_KEY);
    }

    /**
     * This method parses response from the Saltstack Server when we do a post
     * and returns an SaltstackResult object.
     */
    public SaltstackResult parseResponse(SvcLogicContext ctx, String pfx,
                                         SaltstackResult saltstackResult, boolean slsExec) throws IOException {
        int code = saltstackResult.getStatusCode();
        InputStream in = null;
        boolean executionStatus = true, retCodeFound = false;
        if (code != SaltstackResultCodes.SUCCESS.getValue()) {
            return saltstackResult;
        }
        try {
            File file = new File(saltstackResult.getOutputFileName());
            in = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            in.read(data);
            String str = new String(data, "UTF-8");
            in.close();
            Map<String, String> mm = JsonParser.convertToProperties(str);
            if (mm != null) {
                for (Map.Entry<String, String> entry : mm.entrySet()) {
                    if (entry.getKey().contains("retcode")) {
                        retCodeFound = true;
                        if (!entry.getValue().equalsIgnoreCase("0")) {
                            executionStatus = false;
                        }
                    }
                    ctx.setAttribute(pfx + "." + entry.getKey(), entry.getValue());
                    LOGGER.info("+++ " + pfx + "." + entry.getKey() + ": [" + entry.getValue() + "]");
                }
            }
        } catch (FileNotFoundException e) {
            return new SaltstackResult(SaltstackResultCodes.INVALID_RESPONSE_FILE.getValue(), "error parsing response file "
                    + saltstackResult.getOutputFileName() + " : " + e.getMessage());
        } catch (org.codehaus.jettison.json.JSONException e) {
            LOGGER.info("Output not in JSON format");
            return putToProperties(ctx, pfx, saltstackResult);
        } catch (Exception e) {
            return new SaltstackResult(SaltstackResultCodes.INVALID_RESPONSE_FILE.getValue(), "error parsing response file "
                    + saltstackResult.getOutputFileName() + " : " + e.getMessage());
        } finally {
            if (in != null) {
                in.close();
            }
        }
        if (slsExec) {
            if (!retCodeFound) {
                return new SaltstackResult(SaltstackResultCodes.COMMAND_EXEC_FAILED_STATUS.getValue(),
                                           "error in executing configuration at the server, check your command input");
            }
            if (!executionStatus) {
                return new SaltstackResult(SaltstackResultCodes.COMMAND_EXEC_FAILED_STATUS.getValue(),
                                           "error in executing configuration at the server, check your command input");
            }
        }
        saltstackResult.setStatusCode(SaltstackResultCodes.FINAL_SUCCESS.getValue());
        return saltstackResult;
    }

    public SaltstackResult putToProperties(SvcLogicContext ctx, String pfx,
                                           SaltstackResult saltstackResult) throws IOException {
        InputStream in = null;
        try {
            File file = new File(saltstackResult.getOutputFileName());
            in = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(in);
            ctx.setAttribute(pfx + "completeResult", prop.toString());
            for (Object key : prop.keySet()) {
                String name = (String) key;
                String value = prop.getProperty(name);
                if (value != null && value.trim().length() > 0) {
                    ctx.setAttribute(pfx + "." + name, value.trim());
                    LOGGER.info("+++ " + pfx + "." + name + ": [" + value + "]");
                }
            }
        } catch (Exception e) {
            saltstackResult = new SaltstackResult(SaltstackResultCodes.INVALID_RESPONSE_FILE.getValue(), "Error parsing response file = "
                    + saltstackResult.getOutputFileName() + ". Error = " + e.getMessage());
        } finally {
            if (in != null) {
                in.close();
            }
        }
        saltstackResult.setStatusCode(SaltstackResultCodes.FINAL_SUCCESS.getValue());
        return saltstackResult;
    }

    private void throwIfMissingMandatoryParam(Map<String, String> params, String key) throws SvcLogicException {
        if (!params.containsKey(key)) {
            throw new SvcLogicException(String.format(
                    "Saltstack: Mandatory SaltstackAdapter key %s not found in parameters provided by calling agent !",
                    key));
        }
        if (Strings.isNullOrEmpty(params.get(key))) {
            throw new SvcLogicException(String.format(
                    "Saltstack: Mandatory SaltstackAdapter key %s not found in parameters provided by calling agent !",
                    key));
        }
    }
}
