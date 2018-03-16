package org.onap.ccsdk.sli.adaptors.aai.data.notify;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class KeyDatumTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public void testSetKeyName() {
        String testString = "test-value";
        KeyDatum event = new  KeyDatum();
        event.setKeyName(testString);
        assertEquals(event.getKeyName(), testString);
    }

    @Test
    public void testSetKeyValue() {
        String testString = "test-value";
        KeyDatum event = new  KeyDatum();
        event.setKeyValue(testString);
        assertEquals(event.getKeyValue(), testString);
    }

    @Test
    public void testSetAdditionalProperty() {
        String testString = "test-value";
        KeyDatum event = new  KeyDatum();
        event.setAdditionalProperty("test-key", testString);
        assertEquals(event.getAdditionalProperties().get("test-key"), testString);
    }

}
