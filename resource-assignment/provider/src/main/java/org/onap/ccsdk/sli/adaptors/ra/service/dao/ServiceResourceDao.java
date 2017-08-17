/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
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

package org.onap.ccsdk.sli.adaptors.ra.service.dao;

import org.onap.ccsdk.sli.adaptors.ra.service.data.ServiceResource;
import org.onap.ccsdk.sli.adaptors.ra.service.data.ServiceStatus;

public interface ServiceResourceDao {

    ServiceResource getServiceResource(String serviceInstanceId, ServiceStatus serviceStatus);

    void addServiceResource(ServiceResource serviceResource);

    void updateServiceResource(ServiceResource serviceResource);

    void deleteServiceResource(String serviceInstanceId, ServiceStatus serviceStatus);

    void updateServiceStatus(String serviceInstanceId, ServiceStatus serviceStatus, ServiceStatus newServiceStatus);
}
