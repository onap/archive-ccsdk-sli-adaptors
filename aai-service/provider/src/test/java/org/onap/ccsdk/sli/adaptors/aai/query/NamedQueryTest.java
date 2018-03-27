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

public class NamedQueryTest {

	NamedQuery _nqInstance;
	protected String   _namedQueryUuid;
	protected Map<String, Object> _additionalProperties;
	
	@Before
	public void setUp() throws Exception {
		_nqInstance = new NamedQuery();
		_namedQueryUuid = "uuid";
		_additionalProperties = new HashMap<String, Object>() {{
			put("prop1", "propvalue1");
			put("prop2", "propvalue2");
		}};
	}

	@After
	public void tearDown() throws Exception {
		_nqInstance = null;
		_namedQueryUuid = null;
		_additionalProperties = null;
	}

	@Test
	public void testSetNamedQueryUuid() {
		_nqInstance.setNamedQueryUuid(_namedQueryUuid);
		assertEquals(_nqInstance.getNamedQueryUuid(), _namedQueryUuid);
	}

	@Test
	public void testSetAdditionalProperty() {
		_nqInstance.setAdditionalProperty("prop1", "propvalue1");
		_nqInstance.setAdditionalProperty("prop2", "propvalue2");
		assertEquals(_nqInstance.getAdditionalProperties(), _additionalProperties);
	}

}
