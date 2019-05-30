package org.onap.ccsdk.sli.adaptors.messagerouter.consumer.api;

public interface PollingConsumer extends ConsumerApi {

    // Starts polling message router for messages, won't stop until close it called
    void start();
}
