package org.onap.ccsdk.sli.adaptors.messagerouter.consumer.api;

public interface PullingConsumer extends ConsumerApi {
    
    //Pulls a single batch of messages
    void pull();
}
