package org.onap.ccsdk.sli.adaptors.aai.update;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class UpdateNodeKeyTest {
	UpdateNodeKey _oInstance;
	protected Map<String, Object> _additionalProperties;
	protected String _keyName;
	protected String _keyValue;
	@Before
	public void setUp() throws Exception {
		_oInstance = new UpdateNodeKey();
		_keyName = "keyName";
		_keyValue = "keyValue";
		_additionalProperties = new HashMap<String, Object>() {{
			put("prop1", "propvalue1");
			put("prop2", "propvalue2");
		}};
	}

	@After
	public void tearDown() throws Exception {
		_oInstance = null;
		_additionalProperties = null;
	}

	@Test
	public void testSetKeyName() {
		_oInstance.setKeyName(_keyName);
		assertEquals(_oInstance.getKeyName(), _keyName);
	}

	@Test
	public void testSetKeyValue() {
		_oInstance.setKeyName(_keyName);
		assertEquals(_oInstance.getKeyName(), _keyName);
	}

	@Test
	public void testSetAdditionalProperty() {
		_oInstance.setAdditionalProperty("prop1", "propvalue1");
		_oInstance.setAdditionalProperty("prop2", "propvalue2");
		assertEquals(_oInstance.getAdditionalProperties(), _additionalProperties);
	}

}
