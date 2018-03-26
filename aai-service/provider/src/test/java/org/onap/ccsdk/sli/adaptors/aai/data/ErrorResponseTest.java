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

public class ErrorResponseTest {

	ErrorResponse _erInstance;
	protected RequestError  _requestError;
	protected Map<String, Object> _additionalProperties;
	
	@Before
	public void setUp() throws Exception {
		_erInstance = new ErrorResponse();
		_requestError = mock(RequestError.class);
		_additionalProperties = new HashMap<String, Object>() {{
			put("prop1", "propvalue1");
			put("prop2", "propvalue2");
		}};
	}

	@After
	public void tearDown() throws Exception {
		_erInstance = null;
		_requestError = null;
		_additionalProperties = null;
	}

	@Test
	public void testSetRequestError() {
		_erInstance.setRequestError(_requestError);
		assertEquals(_erInstance.getRequestError(), _requestError);
	}

	@Test
	public void testSetAdditionalProperty() {
		_erInstance.setAdditionalProperty("prop1", "propvalue1");
		_erInstance.setAdditionalProperty("prop2", "propvalue2");
		assertEquals(_erInstance.getAdditionalProperties(), _additionalProperties);
	}

}
