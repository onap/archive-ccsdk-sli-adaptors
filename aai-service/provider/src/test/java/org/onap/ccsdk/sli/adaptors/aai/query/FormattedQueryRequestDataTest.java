package org.onap.ccsdk.sli.adaptors.aai.query;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.aai.data.Variables;

public class FormattedQueryRequestDataTest {

	FormattedQueryRequestData _fqrdInstance;
	protected List<String> _start;
	protected String _query;
	
	@Before
	public void setUp() throws Exception {
		_fqrdInstance = new FormattedQueryRequestData();
		_start =  new ArrayList<>(Arrays.asList("start1", "start2", "start3"));
		_query = "query";
	}

	@After
	public void tearDown() throws Exception {
		_fqrdInstance = null;
		_start = null;
		_query = null;
	}

	@Test
	public void testSetStart() {
		_fqrdInstance.setStart(_start);
		assertEquals(_fqrdInstance.getStart(), _start);
	}

	@Test
	public void testSetQuery() {
		_fqrdInstance.setQuery(_query);
		assertEquals(_fqrdInstance.getQuery(), _query);
	}

	@Test
	public void testToString() {
		_fqrdInstance.setStart(_start);
		_fqrdInstance.setQuery(_query);
		 assertNotNull(_fqrdInstance.toString());
	}
	
}
