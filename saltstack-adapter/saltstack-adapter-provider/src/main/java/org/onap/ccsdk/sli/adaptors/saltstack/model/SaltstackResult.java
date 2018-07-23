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

package org.onap.ccsdk.sli.adaptors.saltstack.model;

import java.io.ByteArrayOutputStream;

/**
 * Simple class to store code and message returned by POST/GET to an Saltstack Server
 */
public class SaltstackResult {

    private static final String EMPTY_VALUE = "UNKNOWN";

    private int statusCode;
    private String statusMessage;
    private String results;
    private ByteArrayOutputStream out;
    private int sshExitStatus;

    public SaltstackResult() {
        this(-1, EMPTY_VALUE, EMPTY_VALUE, -1);
    }

    public SaltstackResult(int code, String message) {
        this(code, message, EMPTY_VALUE, -1);
    }

    public SaltstackResult(int code, String message, String result, int sshCode) {
        statusCode = code;
        statusMessage = message;
        results = result;
        sshExitStatus = sshCode;
    }

    void set(int code, String message, String results) {
        this.statusCode = code;
        this.statusMessage = message;
        this.results = results;
    }

    public ByteArrayOutputStream getOutputMessage() {
        return out;
    }

    public void setOutputMessage(ByteArrayOutputStream out) {
        this.out = out;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public void setStatusCode(int code) {
        this.statusCode = code;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    public void setStatusMessage(String message) {
        this.statusMessage = message;
    }

    public String getResults() {
        return this.results;
    }

    public void setResults(String results) {
        this.results = results;
    }

    public int getSshExitStatus() {
        return sshExitStatus;
    }

    public void setSshExitStatus(int sshExitStatus) {
        this.sshExitStatus = sshExitStatus;
    }
}
