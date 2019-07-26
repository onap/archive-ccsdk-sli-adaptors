package org.onap.ccsdk.sli.adaptors.messagerouter.consumer.provider.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.Test;

public class ConsumerFactoryTest {

    @Test
    public void testFactoryClientCreation() throws Exception {
        Properties props = new Properties();
        String userName = "deadpool";
        String password = "notSECURE";
        String host = "http://localhost:7001";
        String group = "myCluster";
        String id = "node1";
        Integer connectTimeout = 10000;
        Integer readTimeout = 20000;
        props.put("username", userName);
        props.put("password", password);
        props.put("host", host);
        props.put("group", group);

        ConsumerFactory factory = new ConsumerFactory(userName, password, host, group, id, connectTimeout, readTimeout);
       assertNotNull(factory.createPollingClient());
       assertNotNull(factory.createPullingClient());
    }
    
    @Test
    public void testFactoryDefaults() throws Exception {
        Properties props = new Properties();
        String userName = "deadpool";
        String password = "notSECURE";
        String host = "http://localhost:7001";
        String group = "myCluster";
        String id = "node1";
        Integer connectTimeout = 10000;
        Integer readTimeout = 20000;
        props.put("username", userName);
        props.put("password", password);
        props.put("host", host);
        props.put("group", group);

        ConsumerFactory factory = new ConsumerFactory(userName, password, host, group, id, connectTimeout, readTimeout);

        assertNotNull(factory.getAuth());
        assertNotNull(factory.getConnectTimeout());
        assertNotNull(factory.getReadTimeout());
        assertNotNull(factory.getFetchPause());
        assertNotNull(factory.getLimit());
        assertNotNull(factory.getTimeoutQueryParamValue());
    }

    @Test
    public void testFactoryDefaultsWithProps() {
        Properties props = new Properties();
        String userName = "deadpool";
        String password = "notSECURE";
        String host = "http://localhost:7001";
        String auth = "basic";
        String group = "myCluster";
        props.put("username", userName);
        props.put("password", password);
        props.put("host", host);
        props.put("group", group);

        ConsumerFactory factory = new ConsumerFactory(props);

        assertNotNull(factory.getAuth());
        assertNotNull(factory.getConnectTimeout());
        assertNotNull(factory.getReadTimeout());
        assertNotNull(factory.getFetchPause());
        assertNotNull(factory.getLimit());
        assertNotNull(factory.getTimeoutQueryParamValue());
    }

    @Test
    public void testFactoryOverrides() throws Exception {
        Properties props = new Properties();
        String userName = "deadpool";
        String password = "notSECURE";
        String host = "http://localhost:7001";
        String group = "myCluster";
        props.put("username", userName);
        props.put("password", password);
        props.put("host", host);
        props.put("group", group);

        String connectTimeout = "200";
        String readTimeout = "300";
        String fetchPause = "1000";
        String auth = "noauth";
        String timeoutQueryParamValue = "50";
        String limit = "2";
        props.put("connectTimeout", connectTimeout);
        props.put("readTimeout", readTimeout);
        props.put("fetchPause", fetchPause);
        props.put("auth", auth);
        props.put("timeoutQueryParamValue", timeoutQueryParamValue);
        props.put("limit", limit);

        ConsumerFactory factory = new ConsumerFactory(props);

        assertEquals(auth, factory.getAuth());
        assertEquals(Integer.valueOf(connectTimeout), factory.getConnectTimeout());
        assertEquals(Integer.valueOf(readTimeout), factory.getReadTimeout());
        assertEquals(Integer.valueOf(fetchPause), factory.getFetchPause());
        assertEquals(limit, factory.getLimit());
        assertEquals(timeoutQueryParamValue, factory.getTimeoutQueryParamValue());
    }

    @Test
    public void testManualOverrides() {
        Properties props = new Properties();
        String userName = "deadpool";
        String password = "notSECURE";
        String host = "http://localhost:7001";
        String auth = "basic";
        String group = "myCluster";
        props.put("username", userName);
        props.put("password", password);
        props.put("host", host);
        props.put("group", group);

        ConsumerFactory factory = new ConsumerFactory(props);

        assertNotNull(factory.getAuth());
        assertNotNull(factory.getConnectTimeout());
        assertNotNull(factory.getReadTimeout());
        assertNotNull(factory.getFetchPause());
        assertNotNull(factory.getLimit());
        assertNotNull(factory.getTimeoutQueryParamValue());
        String newAuth = "noauth";
        factory.setAuth(newAuth);
        assertEquals(newAuth, factory.getAuth());

        Integer connectTimeout = 1;
        factory.setConnectTimeout(connectTimeout);
        assertEquals(connectTimeout, factory.getConnectTimeout());

        Integer fetchPause = 5;
        factory.setFetchPause(fetchPause);
        assertEquals(fetchPause, factory.getFetchPause());

        factory.setFilter("\"filter\":{\n" + "\"class\":\"And\",\n" + "\"filters\":\n" + "[\n" + "{ \"class\":\"Equals\", \"foo\":\"abc\" },\n" + "{ \"class\":\"Assigned\", \"field\":\"bar\" }\n" + "]\n" + "}");
        assertNotNull(factory.getFilter());

        Integer limit = 3;
        factory.setLimit(limit);
        assertEquals(limit, factory.getLimit());

        Integer readTimeout = 2;
        factory.setReadTimeout(readTimeout);
        assertEquals(readTimeout, factory.getReadTimeout());

        Integer timeoutQueryParamValue = 47;
        factory.setTimeoutQueryParamValue(timeoutQueryParamValue);
        assertEquals(timeoutQueryParamValue, factory.getTimeoutQueryParamValue());
    }

}
