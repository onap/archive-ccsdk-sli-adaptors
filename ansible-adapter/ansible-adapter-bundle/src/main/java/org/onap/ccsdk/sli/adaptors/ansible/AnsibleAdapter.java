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

package org.onap.ccsdk.sli.adaptors.ansible;

import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

/**
 * This interface defines the operations that the Ansible adapter exposes.
 *
 */
public interface AnsibleAdapter extends SvcLogicJavaPlugin {
    /**
     * Returns the symbolic name of the adapter
     *
     * @return The adapter name
     */
    String getAdapterName();

    /* Method to post request for execution of Playbook */
    void reqExec(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;

    /* Method to get result of a playbook execution request */
    void reqExecResult(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;

    /* Method to get log of a playbook execution request */
    void reqExecLog(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;

    /* Method to get output of a playbook execution request */
    void reqExecOutput(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;
}
