/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *             reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
/**
 * @author Rich Tabedzki
 *
 */
package org.onap.ccsdk.sli.adaptors.aai;

import org.onap.ccsdk.sli.adaptors.aai.data.ErrorResponse;

public class AAIServiceException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -9039257722542999522L;

    protected final ErrorResponse errorResponse;
    protected final int returnCode;

    public AAIServiceException() {
        returnCode = -1;
        errorResponse = null;
    }

    public AAIServiceException(String message) {
        super(message);
        returnCode = -1;
        errorResponse = null;
    }

    public AAIServiceException(Throwable cause) {
        super(cause);
        returnCode = -1;
        errorResponse = null;
    }

    public AAIServiceException(String message, Throwable cause) {
        super(message, cause);
        returnCode = -1;
        errorResponse = null;
    }

    public AAIServiceException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        returnCode = -1;
        errorResponse = null;
    }

    public AAIServiceException(int returnCode, ErrorResponse errorresponse) {
        this.errorResponse = errorresponse;
        this.returnCode = returnCode;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public String getMessage() {
        if(errorResponse != null) {
            return errorResponse.getRequestError().getServiceException().getText();
        } else {
            return super.getMessage();
        }
    }
}
