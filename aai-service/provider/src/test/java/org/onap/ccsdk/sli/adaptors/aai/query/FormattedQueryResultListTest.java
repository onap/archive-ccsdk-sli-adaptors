package org.onap.ccsdk.sli.adaptors.aai.query;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.util.ArrayList;

public class FormattedQueryResultListTest {
	FormattedQueryResultList _fqrlInstance;
	protected List<Result> _results;
	
	@Before
	public void setUp() throws Exception {
		_fqrlInstance = new FormattedQueryResultList();
		_results = 	new ArrayList<>();
		Result r1 = mock(Result.class);
		Result r2  = mock(Result.class);
		_results.add(r1);
		_results.add(r2);
	}
	
	@After
	public void tearDown() throws Exception {
		_fqrlInstance = null;
		_results = null;
	}


	@Test
	public void testSetResults() {
		_fqrlInstance.setResults(_results);
		assertEquals(_fqrlInstance.getResults(), _results);
	}

	@Test
	public void testToString() {
		_fqrlInstance.setResults(_results);
		 assertTrue(_fqrlInstance.toString() != null);
	}

}
