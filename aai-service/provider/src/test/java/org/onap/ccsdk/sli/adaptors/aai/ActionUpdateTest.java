package org.onap.ccsdk.sli.adaptors.aai;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.onap.ccsdk.sli.adaptors.aai.update.Action;
import org.onap.ccsdk.sli.adaptors.aai.update.ActionDatum;
import org.junit.Test;

public class ActionUpdateTest {
    @Test
    public void getActionData() throws Exception {
        final Action resolver = new Action();
        assertNotNull(resolver.getActionData());
    }
    
    @Test
    public void setActionData() throws Exception {
        final Action resolver = new Action();
        resolver.setActionData( new ArrayList<ActionDatum>());
    }
    
    @Test
    public void setActionType() throws Exception {
        final Action resolver = new Action();
        resolver.setActionType("create");
        assertEquals("create", resolver.getActionType());
    }
    
    @Test
    public void getAdditionalProperties() throws Exception {
        final Action resolver = new Action();
        assertNotNull(resolver.getAdditionalProperties());
    }
    
    @Test
    public void setAdditionalProperty() throws Exception {
        final Action resolver = new Action();
        resolver.setAdditionalProperty("outcome", "success");
        assertEquals("success", resolver.getAdditionalProperties().get("outcome"));
    }
}