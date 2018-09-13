/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.aai;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.onap.aai.inventory.v14.LInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GenericRequestTest {

	private static final Logger LOG = LoggerFactory.getLogger(GenericRequestTest.class);

	protected static AAIClient client;
	protected static AAIRequest request;

	@BeforeClass
	public static void setUp() throws Exception {
		URL url = AAIService.class.getResource(AAIService.AAICLIENT_PROPERTIES);
		client = new AAIService(url);
		request = AAIRequest.createRequest("generic-vnf", new HashMap<String, String>());
		LOG.info("\nTaicAAIResourceTest.setUp\n");
	}

	@AfterClass
	public static void tearDown() throws Exception {
		client = null;
		LOG.info("----------------------- AAIResourceTest.tearDown -----------------------");
	}

	@Test
	public void test001()
	{
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try
		{
			Map<String, String> key = new HashMap<String, String>();
			AAIRequest request = AAIRequest.createRequest("vserver", key);
			key.put("vserver.vserver_id", "e8faf166-2402-4ae2-be45-067954c63aed");
			key.put("tenant.tenant_id", "1863027683132547");
			request.processRequestPathValues(key);
			String uri = request.getTargetUri();

			assertNotNull(uri);

		}
		catch (Exception e)
		{
			LOG.error("Caught exception", e);
			fail("Caught exception");
		}
	}

	@Test
	public void test002() {
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");
		try
		{
			URL resource = this.getClass().getResource("json/linterfaceJson.txt");

			LOG.info("Resource is " + resource.getFile());
			File requestFile = new File(resource.getFile());
			if(!requestFile.exists()) {
				fail("Test file does not exist");
			}

		    ObjectMapper mapper = AAIService.getObjectMapper();
		    LInterface request = mapper.readValue(requestFile, LInterface.class);
		    String vnf_id = request.getInterfaceName();
		    LOG.info(vnf_id);

		}
		catch (Exception e)
		{
			LOG.error("Caught exception", e);
		}
	}

//    @Test
//	public void test003() {
//		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");
//		try
//		{
//		    String vnf_id = "4718302b-7884-4959-a499-f470c62418ff";
//
//		    GenericVnf genericVnf = client.requestGenericVnfData(vnf_id);
//
//		    client.deleteGenericVnfData(vnf_id, genericVnf.getResourceVersion());
//
//		}
//		catch (Throwable e)
//		{
//			LOG.error("Caught exception", e);
//		}
//	}


	@Test
	public void test004() {
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		URL url;
		try {
			url = request.getRequestUrl("GET", null);
			assertNotNull(url);
		} catch (UnsupportedEncodingException | MalformedURLException | URISyntaxException exc) {
			LOG.error("Failed test", exc);
		}

	}

	@Test
	public void runToJSONStringTest() {
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try {
			String json = request.toJSONString();
			assertNotNull(json);
		} catch (Exception exc) {
			LOG.error("Failed test", exc);
		}

	}

	@Test
	public void runGetArgsListTest() {
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try {
			String[] args = request.getArgsList();
			assertNotNull(args);
		} catch (Exception exc) {
			LOG.error("Failed test", exc);
		}

	}

	@Test
	public void runGetModelTest() {
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try {
			Class<?  extends AAIDatum> clazz = request.getModelClass();
			assertNotNull(clazz);
		} catch (Exception exc) {
			LOG.error("Failed test", exc);
		}

	}
}
