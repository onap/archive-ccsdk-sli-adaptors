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

package org.onap.ccsdk.sli.adaptors.aai.data.v1507;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "imageId",
    "imageName",
    "osType",
    "osVersion",
    "application",
    "applicationVersion",
    "applicationVendor",
    "imageLink"
})
public class Image {

    @JsonProperty("imageId")
    private String imageId;
    @JsonProperty("imageName")
    private String imageName;
    @JsonProperty("osType")
    private String osType;
    @JsonProperty("osVersion")
    private String osVersion;
    @JsonProperty("application")
    private String application;
    @JsonProperty("applicationVersion")
    private String applicationVersion;
    @JsonProperty("applicationVendor")
    private String applicationVendor;
    @JsonProperty("imageLink")
    private String imageLink;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * 
     * @return
     *     The imageId
     */
    @JsonProperty("imageId")
    public String getImageId() {
        return imageId;
    }

    /**
     * 
     * @param imageId
     *     The imageId
     */
    @JsonProperty("imageId")
    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    /**
     * 
     * @return
     *     The imageName
     */
    @JsonProperty("imageName")
    public String getImageName() {
        return imageName;
    }

    /**
     * 
     * @param imageName
     *     The imageName
     */
    @JsonProperty("imageName")
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * 
     * @return
     *     The osType
     */
    @JsonProperty("osType")
    public String getOsType() {
        return osType;
    }

    /**
     * 
     * @param osType
     *     The osType
     */
    @JsonProperty("osType")
    public void setOsType(String osType) {
        this.osType = osType;
    }

    /**
     * 
     * @return
     *     The osVersion
     */
    @JsonProperty("osVersion")
    public String getOsVersion() {
        return osVersion;
    }

    /**
     * 
     * @param osVersion
     *     The osVersion
     */
    @JsonProperty("osVersion")
    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    /**
     * 
     * @return
     *     The application
     */
    @JsonProperty("application")
    public String getApplication() {
        return application;
    }

    /**
     * 
     * @param application
     *     The application
     */
    @JsonProperty("application")
    public void setApplication(String application) {
        this.application = application;
    }

    /**
     * 
     * @return
     *     The applicationVersion
     */
    @JsonProperty("applicationVersion")
    public String getApplicationVersion() {
        return applicationVersion;
    }

    /**
     * 
     * @param applicationVersion
     *     The applicationVersion
     */
    @JsonProperty("applicationVersion")
    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    /**
     * 
     * @return
     *     The applicationVendor
     */
    @JsonProperty("applicationVendor")
    public String getApplicationVendor() {
        return applicationVendor;
    }

    /**
     * 
     * @param applicationVendor
     *     The applicationVendor
     */
    @JsonProperty("applicationVendor")
    public void setApplicationVendor(String applicationVendor) {
        this.applicationVendor = applicationVendor;
    }

    /**
     * 
     * @return
     *     The imageLink
     */
    @JsonProperty("imageLink")
    public String getImageLink() {
        return imageLink;
    }

    /**
     * 
     * @param imageLink
     *     The imageLink
     */
    @JsonProperty("imageLink")
    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
