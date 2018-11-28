package jtest.org.onap.ccsdk.sli.adaptors.ra;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.sli.adaptors.ra.ResourceAllocator;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestGetResource {

    private static final Logger log = LoggerFactory.getLogger(TestGetResource.class);

    @Autowired(required = true)
    private ResourceAllocator resourceAllocator;

    @Autowired(required = true)
    private DataSetup dataSetup;

    private void setupResourceData() {
        dataSetup.cleanup();

        String targetId = "GBLOND2025MG2";
        String assetId = "Device::" + targetId;
        String resourceName = "internal-vlan";

        for (int i = 0; i < 5; i++) {
            String entityId = "TEST" + i;

            String resourceUnion = "EVC::" + entityId;
            String resourceSet = resourceUnion + "::1";

            dataSetup.setupRangeItem(resourceName, assetId, resourceSet, resourceUnion, String.valueOf(i));
        }

        for (int i = 0; i < 5; i++) {
            String entityId = "TEST" + (i + 10);

            String resourceUnion = "EVC::SVLAN::" + entityId;
            String resourceSet = resourceUnion + "::1";

            dataSetup.setupRangeItem(resourceName, assetId, resourceSet, resourceUnion, String.valueOf(10 + i));
        }

        for (int i = 0; i < 5; i++) {
            String entityId = "TEST" + (i + 20);

            String resourceUnion = "EVC::" + entityId;
            String resourceSet = resourceUnion + "::1";
            String resourceShareGroup = "SHARE1";

            dataSetup.setupRangeItem(resourceName, assetId, resourceSet, resourceUnion, resourceShareGroup,
                    String.valueOf(20 + i));
        }

        for (int i = 0; i < 5; i++) {
            String entityId = "TEST" + (i + 30);

            String resourceUnion = "EVC::SVLAN::" + entityId;
            String resourceSet = resourceUnion + "::1";
            String resourceShareGroup = "SHARE1";

            dataSetup.setupRangeItem(resourceName, assetId, resourceSet, resourceUnion, resourceShareGroup,
                    String.valueOf(30 + i));
        }
    }

    @Test
    public void test001() throws Exception {

        String t = "001";
        log.info("============== get-resource node " + t + " ================================");
        log.info("=== Test query for resource target - no additional criteria");

        setupResourceData();

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-target-id", "GBLOND2025MG2");
        ctx.setAttribute("ra-input.resource-target-type", "Device");

        ctx.setAttribute("ra-input.resource-name", "internal-vlan");

        QueryStatus st = resourceAllocator.query("NetworkCapacity", false, null, null, "ra-output", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list_length"), "1");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-name"), "internal-vlan");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-type"), "Device");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-id"), "GBLOND2025MG2");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].allocated"),
                "0, 1, 2, 3, 4, 10, 11, 12, 13, 14, 20, 21, 22, 23, 24, 30, 31, 32, 33, 34");
    }

    @Test
    public void test002() throws Exception {

        String t = "002";
        log.info("============== get-resource node " + t + " ================================");
        log.info("=== Test query for resource target - with resource entity condition");

        setupResourceData();

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-target-id", "GBLOND2025MG2");
        ctx.setAttribute("ra-input.resource-target-type", "Device");

        ctx.setAttribute("ra-input.resource-name", "internal-vlan");

        ctx.setAttribute("ra-input.resource-entity-type-filter", "EVC");
        ctx.setAttribute("ra-input.resource-entity-id-filter", "SVLAN%");

        QueryStatus st = resourceAllocator.query("NetworkCapacity", false, null, null, "ra-output", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list_length"), "1");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-name"), "internal-vlan");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-type"), "Device");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-id"), "GBLOND2025MG2");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].allocated"),
                "10, 11, 12, 13, 14, 30, 31, 32, 33, 34");
    }

    @Test
    public void test003() throws Exception {

        String t = "003";
        log.info("============== get-resource node " + t + " ================================");
        log.info("=== Test query for resource target - with resource share group condition");

        setupResourceData();

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-target-id", "GBLOND2025MG2");
        ctx.setAttribute("ra-input.resource-target-type", "Device");

        ctx.setAttribute("ra-input.resource-name", "internal-vlan");

        ctx.setAttribute("ra-input.resource-share-group-filter", "SHARE1");

        QueryStatus st = resourceAllocator.query("NetworkCapacity", false, null, null, "ra-output", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list_length"), "1");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-name"), "internal-vlan");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-type"), "Device");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-id"), "GBLOND2025MG2");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].allocated"),
                "20, 21, 22, 23, 24, 30, 31, 32, 33, 34");
    }

    @Test
    public void test004() throws Exception {

        String t = "004";
        log.info("============== get-resource node " + t + " ================================");
        log.info("=== Test query for resource target - with resource share group condition NULL");

        setupResourceData();

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-target-id", "GBLOND2025MG2");
        ctx.setAttribute("ra-input.resource-target-type", "Device");

        ctx.setAttribute("ra-input.resource-name", "internal-vlan");

        ctx.setAttribute("ra-input.resource-share-group-filter", "null");

        QueryStatus st = resourceAllocator.query("NetworkCapacity", false, null, null, "ra-output", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list_length"), "1");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-name"), "internal-vlan");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-type"), "Device");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-id"), "GBLOND2025MG2");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].allocated"),
                "0, 1, 2, 3, 4, 10, 11, 12, 13, 14");
    }

    @Test
    public void test005() throws Exception {

        String t = "005";
        log.info("============== get-resource node " + t + " ================================");
        log.info("=== Test query for resource target - with both resource entity and resource share group conditions");

        setupResourceData();

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-target-id", "GBLOND2025MG2");
        ctx.setAttribute("ra-input.resource-target-type", "Device");

        ctx.setAttribute("ra-input.resource-name", "internal-vlan");

        ctx.setAttribute("ra-input.resource-entity-type-filter", "EVC");
        ctx.setAttribute("ra-input.resource-entity-id-filter", "SVLAN%");
        ctx.setAttribute("ra-input.resource-share-group-filter", "null");

        QueryStatus st = resourceAllocator.query("NetworkCapacity", false, null, null, "ra-output", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list_length"), "1");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-name"), "internal-vlan");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-type"), "Device");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-id"), "GBLOND2025MG2");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].allocated"), "10, 11, 12, 13, 14");
    }
}
