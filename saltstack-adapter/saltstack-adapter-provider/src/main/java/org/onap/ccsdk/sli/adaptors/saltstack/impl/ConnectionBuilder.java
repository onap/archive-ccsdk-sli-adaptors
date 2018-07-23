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

package org.onap.ccsdk.sli.adaptors.saltstack.impl;

import com.att.eelf.configuration.EELFLogger;
import com.att.eelf.configuration.EELFManager;
import org.onap.ccsdk.sli.adaptors.saltstack.model.SaltstackResult;
import org.onap.ccsdk.sli.adaptors.saltstack.model.SaltstackResultCodes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Returns a custom SSH client
 * - based on options
 * - can create one with ssl using an X509 certificate that does NOT have a known CA
 * - create one which trusts ALL SSL certificates
 * - return default sshclient (which only trusts known CAs from default cacerts file for process) this is the default
 * option
 **/
public class ConnectionBuilder {

    private static final EELFLogger logger = EELFManager.getInstance().getLogger(ConnectionBuilder.class);
    SshConnection sshConnection;

    /**
     * Constructor that initializes an ssh client based on username and password
     **/
    public ConnectionBuilder(String host, String port, String userName, String userPasswd) {
        sshConnection = new SshConnection(host, Integer.parseInt(port), userName, userPasswd);
    }

    /**
     * Constructor that initializes an ssh client based on ssh certificate
     **/
    public ConnectionBuilder(String host, String port, String certFile) {
        sshConnection = new SshConnection(host, Integer.parseInt(port), certFile);
    }

    /**
     * Constructor that initializes an ssh client based on ssh username password and certificate
     **/
    public ConnectionBuilder(String host, String port, String userName, String userPasswd,
                             String certFile) {

        sshConnection = new SshConnection(host, Integer.parseInt(port), userName, userPasswd, certFile);
    }

    /**
     * 1. Connect to SSH server.
     * 2. Exec remote command over SSH. Return command execution status.
     * Command output is written to out or err stream.
     *
     * @param cmd Commands to execute
     * @return command execution status
     */
    public SaltstackResult connectNExecute(String cmd, long execTimeout) throws IOException {
        return connectNExecute(cmd, -1, -1, execTimeout);
    }

    /**
     * 1. Connect to SSH server with retry enabled.
     * 2. Exec remote command over SSH. Return command execution status.
     * Command output is written to out or err stream.
     *
     * @param cmd        Commands to execute
     * @param retryDelay delay between retry to make a SSH connection.
     * @param retryCount number of count retry to make a SSH connection.
     * @return command execution status
     */
    public SaltstackResult connectNExecute(String cmd, int retryCount, int retryDelay, long execTimeout)
            throws IOException {

        SaltstackResult result = new SaltstackResult();
        ByteArrayOutputStream out = null;
        ByteArrayOutputStream errs = null;
        if (execTimeout >= 0) {
            sshConnection.setExecTimeout(execTimeout);
        }

        try {
            if (retryCount != -1) {
                result = sshConnection.connectWithRetry(retryCount, retryDelay);
            } else {
                result = sshConnection.connect();
            }
            if (result.getStatusCode() != SaltstackResultCodes.SUCCESS.getValue()) {
                return result;
            }
            out = new ByteArrayOutputStream();
            errs = new ByteArrayOutputStream();
            result = sshConnection.execCommand(cmd, out, errs, result);
            sshConnection.disconnect();
            if (result.getSshExitStatus() != 0) {
                return sortExitStatus(result.getSshExitStatus(), errs.toString(), cmd);
            }
            if (result.getStatusCode() != SaltstackResultCodes.SUCCESS.getValue()) {
                return result;
            }
            result.setStatusMessage("Success");
            result.setOutputMessage(out);
        } catch (Exception io) {
            logger.error("Caught Exception", io);
            result.setStatusCode(SaltstackResultCodes.UNKNOWN_EXCEPTION.getValue());
            result.setStatusMessage(io.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
            if (errs != null) {
                errs.close();
            }
        }
        return result;
    }

    public SaltstackResult sortExitStatus(int exitStatus, String errMess, String cmd) {
        SaltstackResult result = new SaltstackResult();
        if (exitStatus == 255 || exitStatus == 1) {
            String errMessage = "Error executing command [" + cmd + "] over SSH [" + sshConnection.toString()
                    + "]. Exit Code " + exitStatus + " and Error message : " +
                    "Malformed configuration. " + errMess;
            logger.error(errMessage);
            result.setStatusCode(SaltstackResultCodes.INVALID_COMMAND.getValue());
            result.setStatusMessage(errMessage);
        } else if (exitStatus == 5 || exitStatus == 65) {
            String errMessage = "Error executing command [" + cmd + "] over SSH [" + sshConnection.toString()
                    + "]. Exit Code " + exitStatus + " and Error message : " +
                    "Host not allowed to connect. " + errMess;
            logger.error(errMessage);
            result.setStatusCode(SaltstackResultCodes.USER_UNAUTHORIZED.getValue());
            result.setStatusMessage(errMessage);
        } else if (exitStatus == 67 || exitStatus == 73) {
            String errMessage = "Error executing command [" + cmd + "] over SSH [" + sshConnection.toString()
                    + "]. Exit Code " + exitStatus + " and Error message : " +
                    "Key exchange failed. " + errMess;
            logger.error(errMessage);
            result.setStatusCode(SaltstackResultCodes.CERTIFICATE_ERROR.getValue());
            result.setStatusMessage(errMessage);
        } else {
            String errMessage = "Error executing command [" + cmd + "] over SSH [" + sshConnection.toString()
                    + "]. Exit Code " + exitStatus + " and Error message : " + errMess;
            logger.error(errMessage);
            result.setStatusCode(SaltstackResultCodes.UNKNOWN_EXCEPTION.getValue());
            result.setStatusMessage(errMessage);
        }
        return result;
    }
}
