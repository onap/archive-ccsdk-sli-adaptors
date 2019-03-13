package org.onap.ccsdk.messagerouter.publisher.api;

public interface PublisherApi {
	public Boolean publish(String topic, String body);
}
