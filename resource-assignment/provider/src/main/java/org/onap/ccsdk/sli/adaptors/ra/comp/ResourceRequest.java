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

package org.onap.ccsdk.sli.adaptors.ra.comp;

import java.util.List;
import org.onap.ccsdk.sli.adaptors.rm.data.Range;
import org.onap.ccsdk.sli.adaptors.rm.data.ResourceType;

public class ResourceRequest {

    public String resourceName;
    public String resourceShareGroup;
    public String rangeRequestedNumbers;
    public String rangeExcludeNumbers;
    public boolean rangeReverseOrder;
    public int rangeMinOverride;
    public int rangeMaxOverride;
    public boolean rangeForceNewNumbers;
    public boolean replace;
    public String requestType;
    public String serviceModel;
    public boolean checkOnly;
    public String applicationId;
    public String endPointPosition;
    public ResourceType resourceType;
    public List<Range> rangeOverrideList;
    public String resourceEntityTypeFilter;
    public String resourceEntityIdFilter;
    public String resourceShareGroupFilter;
    public String resourceTargetTypeFilter;
    public String resourceTargetIdFilter;
    public String rangeReleaseNumbers;
    public int limitReleaseAmount;
}
