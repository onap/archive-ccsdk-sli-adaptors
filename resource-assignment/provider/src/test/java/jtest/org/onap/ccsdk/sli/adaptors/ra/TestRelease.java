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
public class TestRelease {

    private static final Logger log = LoggerFactory.getLogger(TestRelease.class);

    @Autowired(required = true)
    private ResourceAllocator resourceAllocator;

    @Autowired(required = true)
    private DataSetup dataSetup;

    private void setupResourceData() {
        dataSetup.cleanup();

        dataSetup.setupRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-1::1", "EVC::TEST-1", "1");

        dataSetup.setupRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-2::1", "EVC::TEST-2", "2");
        dataSetup.setupRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-2::2", "EVC::TEST-2", "2");

        dataSetup.setupRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::1", "EVC::TEST-3", "3");
        dataSetup.setupRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::2", "EVC::TEST-3", "4");

        dataSetup.setupRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::1", "EVC::TEST-4", "5");
        dataSetup.setupRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::2", "EVC::TEST-4", "5");
        dataSetup.setupRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::3", "EVC::TEST-4", "6");

        dataSetup.setupRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-1::1", "EVC::TEST-1", "1");

        dataSetup.setupRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::1", "EVC::TEST-3", "3");
        dataSetup.setupRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::2", "EVC::TEST-3", "4");

        dataSetup.setupRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-5::1", "EVC::TEST-5", "5");

        dataSetup.setupRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-6::1", "EVC::TEST-6", "6-20");

        dataSetup.setupLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-1::1", "EVC::TEST-1", 100);

        dataSetup.setupLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-2::1", "EVC::TEST-2", 200);
        dataSetup.setupLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-2::2", "EVC::TEST-2", 200);

        dataSetup.setupLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::1", "EVC::TEST-3", 300);
        dataSetup.setupLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::2", "EVC::TEST-3", 400);

        dataSetup.setupLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::1", "EVC::TEST-4", 500);
        dataSetup.setupLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::2", "EVC::TEST-4", 500);
        dataSetup.setupLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::3", "EVC::TEST-4", 600);

        dataSetup.setupLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-1::1", "EVC::TEST-1", 100);

        dataSetup.setupLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::1", "EVC::TEST-3", 300);
        dataSetup.setupLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::2", "EVC::TEST-3", 400);

        dataSetup.setupLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-5::1", "EVC::TEST-5", 500);

        dataSetup.setupLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-6::1", "EVC::TEST-6", 1000);
    }

