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
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.saltstack;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

import java.util.Map;

/**
 * This interface defines the operations that the Saltstack adapter exposes.
 */
public interface SaltstackAdapter extends SvcLogicJavaPlugin {
    /**
     * Returns the symbolic name of the adapter
     *
     * @return The adapter name
     */
    String getAdapterName();

    /* Method to post a single command request for execution on SaltState server
     *  The response from Saltstack comes in json format and it is automatically put
     *  to context for DGs access, with a certain prefix*/
    void reqExecCommand(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;

    /* Method for execution of saltstack SLS command on SaltState server
     *  The response from Saltstack comes in json format and it is automatically put
     *  to context for DGs access, with a certain prefix*/
    void reqExecSLS(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;

    /* When SLS file is created/available then this Method can be used to post
     *  the file to saltstack server and execute the SLS file on SaltState server
     *  The response from Saltstack comes in json format and it is automatically put
     *  to context for DGs access, with a certain prefix*/
    void reqExecSLSFile(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;
}
