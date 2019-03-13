package org.onap.ccsdk.messagerouter.publisher.client.impl;

import org.onap.ccsdk.messagerouter.publisher.api.PublisherApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientImpl {
	private static final Logger logger = LoggerFactory.getLogger(ClientImpl.class);
	private String topic;
	private PublisherApi publisher;

	public void setPublisher(PublisherApi publisherApi) {
		this.publisher = publisherApi;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public ClientImpl() {

	}

	public void init() {
		for (int i = 0; i < 5; i++) {
			String body = "{\"hello\":\"world " + String.valueOf(Math.random()) + "\"}";
			logger.error("Loop iteration " + i + " sending body " + body + " to the topic " + topic);
			Boolean result = publisher.publish(topic, body);
			logger.error("Loop iteration " + i + " returned the boolean value " + result);
		}
	}

}