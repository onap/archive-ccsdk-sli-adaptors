/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017-2018 AT&T Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.sli.adaptors.saltstack.model;

/**
 * This module implements the APP-C/Saltstack Server interface
 * based on the REST API specifications
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that validates and constructs requests sent/received from
 * Saltstack Server
 */
//TODO: This class is to be altered completely based on the SALTSTACK server communicaiton.
public class SaltstackMessageParser {

    private static final String STATUS_MESSAGE_KEY = "StatusMessage";
    private static final String STATUS_CODE_KEY = "StatusCode";

    private static final String SALTSTATE_NAME_KEY = "SaltStateName";
    private static final String SS_AGENT_HOSTNAME_KEY = "HostName";
    private static final String SS_AGENT_PORT_KEY = "Port";
    private static final String PASS_KEY = "Password";
    private static final String USER_KEY = "User";

    private static final String LOCAL_PARAMETERS_OPT_KEY = "LocalParameters";
    private static final String FILE_PARAMETERS_OPT_KEY = "FileParameters";
    private static final String ENV_PARAMETERS_OPT_KEY = "EnvParameters";
    private static final String NODE_LIST_OPT_KEY = "NodeList";
    private static final String TIMEOUT_OPT_KEY = "Timeout";
    private static final String VERSION_OPT_KEY = "Version";
    private static final String ACTION_OPT_KEY = "Action";

    private static final Logger LOGGER = LoggerFactory.getLogger(SaltstackMessageParser.class);

    /**
     * Accepts a map of strings and
     * a) validates if all parameters are appropriate (else, throws an exception) and
     * b) if correct returns a JSON object with appropriate key-value pairs to send to the server.
     *
     * Mandatory parameters, that must be in the supplied information to the Saltstack Adapter
     * 1. URL to connect to
     * 2. credentials for URL (assume username password for now)
     * 3. SaltState name
     *
     */
    public JSONObject reqMessage(Map<String, String> params) throws SvcLogicException {
        final String[] mandatoryTestParams = {SS_AGENT_HOSTNAME_KEY, SALTSTATE_NAME_KEY, USER_KEY, PASS_KEY};
        final String[] optionalTestParams = {ENV_PARAMETERS_OPT_KEY, NODE_LIST_OPT_KEY, LOCAL_PARAMETERS_OPT_KEY,
                TIMEOUT_OPT_KEY, VERSION_OPT_KEY, FILE_PARAMETERS_OPT_KEY, ACTION_OPT_KEY};

        JSONObject jsonPayload = new JSONObject();

        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
            jsonPayload.put(key, params.get(key));
        }

        parseOptionalParams(params, optionalTestParams, jsonPayload);

        // Generate a unique uuid for the test
        String reqId = UUID.randomUUID().toString();
        jsonPayload.put(SS_AGENT_HOSTNAME_KEY, reqId);

