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
import org.openecomp.aai.inventory.v11.Vlans;

public class ResponseMessageTest {

		ResponseMessage _rmInstance;
	    protected String _messageId;
	    protected String _text;
	    protected Variables _variables;
	    protected Map<String, Object> _additionalProperties;

	@Before
	public void setUp() throws Exception {
		_rmInstance = new ResponseMessage();
		_messageId = "messageId";
		_text = "text";
		_variables = mock(Variables.class);
		_additionalProperties = new HashMap<String, Object>() {{
			put("prop1", "propvalue1");
			put("prop2", "propvalue2");
		}};
	}

	@After
	public void tearDown() throws Exception {
		_rmInstance = null;
		_messageId = null;
		_text = null;
		_variables = null;
		_additionalProperties = null;
	}

	@Test
	public void testSetMessageId() {
		_rmInstance.setMessageId(_messageId);
		assertTrue(_rmInstance.getMessageId().equals(_messageId));
	}

	@Test
	public void testSetText() {
		_rmInstance.setText(_text);
		assertTrue(_rmInstance.getText().equals(_text));
	}

	@Test
	public void testSetVariables() {
		_rmInstance.setVariables(_variables);
		assertEquals(_rmInstance.getVariables(), _variables);
	}

	@Test
	public void testSetAdditionalProperty() {
		_rmInstance.setAdditionalProperty("prop1", "propvalue1");
		_rmInstance.setAdditionalProperty("prop2", "propvalue2");
		assertEquals(_rmInstance.getAdditionalProperties(), _additionalProperties);
	}

}
