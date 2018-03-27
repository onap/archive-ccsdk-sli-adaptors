package org.onap.ccsdk.sli.adaptors.aai.update;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ActionDatumTest {

	ActionDatum _oInstance;
	protected Map<String, Object> _additionalProperties;
	protected String _propertyName;
	protected String _propertyValue;
	
	@Before
	public void setUp() throws Exception {
		_oInstance = new ActionDatum();
		_propertyName = "propertyName";
		_propertyValue = "propertyValue";
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
	public void testSetPropertyName() {
		_oInstance.setPropertyName(_propertyName);
		assertEquals(_oInstance.getPropertyName(), _propertyName);
	}

	@Test
	public void testSetPropertyValue() {
		_oInstance.setPropertyValue(_propertyValue);
		assertEquals(_oInstance.getPropertyValue(), _propertyValue);
	}

	@Test
	public void testSetAdditionalProperty() {
		_oInstance.setAdditionalProperty("prop1", "propvalue1");
		_oInstance.setAdditionalProperty("prop2", "propvalue2");
		assertEquals(_oInstance.getAdditionalProperties(), _additionalProperties);
	}

}
