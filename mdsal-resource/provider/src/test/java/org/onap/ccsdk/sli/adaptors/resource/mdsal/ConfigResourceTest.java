/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

package org.onap.ccsdk.sli.adaptors.resource.mdsal;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.onap.ccsdk.sli.core.api.SvcLogicContext;
import org.onap.ccsdk.sli.core.api.exceptions.SvcLogicException;
import org.onap.ccsdk.sli.core.api.extensions.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.core.sli.provider.base.SvcLogicContextImpl;

public class ConfigResourceTest {
    ConfigResource configResource = new ConfigResource("http", "local",
                                                       "10001", "admin",
                                                       "password");

    @Test
    public void isAvailableNegativeTest() throws SvcLogicException {
        SvcLogicContext ctx = new SvcLogicContextImpl();
        assertEquals(QueryStatus.NOT_FOUND, configResource.isAvailable
                ("xyz", "key", "prefix", ctx));
    }
}

