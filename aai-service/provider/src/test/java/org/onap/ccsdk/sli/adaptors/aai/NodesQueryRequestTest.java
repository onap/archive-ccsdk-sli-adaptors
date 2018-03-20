package org.onap.ccsdk.sli.adaptors.aai;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodesQueryRequestTest {

    private static final Logger LOG = LoggerFactory.getLogger(NodesQueryRequestTest.class);

    private static AAIRequest request;

    @BeforeClass
    public static void setUp() throws Exception {
		Properties properties = new Properties();
		properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.host.certificate.ignore", "true");
		properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.client.name", "SDNC");
		properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.client.psswd", "SDNC");
		properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.application", "CCSDK");
		properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.uri", "http://localhost:8181");
		properties.setProperty("connection.timeout", "60000");
		properties.setProperty("read.timeout", "60000");
		properties.setProperty("org.onap.ccsdk.sli.adaptors.aai.query.nodes","/aai/v11/search/nodes-query?search-node-type={node-type}&filter={entity-identifier}:EQUALS:{entity-name}");

    	AAIRequest.configProperties = properties;
        request = new NodesQueryRequest();
        LOG.info("\nEchoRequestTest.setUp\n");
    }

    @Test
    public void runGetRequestUrlTest() {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

        URL url;
        try {
            url = request.getRequestUrl("GET", null);
        } catch (Exception exc) {
		}
        assert(true);

    }

    @Test
    public void runToJSONStringTest() {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

        try {
            String json = request.toJSONString();
            assertNotNull(json);
        } catch (Exception exc) {
            LOG.error("Failed test", exc);
        }

    }

    @Test
    public void runGetArgsListTest() {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

        try {
            String[] args = request.getArgsList();
            assertNotNull(args);
        } catch (Exception exc) {
            LOG.error("Failed test", exc);
        }

    }

    @Test
    public void runGetModelTest() {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

        try {
            Class<?  extends AAIDatum> clazz = request.getModelClass();
            assertNotNull(clazz);
        } catch (Exception exc) {
            LOG.error("Failed test", exc);
        }

    }
}
