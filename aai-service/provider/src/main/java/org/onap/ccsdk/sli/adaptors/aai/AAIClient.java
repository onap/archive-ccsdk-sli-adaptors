/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *             reserved.
 * Modifications Copyright © 2018 IBM.
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.onap.aai.inventory.v14.*;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.adaptors.aai.data.notify.NotifyEvent;



public interface AAIClient extends SvcLogicResource, SvcLogicJavaPlugin {

    public SearchResults requestServiceInstanceURL(String svcInstanceId) throws AAIServiceException;

    // VServers
    public Vserver requestVServerData(String tenantId, String vserverId, String cloudOwner, String cloudRegionId) throws AAIServiceException;

    public URL requestVserverURLNodeQuery(String vserverName) throws AAIServiceException;
    public String getTenantIdFromVserverUrl(URL url);
    public String getCloudOwnerFromVserverUrl(URL url);
    public String getCloudRegionFromVserverUrl(URL url);
    public String getVServerIdFromVserverUrl(URL url, String tennantId);
    public Vserver requestVServerDataByURL(URL url) throws AAIServiceException;



    // ----------------- Release 1510 ----------------------
    //    // GenericVNF
    public GenericVnf requestGenericVnfData(String vnfId) throws AAIServiceException;
    public boolean postGenericVnfData(String vnfId, GenericVnf request) throws AAIServiceException;

    // Physical Link
    public PhysicalLink requestPhysicalLinkData(String vnfId) throws AAIServiceException;
    public boolean postPhysicalLinkData(String vnfId, PhysicalLink request) throws AAIServiceException;
    public boolean deletePhysicalLinkData(String vnfId, String resourceVersion) throws AAIServiceException;

    // UBB Notify
    public boolean sendNotify(NotifyEvent event, String serviceInstanceId, String pathCode) throws AAIServiceException;

    // Node Query - 1602
    public SearchResults requestNodeQuery(String type, String entityIdentifier, String entityName) throws AAIServiceException;
    public String requestDataByURL(URL url) throws AAIServiceException;
    public GenericVnf requestGenericVnfeNodeQuery(String vnfName) throws AAIServiceException;

    public QueryStatus backup(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;
    public QueryStatus restore(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;

    public void logKeyError(String keys);

    public QueryStatus processResponseData(String rv, String resource, AAIRequest request, String prefix,  SvcLogicContext ctx, Map<String, String> nameValues, String modifier) throws IOException, AAIServiceException ;
    public String getPathTemplateForResource(String resoourceName, String join, SvcLogicContext ctx) throws MalformedURLException;
    public boolean isDeprecatedFormat(String resource, Map<String, String> nameValues);

    String query(AAIRequest request) throws AAIServiceException;
    String save(AAIRequest request) throws AAIServiceException;
    boolean delete(AAIRequest request, String resourceVersion) throws AAIServiceException;
    boolean update(AAIRequest request, String resourceVersion) throws AAIServiceException;

}
