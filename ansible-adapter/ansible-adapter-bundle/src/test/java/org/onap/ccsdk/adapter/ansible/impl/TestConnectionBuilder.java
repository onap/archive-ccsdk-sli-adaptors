/*-
 * ============LICENSE_START=======================================================
 * onap
 * ================================================================================
 * Copyright (C) 2018 Samsung
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.adapter.ansible.impl;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.ansible.impl.ConnectionBuilder;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResult;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import javax.net.ssl.SSLException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.Assert.assertEquals;

public class TestConnectionBuilder {
    ConnectionBuilder builder;
    @Before
    public void setup()
            throws SSLException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        builder = new ConnectionBuilder(1);
    }


    @Test
    public void testSetHttpContext() throws IllegalStateException, IllegalArgumentException {
        String user = "testUser";
        String pass = "testPassword";

        builder.setHttpContext(user, pass);
    }

    @Test
    public void testPost() throws IllegalStateException, IllegalArgumentException {
        String user = "testUser";
        String pass = "testPassword";
        String agentUrl = "test/server.com";
        String payload = "testPayload";

        builder.setHttpContext(user, pass);
        AnsibleResult result = builder.post(agentUrl, payload);

        assertEquals(611, result.getStatusCode());
        assertEquals(null, result.getStatusMessage());
        assertEquals("UNKNOWN", result.getResults());
    }

    @Test
    public void testGet() throws IllegalStateException, IllegalArgumentException {
        String user = "testUser";
        String pass = "testPassword";
        String agentUrl = "test/server.com";

        builder.setHttpContext(user, pass);
        AnsibleResult result = builder.get(agentUrl);

        assertEquals(611, result.getStatusCode());
        assertEquals(null, result.getStatusMessage());
        assertEquals("UNKNOWN", result.getResults());
    }

    @Test
    public void testGetMode()
            throws SSLException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        String user = "testUser";
        String pass = "testPassword";
        String agentUrl = "test/server.com";

        builder = new ConnectionBuilder(2);
        builder.setHttpContext(user, pass);
        AnsibleResult result = builder.get(agentUrl);

        assertEquals(611, result.getStatusCode());
        assertEquals(null, result.getStatusMessage());
        assertEquals("UNKNOWN", result.getResults());
    }

    @Test (expected = FileNotFoundException.class)
    public void testGetModeCert()
            throws KeyStoreException, CertificateException, IOException,
            KeyManagementException, NoSuchAlgorithmException, SvcLogicException {
        String user = "testUser";
        String pass = "testPassword";
        String agentUrl = "test/server.com";
        String certFile = "testCert";

        builder = new ConnectionBuilder(certFile);
        builder.setHttpContext(user, pass);
        AnsibleResult result = builder.get(agentUrl);

        assertEquals(611, result.getStatusCode());
        assertEquals(null, result.getStatusMessage());
        assertEquals("UNKNOWN", result.getResults());
    }

}
