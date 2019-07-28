/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 - 2019 AT&T Intellectual Property. All rights
 *                      reserved.
 *                      
 * Modifications Copyright (C) 2019 IBM.
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

import org.onap.ccsdk.sli.adaptors.messagerouter.consumer.api.PullingConsumer;


public class PullingConsumerImpl extends AbstractBaseConsumer implements PullingConsumer {

    public PullingConsumerImpl(String username, String password, String host, String authentication, Integer connectTimeout, Integer readTimeout, String group, String id, String filter, Integer limit, Integer timeoutQueryParamValue) {
	super(username, password, host, authentication, connectTimeout, readTimeout, group, id, filter, limit, timeoutQueryParamValue);
    }

    @Override
    public void pull() {
	this.poll();
    }

}
