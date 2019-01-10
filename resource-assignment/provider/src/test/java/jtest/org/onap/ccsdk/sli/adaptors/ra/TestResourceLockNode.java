package jtest.org.onap.ccsdk.sli.adaptors.ra;

import java.util.HashMap;
import java.util.Map;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.sli.adaptors.ra.ResourceLockNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-context.xml" })
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestResourceLockNode {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(TestResourceLockNode.class);

    @Autowired
    private ResourceLockNode resourceLockNode;

    @Test
    public void test1() throws Exception {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("resource-name", "test-resource-1");
        paramMap.put("lock-requester", "SDNA");

        resourceLockNode.lockResource(paramMap, null);
        resourceLockNode.unlockResource(paramMap, null);
    }
}
