package org.onap.ccsdk.sli.adaptors.aai.update;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UpdateTest {
	
	Update _oInstance;
	protected Map<String, Object> _additionalProperties;
	protected String _updateNodeType;
	protected List<UpdateNodeKey> _updateNodeKey;
	protected List<Action> _action;
	
	@Before
	public void setUp() throws Exception {
		_oInstance = new Update();
		_updateNodeType = "updateNodeType";
		_action = 	new ArrayList<>();
		Action a1 = mock(Action.class);
		Action a2  = mock(Action.class);
		_action.add(a1);
		_action.add(a2);
		_updateNodeKey = 	new ArrayList<>();
		UpdateNodeKey k1 = mock(UpdateNodeKey.class);
		UpdateNodeKey k2  = mock(UpdateNodeKey.class);
		_updateNodeKey.add(k1);
		_updateNodeKey.add(k2);
		_additionalProperties = new HashMap<String, Object>() {{
			put("prop1", "propvalue1");
			put("prop2", "propvalue2");
		}};
	}

	@After
	public void tearDown() throws Exception {
		_oInstance = null;
		_action = null;
		_additionalProperties = null;
	}

	@Test
	public void testSetUpdateNodeType() {
		_oInstance.setUpdateNodeType(_updateNodeType);
		assertEquals(_oInstance.getUpdateNodeType(), _updateNodeType);
	}

	@Test
	public void testSetAction() {
		_oInstance.setAction(_action);
		assertEquals(_oInstance.getAction(), _action);
	}

	@Test
	public void testSetUpdateNodeKey() {
		_oInstance.setUpdateNodeKey(_updateNodeKey);
		assertEquals(_oInstance.getUpdateNodeKey(), _updateNodeKey);
	}


	@Test
	public void testSetAdditionalProperty() {
		_oInstance.setAdditionalProperty("prop1", "propvalue1");
		_oInstance.setAdditionalProperty("prop2", "propvalue2");
		assertEquals(_oInstance.getAdditionalProperties(), _additionalProperties);
	}
}
