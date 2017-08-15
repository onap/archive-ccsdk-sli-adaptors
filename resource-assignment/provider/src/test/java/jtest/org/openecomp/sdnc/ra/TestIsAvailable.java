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
public class TestIsAvailable {

    private static final Logger log = LoggerFactory.getLogger(TestIsAvailable.class);

    @Autowired(required = true)
    private ResourceAllocator resourceAllocator;

    @Autowired(required = true)
    private DataSetup dataSetup;

    @Test
    public void test001() throws Exception {
        String t = "001";
        log.info("============== isAvailable " + t + " ================================");
        log.info("=== Test successful response - all resources available");

        String service1 = "isAvailable" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "300");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.isAvailable("NetworkCapacity", null, null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test002() throws Exception {
        String t = "002";
        log.info("============== isAvailable " + t + " ================================");
        log.info("=== Test capacity not found - request very big number that is above the limits");

        String service1 = "isAvailable" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "300");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Gbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.isAvailable("NetworkCapacity", null, null, ctx);

        String maxAvailableSpeed = ctx.getAttribute("tmp.resource-allocator-output.max-available-speed");
        String speedUnit = ctx.getAttribute("tmp.resource-allocator-output.speed-unit");

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " + maxAvailableSpeed);
        log.info("  tmp.resource-allocator-output.speed-unit: " + speedUnit);

        Assert.assertTrue(st == QueryStatus.NOT_FOUND);
        Assert.assertTrue(maxAvailableSpeed.equals("960000"));
        Assert.assertTrue(speedUnit.equals("kbps"));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test003() throws Exception {
        String t = "003";
        log.info("============== isAvailable " + t + " ================================");
        log.info("=== Test capacity not found - PROV check for VPE");

        String service1 = "isAvailable" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "---", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "300");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.isAvailable("NetworkCapacity", null, null, ctx);

        String maxAvailableSpeed = ctx.getAttribute("tmp.resource-allocator-output.max-available-speed");
        String speedUnit = ctx.getAttribute("tmp.resource-allocator-output.speed-unit");

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " + maxAvailableSpeed);
        log.info("  tmp.resource-allocator-output.speed-unit: " + speedUnit);

        Assert.assertTrue(st == QueryStatus.NOT_FOUND);
        Assert.assertTrue(maxAvailableSpeed.equals("0"));
        Assert.assertTrue(speedUnit.equals("kbps"));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test004() throws Exception {
        String t = "004";
        log.info("============== isAvailable " + t + " ================================");
        log.info("=== Test capacity not found - PROV check for VPLSPE");

        String service1 = "isAvailable" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "---", null);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "300");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.isAvailable("NetworkCapacity", null, null, ctx);

        String maxAvailableSpeed = ctx.getAttribute("tmp.resource-allocator-output.max-available-speed");
        String speedUnit = ctx.getAttribute("tmp.resource-allocator-output.speed-unit");

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " + maxAvailableSpeed);
        log.info("  tmp.resource-allocator-output.speed-unit: " + speedUnit);

        Assert.assertTrue(st == QueryStatus.NOT_FOUND);
        Assert.assertTrue(maxAvailableSpeed.equals("0"));
        Assert.assertTrue(speedUnit.equals("kbps"));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test005() throws Exception {
        String t = "005";
        log.info("============== isAvailable " + t + " ================================");
        log.info("=== Test capacity not found - VPE not found in DB");

        String service1 = "isAvailable" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "300");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.isAvailable("NetworkCapacity", null, null, ctx);

        String maxAvailableSpeed = ctx.getAttribute("tmp.resource-allocator-output.max-available-speed");
        String speedUnit = ctx.getAttribute("tmp.resource-allocator-output.speed-unit");

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " + maxAvailableSpeed);
        log.info("  tmp.resource-allocator-output.speed-unit: " + speedUnit);

        Assert.assertTrue(st == QueryStatus.NOT_FOUND);
        Assert.assertTrue(maxAvailableSpeed.equals("0"));
        Assert.assertTrue(speedUnit.equals("kbps"));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test006() throws Exception {
        String t = "006";
        log.info("============== isAvailable " + t + " ================================");
        log.info("=== Test capacity not found - VPLSPE not found in DB");

        String service1 = "isAvailable" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "300");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.isAvailable("NetworkCapacity", null, null, ctx);

        String maxAvailableSpeed = ctx.getAttribute("tmp.resource-allocator-output.max-available-speed");
        String speedUnit = ctx.getAttribute("tmp.resource-allocator-output.speed-unit");

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " + maxAvailableSpeed);
        log.info("  tmp.resource-allocator-output.speed-unit: " + speedUnit);

