/**
 * ============LICENSE_START====================================================
 * org.onap.aaf
 * ===========================================================================
 * Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 *
 * Modifications Copyright (C) 2019 IBM.
 * ===========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END====================================================
 *
 */

package org.onap.ccsdk.sli.adaptors.messagerouter.publisher.client.impl;

import org.onap.ccsdk.sli.adaptors.messagerouter.publisher.api.PublisherApi;
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
			String body = "{\"hello\":\"world " + Math.random() + "\"}";
			logger.error("Loop iteration " + i + " sending body " + body + " to the topic " + topic);
			Boolean result = publisher.publish(topic, body);
			logger.error("Loop iteration " + i + " returned the boolean value " + result);
		}
	}

}