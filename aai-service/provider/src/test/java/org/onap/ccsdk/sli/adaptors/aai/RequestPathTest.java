/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights
 *                         reserved.
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

package org.onap.ccsdk.sli.adaptors.aai;

import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RequestPathTest {

    private static final Logger LOG = LoggerFactory.getLogger(RequestPathTest.class);

    protected static AAIClient client;

    @BeforeClass
    public static void setUp() throws Exception {
        URL url = AAIService.class.getResource(AAIService.AAICLIENT_PROPERTIES);
        client = new AAIService(url);
        LOG.info("\nTaicAAIResourceTest.setUp\n");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        client = null;
        LOG.info("----------------------- AAIResourceTest.tearDown -----------------------");
    }

    @Test
    public void getRequestUrl() {
        try {
            Map<String, String> nameValues = new HashMap<String, String> ();
            nameValues.put("generic-vnf.vnf-id","AABBCCDDEEFFGG0123");
            
            AAIRequest request = AAIRequest.createRequest("service-instances", nameValues);
            request.addRequestProperty("generic-vnf.vnf-id","AABBCCDDEEFFGG0123");

            URL url = request.getRequestUrl("GET", null);
            assertNotNull(url);
            url.getPath();
            LOG.info("Received response");
        } catch(Exception exc) {
            LOG.info("Caught exception", exc);
        }
    }
}
