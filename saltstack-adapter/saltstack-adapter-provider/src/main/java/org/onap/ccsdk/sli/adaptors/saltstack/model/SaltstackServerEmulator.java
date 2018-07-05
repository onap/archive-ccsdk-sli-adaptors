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



/*
 * Class to emulate responses from the Saltstack Server that is compliant with the APP-C Saltstack Server
 * Interface. Used for jUnit tests to verify code is working. In tests it can be used
 * as a replacement for methods from ConnectionBuilder class
 */

package org.onap.ccsdk.sli.adaptors.saltstack.model;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class SaltstackServerEmulator {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(SaltstackServerEmulator.class);

    private static final String SALTSTATE_NAME = "SaltStateName";
    private static final String STATUS_CODE = "StatusCode";
    private static final String STATUS_MESSAGE = "StatusMessage";

    private String saltStateName = "test_saltState.yaml";

    /**
     * Method that emulates the response from an Saltstack Server
     * when presented with a request to execute a saltState
     * Returns an saltstack object result. The response code is always the ssh code 200 (i.e connection successful)
     * payload is json string as would be sent back by Saltstack Server
     **/
    public SaltstackResult MockReqExec(Map<String, String> params) {
        SaltstackResult result = new SaltstackResult();

        try {
            if (params.get("Test") == "fail") {
                result = rejectRequest(result, "Must provide a valid Id");
            } else {
                String fileName = params.get("fileName");
                if (fileName == null)
                    result = acceptRequest(result, "");
                else
                    result = acceptRequest(result, fileName);
            }
        } catch (Exception e) {
            logger.error("JSONException caught", e);
            rejectRequest(result, e.getMessage());
        }
        return result;
    }

    /**
     * Method to emulate response from an Saltstack
     * Server when presented with a GET request
     * Returns an saltstack object result. The response code is always the ssh code 200 (i.e connection successful)
     * payload is json string as would be sent back by Saltstack Server
     *
     **/
    public SaltstackResult Execute(String agentUrl) {

        Pattern pattern = Pattern.compile(".*?\\?Id=(.*?)&Type.*");
        Matcher matcher = pattern.matcher(agentUrl);
        String id = StringUtils.EMPTY;
        String vmAddress = "192.168.1.10";

        if (matcher.find()) {
            id = matcher.group(1);
        }

        SaltstackResult getResult = new SaltstackResult();

        JSONObject response = new JSONObject();
        response.put(STATUS_CODE, 200);
        response.put(STATUS_MESSAGE, "FINISHED");

        JSONObject results = new JSONObject();

        JSONObject vmResults = new JSONObject();
        vmResults.put(STATUS_CODE, 200);
        vmResults.put(STATUS_MESSAGE, "SUCCESS");
        vmResults.put("Id", id);
        results.put(vmAddress, vmResults);

        response.put("Results", results);

        getResult.setStatusCode(200);
        getResult.setStatusMessage(response.toString());

        return getResult;
    }

    private SaltstackResult rejectRequest(SaltstackResult result, String Message) {
        result.setStatusCode(SaltstackResultCodes.REJECTED.getValue());
        result.setStatusMessage("Rejected");
        return result;
    }

    private SaltstackResult acceptRequest(SaltstackResult result, String fileName) {
        result.setStatusCode(SaltstackResultCodes.SUCCESS.getValue());
        result.setStatusMessage("Success");
        result.setOutputFileName(fileName);
        return result;
    }
}