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
import org.onap.ccsdk.sli.adaptors.saltstack.impl.ConnectionBuilder;
import org.onap.ccsdk.sli.adaptors.saltstack.model.SaltstackResult;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class TestConnectionBuilder {

    private final String PENDING = "100";
    private final String SUCCESS = "400";
    private String message = "{\"Results\":{\"192.168.1.10\":{\"Id\":\"101\",\"StatusCode\":200,\"StatusMessage\":\"SUCCESS\"}},\"StatusCode\":200,\"StatusMessage\":\"FINISHED\"}";

    private ConnectionBuilder connBuilder;
    private String TestId;
    private boolean testMode = true;
    private Map<String, String> params;
    private SvcLogicContext svcContext;


    @Before
    public void setup() throws IllegalArgumentException {
        testMode = true;
        svcContext = new SvcLogicContext();
        String HostName = "test";
        String Port = "10";
        String User = "test";
        String Password = "test";
        connBuilder = new ConnectionBuilder(HostName, Port, User, Password);

        params = new HashMap<>();
        params.put("AgentUrl", "https://192.168.1.1");
        params.put("User", "test");
        params.put("Password", "test");
    }

    @After
    public void tearDown() {
        testMode = false;
        connBuilder = null;
        params = null;
        svcContext = null;
    }

    @Test
    public void reqExecCommand_exitStatus255() {

        int exitStatus = 255;
        String errFilePath = "src/test/resources/test.json";
        String command = "test";

        SaltstackResult result = connBuilder.sortExitStatus(exitStatus, errFilePath, command);
        int status = result.getStatusCode();
        assertEquals(698, status);
    }

    @Test
    public void reqExecCommand_exitStatus1() {

        int exitStatus = 1;
        String errFilePath = "src/test/resources/test.json";
        String command = "test";

        SaltstackResult result = connBuilder.sortExitStatus(exitStatus, errFilePath, command);
        int status = result.getStatusCode();
        assertEquals(698, status);
    }

    @Test
    public void reqExecCommand_exitStatus5() {

        int exitStatus = 5;
        String errFilePath = "src/test/resources/test.json";
        String command = "test";

        SaltstackResult result = connBuilder.sortExitStatus(exitStatus, errFilePath, command);
        int status = result.getStatusCode();
        assertEquals(613, status);
    }

    @Test
    public void reqExecCommand_exitStatus65() {

        int exitStatus = 65;
        String errFilePath = "src/test/resources/test.json";
        String command = "test";

        SaltstackResult result = connBuilder.sortExitStatus(exitStatus, errFilePath, command);
        int status = result.getStatusCode();
        assertEquals(613, status);
    }

    @Test
    public void reqExecCommand_exitStatus67() {

        int exitStatus = 5;
        String errFilePath = "src/test/resources/test.json";
        String command = "test";

        SaltstackResult result = connBuilder.sortExitStatus(exitStatus, errFilePath, command);
        int status = result.getStatusCode();
        assertEquals(613, status);
    }

    @Test
    public void reqExecCommand_exitStatus73() {

        int exitStatus = 65;
        String errFilePath = "src/test/resources/test.json";
        String command = "test";

        SaltstackResult result = connBuilder.sortExitStatus(exitStatus, errFilePath, command);
        int status = result.getStatusCode();
        assertEquals(613, status);
    }

    @Test
    public void reqExecCommand_exitStatusUnknown() {

        int exitStatus = 5121;
        String errFilePath = "src/test/resources/test.json";
        String command = "test";

        SaltstackResult result = connBuilder.sortExitStatus(exitStatus, errFilePath, command);
        int status = result.getStatusCode();
        assertEquals(699, status);
    }

    @Test
    public void reqExecCommand_exitStatusNoFile() {

        int exitStatus = 65;
        String errFilePath = "src/test/resource/test.json";
        String command = "test";

        SaltstackResult result = connBuilder.sortExitStatus(exitStatus, errFilePath, command);
        int status = result.getStatusCode();
        assertEquals(613, status);
    }
}
