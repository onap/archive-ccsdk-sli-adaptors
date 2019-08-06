package org.onap.ccsdk.sli.adaptors.base.http;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class BasicAuthFilterTest {
    
    @Test
    public void notNullParameters() throws Exception {
        BasicAuthFilter myFilter = new BasicAuthFilter("helloworld");
        assertNotNull(myFilter);
    }
    
    @Test
    public void nullParameters() throws Exception {
        BasicAuthFilter myFilter = new BasicAuthFilter(null);
        assertNotNull(myFilter);
    }
}
