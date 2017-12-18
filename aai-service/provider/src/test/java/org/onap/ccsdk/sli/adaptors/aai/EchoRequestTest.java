/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EchoRequestTest {

    private static final Logger LOG = LoggerFactory.getLogger(EchoRequestTest.class);

    private static AAIRequest request;
    private static AAIService aaiService;

    @BeforeClass
    public static void setUp() throws Exception {
        aaiService = new AAIService(
                AAIService.class.getResource(AAIService.AAICLIENT_PROPERTIES));
        request = new EchoRequest();
        LOG.info("\nEchoRequestTest.setUp\n");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        request = null;
        LOG.info("----------------------- EchoRequestTest.tearDown -----------------------");
    }

    @Test
    public void runGetRequestUrlTest() {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

        URL url;
        try {
            url = request.getRequestUrl("GET", null);
            assertNotNull(url);
        } catch (UnsupportedEncodingException | MalformedURLException | URISyntaxException exc) {
            LOG.error("Failed test", exc);
		}

    }

    @Test
    public void runToJSONStringTest() {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

        try {
            String json = request.toJSONString();
            assertNotNull(json);
        } catch (Exception exc) {
            LOG.error("Failed test", exc);
        }

    }

    @Test
    public void runGetArgsListTest() {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

        try {
            String[] args = request.getArgsList();
            assertNotNull(args);
        } catch (Exception exc) {
            LOG.error("Failed test", exc);
        }

    }

    @Test
    public void runGetModelTest() {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

        try {
            Class<?  extends AAIDatum> clazz = request.getModelClass();
            assertNotNull(clazz);
        } catch (Exception exc) {
            LOG.error("Failed test", exc);
        }

    }
}
