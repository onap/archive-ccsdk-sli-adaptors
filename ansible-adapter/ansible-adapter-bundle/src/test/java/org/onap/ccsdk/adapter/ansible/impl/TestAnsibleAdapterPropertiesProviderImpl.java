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
import org.onap.ccsdk.sli.adaptors.ansible.impl.AnsibleAdapterPropertiesProviderImpl;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TestAnsibleAdapterPropertiesProviderImpl {
    AnsibleAdapterPropertiesProviderImpl adaptor;
    @Before
    public void setup() throws IllegalArgumentException {
        adaptor = new AnsibleAdapterPropertiesProviderImpl();
    }


    @Test
    public void testGetProperties() throws IllegalStateException, IllegalArgumentException {
        Properties prop = adaptor.getProperties();

        System.out.println("All Property params : " + prop);
        assertEquals("TRUST_ALL", prop.getProperty("org.onap.appc.adapter.ansible.clientType"));
        assertEquals("org.onap.appc.appc_ansible_adapter", prop.getProperty("org.onap.appc.provider.adaptor.name"));
        assertEquals("changeit", prop.getProperty("org.onap.appc.adapter.ansible.trustStore.trustPasswd"));
        assertEquals("${user.home},/opt/opendaylight/current/properties", prop.getProperty("org.onap.appc.bootstrap.path"));
        assertEquals("APPC", prop.getProperty("appc.application.name"));
        assertEquals("appc.properties", prop.getProperty("org.onap.appc.bootstrap.file"));
        assertEquals("org.onap/appc/i18n/MessageResources", prop.getProperty("org.onap.appc.resources"));
        assertEquals("/opt/opendaylight/tls-client/mykeystore.js", prop.getProperty("org.onap.appc.adapter.ansible.trustStore"));
    }
}
