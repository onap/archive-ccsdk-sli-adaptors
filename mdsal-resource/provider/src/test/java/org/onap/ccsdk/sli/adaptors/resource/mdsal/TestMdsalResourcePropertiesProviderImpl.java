/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2018 Samsung. All rights
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

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TestMdsalResourcePropertiesProviderImpl {

    MdsalResourcePropertiesProviderImpl mdsal;

    @Before
    public void setup() {
        mdsal = new MdsalResourcePropertiesProviderImpl();
    }


    @Test
    public void testGetProperties() {
        Properties prop = mdsal.getProperties();

        System.out.println("All Default Properties : " + prop);

        assertEquals("localhost",prop.getProperty("org.onap.ccsdk.sli.adaptors.resource.mdsal.sdnc-host"));
        assertEquals("Kp8bJ4SXszM0WXlhak3eHlcse2gAw84vaoGGmJvUy2U",prop.getProperty("org.onap.ccsdk.sli.adaptors.resource.mdsal.sdnc-passwd"));
        assertEquals("http",prop.getProperty("org.onap.ccsdk.sli.adaptors.resource.mdsal.sdnc-protocol"));
        assertEquals("8181",prop.getProperty("org.onap.ccsdk.sli.adaptors.resource.mdsal.sdnc-port"));
        assertEquals("admin",prop.getProperty("org.onap.ccsdk.sli.adaptors.resource.mdsal.sdnc-user"));
    }


    @Test
    public void testGetPropertie() throws IllegalStateException, IllegalArgumentException {
        MdsalResourcePropertiesProviderImpl test = new MdsalResourcePropertiesProviderImpl();

        mdsal.determinePropertiesFile(test);

    }
}
