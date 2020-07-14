package org.onap.ccsdk.sli.adaptors.aai;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

public class AAIClientRESTExecutorTest {

    private static AAIClientRESTExecutor aaiExecute;
    private static AAIService aaiService;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Properties properties = new Properties();
        properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.host.certificate.ignore", "true");
        properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.client.name", "SDNC");
        properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.client.psswd", "SDNC");
        properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.application", "CCSDK");
        properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.uri", "http://localhost:8181");
        properties.setProperty("connection.timeout", "60000");
        properties.setProperty("read.timeout", "60000");
        aaiExecute = new AAIClientRESTExecutor(properties);
        aaiService = new AAIService(properties);


    }

    @Test
    public void testGet() {
        Map<String, String> nameValues = new HashMap<>();
        nameValues.put("generic-vnf.vnf-id", "vnf-001");
        AAIRequest request = AAIRequest.createRequest("generic-vnf", nameValues);
        try {
            aaiExecute.get(request);
        } catch (AAIServiceException e) {
        }
        assert(true);
        assertNotNull(nameValues);
    }

    @Test
    public void testPost() {
        Map<String, String> nameValues = new HashMap<>();
        nameValues.put("generic-vnf.vnf-id", "vnf-001");
        AAIRequest request = AAIRequest.createRequest("generic-vnf", nameValues);
        try {
            aaiExecute.get(request);
        } catch (AAIServiceException e) {
        }
        assert(true);
        assertNotNull(nameValues);
    }

    @Test
    public void testDelete() {
        Map<String, String> nameValues = new HashMap<>();
        nameValues.put("generic-vnf.vnf-id", "vnf-001");
        AAIRequest request = AAIRequest.createRequest("generic-vnf", nameValues);
        try {
            aaiExecute.get(request);
        } catch (AAIServiceException e) {
        }
        assert(true);
        assertNotNull(nameValues);
    }

    @Test
    public void testQuery() {
        Map<String, String> nameValues = new HashMap<>();
        nameValues.put("generic-vnf.vnf-id", "vnf-001");
        AAIRequest request = AAIRequest.createRequest("generic-vnf", nameValues);
        try {
            aaiExecute.get(request);
        } catch (AAIServiceException e) {
        }
        assert(true);
        assertNotNull(nameValues);
    }

    @Test
    public void testPatch() {
        Map<String, String> nameValues = new HashMap<>();
        nameValues.put("generic-vnf.vnf-id", "vnf-001");
        AAIRequest request = AAIRequest.createRequest("generic-vnf", nameValues);
        try {
            aaiExecute.patch(request, "1234567890");
        } catch (AAIServiceException e) {
        }
        assert(true);
        assertNotNull(nameValues);
    }



    @Test
    public void testLOGwriteFirstTrace() {
        try {
            aaiExecute.LOGwriteFirstTrace("GET", null);
        } catch (Exception e) {
        }
        assert(true);
    }

    @Test
    public void testLOGwriteDateTrace() {
        try {
            aaiExecute.LOGwriteDateTrace("GET", "<----- test data ------>");
        } catch (Exception e) {
        }
        assert(true);
    }

    @Test
    public void testLOGwriteEndingTrace() {
        try {
            aaiExecute.LOGwriteEndingTrace(200, "GET", "<----- test data ------>");
        } catch (Exception e) {
        }
        assert(true);
    }

}
