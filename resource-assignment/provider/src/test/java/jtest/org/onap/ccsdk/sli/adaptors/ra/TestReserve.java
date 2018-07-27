package jtest.org.onap.ccsdk.sli.adaptors.ra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.sli.adaptors.ra.ResourceAllocator;
import org.onap.ccsdk.sli.adaptors.ra.comp.ResourceEntity;
import org.onap.ccsdk.sli.adaptors.ra.comp.ResourceRequest;
import org.onap.ccsdk.sli.adaptors.ra.comp.ResourceResponse;
import org.onap.ccsdk.sli.adaptors.ra.comp.ResourceTarget;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationStatus;
import org.onap.ccsdk.sli.adaptors.util.str.StrUtil;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import jtest.util.org.onap.ccsdk.sli.adaptors.ra.TestTable;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestReserve {

    private static final Logger log = LoggerFactory.getLogger(TestReserve.class);

    private JdbcTemplate jdbcTemplate;

    private static final String[] RESOURCE_COLUMNS = {"asset_id", "resource_name", "resource_type", "lt_used"};

    private static final String[] ALLOCATION_ITEM_COLUMNS = {"resource_id", "application_id", "resource_set_id",
            "resource_union_id", "resource_share_group_list", "lt_used", "allocation_time"};


    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Autowired(required = true)
    private ResourceAllocator resourceAllocator;

    /*
     * @Autowired(required = true) private ResourceAllocatorApi resourceAllocatorApi;
     */

    @Autowired(required = true)
    private DataSetup dataSetup;

    @Test
    public void test001() throws Exception {
        String t = "001";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test successful response - new start - all resources available");

        // String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();

        TestTable resource = new TestTable(jdbcTemplate, "RESOURCE", "resource_id", RESOURCE_COLUMNS);
        TestTable allocationItem =
                new TestTable(jdbcTemplate, "ALLOCATION_ITEM", "allocation_item_id", ALLOCATION_ITEM_COLUMNS);


        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.service-model", "ADIG");
        ctx.setAttribute("ra-input.check-only", "false");
        ctx.setAttribute("ra-input.reservation-entity-type", "SI");
        ctx.setAttribute("ra-input.reservation-entity-id", "ICOREPVCID-123456");
        ctx.setAttribute("ra-input.reservation-entity-data.service-speed", "100");
        ctx.setAttribute("ra-input.reservation-entity-data.service-speed-unit", "Mbps");

        ctx.setAttribute("ra-input.reservation-target-data.vnf-type", "VPE");
        ctx.setAttribute("ra-input.reservation-target-data.vpe-name", "mdt300vpe54");
        ctx.setAttribute("ra-input.reservation-target-id", "mdt300vpe54");
        ctx.setAttribute("ra-input.reservation-target-type", "VNF");

        ctx.setAttribute("ra-input.reservation-target-data.max-vpe-bandwidth-mbps", "5000");


        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        resource.print();
        allocationItem.print();

        ctx.setAttribute("ra-input.service-model", "ADIG");
        ctx.setAttribute("ra-input.check-only", "false");
        ctx.setAttribute("ra-input.reservation-entity-type", "SI");
        ctx.setAttribute("ra-input.reservation-entity-id", "ICOREPVCID-123456");
        ctx.setAttribute("ra-input.reservation-entity-data.service-speed", "100");
        ctx.setAttribute("ra-input.reservation-entity-data.service-speed-unit", "Mbps");

        ctx.setAttribute("ra-input.reservation-target-data.service-speed", "100");
        ctx.setAttribute("ra-input.reservation-target-data.service-speed-unit", "Mbps");
        ctx.setAttribute("ra-input.reservation-target-id", "ICORESITEID-123456");
        ctx.setAttribute("ra-input.reservation-target-type", "Port");


        st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        ctx.setAttribute("ra-input.service-model", "ADIG");
        ctx.setAttribute("ra-input.check-only", "false");
        ctx.setAttribute("ra-input.reservation-entity-type", "SI");
        ctx.setAttribute("ra-input.reservation-entity-id", "ICOREPVCID-123456");
        ctx.setAttribute("ra-input.reservation-entity-data.service-speed", "100");
        ctx.setAttribute("ra-input.reservation-entity-data.service-speed-unit", "Mbps");

        ctx.setAttribute("ra-input.reservation-target-data.vnf-type", "VPE");
        ctx.setAttribute("ra-input.reservation-target-data.vpe-name", "mdt300vpe54");
        ctx.setAttribute("ra-input.reservation-target-id", "mdt300vpe54");
        ctx.setAttribute("ra-input.reservation-target-type", "AffinityLink");


        st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        resource.print();
        allocationItem.print();


        /* Query Using ReservationEntityId using ServiceLogicContext */
        ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.service-model", "ADIG");
        ctx.setAttribute("ra-input.reservation-entity-id", "ICOREPVCID-123456");
        ctx.setAttribute("ra-input.reservation-entity-type", "SI");


        st = resourceAllocator.query("NetworkCapacity", false, null, null, null, null, ctx);
        Assert.assertTrue(st == QueryStatus.SUCCESS);


        /* Query Using ReservationTargetId using ServiceLogicContext */
        ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.service-model", "ADIG");
        ctx.setAttribute("ra-input.reservation-target-id", "ICORESITEID-123456");
        ctx.setAttribute("ra-input.reservation-target-type", "Port");
        ctx.setAttribute("ra-input.resource-name", "Bandwidth");

        st = resourceAllocator.query("NetworkCapacity", false, null, null, null, null, ctx);
        Assert.assertTrue(st == QueryStatus.SUCCESS);

        log.info("========================  Query Using ResourceEntity==============================");
        /* Query Using ResourceEntity bean */
        ResourceEntity sd = new ResourceEntity();
        sd.resourceEntityId = "ICOREPVCID-123456";
        sd.resourceEntityType = "SI";


        ResourceRequest rr = new ResourceRequest();
        rr.serviceModel = "ADIG";
        rr.resourceName = "cust-vlan-id";
        rr.requestType = "New";
        rr.rangeMaxOverride = -1;
        rr.rangeMinOverride = -1;

        List<ResourceResponse> rsList = new ArrayList<>();
        resourceAllocator.query(sd, null, null, rsList);

        rsList.forEach(r -> {
            StrUtil.info(log, r);
        });

        log.info("========================  release Using ResourceEntity==============================");
        rsList = new ArrayList<>();
        AllocationStatus status = resourceAllocator.release(sd);
        Assert.assertTrue(status == AllocationStatus.Success);


        log.info("========================  Query Using ResourceEntity==============================");
        rsList = new ArrayList<>();
        resourceAllocator.query(sd, null, null, rsList);


        rsList.forEach(r -> {
            StrUtil.info(log, r);
        });

    }

    @Test
    public void test002() throws Exception {
        String t = "002";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test successful response - new start - all resources available");

        Map<String, String> data = new HashMap<>();
        data.put("service-speed", "100");
        data.put("service-speed-unit", "Mbps");

        ResourceEntity sd = new ResourceEntity();
        sd.resourceEntityId = "ICOREPVCID-123456";
        sd.resourceEntityType = "SI";
        sd.data = data;

        data = new HashMap<>();
        data.put("vnf-type", "VPE");
        data.put("vpe-name", "mdt300vpe54");
        data.put("max-vpe-bandwidth-mbps", "5000");

        ResourceTarget rt = new ResourceTarget();
        rt.resourceTargetId = "mdt300vpe54";
        rt.resourceTargetType = "VNF";
        rt.data = data;

        ResourceRequest rr = new ResourceRequest();
        rr.serviceModel = "ADIG";
        // rr.resourceName = "cust-vlan-id";
        rr.requestType = "New";
        rr.rangeMaxOverride = -1;
        rr.rangeMinOverride = -1;

        List<ResourceResponse> rsList = new ArrayList<>();

        resourceAllocator.reserve(sd, rt, rr, rsList);

        rsList.forEach(r -> {
            StrUtil.info(log, r);
        });

        log.info("========================  Query + t ==============================");
        rsList = new ArrayList<>();
        resourceAllocator.query(sd, null, rr, rsList);

        rsList.forEach(r -> {
            StrUtil.info(log, r);
        });

    }


    @Test
    public void test003() throws Exception {
        String t = "003";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test successful response - new start - all resources available");

        ResourceEntity sd = new ResourceEntity();
        sd.resourceEntityId = "gblond2003me6";
        sd.resourceEntityType = "VNF";

        ResourceTarget rt = new ResourceTarget();
        rt.resourceTargetId = "MDTWNJ21A5";
        rt.resourceTargetType = "Site";


        ResourceRequest rr = new ResourceRequest();
        rr.serviceModel = "MY-SERV-MODEL";
        rr.resourceName = "VPE-Cust";
        // rr.requestType = "New";
        // rr.rangeMaxOverride = 5;
        // rr.rangeMinOverride = 5;

        List<ResourceResponse> rsList = new ArrayList<>();
        resourceAllocator.reserve(sd, rt, rr, rsList);

        rsList.forEach(r -> {
            StrUtil.info(log, r);
        });

        log.info("========================  Query + t ==============================");
        rsList = new ArrayList<>();
        resourceAllocator.query(sd, null, rr, rsList);

        rsList.forEach(r -> {
            StrUtil.info(log, r);
        });

    }



    @Test
    public void test004() throws Exception {
        String t = "004";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test successful response - new start - all resources available");

        ResourceEntity sd = new ResourceEntity();
        sd.resourceEntityId = "gblond2003me6";
        sd.resourceEntityType = "VNF";

        ResourceTarget rt = new ResourceTarget();
        rt.resourceTargetId = "MDTWNJ21A5";
        rt.resourceTargetType = "Site";

        List<ResourceRequest> rrs = new ArrayList<>();
        ResourceRequest rr = new ResourceRequest();
        rr.serviceModel = "MY-SERV-MODEL";
        rr.resourceName = "VPE-Cust";
        rrs.add(rr);

        rr = new ResourceRequest();
        rr.serviceModel = "MY-SERV-MODEL";
        rr.resourceName = "VPE-Core1";
        rrs.add(rr);

        rr = new ResourceRequest();
        rr.serviceModel = "MY-SERV-MODEL";
        rr.resourceName = "VPE-Core2";
        rrs.add(rr);



        List<ResourceResponse> rsList = new ArrayList<>();
        // resourceAllocator.reserve(sd, rt, rrs, rsList);

        rsList.forEach(r -> {
            StrUtil.info(log, r);
        });

        log.info("========================  Query + t ==============================");
        rsList = new ArrayList<>();
        resourceAllocator.query(sd, null, rr, rsList);

        rsList.forEach(r -> {
            StrUtil.info(log, r);
        });

    }


    @Test
    public void test005() throws Exception {
        String t = "005";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test successful response - new start - all resources available");

        // String service1 = "reserve" + t + "/service1";

        dataSetup.cleanup();

        TestTable resource = new TestTable(jdbcTemplate, "RESOURCE", "resource_id", RESOURCE_COLUMNS);
        TestTable allocationItem =
                new TestTable(jdbcTemplate, "ALLOCATION_ITEM", "allocation_item_id", ALLOCATION_ITEM_COLUMNS);


        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.service-model", "MY-SERV-MODEL-1");
        ctx.setAttribute("ra-input.check-only", "false");
        ctx.setAttribute("ra-input.reservation-entity-type", "VPE-Cust");
        ctx.setAttribute("ra-input.reservation-entity-id", "gblond2003me6");

        ctx.setAttribute("ra-input.reservation-target-id", "MDTWNJ21A5");
        ctx.setAttribute("ra-input.reservation-target-type", "Site");

        ctx.setAttribute("ra-input.resource-name", "cust-vlan-id");


        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        resource.print();
        allocationItem.print();

        ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.service-model", "MY-SERV-MODEL-1");
        ctx.setAttribute("ra-input.check-only", "false");
        ctx.setAttribute("ra-input.reservation-entity-type", "VPE-Core1");
        ctx.setAttribute("ra-input.reservation-entity-id", "gblond2003me6");

        ctx.setAttribute("ra-input.reservation-target-id", "MDTWNJ21A5");
        ctx.setAttribute("ra-input.reservation-target-type", "Site");

        ctx.setAttribute("ra-input.resource-name", "vlan-id-inner");


        st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        resource.print();
        allocationItem.print();

        ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.service-model", "MY-SERV-MODEL-1");
        ctx.setAttribute("ra-input.check-only", "false");
        ctx.setAttribute("ra-input.reservation-entity-type", "VPE-Core2");
        ctx.setAttribute("ra-input.reservation-entity-id", "gblond2003me6");

        ctx.setAttribute("ra-input.reservation-target-id", "MDTWNJ21A5");
        ctx.setAttribute("ra-input.reservation-target-type", "Site");

        ctx.setAttribute("ra-input.resource-name", "vlan-id-inner");
        ctx.setAttribute("ra-input.replace", "false");


        st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);

        resource.print();
        allocationItem.print();


        /* Query Using ReservationEntityId using ServiceLogicContext */
        ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.service-model", "MY-SERV-MODEL-1");
        ctx.setAttribute("ra-input.reservation-entity-id", "gblond2003me6");
        ctx.setAttribute("ra-input.reservation-entity-type", "VPE-Core1");


        st = resourceAllocator.query("NetworkCapacity", false, null, null, null, null, ctx);
        Assert.assertTrue(st == QueryStatus.SUCCESS);


        /* Query Using ReservationTargetId using ServiceLogicContext */
        ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.service-model", "MY-SERV-MODEL-1");
        ctx.setAttribute("ra-input.reservation-target-id", "MDTWNJ21A5");
        ctx.setAttribute("ra-input.reservation-target-type", "Site");
        ctx.setAttribute("ra-input.resource-name", "vlan-id-inner");

        st = resourceAllocator.query("NetworkCapacity", false, null, null, null, null, ctx);
        Assert.assertTrue(st == QueryStatus.SUCCESS);

        log.info("========================  Query Using ResourceEntity==============================");
        /* Query Using ResourceEntity bean */
        ResourceEntity sd = new ResourceEntity();
        sd.resourceEntityId = "gblond2003me6";
        sd.resourceEntityType = "VPE-Core1";


        ResourceRequest rr = new ResourceRequest();
        rr.serviceModel = "MY-SERV-MODEL-1";
        rr.resourceName = "vlan-id-inner";
        rr.requestType = "New";
        rr.rangeMaxOverride = -1;
        rr.rangeMinOverride = -1;

        List<ResourceResponse> rsList = new ArrayList<>();
        resourceAllocator.query(sd, null, null, rsList);

        rsList.forEach(r -> {
            StrUtil.info(log, r);
        });

        /*
         * log.info("========================  release Using ResourceEntity==============================");
         * rsList = new ArrayList<ResourceResponse>(); AllocationStatus status =
         * resourceAllocator.release(sd); Assert.assertTrue(status == AllocationStatus.Success);
         *
         *
         * log.info("========================  Query Using ResourceEntity==============================");
         * rsList = new ArrayList<ResourceResponse>(); resourceAllocator.query(sd, null, null, rsList);
         *
         *
         * rsList.forEach(r -> { StrUtil.info(log, r); });
         */

    }



    @Test
    public void test006() throws Exception {
        String t = "006";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test successful response - new start - all resources available");

        ResourceEntity sd = new ResourceEntity();
        sd.resourceEntityId = "gblond2003me6";
        sd.resourceEntityType = "VPE-Cust";

        ResourceTarget rt = new ResourceTarget();
        rt.resourceTargetId = "MDTWNJ21A5";
        rt.resourceTargetType = "Site";


        ResourceRequest rr = new ResourceRequest();
        rr.serviceModel = "MY-SERV-MODEL-1";
        rr.resourceName = "cust-vlan-id";


        List<ResourceResponse> rsList = new ArrayList<>();
        resourceAllocator.reserve(sd, rt, rr, rsList);

        rsList.forEach(r -> {
            StrUtil.info(log, r);
        });

        log.info("========================  Query + t ==============================");
        rsList = new ArrayList<>();
        resourceAllocator.query(sd, null, rr, rsList);

        rsList.forEach(r -> {
            StrUtil.info(log, r);
        });

    }

    @Test
    public void test007() throws Exception {
        String t = "007";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test successful response - new start - all resources available");

        dataSetup.cleanup();

        TestTable resource = new TestTable(jdbcTemplate, "RESOURCE", "resource_id", RESOURCE_COLUMNS);
        TestTable allocationItem =
                new TestTable(jdbcTemplate, "ALLOCATION_ITEM", "allocation_item_id", ALLOCATION_ITEM_COLUMNS);


        ResourceEntity sd = new ResourceEntity();
        sd.resourceEntityId = "gblond2003me6";
        sd.resourceEntityType = "VPE";
        sd.resourceEntityVersion = "1";

        ResourceTarget rt = new ResourceTarget();
        rt.resourceTargetId = "MDTWNJ21A5";
        rt.resourceTargetType = "Site";


        ResourceRequest rr = new ResourceRequest();
        rr.serviceModel = "MY-SERV-MODEL";
        // rr.resourceName = "vlan-id-outer";
        rr.endPointPosition = "VPE-Cust";
        rr.rangeMaxOverride = -1;
        rr.rangeMinOverride = -1;


        List<ResourceResponse> rsList = new ArrayList<>();
        resourceAllocator.reserve(sd, rt, rr, rsList);

        // VPE-Core1
        sd = new ResourceEntity();
        sd.resourceEntityId = "gblond2003me6";
        sd.resourceEntityType = "VPE";
        sd.resourceEntityVersion = "1";

        rt = new ResourceTarget();
        rt.resourceTargetId = "MDTWNJ21A5";
        rt.resourceTargetType = "Site";


        rr = new ResourceRequest();
        rr.serviceModel = "MY-SERV-MODEL";
        // rr.resourceName = "vlan-id-filter";
        rr.endPointPosition = "VPE-Core1";
        rr.rangeMaxOverride = -1;
        rr.rangeMinOverride = -1;


        rsList = new ArrayList<>();
        resourceAllocator.reserve(sd, rt, rr, rsList);


        // VPE-Core2
        sd = new ResourceEntity();
        sd.resourceEntityId = "gblond2003me6";
        sd.resourceEntityType = "VPE";
        sd.resourceEntityVersion = "1";

        rt = new ResourceTarget();
        rt.resourceTargetId = "MDTWNJ21A5";
        rt.resourceTargetType = "Site";


        rr = new ResourceRequest();
        rr.serviceModel = "MY-SERV-MODEL";
        // rr.resourceName = "vlan-id-filter";
        rr.endPointPosition = "VPE-Core2";
        rr.rangeMaxOverride = -1;
        rr.rangeMinOverride = -1;


        rsList = new ArrayList<>();
        resourceAllocator.reserve(sd, rt, rr, rsList);


        // VPE-Core3
        sd = new ResourceEntity();
        sd.resourceEntityId = "gblond2003me6";
        sd.resourceEntityType = "VPE";
        sd.resourceEntityVersion = "1";

        rt = new ResourceTarget();
        rt.resourceTargetId = "MDTWNJ21A5";
        rt.resourceTargetType = "Site";


        rr = new ResourceRequest();
        rr.serviceModel = "MY-SERV-MODEL";
        // rr.resourceName = "vlan-id-filter";
        rr.endPointPosition = "VPE-Core3";
        rr.rangeMaxOverride = -1;
        rr.rangeMinOverride = -1;


        rsList = new ArrayList<>();
        resourceAllocator.reserve(sd, rt, rr, rsList);

        rsList.forEach(r -> {
            StrUtil.info(log, r);
        });

        resource.print();
        allocationItem.print();

        log.info("========================  Query + t ==============================");
        rsList = new ArrayList<>();
        resourceAllocator.query(sd, null, rr, rsList);

        rsList.forEach(r -> {
            StrUtil.info(log, r);
        });



        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.reservation-entity-id", "gblond2003me6");
        ctx.setAttribute("ra-input.reservation-entity-type", "VPE");


        QueryStatus st = resourceAllocator.release("NetworkCapacity", "gblond2003me6", ctx);
        Assert.assertTrue(st == QueryStatus.SUCCESS);

    }

    @Test
    public void test008() throws Exception {
        String t = "008";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test range-force-new-numbers = false");

        String entityId = "reserve" + t;
        String targetId = "port-id-1";
        String resourceName = "cust-vlan-id";

        String assetId = "VNF::" + targetId;
        String resourceUnion = "SI::" + entityId;
        String resourceSet1 = resourceUnion + "::1";
        String resourceSet2 = resourceUnion + "::2";

        dataSetup.cleanup();

        dataSetup.setupRangeItem(resourceName, assetId, resourceSet1, resourceUnion, "201");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.service-model", "ADIG");
        ctx.setAttribute("ra-input.check-only", "false");

        ctx.setAttribute("ra-input.resource-name", resourceName);
        ctx.setAttribute("ra-input.range-force-new-numbers", "false");

        ctx.setAttribute("ra-input.reservation-entity-type", "SI");
        ctx.setAttribute("ra-input.reservation-entity-id", entityId);
        ctx.setAttribute("ra-input.reservation-entity-version", "2");

        ctx.setAttribute("ra-input.reservation-target-id", targetId);
        ctx.setAttribute("ra-input.reservation-target-type", "VNF");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.checkRangeItem(resourceName, assetId, resourceSet1, "201"));
        Assert.assertTrue(dataSetup.checkRangeItem(resourceName, assetId, resourceSet2, "201"));
    }

    @Test
    public void test009() throws Exception {
        String t = "009";
        log.info("============== reserve " + t + " ================================");
        log.info("=== Test range-force-new-numbers = true");

        String entityId = "reserve" + t;
        String targetId = "port-id-1";
        String resourceName = "cust-vlan-id";

        String assetId = "VNF::" + targetId;
        String resourceUnion = "SI::" + entityId;
        String resourceSet1 = resourceUnion + "::1";
        String resourceSet2 = resourceUnion + "::2";

        dataSetup.cleanup();

        dataSetup.setupRangeItem(resourceName, assetId, resourceSet1, resourceUnion, "201");

        SvcLogicContext ctx = new SvcLogicContext();
        ctx.setAttribute("ra-input.service-model", "ADIG");
        ctx.setAttribute("ra-input.check-only", "false");

        ctx.setAttribute("ra-input.resource-name", resourceName);
        ctx.setAttribute("ra-input.range-force-new-numbers", "true");

        ctx.setAttribute("ra-input.reservation-entity-type", "SI");
        ctx.setAttribute("ra-input.reservation-entity-id", entityId);
        ctx.setAttribute("ra-input.reservation-entity-version", "2");

        ctx.setAttribute("ra-input.reservation-target-id", targetId);
        ctx.setAttribute("ra-input.reservation-target-type", "VNF");

        QueryStatus st = resourceAllocator.reserve("NetworkCapacity", null, null, null, ctx);

        Assert.assertTrue(st == QueryStatus.SUCCESS);
        Assert.assertTrue(dataSetup.checkRangeItem(resourceName, assetId, resourceSet1, "201"));
        Assert.assertFalse(dataSetup.checkRangeItem(resourceName, assetId, resourceSet2, "201"));
    }
}
