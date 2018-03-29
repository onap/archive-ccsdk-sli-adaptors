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
 * Class to emulate responses from the Ansible Server that is compliant with the APP-C Ansible Server
 * Interface. Used for jUnit tests to verify code is working. In tests it can be used
 * as a replacement for methods from ConnectionBuilder class
 */

package org.onap.ccsdk.sli.adaptors.ansible.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

public class AnsibleServerEmulator {

    private final EELFLogger logger = EELFManager.getInstance().getLogger(AnsibleServerEmulator.class);

    private static final String PLAYBOOK_NAME = "PlaybookName";
    private static final String STATUS_CODE = "StatusCode";
    private static final String STATUS_MESSAGE = "StatusMessage";

    private String playbookName = "test_playbook.yaml";

    /**
     * Method that emulates the response from an Ansible Server
     * when presented with a request to execute a playbook
     * Returns an ansible object result. The response code is always the http code 200 (i.e connection successful)
     * payload is json string as would be sent back by Ansible Server
     **/
    public AnsibleResult Post(String agentUrl, String payload) {
        AnsibleResult result = new AnsibleResult();

        try {
            // Request must be a JSON object

            JSONObject message = new JSONObject(payload);
            if (message.isNull("Id")) {
                rejectRequest(result, "Must provide a valid Id");
            } else if (message.isNull(PLAYBOOK_NAME)) {
                rejectRequest(result, "Must provide a playbook Name");
            } else if (!message.getString(PLAYBOOK_NAME).equals(playbookName)) {
                rejectRequest(result, "Playbook " + message.getString(PLAYBOOK_NAME) + "  not found in catalog");
            } else {
                acceptRequest(result);
            }
        } catch (JSONException e) {
            logger.error("JSONException caught", e);
            rejectRequest(result, e.getMessage());
        }
        return result;
    }

    /**
     * Method to emulate response from an Ansible
     * Server when presented with a GET request
     * Returns an ansibl object result. The response code is always the http code 200 (i.e connection successful)
     * payload is json string as would be sent back by Ansible Server
     *
     **/
    public AnsibleResult Get(String agentUrl) {

        Pattern pattern = Pattern.compile(".*?\\?Id=(.*?)&Type.*");
        Matcher matcher = pattern.matcher(agentUrl);
        String id = StringUtils.EMPTY;
        String vmAddress = "192.168.1.10";

        if (matcher.find()) {
            id = matcher.group(1);
        }

        AnsibleResult getResult = new AnsibleResult();

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

    private void rejectRequest(AnsibleResult result, String Message) {
        result.setStatusCode(200);
        JSONObject response = new JSONObject();
        response.put(STATUS_CODE, AnsibleResultCodes.REJECTED.getValue());
        response.put(STATUS_MESSAGE, Message);
        result.setStatusMessage(response.toString());
    }

    private void acceptRequest(AnsibleResult result) {
        result.setStatusCode(200);
        JSONObject response = new JSONObject();
        response.put(STATUS_CODE, AnsibleResultCodes.PENDING.getValue());
        response.put(STATUS_MESSAGE, "PENDING");
        result.setStatusMessage(response.toString());
    }
}