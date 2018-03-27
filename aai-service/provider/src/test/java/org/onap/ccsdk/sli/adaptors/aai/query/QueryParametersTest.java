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

import org.onap.ccsdk.sli.adaptors.aai.data.ErrorResponse;
import org.onap.ccsdk.sli.adaptors.aai.data.RequestError;

public class QueryParametersTest {

	QueryParameters _qpInstance;
	protected NamedQuery  _namedQuery;
	protected Map<String, Object> _additionalProperties;
	
	@Before
	public void setUp() throws Exception {
		_qpInstance = new QueryParameters();
		_namedQuery = mock(NamedQuery.class);
		_additionalProperties = new HashMap<String, Object>() {{
			put("prop1", "propvalue1");
			put("prop2", "propvalue2");
		}};
	}

	@After
	public void tearDown() throws Exception {
		_qpInstance = null;
		_namedQuery = null;
		_additionalProperties = null;
	}

	@Test
	public void testSetNamedQuery() {
		_qpInstance.setNamedQuery(_namedQuery);
		assertEquals(_qpInstance.getNamedQuery(), _namedQuery);
	}

	@Test
	public void testSetAdditionalProperty() {
		_qpInstance.setAdditionalProperty("prop1", "propvalue1");
		_qpInstance.setAdditionalProperty("prop2", "propvalue2");
		assertEquals(_qpInstance.getAdditionalProperties(), _additionalProperties);
	}

}
