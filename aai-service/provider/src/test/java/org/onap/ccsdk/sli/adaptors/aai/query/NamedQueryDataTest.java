package org.onap.ccsdk.sli.adaptors.aai.query;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NamedQueryDataTest {

	NamedQueryData _nqdInstance;
	protected QueryParameters  _queryParameters;
	protected InstanceFilters  _instanceFilters;
	protected Map<String, Object> _additionalProperties;
	
	@Before
	public void setUp() throws Exception {
		_nqdInstance = new NamedQueryData();
		_queryParameters = mock(QueryParameters.class);
		_instanceFilters = mock(InstanceFilters.class);
		_additionalProperties = new HashMap<String, Object>() {{
			put("prop1", "propvalue1");
			put("prop2", "propvalue2");
		}};
	}

	@After
	public void tearDown() throws Exception {
		_nqdInstance = null;
		_queryParameters = null;
		_instanceFilters = null;
		_additionalProperties = null;
	}

	@Test
	public void testSetQueryParameters() {
		_nqdInstance.setQueryParameters(_queryParameters);
		assertEquals(_nqdInstance.getQueryParameters(), _queryParameters);
	}

	@Test
	public void testSetInstanceFilters() {
		_nqdInstance.setInstanceFilters(_instanceFilters);
		assertEquals(_nqdInstance.getInstanceFilters(), _instanceFilters);
	}

	@Test
	public void testSetAdditionalProperty() {
		_nqdInstance.setAdditionalProperty("prop1", "propvalue1");
		_nqdInstance.setAdditionalProperty("prop2", "propvalue2");
		assertEquals(_nqdInstance.getAdditionalProperties(), _additionalProperties);
	}

}
