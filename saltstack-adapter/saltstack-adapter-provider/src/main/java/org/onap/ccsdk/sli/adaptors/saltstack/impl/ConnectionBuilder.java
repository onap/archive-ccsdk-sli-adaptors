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

package org.onap.ccsdk.sli.adaptors.saltstack.impl;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import org.onap.ccsdk.sli.adaptors.saltstack.model.SaltstackResult;
import org.onap.ccsdk.sli.adaptors.saltstack.model.SaltstackResultCodes;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;

/**
 * Returns a custom SSH client
 * - based on options
 * - can create one with ssl using an X509 certificate that does NOT have a known CA
 * - create one which trusts ALL SSL certificates
 * - return default sshclient (which only trusts known CAs from default cacerts file for process) this is the default
 * option
 **/
//TODO: This class is to be altered completely based on the SALTSTACK server communication.
public class ConnectionBuilder {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ConnectionBuilder.class);


    /**
     * Constructor that initializes an ssh client based on certificate
     **/
    public ConnectionBuilder(String userName, String userPasswd) throws KeyStoreException, CertificateException, IOException,
            KeyManagementException, NoSuchAlgorithmException, SvcLogicException {


    }

    /**
     * Constructor which trusts all certificates in a specific java keystore file (assumes a JKS
     * file)
     **/
    public ConnectionBuilder(String certFile) throws KeyStoreException, IOException,
            KeyManagementException, NoSuchAlgorithmException, CertificateException {

    }

    /**
     * Connect to SSH server.
     */
    public SaltstackResult connect(String agentUrl, String payload) {

        SaltstackResult result = new SaltstackResult();
        try {
            //TODO: to implement SSH connected client to Saltstack Server
        } catch (Exception io) {
            logger.error("Caught Exception", io);
            result.setStatusCode(SaltstackResultCodes.IO_EXCEPTION.getValue());
            result.setStatusMessage(io.getMessage());
        }
        return result;
    }

    /**
     * Disconnect from SSH server.
     */
    public SaltstackResult disConnect(){

        SaltstackResult result = new SaltstackResult();
        try {
            //TODO: to implement SSH connected client to Saltstack Server
        } catch (Exception io) {
            logger.error("Caught Exception", io);
            result.setStatusCode(SaltstackResultCodes.IO_EXCEPTION.getValue());
            result.setStatusMessage(io.getMessage());
        }
        return result;
    }

    /**
     * Exec remote command over SSH. Return command execution status.
     * Command output is written to out or err stream.
     *
     * @param cmd command to execute
     * @param out content of sysout will go to this stream
     * @param err content of syserr will go to this stream
     * @return command execution status
     */
    public SaltstackResult execute(String cmd) {

        SaltstackResult result = new SaltstackResult();

        try {
            //TODO: to implement SSH command execute
        } catch (Exception io) {
            result.setStatusCode(SaltstackResultCodes.IO_EXCEPTION.getValue());
            result.setStatusMessage(io.getMessage());
            logger.error("Caught IOException", io);
        }
        return result;
    }
}