    @Test
    public void test001() throws Exception {

        String t = "001";
        log.info("============== release node " + t + " ================================");
        log.info("=== Test release - with resource set");

        setupResourceData();

        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::1", "5"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::2", "5"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::3", "6"));

        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::1", 500));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::2", 500));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::3", 600));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-entity-type", "EVC");
        ctx.setAttribute("ra-input.resource-entity-id", "TEST-4");
        ctx.setAttribute("ra-input.resource-entity-version", "2");

        QueryStatus st = resourceAllocator.release("NETWORK-CAPACITY", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::1", "5"));
        Assert.assertFalse(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::2", "5"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::3", "6"));

        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::1", 500));
        Assert.assertFalse(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::2", 500));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::3", 600));
    }

    @Test
    public void test002() throws Exception {

        String t = "002";
        log.info("============== query node " + t + " ================================");
        log.info("=== Test release - with resource union");

        setupResourceData();

        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::1", "5"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::2", "5"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::3", "6"));

        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::1", 500));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::2", 500));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::3", 600));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-entity-type", "EVC");
        ctx.setAttribute("ra-input.resource-entity-id", "TEST-4");

        QueryStatus st = resourceAllocator.release("NETWORK-CAPACITY", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertFalse(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::1", "5"));
        Assert.assertFalse(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::2", "5"));
        Assert.assertFalse(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-4::3", "6"));

        Assert.assertFalse(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::1", 500));
        Assert.assertFalse(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::2", 500));
        Assert.assertFalse(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-4::3", 600));
    }

    @Test
    public void test003() throws Exception {

        String t = "003";
        log.info("============== release node " + t + " ================================");
        log.info("=== Test release - with resource set on 2 ports");

        setupResourceData();

        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::1", "3"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::2", "4"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::1", "3"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::2", "4"));

        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::1", 300));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::2", 400));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::1", 300));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::2", 400));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-entity-type", "EVC");
        ctx.setAttribute("ra-input.resource-entity-id", "TEST-3");
        ctx.setAttribute("ra-input.resource-entity-version", "1");

        QueryStatus st = resourceAllocator.release("NETWORK-CAPACITY", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertFalse(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::1", "3"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::2", "4"));
        Assert.assertFalse(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::1", "3"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::2", "4"));

        Assert.assertFalse(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::1", 300));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::2", 400));
        Assert.assertFalse(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::1", 300));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::2", 400));
    }

    @Test
    public void test004() throws Exception {

        String t = "004";
        log.info("============== release node " + t + " ================================");
        log.info("=== Test release - with resource union on 2 ports");

        setupResourceData();

        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::1", "3"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::2", "4"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::1", "3"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::2", "4"));

        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::1", 300));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::2", 400));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::1", 300));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::2", 400));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-entity-type", "EVC");
        ctx.setAttribute("ra-input.resource-entity-id", "TEST-3");

        QueryStatus st = resourceAllocator.release("NETWORK-CAPACITY", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertFalse(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::1", "3"));
        Assert.assertFalse(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::2", "4"));
        Assert.assertFalse(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::1", "3"));
        Assert.assertFalse(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::2", "4"));

        Assert.assertFalse(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::1", 300));
        Assert.assertFalse(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::2", 400));
        Assert.assertFalse(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::1", 300));
        Assert.assertFalse(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::2", 400));
    }

    @Test
    public void test005() throws Exception {

        String t = "005";
        log.info("============== release node " + t + " ================================");
        log.info("=== Test release - with resource set and asset");

        setupResourceData();

        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::1", "3"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::2", "4"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::1", "3"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::2", "4"));

        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::1", 300));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::2", 400));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::1", 300));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::2", 400));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-entity-type", "EVC");
        ctx.setAttribute("ra-input.resource-entity-id", "TEST-3");
        ctx.setAttribute("ra-input.resource-entity-version", "1");

        ctx.setAttribute("ra-input.resource-target-type", "Port");
        ctx.setAttribute("ra-input.resource-target-id", "TESTPORT-1");

        QueryStatus st = resourceAllocator.release("NETWORK-CAPACITY", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertFalse(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::1", "3"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::2", "4"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::1", "3"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::2", "4"));

        Assert.assertFalse(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::1", 300));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::2", 400));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::1", 300));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::2", 400));
    }

    @Test
    public void test006() throws Exception {

        String t = "006";
        log.info("============== release node " + t + " ================================");
        log.info("=== Test release - with resource union on 2 ports");

        setupResourceData();

        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::1", "3"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::2", "4"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::1", "3"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::2", "4"));

        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::1", 300));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::2", 400));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::1", 300));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::2", 400));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-entity-type", "EVC");
        ctx.setAttribute("ra-input.resource-entity-id", "TEST-3");

        ctx.setAttribute("ra-input.resource-target-type", "Port");
        ctx.setAttribute("ra-input.resource-target-id", "TESTPORT-1");

        QueryStatus st = resourceAllocator.release("NETWORK-CAPACITY", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertFalse(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::1", "3"));
        Assert.assertFalse(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-1", "EVC::TEST-3::2", "4"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::1", "3"));
        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-3::2", "4"));

        Assert.assertFalse(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::1", 300));
        Assert.assertFalse(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-1", "EVC::TEST-3::2", 400));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::1", 300));
        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-3::2", 400));
    }

    @Test
    public void test007() throws Exception {

        String t = "007";
        log.info("============== release node " + t + " ================================");
        log.info("=== Test release - partial release of range");

        setupResourceData();

        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-6::1", "6-20"));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-entity-type", "EVC");
        ctx.setAttribute("ra-input.resource-entity-id", "TEST-6");
        ctx.setAttribute("ra-input.resource-entity-version", "1");

        ctx.setAttribute("ra-input.resource-target-type", "Port");
        ctx.setAttribute("ra-input.resource-target-id", "TESTPORT-2");

        ctx.setAttribute("ra-input.resource-name", "test-range-1");
        ctx.setAttribute("ra-input.range-release-numbers", "7,9,15-17");

        QueryStatus st = resourceAllocator.release("NETWORK-CAPACITY", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-6::1", "6,8,10-14,18-20"));
    }

    @Test
    public void test008() throws Exception {

        String t = "008";
        log.info("============== release node " + t + " ================================");
        log.info("=== Test release - partial release of range, but release all numbers");

        setupResourceData();

        Assert.assertTrue(dataSetup.checkRangeItem("test-range-1", "Port::TESTPORT-2", "EVC::TEST-6::1", "6-20"));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-entity-type", "EVC");
        ctx.setAttribute("ra-input.resource-entity-id", "TEST-6");
        ctx.setAttribute("ra-input.resource-entity-version", "1");

        ctx.setAttribute("ra-input.resource-target-type", "Port");
        ctx.setAttribute("ra-input.resource-target-id", "TESTPORT-2");

        ctx.setAttribute("ra-input.resource-name", "test-range-1");
        ctx.setAttribute("ra-input.range-release-numbers", "6-25");

        QueryStatus st = resourceAllocator.release("NETWORK-CAPACITY", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertTrue(dataSetup.checkItemNotThere("test-range-1", "Port::TESTPORT-2", "EVC::TEST-6::1"));
    }

    @Test
    public void test009() throws Exception {

        String t = "009";
        log.info("============== release node " + t + " ================================");
        log.info("=== Test release - partial release of limit");

        setupResourceData();

        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-6::1", 1000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-entity-type", "EVC");
        ctx.setAttribute("ra-input.resource-entity-id", "TEST-6");
        ctx.setAttribute("ra-input.resource-entity-version", "1");

        ctx.setAttribute("ra-input.resource-target-type", "Port");
        ctx.setAttribute("ra-input.resource-target-id", "TESTPORT-2");

        ctx.setAttribute("ra-input.resource-name", "test-limit-1");
        ctx.setAttribute("ra-input.limit-release-amount", "200");

        QueryStatus st = resourceAllocator.release("NETWORK-CAPACITY", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-6::1", 800));
    }

    @Test
    public void test010() throws Exception {

        String t = "010";
        log.info("============== release node " + t + " ================================");
        log.info("=== Test release - partial release of limit, but release big number");

        setupResourceData();

        Assert.assertTrue(dataSetup.checkLimitItem("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-6::1", 1000));

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.resource-entity-type", "EVC");
        ctx.setAttribute("ra-input.resource-entity-id", "TEST-6");
        ctx.setAttribute("ra-input.resource-entity-version", "1");

        ctx.setAttribute("ra-input.resource-target-type", "Port");
        ctx.setAttribute("ra-input.resource-target-id", "TESTPORT-2");

        ctx.setAttribute("ra-input.resource-name", "test-limit-1");
        ctx.setAttribute("ra-input.limit-release-amount", "2000");

        QueryStatus st = resourceAllocator.release("NETWORK-CAPACITY", null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        Assert.assertTrue(dataSetup.checkItemNotThere("test-limit-1", "Port::TESTPORT-2", "EVC::TEST-6::1"));
    }
}
