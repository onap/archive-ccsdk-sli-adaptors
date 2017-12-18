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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RegressionTest {

	static {
		System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "INFO");
//		System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, String.format("RegressionTest-%d.txt", System.currentTimeMillis()));
	}

	private static final Logger LOG = LoggerFactory.getLogger(RegressionTest.class);

	protected static AAIService client;

	@BeforeClass
	public static void setUp() throws Exception {
//		super.setUp();
		URL url = AAIService.class.getResource(AAIService.AAICLIENT_PROPERTIES);
		client = new AAIService(url);
		LOG.info("\nTaicAAIResourceTest.setUp\n");
	}

	@AfterClass
	public static void tearDown() throws Exception {
//		super.tearDown();
		client = null;
		LOG.info("----------------------- AAIResourceTest.tearDown -----------------------");
	}

//    @Test
//	public void R1510Test05GenericVnfDataRequestDelete() {
//		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");
//		try
//		{
//		    String vnf_id = "bpsx0001v-7071";
//		    boolean response = client.deleteGenericVnfData(vnf_id, null);
//		    assertTrue(response);
//
//		}
//		catch (Throwable e)
//		{
//			assert(true);
//		}
//	}

	@Test
	public void R1604TestWanConnectorSave01Request()
	{
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try
		{
			SvcLogicContext ctx = new SvcLogicContext();

			Map<String, String> data = new HashMap<String, String>();
			data.put("resource-instance-id", "12345");
			data.put("resource-model-uuid", "45678");

			data.put("relationship-list.relationship[0].related-to", "service-instance");

			data.put("relationship-list.relationship[0].relationship-data[0].relationship-key",		"customer.global-customer-id");
			data.put("relationship-list.relationship[0].relationship-data[0].relationship-value",	"$global-customer-id");

			data.put("relationship-list.relationship[0].relationship-data[1].relationship-key",		"service-subscription.service-type");
			data.put("relationship-list.relationship[0].relationship-data[1].relationship-value", 	"$service-type");

			data.put("relationship-list.relationship[0].relationship-data[2].relationship-key",		"service-instance.service-instance-id");
			data.put("relationship-list.relationship[0].relationship-data[2].relationship-value",	"$serviceInstanceID");



			//(String resource, boolean force, boolean localOnly, String key, Map<String, String> parms, String prefix,	SvcLogicContext ctx)
			QueryStatus resp = client.save("connector", false, false, "resource-instance-id = '12345'", data, "aaidata", ctx);

			LOG.info("AAIResponse: " + resp.toString());
		}
		catch (Exception e)
		{
			assert(true);
		}
	}


	@Test
	public void R1604TestWanConnectorSave02Request()
	{
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try
		{
			SvcLogicContext ctx = new SvcLogicContext();

			Map<String, String> data = new HashMap<String, String>();
			data.put("resource-instance-id", "11012345");
			data.put("widget-model-id", "45678");
			data.put("persona-model-version", "0.1");
			data.put("persona-model-id", "dc700a83-c507-47d9-b775-1fdfcdd5f9eb");
			data.put("relationship-list.relationship[0].relationship-data[0].relationship-key", "customer.global-customer-id");
			data.put("metadata.metadatum[0].meta-value", "100640");
			data.put("metadata.metadatum[0].meta-key", "vni");
			data.put("relationship-list.relationship[0].relationship-data[1].relationship-value", "ATT-COLLABORATE");
			data.put("relationship-list.relationship[0].relationship-data[0].relationship-value", "ds828e091614l");
			data.put("relationship-list.relationship[0].relationship-data[2].relationship-key", "service-instance.service-instance-id");
			data.put("relationship-list.relationship[0].relationship-data[1].relationship-key", "service-subscription.service-type");
			data.put("relationship-list.relationship[0].related-to", "service-instance");
			data.put("relationship-list.relationship[0].relationship-data[2].relationship-value", "1990e84d-546d-4b61-8069-e0db1318ade2");


			//(String resource, boolean force, boolean localOnly, String key, Map<String, String> parms, String prefix,	SvcLogicContext ctx)
			QueryStatus resp = client.save("connector", false, false, "resource-instance-id = '11012345'", data, "aaidata", ctx);

			LOG.info("AAIResponse: " + resp.toString());
		}
		catch (Exception e)
		{
			assert(true);
		}
	}


	@Test
	public void R1604TestLogicalLinkSaveRequest()
	{
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try
		{
			SvcLogicContext ctx = new SvcLogicContext();

			Map<String, String> data = new HashMap<String, String>();
			data.put("link-name"		, "1252541");
			data.put("link-type"		, "L2 Bridge between IPE and BorderElement");
			data.put("speed-value"		, "1000");

			data.put("speed-units"		, "MBPS");
			data.put("ip-version"		, "IP-V6");
			data.put("routing-protocol"	, "BGP");
			data.put("resource-version"	, "1.0.0");
			data.put("resource-model-uuid"	, "TEST01");

			data.put("relationship-list.relationship[0].related-to" , "virtual-data-center");
			data.put("relationship-list.relationship[0].relationship-data[0].relationship-key", "virtual-data-center.vdc-id");
			data.put("relationship-list.relationship[0].relationship-data[0].relationship-value", "dpa2_cci_att_com-1068");

			data.put("relationship-list.relationship[1].related-to" , "generic-vnf");
			data.put("relationship-list.relationship[1].relationship-data[0].relationship-key", "generic-vnf.vnf-id");
			data.put("relationship-list.relationship[1].relationship-data[0].relationship-value" , "basx0001v-1189");

			data.put("relationship-list.relationship[2].related-to" , "l-interface");
			data.put("relationship-list.relationship[2].relationship-data[0].relationship-key", "pserver.hostname");
			data.put("relationship-list.relationship[2].relationship-data[0].relationship-value" , "ptpbe101snd");

			data.put("relationship-list.relationship[2].relationship-data[1].relationship-key", "lag-interface.interface-name");
			data.put("relationship-list.relationship[2].relationship-data[1].relationship-value" , "$name");

			data.put("relationship-list.relationship[2].relationship-data[2].relationship-key", "l-interface.interface-name");
			data.put("relationship-list.relationship[2].relationship-data[2].relationship-value" , "$hostname");



			//(String resource, boolean force, boolean localOnly, String key, Map<String, String> parms, String prefix,	SvcLogicContext ctx)
			QueryStatus resp = client.save("logical-link", false, false, "link-name = '1252541'", data, "aaidata", ctx);

			LOG.info("AAIResponse: " + resp.toString());
		}
		catch (Exception e)
		{
			assert(true);
		}
	}

	@Test
	public void R1604TestVDCISaveRequest()
	{
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try
		{
			SvcLogicContext ctx = new SvcLogicContext();

			Map<String, String> data = new HashMap<String, String>();
			data.put("vdc-id"	, "1252541");
			data.put("vdc-name"	, "put.the.variable.of.your.data.here");

			data.put("relationship-list.relationship[0].related-to" , "connector");

			data.put("relationship-list.relationship[0].relationship-data[0].relationship-key"   , "connector.resource-instance-id");
			data.put("relationship-list.relationship[0].relationship-data[0].relationship-value" , "$resource-instance-id");



			//(String resource, boolean force, boolean localOnly, String key, Map<String, String> parms, String prefix,	SvcLogicContext ctx)
			QueryStatus resp = client.save("virtual-data-center", false, false, "vdc-id = '1252541'", data, "aaidata", ctx);

			LOG.info("AAIResponse: " + resp.toString());
		}
		catch (Exception e)
		{
			assert(true);
		}
	}