        return jsonPayload;
    }

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate PORT number.
     */
    public String reqPortResult(Map<String, String> params) throws SvcLogicException {

        final String[] mandatoryTestParams = {SS_AGENT_HOSTNAME_KEY, SS_AGENT_PORT_KEY, USER_KEY, PASS_KEY};

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

        final String[] mandatoryTestParams = {SS_AGENT_HOSTNAME_KEY, SS_AGENT_PORT_KEY, USER_KEY, PASS_KEY};

        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
        }
        return params.get(SS_AGENT_HOSTNAME_KEY);
    }

    /**
     * Method that validates that the Map has enough information
     * to query Saltstack server for a result. If so, it returns
     * the appropriate Saltstack server login user name.
     */
    public String reqUserNameResult(Map<String, String> params) throws SvcLogicException {

        final String[] mandatoryTestParams = {SS_AGENT_HOSTNAME_KEY, SS_AGENT_PORT_KEY, USER_KEY, PASS_KEY};

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

        final String[] mandatoryTestParams = {SS_AGENT_HOSTNAME_KEY, SS_AGENT_PORT_KEY, USER_KEY, PASS_KEY};

        for (String key : mandatoryTestParams) {
            throwIfMissingMandatoryParam(params, key);
        }
        return params.get(PASS_KEY);
    }

    /**
     * This method parses response from the Saltstack Server when we do a post
     * and returns an SaltstackResult object.
     */
    public SaltstackResult parseResponse(SvcLogicContext ctx, String pfx, SaltstackResult saltstackResult) {
        int code = saltstackResult.getStatusCode();
        if (code != SaltstackResultCodes.SUCCESS.getValue()) {
            return saltstackResult;
        }
        try {
            File file = new File(saltstackResult.getOutputFileName());
            InputStream in = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            in.read(data);
            String str = new String(data, "UTF-8");
            in.close();
            Map<String, String> mm = JsonParser.convertToProperties(str);
            if (mm != null) {
                for (Map.Entry<String,String> entry : mm.entrySet()) {
                    ctx.setAttribute(pfx + entry.getKey(), entry.getValue());
                    LOGGER.info("+++ " + pfx + entry.getKey() + ": [" + entry.getValue() + "]");
                }
            }
        } catch (FileNotFoundException e){
            return new SaltstackResult(SaltstackResultCodes.INVALID_RESPONSE_FILE.getValue(), "Error parsing response file = "
                    + saltstackResult.getOutputFileName() + ". Error = " + e.getMessage());
        } catch (JSONException e) {
            LOGGER.info("Output not in JSON format");
            return putToProperties(ctx, pfx, saltstackResult);
        } catch (Exception e) {
            return new SaltstackResult(SaltstackResultCodes.INVALID_RESPONSE_FILE.getValue(), "Error parsing response file = "
                    + saltstackResult.getOutputFileName() + ". Error = " + e.getMessage());
        }
        saltstackResult.setStatusCode(SaltstackResultCodes.FINAL_SUCCESS.getValue());
        return saltstackResult;
    }

    public SaltstackResult putToProperties(SvcLogicContext ctx, String pfx, SaltstackResult saltstackResult) {
        try {
            File file = new File(saltstackResult.getOutputFileName());
            InputStream in = new FileInputStream(file);
            Properties prop = new Properties();
            prop.load(in);
            ctx.setAttribute(pfx + "completeResult", prop.toString());
            for (Object key : prop.keySet()) {
                String name = (String) key;
                String value = prop.getProperty(name);
                if (value != null && value.trim().length() > 0) {
                    ctx.setAttribute(pfx + name, value.trim());
                    LOGGER.info("+++ " + pfx + name + ": [" + value + "]");
                }
            }
        } catch (Exception e) {
            saltstackResult = new SaltstackResult(SaltstackResultCodes.INVALID_RESPONSE_FILE.getValue(), "Error parsing response file = "
                    + saltstackResult.getOutputFileName() + ". Error = " + e.getMessage());
        }
        return saltstackResult;
    }
    /**
     * This method parses response from an Saltstack server when we do a GET for a result
     * and returns an SaltstackResult object.
     **/
    public SaltstackResult parseGetResponse(String input) throws SvcLogicException {

        SaltstackResult saltstackResult = new SaltstackResult();

        try {
            JSONObject postResponse = new JSONObject(input);
            saltstackResult = parseGetResponseNested(saltstackResult, postResponse);
        } catch (JSONException e) {
            saltstackResult = new SaltstackResult(SaltstackResultCodes.INVALID_COMMAND.getValue(),
                    "Error parsing response = " + input + ". Error = " + e.getMessage(), "", -1);
        }
        return saltstackResult;
    }

    private SaltstackResult parseGetResponseNested(SaltstackResult saltstackResult, JSONObject postRsp) throws SvcLogicException  {

        int codeStatus = postRsp.getInt(STATUS_CODE_KEY);
        String messageStatus = postRsp.getString(STATUS_MESSAGE_KEY);
        int finalCode = SaltstackResultCodes.FINAL_SUCCESS.getValue();

        boolean valCode =
                SaltstackResultCodes.CODE.checkValidCode(SaltstackResultCodes.FINALRESPONSE.getValue(), codeStatus);

        if (!valCode) {
            throw new SvcLogicException("Invalid FinalResponse code  = " + codeStatus + " received. MUST be one of "
                    + SaltstackResultCodes.CODE.getValidCodes(SaltstackResultCodes.FINALRESPONSE.getValue()));
        }

        saltstackResult.setStatusCode(codeStatus);
        saltstackResult.setStatusMessage(messageStatus);
        LOGGER.info("Received response with code = {}, Message = {}", codeStatus, messageStatus);

        if (!postRsp.isNull("Results")) {

            // Results are available. process them
            // Results is a dictionary of the form
            // {host :{status:s, group:g, message:m, hostname:h}, ...}
            LOGGER.info("Processing results in response");
            JSONObject results = postRsp.getJSONObject("Results");
            LOGGER.info("Get JSON dictionary from Results ..");
            Iterator<String> hosts = results.keys();
            LOGGER.info("Iterating through hosts");

            while (hosts.hasNext()) {
                String host = hosts.next();
                LOGGER.info("Processing host = {}", host);

                try {
                    JSONObject hostResponse = results.getJSONObject(host);
                    int subCode = hostResponse.getInt(STATUS_CODE_KEY);
                    String message = hostResponse.getString(STATUS_MESSAGE_KEY);

                    LOGGER.info("Code = {}, Message = {}", subCode, message);

                    if (subCode != 200 || !message.equals("SUCCESS")) {
                        finalCode = SaltstackResultCodes.REQ_FAILURE.getValue();
                    }
                } catch (JSONException e) {
                    saltstackResult.setStatusCode(SaltstackResultCodes.INVALID_RESPONSE.getValue());
                    saltstackResult.setStatusMessage(String.format(
                            "Error processing response message = %s from host %s", results.getString(host), host));
                    break;
                }
            }

            saltstackResult.setStatusCode(finalCode);

            // We return entire Results object as message
            saltstackResult.setResults(results.toString());

        } else {
            saltstackResult.setStatusCode(SaltstackResultCodes.INVALID_RESPONSE.getValue());
            saltstackResult.setStatusMessage("Results not found in GET for response");
        }
        return saltstackResult;
    }

    private void parseOptionalParams(Map<String, String> params, String[] optionalTestParams, JSONObject jsonPayload) {

        Set<String> optionalParamsSet = new HashSet<>();
        Collections.addAll(optionalParamsSet, optionalTestParams);

        //@formatter:off
        params.entrySet()
            .stream()
            .filter(entry -> optionalParamsSet.contains(entry.getKey()))
            .filter(entry -> !Strings.isNullOrEmpty(entry.getValue()))
             .forEach(entry -> parseOptionalParam(entry, jsonPayload));
        //@formatter:on
    }

    private void parseOptionalParam(Map.Entry<String, String> params, JSONObject jsonPayload) {
        String key = params.getKey();
        String payload = params.getValue();

        switch (key) {
            case TIMEOUT_OPT_KEY:
                int timeout = Integer.parseInt(payload);
                if (timeout < 0) {
                    throw new NumberFormatException(" : specified negative integer for timeout = " + payload);
                }
                jsonPayload.put(key, payload);
                break;

            case VERSION_OPT_KEY:
                jsonPayload.put(key, payload);
                break;

            case LOCAL_PARAMETERS_OPT_KEY:
            case ENV_PARAMETERS_OPT_KEY:
                JSONObject paramsJson = new JSONObject(payload);
                jsonPayload.put(key, paramsJson);
                break;

            case NODE_LIST_OPT_KEY:
                JSONArray paramsArray = new JSONArray(payload);
                jsonPayload.put(key, paramsArray);
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
