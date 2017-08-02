/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All rights
 * reserved.
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

package jtest.org.openecomp.sdnc.ra;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.openecomp.sdnc.ra.ResourceAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestReserve {

    private static final Logger log = LoggerFactory.getLogger(TestReserve.class);

    @Autowired(required = true)
    private ResourceAllocator resourceAllocator;

    @Autowired(required = true)
    private DataSetup dataSetup;

    @Test
    public void test001() throws Exception {
        String t = "001";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test successful response - new start - all resources available");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupPserver("server1", "MTSNJA4LCP1");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "New");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "300");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " +
                ctx.getAttribute("tmp.resource-allocator-output.max-available-speed"));
        log.info("  tmp.resource-allocator-output.speed-unit: " +
                ctx.getAttribute("tmp.resource-allocator-output.speed-unit"));

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 1, 300000));
    }

    @Test
    public void test002() throws Exception {
        String t = "002";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test successful response - new start supp - all resources available");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Pending", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 2, 200000));

        SvcLogicContext ctx = new SvcLogicContext();
        // ctx.setAttribute("tmp.resource-allocator.request-type", "New"); - Default is New
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "400");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " +
                ctx.getAttribute("tmp.resource-allocator-output.max-available-speed"));
        log.info("  tmp.resource-allocator-output.speed-unit: " +
                ctx.getAttribute("tmp.resource-allocator-output.speed-unit"));

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 3, 400000));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, 2));
    }

    @Test
    public void test003() throws Exception {
        String t = "003";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test successful response - change - all resources available");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Active", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 200000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Change");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "400");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " +
                ctx.getAttribute("tmp.resource-allocator-output.max-available-speed"));
        log.info("  tmp.resource-allocator-output.speed-unit: " +
                ctx.getAttribute("tmp.resource-allocator-output.speed-unit"));

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 200000));
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 3, 400000));
    }

    @Test
    public void test004() throws Exception {
        String t = "004";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test successful response - change supp - all resources available");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Active", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");
        dataSetup.setupService(service1, "Pending", 3, 400000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 200000));
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 3, 400000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Change");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "500");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " +
                ctx.getAttribute("tmp.resource-allocator-output.max-available-speed"));
        log.info("  tmp.resource-allocator-output.speed-unit: " +
                ctx.getAttribute("tmp.resource-allocator-output.speed-unit"));

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 200000));
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 4, 500000));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, 3));
    }

    @Test
    public void test005() throws Exception {
        String t = "005";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test successful response - change - check that hard limits are applied, not soft for change");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Active", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 200000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Change");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "1200000");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "kbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " +
                ctx.getAttribute("tmp.resource-allocator-output.max-available-speed"));
        log.info("  tmp.resource-allocator-output.speed-unit: " +
                ctx.getAttribute("tmp.resource-allocator-output.speed-unit"));

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 200000));
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 3, 1200000));
    }

    @Test
    public void test006() throws Exception {
        String t = "006";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test capacity not found - new start");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "New");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "300");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Gbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.NOT_FOUND);
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test007() throws Exception {
        String t = "007";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test capacity not found - new start supp");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Pending", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 2, 200000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "New");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "2000");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.NOT_FOUND);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 2, 200000));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, 3));
    }

    @Test
    public void test008() throws Exception {
        String t = "008";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test capacity not found - change");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Active", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 200000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Change");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "2000");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.NOT_FOUND);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 200000));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, 3));
    }

    @Test
    public void test009() throws Exception {
        String t = "009";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test capacity not found - change supp");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Active", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");
        dataSetup.setupService(service1, "Pending", 3, 400000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 200000));
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 3, 400000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Change");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "2000");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.NOT_FOUND);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 200000));
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 3, 400000));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, 4));
    }

    @Test
    public void test010() throws Exception {
        String t = "010";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test border condition - connection limit - new start - adding connection " +
                "when we are on the limit should fail");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        for (int i = 1; i <= 40; i++)
            dataSetup.setupService("reserve" + t + "/existing-service" + i, "Active", 2, 1000, "mtanjrsv126",
                    "mtsnj303vr1", "MTSNJA4LCP1/Server1");
        for (int i = 1; i <= 40; i += 4)
            dataSetup.setupService("reserve" + t + "/existing-service" + i, "Pending", 3, 1000, "mtanjrsv126",
                    "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Change");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "1");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.NOT_FOUND);
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test011() throws Exception {
        String t = "011";
        log.info("============== reserve " + t + " ================================");
        log.info(
                "=== Test border condition - connection limit - new start supp should succeed as no new connection being added");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        for (int i = 1; i <= 39; i++)
            dataSetup.setupService("reserve" + t + "/existing-service" + i, "Active", 2, 1000, "mtanjrsv126",
                    "mtsnj303vr1", "MTSNJA4LCP1/Server1");
        for (int i = 1; i <= 39; i += 4)
            dataSetup.setupService("reserve" + t + "/existing-service" + i, "Pending", 3, 1000, "mtanjrsv126",
                    "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        dataSetup.setupService(service1, "Pending", 2, 1000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 2, 1000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Change");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "5");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 3, 5000));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, 2));
    }

    @Test
    public void test012() throws Exception {
        String t = "012";
        log.info("============== reserve " + t + " ================================");
        log.info(
                "=== Test border condition - connection limit - change should succeed as no new connection being added");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        for (int i = 1; i <= 39; i++)
            dataSetup.setupService("reserve" + t + "/existing-service" + i, "Active", 2, 1000, "mtanjrsv126",
                    "mtsnj303vr1", "MTSNJA4LCP1/Server1");
        for (int i = 1; i <= 39; i += 4)
            dataSetup.setupService("reserve" + t + "/existing-service" + i, "Pending", 3, 1000, "mtanjrsv126",
                    "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        dataSetup.setupService(service1, "Active", 2, 1000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 1000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Change");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "5");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 1000));
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 3, 5000));
    }

    @Test
    public void test013() throws Exception {
        String t = "013";
        log.info("============== reserve " + t + " ================================");
        log.info(
                "=== Test border condition - connection limit - change supp should succeed as no new connection being added");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        for (int i = 1; i <= 39; i++)
            dataSetup.setupService("reserve" + t + "/existing-service" + i, "Active", 2, 1000, "mtanjrsv126",
                    "mtsnj303vr1", "MTSNJA4LCP1/Server1");
        for (int i = 1; i <= 39; i += 4)
            dataSetup.setupService("reserve" + t + "/existing-service" + i, "Pending", 3, 1000, "mtanjrsv126",
                    "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        dataSetup.setupService(service1, "Active", 2, 1000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");
        dataSetup.setupService(service1, "Pending", 3, 5000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 1000));
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 3, 5000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Change");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "10");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Active", 2, 1000));
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 4, 10000));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, 3));
    }

    @Test
    public void test014() throws Exception {
        String t = "014";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test input validations - invalid request-type in input");

        String service1 = "reserve" + t + "/service1";

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "xxxxx");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "10");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        try {
            resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);
        } catch (SvcLogicException e) {
            Assert.assertTrue(e.getMessage().equals(
                    "Invalid tmp.resource-allocator.request-type: xxxxx. Supported values are New, Change."));
            return;
        }
        Assert.fail("SvcLogicException expected");
    }

    @Test
    public void test015() throws Exception {
        String t = "015";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test server bw limit depends on number of servers - limit is 960Mbps for 1 server, 1920 for 2");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupPserver("server1", "MTSNJA4LCP1");
        dataSetup.setupPserver("server2", "MTSNJA4LCP1");
        dataSetup.setupPserver("server3", "MTSNJA4LCP1");
        dataSetup.setupPserver("server4", "MTSNJA4LCP1");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "New");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "1200");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " +
                ctx.getAttribute("tmp.resource-allocator-output.max-available-speed"));
        log.info("  tmp.resource-allocator-output.speed-unit: " +
                ctx.getAttribute("tmp.resource-allocator-output.speed-unit"));

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 1, 1200000));
    }

    @Test
    public void test016() throws Exception {
        String t = "016";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test resource threshold output");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupPserver("server1", "MTSNJA4LCP1");
        dataSetup.setupPserver("server2", "MTSNJA4LCP1");
        dataSetup.setupPserver("server3", "MTSNJA4LCP1");
        dataSetup.setupPserver("server4", "MTSNJA4LCP1");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "New");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "1605");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);
        for (String key : ctx.getAttributeKeySet())
            if (key.startsWith("tmp.resource-allocator-output"))
                log.info("  " + key + ": " + ctx.getAttribute(key));

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 1, 1605000));
    }

    @Test
    public void test017() throws Exception {
        String t = "017";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test if evc_count lookup in MAX_SERVER_SPEED depends on the number of primary servers.");
        log.info("=== For 10 existing EVC, it should take the first row, not the second (see data.sql).");
        log.info("=== Applied limit should be 1920Mbps, not 1680Mbps.");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupPserver("server1", "MTSNJA4LCP1");
        dataSetup.setupPserver("server2", "MTSNJA4LCP1");
        dataSetup.setupPserver("server3", "MTSNJA4LCP1");
        dataSetup.setupPserver("server4", "MTSNJA4LCP1");

        for (int i = 1; i <= 10; i++)
            dataSetup.setupService("reserve" + t + "/existing-service" + i, "Active", 2, 100000, "mtanjrsv126",
                    "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "New");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "800"); // 10*100Mbps existing + 800 = 1800
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " +
                ctx.getAttribute("tmp.resource-allocator-output.max-available-speed"));
        log.info("  tmp.resource-allocator-output.speed-unit: " +
                ctx.getAttribute("tmp.resource-allocator-output.speed-unit"));

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb("mtanjrsv126", "MTSNJA4LCP1", service1, "Pending", 1, 800000));
    }

    @Test
    public void test018() throws Exception {
        String t = "018";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test if evc_count lookup in MAX_SERVER_SPEED depends on the number of primary servers.");
        log.info("=== For 11 existing EVC, it should take the second row (see data.sql).");
        log.info("=== Applied limit should be 1680Mbps. We have 11*100 + 700, so this should fail.");

        String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupPserver("server1", "MTSNJA4LCP1");
        dataSetup.setupPserver("server2", "MTSNJA4LCP1");
        dataSetup.setupPserver("server3", "MTSNJA4LCP1");
        dataSetup.setupPserver("server4", "MTSNJA4LCP1");

        for (int i = 1; i <= 11; i++)
            dataSetup.setupService("reserve" + t + "/existing-service" + i, "Active", 2, 100000, "mtanjrsv126",
                    "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "New");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "700"); // 11*100Mbps existing + 700 = 1800
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " +
                ctx.getAttribute("tmp.resource-allocator-output.max-available-speed"));
        log.info("  tmp.resource-allocator-output.speed-unit: " +
                ctx.getAttribute("tmp.resource-allocator-output.speed-unit"));

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.NOT_FOUND);
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }
}