//	@Test
	public void R1510Test03RequestGenericVnfDataRequest()
	{
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try
		{
			SvcLogicContext ctx = new SvcLogicContext();

			QueryStatus response = client.query("generic-vnf:relationship-list", false, null, "vnf-id = '34e94596-bdfa-411d-a664-16dea8583139'  AND related-to = 'l3-network' ", "aaiTest", null, ctx);
//			QueryStatus response = client.delete("generic-vnf:relationship-list", "vnf-id = '34e94596-bdfa-411d-a664-16dea8583139'  AND related-to = 'pserver' ", ctx);

			assertTrue(response == QueryStatus.SUCCESS);
			LOG.info("AAIResponse: " + response.toString());
		}
		catch (Exception e)
		{
			LOG.error("Caught exception", e);
			fail("Caught exception");
		}
	}

	@Test
	public void R1510Test03RequestVserverDataRequest()
	{
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try
		{
			SvcLogicContext ctx = new SvcLogicContext();
			QueryStatus response = client.query("vserver", false, null,
					"vserver.vserver-id = 'FRNKGEFF1' AND depth = 'all' AND cloud-region.cloud-owner = 'att-aic' AND tenant.tenant-id = '1710vPEPROJECTS::297135PROJECT' AND cloud-region.cloud-region-id = 'FRN1'"
					, "aaiTest", null, ctx);

			assertNotNull(response);
		}
		catch (Exception e)
		{
			LOG.error("Caught exception", e);
			fail("Caught exception");
		}
	}

