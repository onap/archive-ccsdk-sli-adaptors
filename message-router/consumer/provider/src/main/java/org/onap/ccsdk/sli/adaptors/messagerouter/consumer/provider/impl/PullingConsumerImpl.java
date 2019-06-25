package org.onap.ccsdk.sli.adaptors.messagerouter.consumer.provider.impl;

import org.onap.ccsdk.sli.adaptors.messagerouter.consumer.api.PullingConsumer;
import org.onap.ccsdk.sli.adaptors.messagerouter.consumer.api.RequestHandler;

public class PullingConsumerImpl extends AbstractBaseConsumer implements PullingConsumer {

    public PullingConsumerImpl(String username, String password, String host, String authentication, Integer connectTimeout, Integer readTimeout, String group, String id, String filter, Integer limit, Integer timeoutQueryParamValue) {
	super(username, password, host, authentication, connectTimeout, readTimeout, group, id, filter, limit, timeoutQueryParamValue);
    }

    @Override
    public void pull() {
	this.poll();
    }

}
