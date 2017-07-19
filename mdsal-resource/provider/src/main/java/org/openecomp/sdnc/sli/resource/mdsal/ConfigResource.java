/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All rights
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

package org.openecomp.sdnc.sli.resource.mdsal;

import java.util.Map;

import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class ConfigResource implements SvcLogicResource {

	private static final Logger LOG = LoggerFactory.getLogger(ConfigResource.class);

	private RestService restService;

	public ConfigResource(String sdncProtocol, String sdncHost, String sdncPort, String sdncUser, String sdncPasswd)
	{
		restService = new RestService(sdncProtocol, sdncHost, sdncPort, sdncUser, sdncPasswd, RestService.PayloadType.XML);
	}

	@Override
	public QueryStatus isAvailable(String resource, String key, String prefix, SvcLogicContext ctx) throws SvcLogicException
	{
		return(query(resource, false, null, key, prefix, null, null));
	}

	@Override
	public QueryStatus exists(String resource, String key, String prefix, SvcLogicContext ctx) throws SvcLogicException
	{

		return(query(resource, false, null, key, prefix, null, null));

	}

	@Override
	public QueryStatus query(String resource, boolean localOnly, String select, String key, String prefix,
			String orderBy, SvcLogicContext ctx) throws SvcLogicException {


		String module = resource;
		StringBuffer restQuery = new StringBuffer();

		String[] keyParts = key.split("/");

		for (String keyPart : keyParts) {
			if (restQuery.length() > 0) {
				restQuery.append("/");
			}
			if (keyPart.startsWith("$")) {

				restQuery.append(ctx.resolve(keyPart.substring(1)));
			} else {
				restQuery.append(keyPart);
			}
		}

		String restQueryStr = restQuery.toString();
		if ((restQueryStr.startsWith("'") && restQueryStr.endsWith("'")) ||
				(restQueryStr.startsWith("\"") && restQueryStr.endsWith("\""))) {
			restQueryStr = restQueryStr.substring(1, restQueryStr.length()-1);
		}

		String urlString = "restconf/config/" + module + ":" + restQueryStr;

                LOG.info("Querying resource: " + resource + ". At URL: " + urlString);

		Document results = restService.get(urlString);


		if (results == null) {
			return(QueryStatus.NOT_FOUND);
		} else {

			if (ctx != null) {
				ctx.mergeDocument(prefix, results);
			}
			return(QueryStatus.SUCCESS);
		}

	}

	@Override
	public QueryStatus reserve(String resource, String select, String key, String prefix,
			SvcLogicContext ctx) throws SvcLogicException {


		return(QueryStatus.SUCCESS);

	}

	@Override
	public QueryStatus release(String resource, String key, SvcLogicContext ctx) throws SvcLogicException {

		return(QueryStatus.SUCCESS);
	}

	@Override
	public QueryStatus delete(String arg0, String arg1, SvcLogicContext arg2)
			throws SvcLogicException {
		// TODO Auto-generated method stub
		return(QueryStatus.SUCCESS);
	}

	@Override
	public QueryStatus save(String arg0, boolean arg1, boolean localOnly, String arg2,
			Map<String, String> arg3, String arg4, SvcLogicContext arg5)
			throws SvcLogicException {
		// TODO Auto-generated method stub
		return(QueryStatus.SUCCESS);
	}

	@Override
	public QueryStatus notify(String resource, String action, String key,
			SvcLogicContext ctx) throws SvcLogicException {
		return(QueryStatus.SUCCESS);
	}


	public QueryStatus update(String resource, String key,
			Map<String, String> parms, String prefix, SvcLogicContext ctx)
			throws SvcLogicException {
		return(QueryStatus.SUCCESS);
	}



}
