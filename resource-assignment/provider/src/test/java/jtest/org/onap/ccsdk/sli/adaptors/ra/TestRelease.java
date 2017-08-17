/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All
 *                         rights
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

package jtest.org.onap.ccsdk.sli.adaptors.ra;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.adaptors.ra.ResourceAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestRelease {

    private static final Logger log = LoggerFactory.getLogger(TestRelease.class);

    @Autowired(required = true)
    private ResourceAllocator resourceAllocator;

    @Autowired(required = true)
    private DataSetup dataSetup;

    @Test
    public void test001() throws Exception {
        String t = "001";
        log.info("============== release " + t + " ================================");
        log.info("=== Test release - cancel - new start");

        String service1 = "release" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Pending", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Pending", 2, 200000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Cancel");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        QueryStatus st = resourceAllocator.release("NetworkCapacity", null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test002() throws Exception {
        String t = "002";
        log.info("============== release " + t + " ================================");
        log.info("=== Test release - cancel - change");

        String service1 = "release" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Active", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");
        dataSetup.setupService(service1, "Pending", 3, 400000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Active", 2, 200000));
        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Pending", 3, 400000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Cancel");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        QueryStatus st = resourceAllocator.release("NetworkCapacity", null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Active", 2, 200000));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, 3));
    }

    @Test
    public void test003() throws Exception {
        String t = "003";
        log.info("============== release " + t + " ================================");
        log.info("=== Test release - cancel - active there, but no pending - should do nothing and return success");

        String service1 = "release" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Active", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Active", 2, 200000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Cancel");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        QueryStatus st = resourceAllocator.release("NetworkCapacity", null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Active", 2, 200000));
    }

    @Test
    public void test004() throws Exception {
        String t = "004";
        log.info("============== release " + t + " ================================");
        log.info("=== Test release - cancel - nothing in DB - should return success");

        String service1 = "release" + t + "/service1";

        dataSetup.cleanup();

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Cancel");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        QueryStatus st = resourceAllocator.release("NetworkCapacity", null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test005() throws Exception {
        String t = "005";
        log.info("============== release " + t + " ================================");
        log.info("=== Test release - activate - new start");

        String service1 = "release" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Pending", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Pending", 2, 200000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Activate");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        QueryStatus st = resourceAllocator.release("NetworkCapacity", null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Active", 2, 200000));
    }

    @Test
    public void test006() throws Exception {
        String t = "006";
        log.info("============== release " + t + " ================================");
        log.info("=== Test release - actovate - change");

        String service1 = "release" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Active", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");
        dataSetup.setupService(service1, "Pending", 3, 400000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Active", 2, 200000));
        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Pending", 3, 400000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Activate");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        QueryStatus st = resourceAllocator.release("NetworkCapacity", null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Active", 3, 400000));
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, 2));
    }

    @Test
    public void test007() throws Exception {
        String t = "007";
        log.info("============== release " + t + " ================================");
        log.info("=== Test release - activate - active there, but no pending - should do nothing and return success");

        String service1 = "release" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Active", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Active", 2, 200000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Activate");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        QueryStatus st = resourceAllocator.release("NetworkCapacity", null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Active", 2, 200000));
    }

    @Test
    public void test008() throws Exception {
        String t = "008";
        log.info("============== release " + t + " ================================");
        log.info("=== Test release - activate - nothing in DB - should return success");

        String service1 = "release" + t + "/service1";

        dataSetup.cleanup();

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Activate");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        QueryStatus st = resourceAllocator.release("NetworkCapacity", null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test009() throws Exception {
        String t = "009";
        log.info("============== release " + t + " ================================");
        log.info("=== Test release - disconnect - only pending in DB");

        String service1 = "release" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Pending", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Pending", 2, 200000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Disconnect");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        QueryStatus st = resourceAllocator.release("NetworkCapacity", null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test010() throws Exception {
        String t = "010";
        log.info("============== release " + t + " ================================");
        log.info("=== Test release - disconnect - only active in DB");

        String service1 = "release" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Active", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Active", 2, 200000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Disconnect");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        QueryStatus st = resourceAllocator.release("NetworkCapacity", null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test011() throws Exception {
        String t = "011";
        log.info("============== release " + t + " ================================");
        log.info("=== Test release - disconnect - both active and pending in DB");

        String service1 = "release" + t + "/service1";

        dataSetup.cleanup();
        dataSetup.setupVpePort("MTSNJA4LCP1", "mtanjrsv126", "ae0", "PROV", "juniper-vpe-image");
        dataSetup.setupVplspePort("MTSNJA4LCP1", "mtsnj303vr1", "xe-0/0/2", "PROV", null);
        dataSetup.setupService(service1, "Active", 2, 200000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");
        dataSetup.setupService(service1, "Pending", 3, 400000, "mtanjrsv126", "mtsnj303vr1", "MTSNJA4LCP1/Server1");

        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Active", 2, 200000));
        Assert.assertTrue(dataSetup.serviceCorrectInDb(service1, "Pending", 3, 400000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Disconnect");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        QueryStatus st = resourceAllocator.release("NetworkCapacity", null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test012() throws Exception {
        String t = "012";
        log.info("============== release " + t + " ================================");
        log.info("=== Test release - disconnect - nothing in DB - should return success");

        String service1 = "release" + t + "/service1";

        dataSetup.cleanup();

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Disconnect");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        QueryStatus st = resourceAllocator.release("NetworkCapacity", null, ctx);

        log.info("Result: " + st);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.serviceNotInDb(service1, null, null));
    }

    @Test
    public void test013() throws Exception {
        String t = "013";
        log.info("============== release " + t + " ================================");
        log.info("=== Test input validations - request-type missing in input");

        String service1 = "release" + t + "/service1";

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        try {
            resourceAllocator.release("NetworkCapacity", null, ctx);
        } catch (SvcLogicException e) {
            Assert.assertTrue(e.getMessage().equals(
                    "tmp.resource-allocator.request-type is required in ResourceAllocator"));
            return;
        }
        Assert.fail("SvcLogicException expected");
    }

    @Test
    public void test014() throws Exception {
        String t = "014";
        log.info("============== release " + t + " ================================");
        log.info("=== Test input validations - invalid request-type in input");

        String service1 = "release" + t + "/service1";

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "xxxxx");
        ctx.setAttribute("tmp.resource-allocator.service-instance-id", service1);

        try {
            resourceAllocator.release("NetworkCapacity", null, ctx);
        } catch (SvcLogicException e) {
            Assert.assertTrue(e.getMessage().equals(
                    "Invalid tmp.resource-allocator.request-type: xxxxx. Supported values are Cancel, Activate, Disconnect."));
            return;
        }
        Assert.fail("SvcLogicException expected");
    }

    @Test
    public void test015() throws Exception {
        String t = "015";
        log.info("============== release " + t + " ================================");
        log.info("=== Test input validations - missing service-instance-id in input");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("tmp.resource-allocator.request-type", "Cancel");

        try {
            resourceAllocator.release("NetworkCapacity", null, ctx);
        } catch (SvcLogicException e) {
            Assert.assertTrue(e.getMessage().equals(
                    "tmp.resource-allocator.service-instance-id is required in ResourceAllocator"));
            return;
        }
        Assert.fail("SvcLogicException expected");
    }
}
