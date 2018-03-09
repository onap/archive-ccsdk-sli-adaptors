package org.onap.ccsdk.sli.adaptors.resource.sql;

import static org.junit.Assert.assertNotNull;

import java.util.Properties;

import org.junit.Test;

public class SqlResourceProviderTest {

    private static SqlResourcePropertiesProvider provider;

    @Test
    public void testSqlResourceProvider() {
        provider = new SqlResourcePropertiesProviderImpl();
        assertNotNull(provider);
    }

    @Test
    public void testGetProperties() {
        Properties properties = provider.getProperties();
        assertNotNull(properties);
    }

}
