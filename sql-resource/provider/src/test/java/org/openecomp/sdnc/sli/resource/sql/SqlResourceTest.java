/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 			reserved.
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

package org.openecomp.sdnc.sli.resource.sql;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Properties;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class SqlResourceTest extends TestCase {

	private static final Logger LOG = LoggerFactory
			.getLogger(SqlResourceTest.class);


	public void testExists() {


		Properties props = new Properties();
		InputStream propStr = getClass().getResourceAsStream("/svclogic.properties");
		if (propStr == null) {
			fail("src/test/resources/svclogic.properties missing");
		}

		try {
			props.load(propStr);
			propStr.close();
		} catch (Exception e) {
			e.printStackTrace();
			fail("Could not initialize properties");
		}

		// Add properties to global properties

		Enumeration propNames = props.keys();

		while (propNames.hasMoreElements()) {
			String propName = (String) propNames.nextElement();

			System.setProperty(propName, props.getProperty(propName));
		}

		SqlResource sqlResource = new SqlResource();



		InputStream testStr = getClass().getResourceAsStream("/save.tests");
		BufferedReader testsReader = new BufferedReader(new InputStreamReader(testStr));
		SvcLogicContext ctx = new SvcLogicContext();

		try {
			String testExpr = null;

			int testNum = 0;
			while ((testExpr = testsReader.readLine()) != null) {
				testExpr = testExpr.trim();

				if (testExpr.startsWith("#")) {
					testExpr = testExpr.substring(1).trim();
					String[] nameValue = testExpr.split("=");
					String name = nameValue[0].trim();
					String value = nameValue[1].trim();

					LOG.info("Setting context attribute " + name + " = "
							+ value);
					ctx.setAttribute(name, value);

				} else {

					testNum++;
					String sqlStmt = testExpr;
					QueryStatus status = sqlResource.save("SQL", true, false, sqlStmt, null, "savetest"+testNum, ctx);

					switch (status) {
					case SUCCESS:
						LOG.info("Found data for query [" + sqlStmt + "]");
						break;
					case NOT_FOUND:
						LOG.info("Did not data for query [" + sqlStmt + "]");
						break;
					default:
						fail("Failure executing query [" + sqlStmt + "]");

					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("Caught exception running tests");
		}


		testStr = getClass().getResourceAsStream("/query.tests");
		testsReader = new BufferedReader(new InputStreamReader(testStr));

		try {
			String testExpr = null;

			int testNum = 0;
			while ((testExpr = testsReader.readLine()) != null) {
				testExpr = testExpr.trim();
				if (testExpr.startsWith("#")) {
					testExpr = testExpr.substring(1).trim();
					String[] nameValue = testExpr.split("=");
					String name = nameValue[0].trim();
					String value = nameValue[1].trim();

					LOG.info("Setting context attribute " + name + " = "
							+ value);
					ctx.setAttribute(name, value);

				} else {

					testNum++;

					String sqlStmt = testExpr;
					QueryStatus status = sqlResource.query("SQL", false, null,
							sqlStmt, "querytest" + testNum, null, ctx);

					switch (status) {
					case SUCCESS:
						LOG.info("Found data for query [" + sqlStmt + "]");
						break;
					case NOT_FOUND:
						LOG.info("Did not data for query [" + sqlStmt + "]");
						break;
					default:
						fail("Failure executing query [" + sqlStmt + "]");

					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("Caught exception running tests");
		}


		testStr = getClass().getResourceAsStream("/delete.tests");
		testsReader = new BufferedReader(new InputStreamReader(testStr));

		try {
			String testExpr = null;

			int testNum = 0;
			while ((testExpr = testsReader.readLine()) != null) {
				testExpr = testExpr.trim();
				if (testExpr.startsWith("#")) {
					testExpr = testExpr.substring(1).trim();
					String[] nameValue = testExpr.split("=");
					String name = nameValue[0].trim();
					String value = nameValue[1].trim();

					LOG.info("Setting context attribute " + name + " = "
							+ value);
					ctx.setAttribute(name, value);

				} else {

					testNum++;

					String sqlStmt = testExpr;
					QueryStatus status = sqlResource.delete("SQL", sqlStmt, ctx);

					switch (status) {
					case SUCCESS:
						LOG.info("Found data for query [" + sqlStmt + "]");
						break;
					case NOT_FOUND:
						LOG.info("Did not data for query [" + sqlStmt + "]");
						break;
					default:
						fail("Failure executing query [" + sqlStmt + "]");

					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			fail("Caught exception running tests");
		}

		for (String attrName : ctx.getAttributeKeySet()) {
			LOG.info("ctx.getAttribute("+attrName+") = "+ctx.getAttribute(attrName));
		}
	}

}
