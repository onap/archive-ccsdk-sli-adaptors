package org.onap.ccsdk.sli.adaptors.aai.data.notify;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class NotifyEventTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public void test01() {
        String testString = "test-value";
        NotifyEvent event = new NotifyEvent();
        event.setAdditionalProperty("test-key", testString);
        assertEquals(event.getAdditionalProperties().get("test-key"), testString);
    }

    @Test
    public void test02() {
        String testString = "test-value";
        NotifyEvent event = new NotifyEvent();
        event.setSelflink(testString);
        assertEquals(event.getSelflink(), testString);
    }

    @Test
    public void test03() {
        String testString = "test-value";
        NotifyEvent event = new NotifyEvent();
        event.setEventId(testString);
        assertEquals(event.getEventId(), testString);
    }

    @Test
    public void test04() {
        String testString = "test-value";
        NotifyEvent event = new NotifyEvent();
        event.setEventTrigger(testString);
        assertEquals(event.getEventTrigger(), testString);
    }

    @Test
    public void test05() {
        String testString = "test-value";
        NotifyEvent event = new NotifyEvent();
        event.setNodeType(testString);
        assertEquals(event.getNodeType(), testString);
    }
}
