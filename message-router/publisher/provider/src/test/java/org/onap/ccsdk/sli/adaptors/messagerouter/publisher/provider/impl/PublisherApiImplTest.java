package org.onap.ccsdk.sli.adaptors.messagerouter.publisher.provider.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.HttpURLConnection;
import java.net.URL;

import org.junit.Test;

public class PublisherApiImplTest {
    @Test
    public void verifyDefaultTimeouts() {
        PublisherApiImpl pub = new PublisherApiImpl();
        assertEquals(pub.DEFAULT_CONNECT_TIMEOUT, pub.connectTimeout);
        assertEquals(pub.DEFAULT_READ_TIMEOUT, pub.readTimeout);
    }

    @Test
    public void buildHttpURLConnection() throws Exception {
        PublisherApiImpl pub = new PublisherApiImpl();
        pub.init();

        String myUserName = "Batman";
        pub.setUsername(myUserName);
        assertEquals(myUserName, pub.username);
        String password = "P@$$";
        pub.setPassword(password);

        HttpURLConnection httpUrlConnection = pub.buildHttpURLConnection(new URL("http://localhost:7001"));
        assertNotNull(httpUrlConnection.getReadTimeout());
        assertNotNull(httpUrlConnection.getConnectTimeout());
        assertEquals("application/json", httpUrlConnection.getRequestProperty("Content-Type"));
        assertEquals("application/json", httpUrlConnection.getRequestProperty("Accept"));
    }

    @Test
    public void testMultipleHosts() {
        PublisherApiImpl pub = new PublisherApiImpl();
        String myTopic = "worldNews";
        String hostOne = "http://localhost:7001";
        String hostTwo = "http://localhost:7002";
        String hostThree = "http://localhost:7003";

        pub.setHost(hostOne + "," + hostTwo + "," + hostThree);

        assertEquals("http://localhost:7001/events/worldNews", pub.buildUrlString(0, myTopic));
        assertEquals("http://localhost:7002/events/worldNews", pub.buildUrlString(1, myTopic));
        assertEquals("http://localhost:7003/events/worldNews", pub.buildUrlString(2, myTopic));
    }
}