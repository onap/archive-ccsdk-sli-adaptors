package org.onap.ccsdk.sli.adaptors.aai.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.aai.inventory.v13.RelationshipList;
import org.openecomp.aai.inventory.v13.Vlans;

public class SubInterfaceTest {
  
    SubInterface _siInstance;
    protected String _interfaceName;
    protected String _interfaceRole;
    protected String _resourceVersion;
    protected Vlans _vlans;
    protected RelationshipList _relationshipList;
    protected Map<String, Object> _additionalProperties;
  
    @Before
    public void setUp() throws Exception {
        _siInstance = new SubInterface();
        _interfaceName = "interfaceName";
        _interfaceRole = "interfaceRole";
        _resourceVersion = "resourceVersion";
        _vlans = mock(Vlans.class);
        _relationshipList = mock(RelationshipList.class);
  
        _additionalProperties = new HashMap<String, Object>() {{
            put("prop1", "propvalue1");
            put("prop2", "propvalue2");
        }};
      
    }
  
    @After
    public void tearDown() throws Exception {
        _siInstance = null;
        _interfaceName = null;
        _interfaceRole = null;
        _resourceVersion = null;
        _vlans = null;
        _relationshipList = null;
        _additionalProperties = null;
    }

    @Test
    public void testSetInterfaceName() {
        _siInstance.setInterfaceName(_interfaceName);
        assertTrue(_siInstance.getInterfaceName().equals(_interfaceName));
    }


    @Test
    public void testSetInterfaceRole() {
        _siInstance.setInterfaceRole(_interfaceRole);
        assertTrue(_siInstance.getInterfaceRole().equals(_interfaceRole));
    }


    @Test
    public void testSetResourceVersion() {
        _siInstance.setResourceVersion(_resourceVersion);
        assertTrue(_siInstance.getResourceVersion().equals(_resourceVersion));
    }


    @Test
    public void testSetVlans() {
        _siInstance.setVlans(_vlans);
        assertTrue(_siInstance.getVlans().equals(_vlans));
    }


    @Test
    public void testSetRelationshipList() {
        _siInstance.setRelationshipList(_relationshipList);
        assertTrue(_siInstance.getRelationshipList().equals(_relationshipList));
    }

    @Test
    public void testSetAdditionalProperties() {
        _siInstance.setAdditionalProperty("prop1", "propvalue1");
        _siInstance.setAdditionalProperty("prop2", "propvalue2");
        assertEquals(_siInstance.getAdditionalProperties(), _additionalProperties);
    }

}
