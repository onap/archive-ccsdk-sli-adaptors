/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights
 *                      reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.messagerouter.consumer.provider.impl;

import org.onap.ccsdk.sli.adaptors.messagerouter.consumer.api.PollingConsumer;
import org.onap.ccsdk.sli.adaptors.messagerouter.consumer.api.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * java.net based client to build message router consumers
 */
public class PollingConsumerImpl implements PollingConsumer {

    //RunnableConsumer is a private inner class so run cannot be called from other code
    private class RunnableConsumer extends AbstractBaseConsumer implements Runnable, PollingConsumer {
	private final Logger LOG = LoggerFactory.getLogger(PollingConsumerImpl.class);
	private volatile Thread t;
	private final Integer fetchPause;

	public RunnableConsumer(String username, String password, String host, String authentication, Integer connectTimeout, Integer readTimeout, Integer fetchPause, String group, String id, String filter, Integer limit, Integer timeoutQueryParamValue) {
	    super(username, password, host, authentication, connectTimeout, readTimeout, group, id, filter, limit, timeoutQueryParamValue);
	    this.fetchPause = fetchPause;
	}

	public void start() {
	    t = new Thread(this);
	    t.start();
	    LOG.info("ConsumerImpl started. Fetch period is {} ms.", fetchPause);
	}

	public void stop() {
	    t = null;
	    LOG.info("ConsumerImpl stopped.");
	}

	@Override
	public void run() {
	    if (this.url != null) {
		Thread thisThread = Thread.currentThread();
		while (t == thisThread) {
		    poll();
		    try {
			LOG.trace("Next fetch from MessageRouter url {} after {} milliseconds.", url, fetchPause);
			Thread.sleep(fetchPause);
		    } catch (InterruptedException e) {
			LOG.warn("Thread sleep was interrupted.", e);
		    }
		}
	    } else {
		LOG.error("URL is null, can't listen for messages");
	    }
	}

	@Override
	public void close() throws Exception {
	    stop();
	}
    }

    private RunnableConsumer c;

    public PollingConsumerImpl(String username, String password, String host, String authentication, Integer connectTimeout, Integer readTimeout, Integer fetchPause, String group, String id, String filter, Integer limit, Integer timeoutQueryParamValue) {
	c = new RunnableConsumer(username, password, host, authentication, connectTimeout, readTimeout, fetchPause, group, id, filter, limit, timeoutQueryParamValue);
    }

    @Override
    public void start() {
	c.start();
    }

    @Override
    public void registerHandler(String topic, RequestHandler requestHandler) {
	c.registerHandler(topic, requestHandler);
    }

    @Override
    public void close() throws Exception {
	c.close();
    }
}
