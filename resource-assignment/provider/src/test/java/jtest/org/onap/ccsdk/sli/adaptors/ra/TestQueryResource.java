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
public class TestQueryResource {

    private static final Logger log = LoggerFactory.getLogger(TestQueryResource.class);

    @Autowired(required = true)
    private ResourceAllocator resourceAllocator;

    @Autowired(required = true)
    private DataSetup dataSetup;

    private void setupResourceData() {
        dataSetup.cleanup();

        for (int k = 0; k < 6; k++) {
            String assetId = "Port::TESTPORT-" + (k / 2 + 1) + "-" + (k + 1);

            for (int i = 0; i < 5; i++) {
                String entityId = "TEST-" + i + "-" + (k / 2 + 1);

                String resourceUnion = "EVC::" + entityId;
                String resourceSet = resourceUnion + "::1";

                dataSetup.setupRangeItem("test-range-1", assetId, resourceSet, resourceUnion, String.valueOf(i));
            }
        }

        for (int k = 0; k < 6; k++) {
            String assetId = "Port::TESTPORT-" + (k / 2 + 1) + "-" + (k + 1);

            for (int i = 0; i < 5; i++) {
                String entityId = "TEST-" + i + "-" + (k / 2 + 1);

                String resourceUnion = "EVC::" + entityId;
                String resourceSet = resourceUnion + "::1";

                dataSetup.setupLimitItem("test-limit-1", assetId, resourceSet, resourceUnion, (i + 1) * 100);
            }
        }
    }

    @Test
    public void test001() throws Exception {

        String t = "001";
        log.info("============== query node " + t + " ================================");
        log.info("=== Test query for resources - with resource target condition - range");

        setupResourceData();

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-target-id-filter", "TESTPORT-1-%");
        ctx.setAttribute("ra-input.resource-target-type-filter", "Port");

        ctx.setAttribute("ra-input.resource-name", "test-range-1");

        QueryStatus st = resourceAllocator.query("NetworkCapacity", false, null, null, "ra-output", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list_length"), "2");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-name"), "test-range-1");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-type"), "Port");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-id"), "TESTPORT-1-1");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].allocated"), "0, 1, 2, 3, 4");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[1].resource-name"), "test-range-1");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[1].resource-target-type"), "Port");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[1].resource-target-id"), "TESTPORT-1-2");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[1].allocated"), "0, 1, 2, 3, 4");
    }

    @Test
    public void test002() throws Exception {

        String t = "002";
        log.info("============== query node " + t + " ================================");
        log.info("=== Test query for resources - with resource target condition - limit");

        setupResourceData();

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-target-id-filter", "TESTPORT-%-1");
        ctx.setAttribute("ra-input.resource-target-type-filter", "Port");

        ctx.setAttribute("ra-input.resource-name", "test-limit-1");

        QueryStatus st = resourceAllocator.query("NetworkCapacity", false, null, null, "ra-output", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list_length"), "1");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-name"), "test-limit-1");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-type"), "Port");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].resource-target-id"), "TESTPORT-1-1");
        Assert.assertEquals(ctx.getAttribute("ra-output.resource-list[0].allocated"), "1500");
    }
}
