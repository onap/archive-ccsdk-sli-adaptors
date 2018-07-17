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

package org.onap.ccsdk.adapter.impl;

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
        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
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
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
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
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
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
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
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
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("101", status);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetMandatoryFailed() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("Test", "fail");
        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("101", status);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_NoResponseFile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("cmd", "test");
        params.put("slsExec", "false");
        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("400", status);
        } catch (NullPointerException e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_NoResponseFileWithRetry() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("retryDelay", "10");
        params.put("retryCount", "10");
        params.put("cmd", "test");
        params.put("slsExec", "false");
        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("400", status);
        } catch (NullPointerException e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_NoResponseFileWithRetryZero() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("retryDelay", "0");
        params.put("retryCount", "0");
        params.put("cmd", "test");
        params.put("slsExec", "false");
        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("400", status);
        } catch (NullPointerException e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_NoResponseFileWithNoRetry() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("retryDelay", "-1");
        params.put("retryCount", "-1");
        params.put("cmd", "test");
        params.put("slsExec", "false");

        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("400", status);
        } catch (NullPointerException e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetFailure() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("cmd", "test");
        params.put("slsExec", "test");
        params.put("Test", "fail");
        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("400", status);
        } catch (NullPointerException e) {
            fail(e.getMessage() + " Unknown exception encountered ");
        }
    }

    @Test
    public void reqExecCommand_shouldSetSuccessNoSLS() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("fileName", "src/test/resources/test.json");
        params.put("Id", "test1");
        params.put("cmd", "test");
        params.put("slsExec", "false");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
        assertEquals(TestId, "test1");
    }

    @Test
    public void reqExecCommand_shouldSetSuccessExecSLS() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("cmd", "test");
        params.put("slsExec", "true");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
        assertEquals(TestId, "test1");
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetFailExecSLS() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("fileName", "src/test/resources/test.json");
        params.put("Id", "test1");
        params.put("cmd", "test");
        params.put("slsExec", "true");

        adapter.reqExecCommand(params, svcContext);
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals(TestId, "test1");
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
        params.put("cmd", "test");
        params.put("slsExec", "false");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
        assertEquals(TestId, "txt");
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
        params.put("Id", "txt");
        params.put("cmd", "test");
        params.put("slsExec", "false");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
        assertEquals(TestId, "txt");
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
        params.put("cmd", "test");
        params.put("slsExec", "false");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_shouldSetFailFileInvalidFile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("fileName", "src/test/resources/test-none.json");
        params.put("Id", "test1");

        adapter.reqExecCommand(params, svcContext);
    }

    @Test
    public void reqExecCommand_shouldSetSuccessFileJsonNoReqID() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("fileName", "src/test/resources/test.json");
        params.put("cmd", "test");
        params.put("slsExec", "false");

        adapter.reqExecCommand(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
    }

    @Test
    public void reqExecSLSFile_shouldSetSuccessJson() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsFile", "src/test/resources/test.sls");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("cmd", "test");

        adapter.reqExecSLSFile(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
        assertEquals(TestId, "test1");
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecSLSFile_NoSLSfile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsFile", "src/test/resources/test-none.sls");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");

        adapter.reqExecSLSFile(params, svcContext);
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals(TestId, "test1");
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecSLSFile_NoExtn() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsFile", "src/test/resources/test-none");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");

        adapter.reqExecSLSFile(params, svcContext);
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals(TestId, "test1");
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecSLSFile_NoResponsefile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsFile", "src/test/resources/test.json");
        params.put("fileName", "src/test/resources/test-none.json");
        params.put("Id", "test1");

        adapter.reqExecSLSFile(params, svcContext);
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals(TestId, "test1");
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecSLSFile_WithMinionSetNotSLSType() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsFile", "src/test/resources/test.json");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("cmd", "test");
        params.put("applyTo", "minion1");

        adapter.reqExecSLSFile(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
        assertEquals(TestId, "test1");
    }

    @Test
    public void reqExecSLSFile_WithMinionSetSuccessSls() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsFile", "src/test/resources/test.sls");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("cmd", "test");
        params.put("applyTo", "minion1");

        adapter.reqExecSLSFile(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
        assertEquals(TestId, "test1");
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecSLSFile_WithMinionNoSLSfile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsFile", "src/test/resources/test-none.json");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("applyTo", "minion1");

        adapter.reqExecSLSFile(params, svcContext);
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals(TestId, "test1");
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecSLSFile_WithMinionNoResponsefile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsFile", "src/test/resources/test.json");
        params.put("fileName", "src/test/resources/test-none.json");
        params.put("Id", "test1");
        params.put("applyTo", "minion1");

        adapter.reqExecSLSFile(params, svcContext);
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals(TestId, "test1");
    }

    @Test
    public void reqExecSLSFile_WithAllMinionSetSuccessJson() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsFile", "src/test/resources/test.sls");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("cmd", "test");
        params.put("applyTo", "*");

        adapter.reqExecSLSFile(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
        assertEquals(TestId, "test1");
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecSLSFile_WithAllMinionNoSLSfile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsFile", "src/test/resources/test-none.json");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("applyTo", "*");

        adapter.reqExecSLSFile(params, svcContext);
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals(TestId, "test1");
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecSLSFile_WithAllMinionNoResponsefile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsFile", "src/test/resources/test.json");
        params.put("fileName", "src/test/resources/test-none.json");
        params.put("Id", "test1");
        params.put("applyTo", "*");

        adapter.reqExecSLSFile(params, svcContext);
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals(TestId, "test1");
    }


    @Test
    public void reqExecSLS_shouldSetSuccessJson() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsName", "src/test.sls");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("cmd", "test");

        adapter.reqExecSLS(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
        assertEquals(TestId, "test1");
    }

    @Test
    public void reqExecSLS_shouldSetNoExtn() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsName", "src/test");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("cmd", "test");

        adapter.reqExecSLS(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
        assertEquals(TestId, "test1");
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecSLS_NoResponsefile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsName", "src/test/resources/test.json");
        params.put("fileName", "src/test/resources/test-none.json");
        params.put("Id", "test1");

        adapter.reqExecSLS(params, svcContext);
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals(TestId, "test1");
    }


    @Test
    public void reqExecSLS_WithMinionSetSuccessSls() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsName", "src/test/resources/test.sls");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("cmd", "test");
        params.put("applyTo", "minion1");

        adapter.reqExecSLS(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
        assertEquals(TestId, "test1");
    }


    @Test(expected = SvcLogicException.class)
    public void reqExecSLS_WithMinionNoResponsefile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsName", "src/test/resources/test.json");
        params.put("fileName", "src/test/resources/test-none.json");
        params.put("Id", "test1");
        params.put("applyTo", "minion1");

        adapter.reqExecSLS(params, svcContext);
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals(TestId, "test1");
    }

    @Test
    public void reqExecSLS_WithAllMinionSetSuccessJson() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsName", "src/test/resources/test.sls");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("cmd", "test");
        params.put("applyTo", "*");

        adapter.reqExecSLS(params, svcContext);
        String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals("200", status);
        assertEquals(TestId, "test1");
    }


    @Test(expected = SvcLogicException.class)
    public void reqExecSLS_WithAllMinionNoResponsefile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "test");
        params.put("Port", "10");
        params.put("User", "test");
        params.put("Password", "test");
        params.put("Test", "success");
        params.put("slsName", "src/test/resources/test.json");
        params.put("fileName", "src/test/resources/test-none.json");
        params.put("Id", "test1");
        params.put("applyTo", "*");

        adapter.reqExecSLS(params, svcContext);
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals(TestId, "test1");
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

    @Test
    public void reqExecCommand_shouldSetSuccessReal() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "127.0.0.1");
        params.put("Port", "22");
        params.put("User", "sdn");
        params.put("Password", "foo");
        params.put("Id", "test1");
        params.put("cmd", "ls -l");
        params.put("slsExec", "false");
        params.put("execTimeout", "12000");
        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e){
            //if local ssh is not enabled
            return;
        }
    }

    @Test
    public void reqExecCommand_shouldSetSuccessRealSLSCommand() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "<IP>");
        params.put("Port", "2222");
        params.put("User", "root");
        params.put("Password", "vagrant");
        params.put("Id", "test1");
        params.put("cmd", "salt '*' test.ping --out=json --static");
        params.put("slsExec", "false");
        params.put("execTimeout", "12000");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
            TestId = svcContext.getAttribute("test1.minion1");
            assertEquals(TestId, "true");
        } catch (Exception e){
            //if saltstack ssh IP is not enabled
            return;
        }
    }

    @Test
    public void reqExecCommand_shouldSetSuccessRealCommand() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "<IP>");
        params.put("Port", "2222");
        params.put("User", "root");
        params.put("Password", "vagrant");
        params.put("Id", "test1");
        params.put("cmd", "cd /srv/salt/; salt '*' state.apply vim --out=json --static");
        params.put("slsExec", "true");
        params.put("execTimeout", "12000");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e){
            //if saltstack ssh IP is not enabled
            return;
        }
    }

    @Test
    public void reqExecCommand_shouldSetSuccessRealSSL() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "<IP>");
        params.put("Port", "2222");
        params.put("User", "root");
        params.put("Password", "vagrant");
        params.put("Id", "test1");
        params.put("slsName", "vim");
        params.put("execTimeout", "12000");
        params.put("applyTo", "minion1");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecSLS(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e){
            //if saltstack ssh IP is not enabled
            return;
        }
    }

    @Test
    public void reqExecCommand_shouldSetSuccessSSLFile() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "<IP>");
        params.put("Port", "2222");
        params.put("User", "root");
        params.put("Password", "vagrant");
        params.put("Id", "test1");
        params.put("execTimeout", "12000");
        params.put("applyTo", "minion1");
        params.put("slsFile", "src/test/resources/config.sls");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecSLSFile(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e){
            //if saltstack ssh IP is not enabled
            return;
        }
    }
}
