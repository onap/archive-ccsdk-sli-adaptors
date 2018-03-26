package org.onap.ccsdk.sli.adaptors.aai.data;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServiceExceptionTest {

		ServiceException _seInstance;
	    protected String _messageId;
	    protected String _text;
	    protected List<String> _variables;
	    protected Map<String, Object> _additionalProperties;

	@Before
	public void setUp() throws Exception {
		_seInstance = new ServiceException();
		_messageId = "messageId";
		_text = "text";
		_variables =  new ArrayList<>(Arrays.asList("var1", "var2", "var3"));
		_additionalProperties = new HashMap<String, Object>() {{
			put("prop1", "propvalue1");
			put("prop2", "propvalue2");
		}};
	}
	
	@After
	public void tearDown() throws Exception {
		_seInstance = null;
		_messageId = null;
		_text = null;
		_variables = null;
		_additionalProperties = null;
	}

	@Test
	public void testSetMessageId() {
		_seInstance.setMessageId(_messageId);
		assertTrue(_seInstance.getMessageId().equals(_messageId));
	}

	@Test
	public void testSetText() {
		_seInstance.setText(_text);
		assertTrue(_seInstance.getText().equals(_text));
	}

	@Test
	public void testSetVariables() {
		_seInstance.setVariables(_variables);
		assertEquals(_seInstance.getVariables(), _variables);
	}

	@Test
	public void testSetAdditionalProperties() {
		_seInstance.setAdditionalProperty("prop1", "propvalue1");
		_seInstance.setAdditionalProperty("prop2", "propvalue2");
		assertEquals(_seInstance.getAdditionalProperties(), _additionalProperties);
	}


}
