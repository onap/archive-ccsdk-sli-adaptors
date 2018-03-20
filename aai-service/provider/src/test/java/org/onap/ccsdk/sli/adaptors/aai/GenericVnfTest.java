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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.sli.adaptors.aai.AAIClient;
import org.onap.ccsdk.sli.adaptors.aai.AAIRequest;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.openecomp.aai.inventory.v11.GenericVnf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GenericVnfTest {

    private static final Logger LOG = LoggerFactory.getLogger(GenericVnfTest.class);

    protected static AAIClient client;

    @BeforeClass
    public static void setUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.host.certificate.ignore", "true");
        properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.client.name", "SDNC");
        properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.client.psswd", "SDNC");
        properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.application", "CCSDK");
        properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.uri", "http://localhost:8181");
        properties.setProperty("connection.timeout", "60000");
        properties.setProperty("read.timeout", "60000");
        client = new AAIService(properties);
        ((AAIService)client).setExecutor(new TestExecutor());
        LOG.info("\nTaicAAIResourceTest.setUp\n");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        client = null;
        LOG.info("----------------------- GenericVnfTest.tearDown -----------------------");
    }


    @Test
    public void test01SaveGenericVnf() {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");
        try
        {
            SvcLogicContext ctx = new SvcLogicContext();

            String uuid = UUID.randomUUID().toString();
            Map<String, String> data = new HashMap<String, String>();
            data.put("vnf-id", uuid);
            data.put("vnf-name"    , "Demo-vmtn5scpx01");
            data.put("vnf-type", "asc_heat-int");
            data.put("service-id", "SDN-MOBILITY");
            data.put("equipment-role", "vSCP");
            data.put("orchestration-status", "active");
            data.put("heat-stack-id", "Devmtn5scpx04/" + data.get("vnf-id"));
            data.put("in-maint", "false");
            data.put("is-closed-loop-disabled", "false");
            data.put("encrypted-access-flag","true");

            QueryStatus resp = client.save("generic-vnf", false, false, "generic-vnf.vnf-id = '"+uuid+"'", data, "aaidata", ctx);

        }
        catch (Throwable e)
        {

        }
    }

    @Test
    public void test02QueryGenericVnf()
    {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

        try
        {
            List<String> keys = new ArrayList<String>();
            keys.add("vnf-id = 'ec14a84d-7b43-45ad-bb04-c12b74083648'");
            keys.add("depth = 'all'");

            SvcLogicContext ctx = new SvcLogicContext();
            QueryStatus response = client.query("generic-vnf", false, null, StringUtils.join(keys, " AND "), "aaiTest", null, ctx);

            assertTrue(response == QueryStatus.SUCCESS);
            LOG.info("AAIResponse: " + response.toString());
        }
        catch (Exception e)
        {
            LOG.error("Caught exception", e);
            fail("Caught exception");
        }
    }

    @Test
    public void test03UpdateGenericVnf() {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");
        try
        {
            SvcLogicContext ctx = new SvcLogicContext();

            String uuid = UUID.randomUUID().toString();
            Map<String, String> data = new HashMap<String, String>();
            data.put("service-id", "SDN-MOBILITY");
            data.put("equipment-role", "vSCP");
            data.put("orchestration-status", "active");
            data.put("heat-stack-id", "Devmtn5scpx04/" + data.get("vnf-id"));
            data.put("in-maint", "false");
            data.put("is-closed-loop-disabled", "false");
            data.put("encrypted-access-flag","true");

            QueryStatus resp = client.update("generic-vnf", "generic-vnf.vnf-id = '"+uuid+"'", data, "aaidata", ctx);

        }
        catch (Throwable e)
        {

        }
    }

    @Test
    public void test04DeleteGenericVnf()
    {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

        try
        {
            SvcLogicContext ctx = new SvcLogicContext();

            List<String> keys = new ArrayList<String>();
            keys.add("generic-vnf.vnf-id = 'VNF-S7'");

            QueryStatus response = client.delete("generic-vnf", StringUtils.join(keys, " AND "), ctx);

            assertTrue(response == QueryStatus.SUCCESS);
            LOG.info("AAIResponse: " + response.toString());
        }
        catch (Exception e)
        {
            LOG.error("Caught exception", e);
            fail("Caught exception");
        }
    }

    @Test
    public void test05GetResource()
    {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

        try
        {
            SvcLogicContext ctx = new SvcLogicContext();
            GenericVnf response = ((AAIService)client).getResource("/network/generic-vnfs/generic-vnf/ec14a84d-7b43-45ad-bb04-c12b74083648", GenericVnf.class);

            assertNotNull(response);
        }
        catch (Exception e)
        {

        }
    }

    static class TestExecutor implements AAIExecutorInterface {
        private String data = "{\"vnf-id\":\"7324200933\",\"vnf-name\":\"vnfinst1m001\",\"vnf-type\":\"TestVnf\",\"service-id\":\"9a77d5a-123e-4ca2-9eb9-0b015d2ee0fb\",\"equipment-role\":\"vTEST\",\"orchestration-status\":\"active\",\"in-maint\":false,\"is-closed-loop-disabled\":false,\"resource-version\":\"1520720941585\"}";

        @Override
        public String get(AAIRequest request) throws AAIServiceException {
            return data;
        }

        @Override
        public String post(AAIRequest request) throws AAIServiceException {
            return "success";
        }

        @Override
        public Boolean delete(AAIRequest request, String resourceVersion) throws AAIServiceException {
            return Boolean.TRUE;
        }

        @Override
        public Object query(AAIRequest request, Class clas) throws AAIServiceException {
            ObjectMapper mapper = AAIService.getObjectMapper();
            try {
                return mapper.readValue(data, GenericVnf.class);
            } catch (IOException e) {
                return new GenericVnf();
            }
        }

        @Override
        public Boolean patch(AAIRequest request, String resourceVersion) throws AAIServiceException {
            return Boolean.TRUE;
        }

    }
}
