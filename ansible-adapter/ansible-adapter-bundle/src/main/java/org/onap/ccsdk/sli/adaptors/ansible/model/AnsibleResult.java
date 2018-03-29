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

package org.onap.ccsdk.sli.adaptors.ansible.model;

/**
 *  Simple class to store code and message returned by POST/GET to an Ansible Server
 */
public class AnsibleResult {

    private static final String EMPTY_VALUE = "UNKNOWN";

    private int statusCode;
    private String statusMessage;
    private String results;

    public AnsibleResult() {
        this(-1, EMPTY_VALUE, EMPTY_VALUE);
    }

    public AnsibleResult(int code, String message) {
        this(code, message, EMPTY_VALUE);
    }

    public AnsibleResult(int code, String message, String result) {
        statusCode = code;
        statusMessage = message;
        results = result;
    }

    public void setStatusCode(int code) {
        this.statusCode = code;
    }

    public void setStatusMessage(String message) {
        this.statusMessage = message;
    }

    public void setResults(String results) {
        this.results = results;
    }

    void set(int code, String message, String results) {
        this.statusCode = code;
        this.statusMessage = message;
        this.results = results;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getStatusMessage() {
        return this.statusMessage;
    }

    public String getResults() {
        return this.results;
    }
}
