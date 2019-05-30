package org.onap.ccsdk.sli.adaptors.messagerouter.consumer.api;

public interface ConsumerApi extends AutoCloseable {   
    	//registers a handler to handle a specific topic, should be called only once per client
	public void registerHandler(String topic, RequestHandler requestHandler);
}
