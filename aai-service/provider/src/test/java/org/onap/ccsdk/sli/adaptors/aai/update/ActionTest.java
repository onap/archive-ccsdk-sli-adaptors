package org.onap.ccsdk.sli.adaptors.aai.update;

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
import org.onap.ccsdk.sli.adaptors.aai.query.Result;

public class ActionTest {

	Action _oInstance;
	protected Map<String, Object> _additionalProperties;
	protected String _actionType;
	protected List<ActionDatum> _actionData;
	
	@Before
	public void setUp() throws Exception {
		_oInstance = new Action();
		_actionType = "actionType";
		_actionData = 	new ArrayList<>();
		ActionDatum a1 = mock(ActionDatum.class);
		ActionDatum a2  = mock(ActionDatum.class);
		_actionData.add(a1);
		_actionData.add(a2);
		_additionalProperties = new HashMap<String, Object>() {{
			put("prop1", "propvalue1");
			put("prop2", "propvalue2");
		}};
	}

	@After
	public void tearDown() throws Exception {
		_oInstance = null;
		_actionData = null;
		_additionalProperties = null;
	}

	@Test
	public void testSetActionData() {
		_oInstance.setActionData(_actionData);
		assertEquals(_oInstance.getActionData(), _actionData);
	}

	@Test
	public void testSetActionType() {
		_oInstance.setActionType(_actionType);
		assertEquals(_oInstance.getActionType(), _actionType);
	}

	@Test
	public void testSetAdditionalProperty() {
		_oInstance.setAdditionalProperty("prop1", "propvalue1");
		_oInstance.setAdditionalProperty("prop2", "propvalue2");
		assertEquals(_oInstance.getAdditionalProperties(), _additionalProperties);
	}
}
