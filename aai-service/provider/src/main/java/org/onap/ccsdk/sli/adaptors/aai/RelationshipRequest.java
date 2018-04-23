/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                         reserved.
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

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.openecomp.aai.inventory.v13.Relationship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

public class RelationshipRequest extends GenericRequest {

    public RelationshipRequest(AAIRequest masterRequest) {
        super(Relationship.class);
        this.addMasterRequest(masterRequest);
    }

   
    @Override
    public URL getRequestUrl(String method, String resourceVersion) throws UnsupportedEncodingException, MalformedURLException, URISyntaxException {
       

        URL url = super.getRequestUrl(method, null);
        URIBuilder builder = new URIBuilder(url.toURI());
        String newPath = builder.getPath() + "/relationship-list/relationship";
        builder.setPath(newPath);
        if(resourceVersion != null) {
            List<NameValuePair> queryList = builder.getQueryParams();
            NameValuePair nvp = new BasicNameValuePair("resourceVersion", resourceVersion);
            queryList.add(nvp);
        }

        aaiService.LOGwriteFirstTrace(method, builder.toString());
       
        return builder.build().toURL();
    }
}
