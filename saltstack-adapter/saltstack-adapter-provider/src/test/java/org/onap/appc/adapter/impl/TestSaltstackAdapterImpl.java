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

package org.onap.appc.adapter.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.saltstack.impl.SaltstackAdapterImpl;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class TestSaltstackAdapterImpl {

    private final String PENDING = "100";
    private final String SUCCESS = "400";
    private String message = "{\"Results\":{\"192.168.1.10\":{\"Id\":\"101\",\"StatusCode\":200,\"StatusMessage\":\"SUCCESS\"}},\"StatusCode\":200,\"StatusMessage\":\"FINISHED\"}";

    private SaltstackAdapterImpl adapter;
    private String TestId;
    private boolean testMode = true;
    private Map<String, String> params;
    private SvcLogicContext svcContext;


    @Before
    public void setup() throws IllegalArgumentException {
        testMode = true;
        svcContext = new SvcLogicContext();
        adapter = new SaltstackAdapterImpl(testMode);

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

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetFailed() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "fail");
        params.put("Id", "test1");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
        assertEquals("101", status);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetUserFailed() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("Password", "test");
        params.put("Test", "fail");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
        assertEquals("101", status);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetHostFailed() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "fail");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
        assertEquals("101", status);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetPortFailed() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "fail");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
        assertEquals("101", status);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetPasswordFailed() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Test", "fail");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
        assertEquals("101", status);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetMandatoryFailed() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("Test", "fail");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
        assertEquals("101", status);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetSuccessNoFile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("PlaybookName", "test_playbook.yaml");
        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("Id", "test1");

        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
            assertEquals("400", status);
        } catch (NullPointerException e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }

    @Test
    public void reqExecCommand_shouldSetSuccessFileJson() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("fileName", "src/test/resources/test.json");
        params.put("Id", "test1");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
        assertEquals("200", status);
    }

    @Test
    public void reqExecCommand_shouldSetSuccessFileTxt() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("fileName", "src/test/resources/test.txt");
        params.put("Id", "txt");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
        assertEquals("200", status);
    }

    @Test
    public void reqExecCommand_shouldSetSuccessFileNoExtension() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("fileName", "src/test/resources/test");
        params.put("Id", "none");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
        assertEquals("200", status);
    }

    @Test
    public void reqExecCommand_shouldSetSuccessFileInvalidJson() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("fileName", "src/test/resources/test-invalid.json");
        params.put("Id", "test1");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
        assertEquals("200", status);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetSuccessFileInvalidFile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("fileName", "src/test/resources/test-none.json");
        params.put("Id", "test1");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
        assertEquals("400", status);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetSuccessFileJsonNoReqID() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("fileName", "src/test/resources/test.json");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
        assertEquals("101", status);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetSuccessWithRetry() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("PlaybookName", "test_playbook.yaml");
        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("retryDelay", "10");
        params.put("retryCount", "10");
        params.put("Id", "test1");

        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
            assertEquals("400", status);
        } catch (NullPointerException e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetSuccessWithRetryZero() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("PlaybookName", "test_playbook.yaml");
        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("retryDelay", "0");
        params.put("retryCount", "0");
        params.put("Id", "test1");

        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
            assertEquals("400", status);
        } catch (NullPointerException e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetSuccessWithNoRetry() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("PlaybookName", "test_playbook.yaml");
        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("retryDelay", "-1");
        params.put("retryCount", "-1");
        params.put("Id", "test1");

        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.Id");
            assertEquals("400", status);
        } catch (NullPointerException e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }
    @Test
    public void reqExecSLS_shouldSetSuccess() throws IllegalStateException, IllegalArgumentException {

        params.put("Id", "100");

        for (String ukey : params.keySet()) {
            System.out.println(String.format("Saltstack Parameter %s = %s", ukey, params.get(ukey)));
        }

        try {
            adapter.reqExecSLS(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            //assertEquals(SUCCESS, status);
            assertEquals(null, status);
        } catch (SvcLogicException e) {
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
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
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.log");
            //assertEquals(message, status);
            assertEquals(null, status);
        } catch (SvcLogicException e) {
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.log");
            fail(e.getMessage() + " Code = " + status);
        } catch (Exception e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }
}
