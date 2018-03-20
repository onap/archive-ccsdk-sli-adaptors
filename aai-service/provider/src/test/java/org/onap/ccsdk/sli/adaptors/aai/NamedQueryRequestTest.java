package org.onap.ccsdk.sli.adaptors.aai;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamedQueryRequestTest {

    private static final Logger LOG = LoggerFactory.getLogger(NamedQueryRequestTest.class);

    private static AAIRequest request;

    @BeforeClass
    public static void setUp() throws Exception {

        request = new NamedQueryRequest();
        LOG.info("\nEchoRequestTest.setUp\n");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        request = null;
        LOG.info("----------------------- EchoRequestTest.tearDown -----------------------");
    }

    @Test
    public void runGetRequestUrlTest() {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

        URL url;
        try {
            url = request.getRequestUrl("GET", null);
            assertNotNull(url);
        } catch (UnsupportedEncodingException | MalformedURLException | URISyntaxException exc) {
            LOG.error("Failed test", exc);
        }

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
