package org.onap.ccsdk.sli.adaptors.aai.data;

import static org.junit.Assert.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.ArrayList;

public class VariablesTest {
	Variables _variablesInstance;
	protected List<String> _variable;
	protected Map<String, Object> _additionalProperties;

	public VariablesTest() {
	}
	
	@Before
	public void setUp() throws Exception {
		_variablesInstance = new Variables();
		_variable =  new ArrayList<>(Arrays.asList("var1", "var2", "var3"));
		_additionalProperties = new HashMap<String, Object>() {{
			put("prop1", "propvalue1");
			put("prop2", "propvalue2");
		}};
	}
	
	@After
	public void tearDown() throws Exception {
		_variablesInstance = null;
		_variable = null;
		_additionalProperties = null;
	}


	@Test
	public void testSetVariable() {
		_variablesInstance.setVariable(_variable);
		assertEquals(_variablesInstance.getVariable(), _variable);
	}

	@Test
	public void testSetAdditionalProperties() {
		_variablesInstance.setAdditionalProperty("prop1", "propvalue1");
		_variablesInstance.setAdditionalProperty("prop2", "propvalue2");
		assertEquals(_variablesInstance.getAdditionalProperties(), _additionalProperties);
	}

}
