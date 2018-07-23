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
        params.put("Cmd", "test");
        params.put("SlsExec", "false");
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
        params.put("Cmd", "test");
        params.put("SlsExec", "false");
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
        params.put("Cmd", "test");
        params.put("SlsExec", "false");
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
        params.put("Cmd", "test");
        params.put("SlsExec", "false");

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
        params.put("Cmd", "test");
        params.put("SlsExec", "test");
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
        params.put("Cmd", "test");
        params.put("SlsExec", "false");

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
        params.put("Cmd", "test");
        params.put("SlsExec", "true");

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
        params.put("Cmd", "test");
        params.put("SlsExec", "true");

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
        params.put("Cmd", "test");
        params.put("SlsExec", "false");

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
        params.put("Cmd", "test");
        params.put("SlsExec", "false");

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
        params.put("Cmd", "test");
        params.put("SlsExec", "false");

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
        params.put("Cmd", "test");
        params.put("SlsExec", "false");

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
        params.put("SlsFile", "src/test/resources/test.sls");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("Cmd", "test");

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
        params.put("SlsFile", "src/test/resources/test-none.sls");
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
        params.put("SlsFile", "src/test/resources/test-none");
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
        params.put("SlsFile", "src/test/resources/test.json");
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
        params.put("SlsFile", "src/test/resources/test.json");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("Cmd", "test");
        params.put("NodeList", "minion1");

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
        params.put("SlsFile", "src/test/resources/test.sls");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("Cmd", "test");
        params.put("NodeList", "minion1");

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
        params.put("SlsFile", "src/test/resources/test-none.json");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("NodeList", "minion1");

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
        params.put("SlsFile", "src/test/resources/test.json");
        params.put("fileName", "src/test/resources/test-none.json");
        params.put("Id", "test1");
        params.put("NodeList", "minion1");

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
        params.put("SlsFile", "src/test/resources/test.sls");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("Cmd", "test");
        params.put("NodeList", "*");

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
        params.put("SlsFile", "src/test/resources/test-none.json");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("NodeList", "*");

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
        params.put("SlsFile", "src/test/resources/test.json");
        params.put("fileName", "src/test/resources/test-none.json");
        params.put("Id", "test1");
        params.put("NodeList", "*");

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
        params.put("SlsName", "src/test.sls");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("Cmd", "test");

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
        params.put("SlsName", "src/test");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("Cmd", "test");

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
        params.put("SlsName", "src/test/resources/test.json");
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
        params.put("SlsName", "src/test/resources/test.sls");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("Cmd", "test");
        params.put("NodeList", "minion1");

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
        params.put("SlsName", "src/test/resources/test.json");
        params.put("fileName", "src/test/resources/test-none.json");
        params.put("Id", "test1");
        params.put("NodeList", "minion1");

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
        params.put("SlsName", "src/test/resources/test.sls");
        params.put("fileName", "src/test/resources/test-sls.json");
        params.put("Id", "test1");
        params.put("Cmd", "test");
        params.put("NodeList", "*");

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
        params.put("SlsName", "src/test/resources/test.json");
        params.put("fileName", "src/test/resources/test-none.json");
        params.put("Id", "test1");
        params.put("NodeList", "*");

        adapter.reqExecSLS(params, svcContext);
        TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
        assertEquals(TestId, "test1");
    }


    @Test
    public void reqExecCommand_shouldSetSuccessReal() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "127.0.0.1");
        params.put("Port", "22");
        params.put("User", "sdn");
        params.put("Password", "foo");
        params.put("Id", "test1");
        params.put("Cmd", "ls -l");
        params.put("SlsExec", "false");
        params.put("Timeout", "120");
        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e) {
            //if local ssh is not enabled
            System.out.print(e.getMessage());
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
        params.put("Cmd", "salt '*' test.ping --out=json --static");
        params.put("SlsExec", "false");
        params.put("Timeout", "120");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
            TestId = svcContext.getAttribute("test1.minion1");
            assertEquals(TestId, "true");
        } catch (Exception e) {
            //if saltstack ssh IP is not enabled
            System.out.print(e.getMessage());
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
        params.put("Cmd", "cd /srv/salt/; salt '*' state.apply vim --out=json --static");
        params.put("SlsExec", "true");
        params.put("Timeout", "120");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e) {
            //if saltstack ssh IP is not enabled
            System.out.print(e.getMessage());
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
        params.put("SlsName", "vim");
        params.put("Timeout", "120");
        params.put("NodeList", "minion1");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecSLS(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e) {
            //if saltstack ssh IP is not enabled
            System.out.print(e.getMessage());
        }
    }

    @Test
    public void reqExecCommand_shouldSetSuccessEnvParam() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "<IP>");
        params.put("Port", "2222");
        params.put("User", "root");
        params.put("Password", "vagrant");
        params.put("Id", "test1");
        params.put("SlsName", "vim");
        params.put("Timeout", "120");
        params.put("NodeList", "minion1");
        params.put("EnvParameters", "{\"exclude\": bar*}");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecSLS(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e) {
            //if saltstack ssh IP is not enabled
            System.out.print(e.getMessage());
        }
    }

    @Test
    public void reqExecCommand_shouldSetSuccessFileParam() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "<IP>");
        params.put("Port", "2222");
        params.put("User", "root");
        params.put("Password", "vagrant");
        params.put("Id", "test1");
        params.put("SlsName", "vim");
        params.put("Timeout", "120");
        params.put("NodeList", "minion1");
        params.put("EnvParameters", "{\"exclude\": \"bar,baz\"}");
        params.put("FileParameters", "{\"config.txt\":\"db_ip=10.1.1.1, sip_timer=10000\"}");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecSLS(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e) {
            //if saltstack ssh IP is not enabled
            System.out.print(e.getMessage());
        }
    }

    @Test
    public void reqExecCommand_shouldSetSuccessPillarParam() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "<IP>");
        params.put("Port", "2222");
        params.put("User", "root");
        params.put("Password", "vagrant");
        params.put("Id", "test1");
        params.put("SlsName", "vim");
        params.put("Timeout", "120");
        params.put("NodeList", "minion1");
        params.put("EnvParameters", "{\"exclude\": \"bar,baz\", \"pillar\":\"'{\\\"foo\\\": \\\"bar\\\"}'\"}");
        params.put("FileParameters", "{\"config.txt\":\"db_ip=10.1.1.1, sip_timer=10000\"}");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecSLS(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e) {
            //if saltstack ssh IP is not enabled
            System.out.print(e.getMessage());
        }
    }

    @Test
    public void reqExecCommand_shouldSetSuccessMultiFileParam() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "<IP>");
        params.put("Port", "2222");
        params.put("User", "root");
        params.put("Password", "vagrant");
        params.put("Id", "test1");
        params.put("SlsName", "vim");
        params.put("Timeout", "120");
        params.put("NodeList", "minion1");
        params.put("EnvParameters", "{\"exclude\": bar*}");
        params.put("FileParameters", "{\"config.txt\":\"db_ip=10.1.1.1, sip_timer=10000\" , \"config-tep.txt\":\"db_ip=10.1.1.1, sip_timer=10000\"}");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecSLS(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e) {
            //if saltstack ssh IP is not enabled
            System.out.print(e.getMessage());
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
        params.put("Timeout", "120");
        params.put("NodeList", "minion1");
        params.put("SlsFile", "src/test/resources/config.sls");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecSLSFile(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e) {
            //if saltstack ssh IP is not enabled
            System.out.print(e.getMessage());
        }
    }

    @Test
    public void reqExecCommand_shouldSetSuccessSSLFileMultiFileParam() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {

        params.put("HostName", "<IP>");
        params.put("Port", "2222");
        params.put("User", "root");
        params.put("Password", "vagrant");
        params.put("Id", "test1");
        params.put("Timeout", "120");
        params.put("NodeList", "minion1");
        params.put("SlsFile", "src/test/resources/config.sls");
        params.put("EnvParameters", "{\"exclude\": bar, \"pillar\":\"'{\\\"foo\\\": \\\"bar\\\"}'\"}");
        params.put("FileParameters", "{\"config.txt\":\"db_ip=10.1.1.1, sip_timer=10000\" , \"config-tep.txt\":\"db_ip=10.1.1.1, sip_timer=10000\"}");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecSLSFile(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e) {
            //if saltstack ssh IP is not enabled
            System.out.print(e.getMessage());
        }
    }
}
