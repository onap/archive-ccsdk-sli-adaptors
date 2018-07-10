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

package org.onap.ccsdk.sli.adaptors.saltstack.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * enum of the various codes that APP-C uses to resolve different
 * status of response from Saltstack Server
 **/

public enum SaltstackResultCodes {

    // @formatter:off
    SUCCESS(400),
    KEYSTORE_EXCEPTION(622),
    CERTIFICATE_ERROR(610),
    IO_EXCEPTION(611),
    HOST_UNKNOWN(625),
    USER_UNAUTHORIZED(613),
    UNKNOWN_EXCEPTION(699),
    OPERATION_TIMEOUT(659),
    SSL_EXCEPTION(697),
    INVALID_COMMAND(698),
    INVALID_RESPONSE(601),
    INVALID_RESPONSE_FILE(600),
    PENDING(100),
    REJECTED(101),
    FINAL_SUCCESS(200),
    CHECK_CTX_FOR_CMD_SUCCESS(250),
    COMMAND_EXEC_FAILED_STATUS(670),
    REQ_FAILURE(401),
    MESSAGE(1),
    CODE(0),
    INITRESPONSE(0),
    FINALRESPONSE(1);
    // @formatter:on

    private final Set<Integer> initCodes = new HashSet<>(Arrays.asList(100, 101));
    private final Set<Integer> finalCodes = new HashSet<>(Arrays.asList(200, 500));
    private final ArrayList<Set<Integer>> codeSets = new ArrayList<>(Arrays.asList(initCodes, finalCodes));
    private final Set<String> messageSet = new HashSet<>(Arrays.asList("PENDING", "FINISHED", "TERMINATED"));
    private final int value;

    SaltstackResultCodes(int value) {
        this.value = value;
    }

    ;

    public int getValue() {
        return value;
    }

    public boolean checkValidCode(int type, int code) {
        return codeSets.get(type).contains(code);
    }

    public String getValidCodes(int type) {
        StringBuilder sb = new StringBuilder("[ ");
        codeSets.get(type).stream().forEach(s -> sb.append(s).append(","));
        return sb.append("]").toString();
    }

    public boolean checkValidMessage(String message) {
        return messageSet.contains(message);
    }

    public String getValidMessages() {
        StringBuilder sb = new StringBuilder("[ ");
        messageSet.stream().forEach(s -> sb.append(s).append(","));
        return sb.append("]").toString();
    }
}
