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

package org.onap.ccsdk.sli.adaptors.aai.query;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;

import com.fasterxml.jackson.annotation.JsonProperty;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "start",
    "query"
})
@XmlRootElement(name = "query-request")
public class FormattedQueryRequestData implements AAIDatum {

	@JsonProperty("start")
	protected List<String> start;

	@JsonProperty("query")
    private String query;

    @JsonProperty("start")
    public List<String> getStart ()
    {
        return start;
    }

    @JsonProperty("start")
    public void setStart (List<String> start)
    {
        this.start = start;
    }

    @JsonProperty("query")
    public String getQuery ()
    {
        return query;
    }

    @JsonProperty("query")
    public void setQuery (String query)
    {
        this.query = query;
    }

    @Override
    public String toString()
    {
        return " [start = "+start+", query = "+query+"]";
    }

    public String getResourceVersion() {
        return null;
    }

}
