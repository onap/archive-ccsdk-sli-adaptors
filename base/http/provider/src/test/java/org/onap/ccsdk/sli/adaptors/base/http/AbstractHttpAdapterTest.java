package org.onap.ccsdk.sli.adaptors.base.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;
import java.util.Set;

import javax.ws.rs.client.Client;

import org.junit.Test;

public class AbstractHttpAdapterTest {
    
    public class TestAdapter extends AbstractHttpAdapter {

    }
    
    @Test
    public void checkTimeouts() throws Exception {
        TestAdapter adapter = new TestAdapter();
        Client client = adapter.getClientBuilder().build();
        assertNotNull(client.getConfiguration().getProperty("jersey.config.client.readTimeout"));
        assertNotNull(client.getConfiguration().getProperty("jersey.config.client.connectTimeout"));
    }
    
    @Test
    public void propertiesTest() throws Exception {
        System.setProperty(AbstractHttpAdapter.SDNC_CONFIG_DIR, "src/test/resources/");
        TestAdapter adapter = new TestAdapter();
        Properties props = adapter.getProperties("testprops.properties");
        assertNotNull(props);
        assertEquals("world", props.get("hello"));
    }
    
    @Test
    public void basicAuthFilter() throws Exception {
        TestAdapter adapter = new TestAdapter();
        adapter.addBasicAuthCredentials("hello", "world");
        Set<Object> objs = adapter.getClientBuilder().getConfiguration().getInstances();
        assertEquals(BasicAuthFilter.class,objs.iterator().next().getClass());
    }

}
