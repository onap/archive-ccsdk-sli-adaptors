package jtest.org.onap.ccsdk.sli.adaptors.rm.util;

import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.LabelAllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.LabelResource;
import org.onap.ccsdk.sli.adaptors.rm.util.LabelUtil;

import java.util.Date;

import static org.junit.Assert.*;

public class LabelUtilTest {

    @Test
    public void testLabelUtils() {
        LabelAllocationRequest req = new LabelAllocationRequest();
        req.check = true;
        req.allocate = true;
        req.label = "testLabel";
        req.resourceUnionId = "123";
        req.applicationId = "testApp";
        req.assetId = "asset1";
        req.resourceName = "resource1";
        req.resourceSetId = "set1";



        LabelResource resource = new LabelResource();
        resource.label = "testLabel";

        LabelUtil.allocateLabel(resource, req);
        LabelUtil.checkLabel(resource, req);
        LabelUtil.recalculate(resource);







    }


}