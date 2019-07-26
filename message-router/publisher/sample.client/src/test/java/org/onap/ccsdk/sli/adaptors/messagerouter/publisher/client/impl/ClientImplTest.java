package org.onap.ccsdk.sli.adaptors.messagerouter.publisher.client.impl;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.messagerouter.publisher.api.PublisherApi;

public class ClientImplTest {
    
    @Test
    public void testSetTopic() {
        ClientImpl impl = new ClientImpl();
        String myTopic = "stock updates";
        impl.setTopic(myTopic);
        
        PublisherApi publisherImpl = new PublisherApi() {

            @Override
            public Boolean publish(String topic, String body) {
                assertEquals(myTopic,topic);
                return true;
            }
           
        };
        impl.setPublisher(publisherImpl);
        impl.init();
    }

}
