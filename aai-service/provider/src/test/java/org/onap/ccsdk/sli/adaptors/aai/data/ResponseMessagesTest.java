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

public class ResponseMessagesTest {

	ResponseMessages _rmsInstance;
	protected List<ResponseMessage> _responseMessage;
    protected Map<String, Object> _additionalProperties;
	
	@Before
	public void setUp() throws Exception {
		_rmsInstance = new ResponseMessages();
		_responseMessage = 	new ArrayList<>();
		ResponseMessage  rm1 = mock(ResponseMessage.class);
		ResponseMessage  rm2 = mock(ResponseMessage.class);
		_responseMessage.add(rm1);
		_responseMessage.add(rm2);
		_additionalProperties = new HashMap<String, Object>() {{
			put("prop1", "propvalue1");
			put("prop2", "propvalue2");
		}};
	}

	@After
	public void tearDown() throws Exception {
		_rmsInstance = null;
		_responseMessage = null;
		_additionalProperties = null;
	}

	@Test
	public void testSetResponseMessage() {
		_rmsInstance.setResponseMessage(_responseMessage);
		assertEquals(_rmsInstance.getResponseMessage(), _responseMessage);
	}

	@Test
	public void testSetAdditionalProperty() {
		_rmsInstance.setAdditionalProperty("prop1", "propvalue1");
		_rmsInstance.setAdditionalProperty("prop2", "propvalue2");
		assertEquals(_rmsInstance.getAdditionalProperties(), _additionalProperties);
	}

}
