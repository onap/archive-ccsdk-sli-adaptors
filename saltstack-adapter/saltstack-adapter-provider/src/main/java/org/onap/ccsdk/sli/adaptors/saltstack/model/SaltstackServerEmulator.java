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



/*
 * Class to emulate responses from the Saltstack Server that is compliant with the APP-C Saltstack Server
 * Interface. Used for jUnit tests to verify code is working. In tests it can be used
 * as a replacement for methods from ConnectionBuilder class
 */

package org.onap.ccsdk.sli.adaptors.saltstack.model;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SaltstackServerEmulator {

    private static final String SALTSTATE_FILE_NAME = "fileName";
    private final EELFLogger logger = EELFManager.getInstance().getLogger(SaltstackServerEmulator.class);

    /**
     * Method that emulates the response from an Saltstack Server
     * when presented with a request to execute a saltState
     * Returns an saltstack object result. The response code is always the ssh code 200 (i.e connection successful)
     * payload is json string as would be sent back by Saltstack Server
     **/
    public SaltstackResult mockReqExec(Map<String, String> params) {
        SaltstackResult result = new SaltstackResult();

        try {
            if (params.get("Test") == "fail") {
                result = rejectRequest(result, "Mocked: Fail");
            } else {
                String fileName = params.get(SALTSTATE_FILE_NAME);
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