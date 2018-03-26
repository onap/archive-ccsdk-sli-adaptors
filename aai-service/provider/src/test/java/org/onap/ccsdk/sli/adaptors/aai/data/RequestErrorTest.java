package org.onap.ccsdk.sli.adaptors.aai.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RequestErrorTest {

	RequestError _reInstance;
	protected ServiceException  _serviceException;
	protected Map<String, Object> _additionalProperties;
	
	@Before
	public void setUp() throws Exception {
		_reInstance = new RequestError();
		_serviceException = mock(ServiceException.class);
		_additionalProperties = new HashMap<String, Object>() {{
			put("prop1", "propvalue1");
			put("prop2", "propvalue2");
		}};
	}

	@After
	public void tearDown() throws Exception {
		_reInstance = null;
		_serviceException = null;
		_additionalProperties = null;
	}

	@Test
	public void testSetServiceException() {
		_reInstance.setServiceException(_serviceException);
		assertEquals(_reInstance.getServiceException(), _serviceException);
	}

	@Test
	public void testSetAdditionalProperty() {
		_reInstance.setAdditionalProperty("prop1", "propvalue1");
		_reInstance.setAdditionalProperty("prop2", "propvalue2");
		assertEquals(_reInstance.getAdditionalProperties(), _additionalProperties);
	}

}
