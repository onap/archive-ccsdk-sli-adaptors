package org.onap.ccsdk.sli.adaptors.resource.sql;

import static org.junit.Assert.assertNotNull;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;

public class SqlResourceProviderTest {

    private static SqlResourcePropertiesProvider provider;
    private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";

    @Test
    public void testSqlResourceProvider() {
        try{
            Map<String, String> env = System.getenv();
            Class<?> cl = env.getClass();
            Field field = cl.getDeclaredField("m");
            field.setAccessible(true);
            Map<String, String> writableEnv = (Map<String, String>) field.get(env);
            writableEnv.put(SDNC_CONFIG_DIR, "./src/test/resources");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to set environment variable", e);
        }

        provider = new SqlResourcePropertiesProviderImpl();
        assertNotNull(provider);
    }

    @Test
    public void testGetProperties() {
        Properties properties = provider.getProperties();
        assertNotNull(properties);
    }

}
