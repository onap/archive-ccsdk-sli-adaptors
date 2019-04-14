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

package org.onap.ccsdk.adapter.ansible.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.ansible.impl.AnsibleAdapterImpl;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;


public class TestAnsibleAdapterImpl {

    private final String PENDING = "100";
    private final String SUCCESS = "400";
    private String message = "{\"Results\":{\"192.168.1.10\":{\"Id\":\"101\",\"StatusCode\":200,\"StatusMessage\":\"SUCCESS\"}},\"StatusCode\":200,\"StatusMessage\":\"FINISHED\"}";

    private AnsibleAdapterImpl adapter;
    private String TestId;
    private boolean testMode = true;
    private Map<String, String> params;
    private SvcLogicContext svcContext;


    @Before
    public void setup() throws IllegalArgumentException {
        testMode = true;
        svcContext = new SvcLogicContext();
        adapter = new AnsibleAdapterImpl(testMode);

        params = new HashMap<>();
        params.put("AgentUrl", "https://192.168.1.1");
        params.put("User", "test");
        params.put("Password", "test");
    }

    @After
    public void tearDown() {
        testMode = false;
        adapter = null;
        params = null;
        svcContext = null;
    }

    @Test
    public void reqExec_shouldSetPending() throws IllegalStateException, IllegalArgumentException {

        params.put("PlaybookName", "test_playbook.yaml");

        try {
            adapter.reqExec(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.ansible.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.ansible.result.Id");
            System.out.println("Comparing " + PENDING + " and " + status);
            assertEquals(PENDING, status);
        } catch (SvcLogicException e) {
            String status = svcContext.getAttribute("org.onap.appc.adapter.ansible.result.code");
            fail(e.getMessage() + " Code = " + status);
        } catch (Exception e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }

    @Test
    public void reqExecResult_shouldSetSuccess() throws IllegalStateException, IllegalArgumentException {

        params.put("Id", "100");

        for (String ukey : params.keySet()) {
            System.out.println(String.format("Ansible Parameter %s = %s", ukey, params.get(ukey)));
        }

        try {
            adapter.reqExecResult(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.ansible.result.code");
            assertEquals(SUCCESS, status);
        } catch (SvcLogicException e) {
            String status = svcContext.getAttribute("org.onap.appc.adapter.ansible.result.code");
            fail(e.getMessage() + " Code = " + status);
        } catch (Exception e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }

    @Test
    public void reqExecLog_shouldSetMessage() throws IllegalStateException, IllegalArgumentException {

        params.put("Id", "101");

        try {
            adapter.reqExecLog(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.ansible.log");
            assertEquals(message, status);
        } catch (SvcLogicException e) {
            String status = svcContext.getAttribute("org.onap.appc.adapter.ansible.log");
            fail(e.getMessage() + " Code = " + status);
        } catch (Exception e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }

    @Test
    public void reqExecOutput_shouldSetMessage() throws IllegalStateException, IllegalArgumentException {

        params.put("Id", "101");

        try {
            adapter.reqExecOutput(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.ansible.output");
            assertEquals(message, status);
        } catch (SvcLogicException e) {
            String status = svcContext.getAttribute("org.onap.appc.adapter.ansible.output");
            fail(e.getMessage() + " Code = " + status);
        } catch (Exception e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }
}
