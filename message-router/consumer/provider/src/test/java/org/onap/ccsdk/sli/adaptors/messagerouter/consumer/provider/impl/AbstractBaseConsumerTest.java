package org.onap.ccsdk.sli.adaptors.messagerouter.consumer.provider.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.messagerouter.consumer.api.RequestHandler;

public class AbstractBaseConsumerTest {
    private class DummyConsumer extends AbstractBaseConsumer {

        public DummyConsumer(String username, String password, String host, String authentication, Integer connectTimeout, Integer readTimeout, String group, String id, String filter, Integer limit, Integer timeoutQueryParamValue) {
            super(username, password, host, authentication, connectTimeout, readTimeout, group, id, filter, limit, timeoutQueryParamValue);
        }

    }

    public DummyConsumer getAuthDummy() {
        String username = "deadpool";
        String password = "notSECURE";
        String host = "http://localhost:7001";
        String group = "myCluster";
        String id = "node1";
        Integer connectTimeout = 10000;
        Integer readTimeout = 20000;
        String authentication = "basic";
        String filter = null;
        Integer limit = 3;
        Integer timeoutQueryParamValue = 5000;
        return new DummyConsumer(username, password, host, authentication, connectTimeout, readTimeout, group, id, filter, limit, timeoutQueryParamValue);
    }

    @Test
    public void createDummyWithAuth() {
        assertNotNull(getAuthDummy());
    }

    @Test
    public void createDummyNohAuth() {
        String username = null;
        String password = null;
        String host = "http://localhost:7001";
        String group = "myCluster";
        String id = "node1";
        Integer connectTimeout = 10000;
        Integer readTimeout = 20000;
        String authentication = "noauth";
        String filter = null;
        Integer limit = 3;
        Integer timeoutQueryParamValue = 5000;
        assertNotNull(new DummyConsumer(username, password, host, authentication, connectTimeout, readTimeout, group, id, filter, limit, timeoutQueryParamValue));
    }

    @Test
    public void callClose() throws Exception {
        DummyConsumer dummy = getAuthDummy();
        dummy.close();
    }

    @Test
    public void registerDummyHandler() throws Exception {
        DummyConsumer dummy = getAuthDummy();
        String topic = "politics";
        RequestHandler requestHandler = new RequestHandler() {

            @Override
            public void handleRequest(String topic, String requestBody) {
                // TODO Auto-generated method stub

            };

        };
        dummy.registerHandler(topic, requestHandler);
        assertEquals(new URL("http://localhost:7001/events/politics/myCluster/node1?timeout=5000&limit=3"), dummy.url);
        assertEquals(topic, dummy.topic);

    }

    @Test
    public void buildURL() throws Exception {
        DummyConsumer dummy = getAuthDummy();
        HttpURLConnection connection = dummy.buildHttpURLConnection(new URL("http://localhost:7001/events/politics/myCluster/node1?timeout=5000&limit=3"));
        assertNotNull(connection);
        assertEquals("application/json", connection.getRequestProperty("Accept"));
    }

}
