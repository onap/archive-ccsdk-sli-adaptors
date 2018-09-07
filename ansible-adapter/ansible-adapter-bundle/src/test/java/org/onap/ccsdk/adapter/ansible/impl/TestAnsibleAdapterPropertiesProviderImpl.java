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