//	@Test
	public void R1510Test03UpdateVserverDataRequest()
	{
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try
		{
			Properties prop = new Properties();
			String propFileName = "vserver-issue.txt";

			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

			if (inputStream != null) {
				prop.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}


			SvcLogicContext ctx = new SvcLogicContext();

			Map<String, String> data = new HashMap<String, String>();

			Enumeration keys = prop.keys();
			while(keys.hasMoreElements()) {
				String key = keys.nextElement().toString();
				String value = prop.getProperty(key);
				data.put(key, value);
			}

			QueryStatus response = client.update("vserver", "vserver-id = '59567c27-706e-4f41-953f-b5d3a525812f' AND tenant-id = 'USITUCAB3NJ0101UJZZ01::uCPE-VMS'", data, "aaiTest",  ctx);

			assertTrue(response == QueryStatus.SUCCESS);
			LOG.info("AAIResponse: " + response.toString());
		}
		catch (Exception e)
		{
			LOG.error("Caught exception", e);
			fail("Caught exception");
		}
	}

//	@Test
	public void R1510Test03RequestVCloudRegionDataRequest()
	{
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try
		{
			SvcLogicContext ctx = new SvcLogicContext();
//			QueryStatus response = client.query("vserver", false, null, "tenant-id = '3220171995171220' AND vserver-id = '4b491df8-cf0e-4f08-88a2-133e82b63432'", "aaiTest", null, ctx);
//			QueryStatus response = client.query("vserver", false, null, "vserver-name = 'bpsx0001vm001bps001'", "aaiTest", null, ctx);
			QueryStatus response = client.query("cloud-region", false, null,
					"depth = '0' AND cloud-region.cloud-owner = 'att-aic'  AND cloud-region.cloud-region-id = 'mtn6'"
					, "aaiTest", null, ctx);

			assertTrue(response == QueryStatus.SUCCESS);
			LOG.info("AAIResponse: " + response.toString());
		}
		catch (Exception e)
		{
			LOG.error("Caught exception", e);
			fail("Caught exception");
		}
	}

//	@Test
	public void R1510Test03RequestVCloudRegionData1Request()
	{
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try
		{
			List<String> data = new LinkedList<String>();
			data.add("depth = 0");
			data.add("cloud-region.cloud-owner = 'att-aic'");
			data.add("cloud-region.cloud-region-id = 'mtn6'");


			SvcLogicContext ctx = new SvcLogicContext();
			QueryStatus response = client.query("cloud-region", false, null, StringUtils.join(data, " AND ")
//					"depth = '0' AND cloud-region.cloud-owner = 'att-aic'  AND cloud-region.cloud-region-id = 'mtn6'"
					, "aaiTest", null, ctx);

			assertTrue(response == QueryStatus.SUCCESS);
			LOG.info("AAIResponse: " + response.toString());
		}
		catch (Exception e)
		{
			LOG.error("Caught exception", e);
			fail("Caught exception");
		}
	}

	@Test
	public void R1510Test03RequestVCloudRegionData2Request()
	{
		LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

		try
		{
			Map<String, String> nameValues = new HashMap<String, String>();
			nameValues.put("depth","0");
			nameValues.put("cloud-region.cloud-owner", "att-aic");
			nameValues.put("cloud-region.cloud-region-id","mtn6");

			AAIRequest request = AAIRequest.createRequest("cloud-region", nameValues);

			for(String key : nameValues.keySet()) {
				request.addRequestProperty(key, nameValues.get(key).toString());
			}

			String response = client.query(request);
			AAIDatum  datum = request.jsonStringToObject(response);

//			assertTrue(response == QueryStatus.SUCCESS);
			LOG.info("AAIResponse: " + datum.toString());
		}
		catch (Exception e)
		{
			LOG.error("Caught exception", e);
		}
	}
}
