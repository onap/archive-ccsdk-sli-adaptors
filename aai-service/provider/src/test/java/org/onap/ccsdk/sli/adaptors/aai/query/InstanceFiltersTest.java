package org.onap.ccsdk.sli.adaptors.aai.query;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InstanceFiltersTest {

    InstanceFilters _ifsInstance;
    protected List<InstanceFilter> _instanceFilter;
    protected Map<String, Object> _additionalProperties;

    @Before
    public void setUp() throws Exception {
        _ifsInstance = new InstanceFilters();
        _instanceFilter =     new ArrayList<>();
        InstanceFilter if1 = mock(InstanceFilter.class);
        InstanceFilter if2  = mock(InstanceFilter.class);
        _instanceFilter.add(if1);
        _instanceFilter.add(if2);
        _additionalProperties = new HashMap<String, Object>() {{
            put("prop1", "propvalue1");
            put("prop2", "propvalue2");
        }};
    }

    @After
    public void tearDown() throws Exception {
        _ifsInstance = null;
        _instanceFilter = null;
        _additionalProperties = null;
    }

    @Test
    public void testSetInstanceFilter() {
        _ifsInstance.setInstanceFilter(_instanceFilter);
        assertEquals(_ifsInstance.getInstanceFilter(), _instanceFilter);

    }

    @Test
    public void testSetAdditionalProperty() {
        _ifsInstance.setAdditionalProperty("prop1", "propvalue1");
        _ifsInstance.setAdditionalProperty("prop2", "propvalue2");
    }

}
