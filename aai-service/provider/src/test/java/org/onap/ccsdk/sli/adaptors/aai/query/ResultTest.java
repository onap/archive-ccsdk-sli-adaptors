package org.onap.ccsdk.sli.adaptors.aai.query;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.openecomp.aai.inventory.v13.CloudRegion;
import org.openecomp.aai.inventory.v13.Complex;
import org.openecomp.aai.inventory.v13.Configuration;
import org.openecomp.aai.inventory.v13.GenericVnf;
import org.openecomp.aai.inventory.v13.L3InterfaceIpv4AddressList;
import org.openecomp.aai.inventory.v13.L3InterfaceIpv6AddressList;
import org.openecomp.aai.inventory.v13.L3Network;
import org.openecomp.aai.inventory.v13.LInterface;
//import org.openecomp.aai.inventory.v13.OwningEntity;
import org.openecomp.aai.inventory.v13.Pserver;
import org.openecomp.aai.inventory.v13.ServiceInstance;
import org.openecomp.aai.inventory.v13.Vnfc;
import org.openecomp.aai.inventory.v13.Vserver;

public class ResultTest {

    Result _rInstance;

    protected CloudRegion _cloudRegion;
    protected Complex _complex;
    protected Configuration _configuration;
    protected GenericVnf _genericVnf;
    protected L3InterfaceIpv4AddressList _l3InterfaceIpv4AddressList;
    protected L3InterfaceIpv6AddressList _l3InterfaceIpv6AddressList;
    protected L3Network _l3Network;
    protected LInterface _lInterface;
    protected Pserver _pserver;
    protected ServiceInstance _serviceInstance;
    protected Vnfc _vnfc;
    protected Vserver _vserver;

    protected Map<String, Object> _additionalProperties;

    @Before
    public void setUp() throws Exception {
        _rInstance = new Result();

         CloudRegion _cloudRegion = mock(CloudRegion.class);
         Complex _complex = mock(Complex.class);
         Configuration _configuration = mock(Configuration.class);
         L3InterfaceIpv4AddressList _l3InterfaceIpv4AddressList = mock(L3InterfaceIpv4AddressList.class);
         L3InterfaceIpv6AddressList _l3InterfaceIpv6AddressList = mock(L3InterfaceIpv6AddressList.class);
         L3Network _l3Network = mock(L3Network.class);
         LInterface _pInterface = mock(LInterface.class);
         GenericVnf _genericVnf = mock(GenericVnf.class);
         Vserver _vserver = mock(Vserver.class);
         Pserver _pserver = mock(Pserver.class);
         Vnfc _vnfc = mock(Vnfc.class);
         ServiceInstance _serviceInstance = mock(ServiceInstance.class);

        _additionalProperties = new HashMap<String, Object>() {{
            put("prop1", "propvalue1");
            put("prop2", "propvalue2");
        }};
    }

    @After
    public void tearDown() throws Exception {
        _rInstance = null;
        _additionalProperties = null;
    }

    @Test
    public void testSetComplex() {
        _rInstance.setComplex(_complex);
        assertEquals(_rInstance.getComplex(), _complex);
    }

    @Test
    public void testSetConfiguration() {
        _rInstance.setConfiguration(_configuration);
        assertEquals(_rInstance.getConfiguration(), _configuration);
    }

    @Test
    public void testSetL3InterfaceIpv4AddressList() {
        _rInstance.setL3InterfaceIpv4AddressList(_l3InterfaceIpv4AddressList);
        assertEquals(_rInstance.getL3InterfaceIpv4AddressList(), _l3InterfaceIpv4AddressList);
    }

    @Test
    public void testSetL3InterfaceIpv6AddressList() {
        _rInstance.setL3InterfaceIpv6AddressList(_l3InterfaceIpv6AddressList);
        assertEquals(_rInstance.getL3InterfaceIpv6AddressList(), _l3InterfaceIpv6AddressList);
    }

    @Test
    public void testSetL3Network() {
        _rInstance.setL3Network(_l3Network);
        assertEquals(_rInstance.getL3Network(), _l3Network);
    }

    @Test
    public void testSetServiceInstance() {
        _rInstance.setServiceInstance(_serviceInstance);
        assertEquals(_rInstance.getServiceInstance(), _serviceInstance);
    }

    @Test
    public void testSetGenericVnf() {
        _rInstance.setGenericVnf(_genericVnf);
        assertEquals(_rInstance.getGenericVnf(), _genericVnf);
    }

    @Test
    public void testSetVserver() {
        _rInstance.setVserver(_vserver);
        assertEquals(_rInstance.getVserver(), _vserver);
    }

    @Test
    public void testSetCloudRegion() {
        _rInstance.setCloudRegion(_cloudRegion);
        assertEquals(_rInstance.getCloudRegion(), _cloudRegion);
    }

    @Test
    public void testSetVnfc() {
        _rInstance.setVnfc(_vnfc);
        assertEquals(_rInstance.getVnfc(), _vnfc);
    }

    @Test
    public void testSetLInterface() {
        _rInstance.setLInterface(_lInterface);
        assertEquals(_rInstance.getLInterface(), _lInterface);
    }

    @Test
    public void testSetPserver() {
        _rInstance.setPserver(_pserver);
        assertEquals(_rInstance.getPserver(), _pserver);
    }

    @Test
    public void testSetAdditionalProperty() {
        _rInstance.setAdditionalProperty("prop1", "propvalue1");
        _rInstance.setAdditionalProperty("prop2", "propvalue2");
        assertEquals(_rInstance.getAdditionalProperties(), _additionalProperties);
    }

    @Test
    public void testToString() {
         assertNotNull(_rInstance.toString());
    }

}
