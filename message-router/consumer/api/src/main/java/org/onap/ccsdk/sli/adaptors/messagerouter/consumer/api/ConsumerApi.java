package org.onap.ccsdk.sli.adaptors.messagerouter.consumer.api;

public interface ConsumerApi extends AutoCloseable {
	public void subscribe(String topic, RequestHandler requestHandler);
}
