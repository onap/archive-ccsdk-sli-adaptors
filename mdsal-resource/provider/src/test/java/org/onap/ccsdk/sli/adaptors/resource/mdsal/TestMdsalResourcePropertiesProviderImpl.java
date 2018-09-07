package org.onap.ccsdk.sli.adaptors.resource.mdsal;

import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TestMdsalResourcePropertiesProviderImpl {

    MdsalResourcePropertiesProviderImpl mdsal;

    @Before
    public void setup() {
        mdsal = new MdsalResourcePropertiesProviderImpl();
    }


    @Test
    public void testGetProperties() {
        Properties prop = mdsal.getProperties();

        System.out.println("All Default Properties : " + prop);

        assertEquals("localhost",prop.getProperty("org.onap.ccsdk.sli.adaptors.resource.mdsal.sdnc-host"));
        assertEquals("Kp8bJ4SXszM0WXlhak3eHlcse2gAw84vaoGGmJvUy2U",prop.getProperty("org.onap.ccsdk.sli.adaptors.resource.mdsal.sdnc-passwd"));
        assertEquals("http",prop.getProperty("org.onap.ccsdk.sli.adaptors.resource.mdsal.sdnc-protocol"));
        assertEquals("8181",prop.getProperty("org.onap.ccsdk.sli.adaptors.resource.mdsal.sdnc-port"));
        assertEquals("admin",prop.getProperty("org.onap.ccsdk.sli.adaptors.resource.mdsal.sdnc-user"));
    }


    @Test
    public void testGetPropertie() throws IllegalStateException, IllegalArgumentException {
        MdsalResourcePropertiesProviderImpl test = new MdsalResourcePropertiesProviderImpl();

        mdsal.determinePropertiesFile(test);

    }
}
