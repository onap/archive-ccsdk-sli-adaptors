'''
/*-
* ============LICENSE_START=======================================================
* ONAP : CCSDK
* ================================================================================
* Copyright (C) 2018 Samsung Electronics.  All rights reserved.
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
'''

<CREATING saltstack environment is outside the scope of this adaptor, however the requirement is as follows>
a. The SaltStack server should have it’s SSH enabled.
b. Via ssh user account we should have the access to run saltstack command (here we will see how to enable root access via ssh and connect to server via root user).
============
INSTALLATION: Saltstack DEMO Environment creation:
============

1, Install VirtualBox.
2, Install Vagrant.
3, Download https://github.com/UtahDave/salt-vagrant-demo. You can use git or download a zip of the project directly from GitHub (sample Vagrant attached).
4, Extract the zip file you downloaded, and then open a command prompt to the extracted directory.
5, Run vagrant up to start the demo environment: vagrant up
   After Vagrant ups (~10 minutes) and you are back at the command prompt, you are ready to continue.
   More info: https://docs.saltstack.com/en/getstarted/fundamentals/

============
Configuration: Sample Saltstack server execution configuration requirement.
============
1, login to Master Saltstack server node:
"sudo vi /etc/ssh/sshd_config" and SET the following
PermitEmptyPasswords yes
PermitRootLogin yes

SAVE and close.

2, Run: "sudo passwd root"
and set the root password.
Then run: "sudo reboot"

3, On the host machine, open the virtual box set a port forwarding to the master server for 2222 -> 22 
This will redirect messages to host machine to the Vagarant Master server.  

============
TESTING: Sample Saltstack server command execution.
============
    
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
        params.put("Timeout", "12000");
        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e){
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
        params.put("Timeout", "12000");

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
        params.put("Timeout", "12000");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecCommand(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e){
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
        params.put("Timeout", "12000");
        params.put("NodeList", "minion1");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecSLS(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e){
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
        params.put("Timeout", "12000");
        params.put("NodeList", "minion1");
        params.put("EnvParameters", "{\"exclude\": bar*}");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecSLS(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e){
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
        params.put("Timeout", "12000");
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
        } catch (Exception e){
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
        params.put("Timeout", "12000");
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
        } catch (Exception e){
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
        params.put("Timeout", "12000");
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
        } catch (Exception e){
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
        params.put("Timeout", "12000");
        params.put("NodeList", "minion1");
        params.put("SlsFile", "src/test/resources/config.sls");

        adapter = new SaltstackAdapterImpl();
        try {
            adapter.reqExecSLSFile(params, svcContext);
            String status = svcContext.getAttribute("org.onap.appc.adapter.saltstack.result.code");
            TestId = svcContext.getAttribute("org.onap.appc.adapter.saltstack.Id");
            assertEquals("200", status);
            assertEquals(TestId, "test1");
        } catch (Exception e){
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
            params.put("Timeout", "12000");
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
            } catch (Exception e){
                //if saltstack ssh IP is not enabled
                System.out.print(e.getMessage());
            }
        }