        Assert.assertTrue(st == QueryStatus.NOT_FOUND);
        Assert.assertTrue(maxAvailableSpeed.equals("0"));
        Assert.assertTrue(speedUnit.equals("kbps"));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test007() throws Exception {
        String t = "007";
        log.info("============== isAvailable " + t + " ================================");
        log.info("=== Test capacity not found - test max available speed calculation");

        String service1 = "isAvailable" + t + "/service1";
        String existingService1 = "isAvailable" + t + "/existing-service1";
        String existingService2 = "isAvailable" + t + "/existing-service2";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv127", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(existingService1, "Active", 2, 200000, "mtanjrsv126", "mtsnj303vr1",
                "MTSNJA4LCP1/Server1");
        dataSetup.setupService(existingService2, "Active", 3, 100000, "mtanjrsv127", "mtsnj303vr1",
                "MTSNJA4LCP1/Server1");
        dataSetup.setupService(existingService2, "Pending", 4, 500000, "mtanjrsv127", "mtsnj303vr1",
                "MTSNJA4LCP1/Server1");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "300");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Gbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.isAvailable("NetworkCapacity", null, null, ctx);

        String maxAvailableSpeed = ctx.getAttribute("tmp.resource-allocator-output.max-available-speed");
        String speedUnit = ctx.getAttribute("tmp.resource-allocator-output.speed-unit");

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " + maxAvailableSpeed);
        log.info("  tmp.resource-allocator-output.speed-unit: " + speedUnit);

        Assert.assertTrue(st == QueryStatus.NOT_FOUND);
        Assert.assertTrue(maxAvailableSpeed.equals("260000"));
        Assert.assertTrue(speedUnit.equals("kbps"));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test008() throws Exception {
        String t = "008";
        log.info("============== isAvailable " + t + " ================================");
        log.info("=== Test capacity not found - test server limit depending on number of connections");

        String service1 = "isAvailable" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv127", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        for (int i = 1; i <= 13; i++)
            dataSetup.setupService("isAvailable" + t + "/existing-service" + i, "Active", 2, 20000, "mtanjrsv126",
                    "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);
        ctx.setAttribute("tmp.resource-allocator.speed", "300");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Gbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.isAvailable("NetworkCapacity", null, null, ctx);

        String maxAvailableSpeed = ctx.getAttribute("tmp.resource-allocator-output.max-available-speed");
        String speedUnit = ctx.getAttribute("tmp.resource-allocator-output.speed-unit");

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " + maxAvailableSpeed);
        log.info("  tmp.resource-allocator-output.speed-unit: " + speedUnit);

        Assert.assertTrue(st == QueryStatus.NOT_FOUND);
        Assert.assertTrue(maxAvailableSpeed.equals("340000"));
        Assert.assertTrue(speedUnit.equals("kbps"));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test009() throws Exception {
        String t = "009";
        log.info("============== isAvailable " + t + " ================================");
        log.info("=== Test successful response - no service instance id in input - all resources available");

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.speed", "300");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Mbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        QueryStatus st = resourceAllocator.isAvailable("NetworkCapacity", null, null, ctx);

        log.info("Result: " + st);
        log.info("  tmp.resource-allocator-output.max-available-speed: " +
                ctx.getAttribute("tmp.resource-allocator-output.max-available-speed"));
        log.info("  tmp.resource-allocator-output.speed-unit: " +
                ctx.getAttribute("tmp.resource-allocator-output.speed-unit"));

        Assert.assertTrue(st == QueryStatus.SUCCESS);
    }

    @Test
    public void test010() throws Exception {
        String t = "010";
        log.info("============== isAvailable " + t + " ================================");
        log.info("=== Test input validations - no aic-site-id in input");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.speed", "300");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Gbps");

        try {
            resourceAllocator.isAvailable("NetworkCapacity", null, null, ctx);
        } catch (SvcLogicException e) {
            Assert.assertTrue(e.getMessage().equals(
                    "tmp.resource-allocator.aic-site-id is required in ResourceAllocator"));
            return;
        }
        Assert.fail("SvcLogicException expected");
    }

    @Test
    public void test011() throws Exception {
        String t = "011";
        log.info("============== isAvailable " + t + " ================================");
        log.info("=== Test input validations - no speed in input");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Gbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        try {
            resourceAllocator.isAvailable("NetworkCapacity", null, null, ctx);
        } catch (SvcLogicException e) {
            Assert.assertTrue(e.getMessage().equals("tmp.resource-allocator.speed is required in ResourceAllocator"));
            return;
        }
        Assert.fail("SvcLogicException expected");
    }

    @Test
    public void test012() throws Exception {
        String t = "012";
        log.info("============== isAvailable " + t + " ================================");
        log.info("=== Test input validations - speed not a number in input");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.speed", "nnnnn");
        ctx.setAttribute("tmp.resource-allocator.speed-unit", "Gbps");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        try {
            resourceAllocator.isAvailable("NetworkCapacity", null, null, ctx);
        } catch (SvcLogicException e) {
            Assert.assertTrue(e.getMessage().equals("Invalid tmp.resource-allocator.speed. Must be a number."));
            return;
        }
        Assert.fail("SvcLogicException expected");
    }

    @Test
    public void test013() throws Exception {
        String t = "013";
        log.info("============== isAvailable " + t + " ================================");
        log.info("=== Test input validations - speed-unit missing in input");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.speed", "300");
        ctx.setAttribute("tmp.resource-allocator.aic-site-id", "MTSNJA4LCP1");

        try {
            resourceAllocator.isAvailable("NetworkCapacity", null, null, ctx);
        } catch (SvcLogicException e) {
            Assert.assertTrue(e.getMessage().equals(
                    "tmp.resource-allocator.speed-unit is required in ResourceAllocator"));
            return;
        }
        Assert.fail("SvcLogicException expected");
    }
}
