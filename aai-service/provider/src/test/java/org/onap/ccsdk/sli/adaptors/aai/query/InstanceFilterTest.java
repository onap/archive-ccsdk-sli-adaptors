package org.onap.ccsdk.sli.adaptors.aai.query;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openecomp.aai.inventory.v13.CloudRegion;
import org.openecomp.aai.inventory.v13.GenericVnf;
import org.openecomp.aai.inventory.v13.L3Network;
import org.openecomp.aai.inventory.v13.LogicalLink;
import org.openecomp.aai.inventory.v13.PInterface;
import org.openecomp.aai.inventory.v13.Pnf;
import org.openecomp.aai.inventory.v13.ServiceInstance;
import org.openecomp.aai.inventory.v13.Tenant;
import org.openecomp.aai.inventory.v13.Vnf;
import org.openecomp.aai.inventory.v13.Vserver;

public class InstanceFilterTest {
    
    InstanceFilter _ifInstance;
    protected LogicalLink _logicalLink;
    protected Pnf _pnf;
    protected L3Network _l3Network;
    protected PInterface _pInterface;
    protected GenericVnf _genericVnf;
    protected Vserver _vserver;
    protected Tenant _tenant;
    protected CloudRegion _cloudRegion;
    protected ServiceInstance _serviceInstance;
    protected Vnf _vnfc;
    protected Map<String, Object> _additionalProperties;

    @Before
    public void setUp() throws Exception {
        _ifInstance = new InstanceFilter();
         LogicalLink _logicalLink = mock(LogicalLink.class);
         Pnf _pnf = mock(Pnf.class);
         L3Network _l3Network = mock(L3Network.class);
         PInterface _pInterface = mock(PInterface.class);
         GenericVnf _genericVnf = mock(GenericVnf.class);
         Vserver _vserver = mock(Vserver.class);
         Tenant _tenant = mock(Tenant.class);
         CloudRegion _cloudRegion = mock(CloudRegion.class);
         ServiceInstance _serviceInstance = mock(ServiceInstance.class);
         // as per class
         Vnf _vnfc = mock(Vnf.class);
        _additionalProperties = new HashMap<String, Object>() {{
            put("prop1", "propvalue1");
            put("prop2", "propvalue2");
        }};
    }

    @After
    public void tearDown() throws Exception {
        _ifInstance = null;
        _additionalProperties = null;
    }

    @Test
    public void testSetLogicalLink() {
        _ifInstance.setLogicalLink(_logicalLink);
        assertEquals(_ifInstance.getLogicalLink(), _logicalLink);
    }

    @Test
    public void testSetPnf() {
        _ifInstance.setPnf(_pnf);
        assertEquals(_ifInstance.getPnf(), _pnf);
    }

    @Test
    public void testSetL3Network() {
        _ifInstance.setL3Network(_l3Network);
        assertEquals(_ifInstance.getL3Network(), _l3Network);
    }

    @Test
    public void testSetServiceInstance() {
        _ifInstance.setServiceInstance(_serviceInstance);
        assertEquals(_ifInstance.getServiceInstance(), _serviceInstance);
    }

    @Test
    public void testSetGenericVnf() {
        _ifInstance.setGenericVnf(_genericVnf);
        assertEquals(_ifInstance.getGenericVnf(), _genericVnf);
    }

    @Test
    public void testSetVserver() {
        _ifInstance.setVserver(_vserver);
        assertEquals(_ifInstance.getVserver(), _vserver);
    }

    @Test
    public void testSetTenant() {
        _ifInstance.setTenant(_tenant);
        assertEquals(_ifInstance.getTenant(), _tenant);
    }

    @Test
    public void testSetCloudRegion() {
        _ifInstance.setCloudRegion(_cloudRegion);
        assertEquals(_ifInstance.getCloudRegion(), _cloudRegion);
    }

    @Test
    public void testSetVnfc() {
        _ifInstance.setVnfc(_vnfc);
        assertEquals(_ifInstance.getVnfc(), _vnfc);
    }
    
    @Test
    public void testSetAdditionalProperty() {
        _ifInstance.setAdditionalProperty("prop1", "propvalue1");
        _ifInstance.setAdditionalProperty("prop2", "propvalue2");
        assertEquals(_ifInstance.getAdditionalProperties(), _additionalProperties);
    }
}
