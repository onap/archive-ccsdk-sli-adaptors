package org.onap.ccsdk.sli.adaptors.aai;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

public class AAIServiceProviderTest {

	private static AAIServiceProvider provider = null;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@Test
	public void testAAIServiceProvider() {
		provider = new AAIServiceProvider();
	}

	@Test
	public void testGetProperties() {
		provider.getProperties();
		assert(true);
	}

	@Test
	public void testDeterminePropertiesFile() {
		provider.determinePropertiesFile();
		assert(true);
	}

}
