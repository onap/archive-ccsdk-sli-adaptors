package org.onap.ccsdk.sli.adaptors.messagerouter.publisher.api;

public interface PublisherApi {
	public Boolean publish(String topic, String body);
}
