/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *             reserved.
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
import java.util.HashMap;
import java.util.Map;

import org.openecomp.aai.inventory.v11.*;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource;
import org.onap.ccsdk.sli.adaptors.aai.data.notify.NotifyEvent;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface AAIClient extends SvcLogicResource, SvcLogicJavaPlugin {

    public URL requestVserverURLNodeQuery(String vserverName) throws AAIServiceException;
    public Vserver requestVServerData(String tenantId, String vserverId, String cloudOwner, String cloudRegionId) throws AAIServiceException;

    public String getTenantIdFromVserverUrl(URL url);
    public String getCloudOwnerFromVserverUrl(URL url);
    public String getCloudRegionFromVserverUrl(URL url);
    public String getVServerIdFromVserverUrl(URL url, String tennantId);
    public Vserver requestVServerDataByURL(URL url) throws AAIServiceException;

    public boolean deleteNetworkVplsPeData(String vnfId, String resourceVersion) throws AAIServiceException;


    // --------------------------------- 1507 ---------------------------
    // Data Change
    public Vserver  dataChangeRequestVServerData(URL url) throws AAIServiceException;

    public CtagPool dataChangeRequestCtagPoolData(URL url) throws AAIServiceException;

    public VplsPe   dataChangeRequestVplsPeData(URL url) throws AAIServiceException;

    public DvsSwitch dataChangeRequestDvsSwitchData(URL url) throws AAIServiceException;

    public Pserver  dataChangeRequestPServerData(URL url) throws AAIServiceException;

    //OAM-Network:
    public OamNetwork  dataChangeRequestOAMNetworkData(URL url) throws AAIServiceException;
    //Availability-Zone:
    public AvailabilityZone  dataChangeRequestAvailabilityZoneData(URL url) throws AAIServiceException;
    //Complex:
    public Complex  dataChangeRequestComplexData(URL url) throws AAIServiceException;


    /* DELETE */
    public boolean dataChangeDeleteVServerData(URL url) throws AAIServiceException;

    public boolean dataChangeDeleteCtagPoolData(URL url) throws AAIServiceException;

    public boolean dataChangeDeleteVplsPeData(URL url) throws AAIServiceException;

    public boolean dataChangeDeleteVpeData(URL url) throws AAIServiceException;

    public boolean dataChangeDeleteDvsSwitchData(URL url) throws AAIServiceException;
    //OAM-Network:
    public boolean dataChangeDeleteOAMNetworkData(URL url) throws AAIServiceException;
    //Availability-Zone:
    public boolean dataChangeDeleteAvailabilityZoneData(URL url) throws AAIServiceException;
    //Complex:
    public boolean dataChangeDeleteComplexData(URL url) throws AAIServiceException;

    //    // GenericVNF
    public GenericVnf requestGenericVnfData(String vnfId) throws AAIServiceException;
    public boolean postGenericVnfData(String vnfId, GenericVnf request) throws AAIServiceException;
    public boolean deleteGenericVnfData(String vnfId, String resourceVersion) throws AAIServiceException;

    // L3Networks
    public L3Network requestL3NetworkData(String networkId) throws AAIServiceException;
    public L3Network requestL3NetworkQueryByName(String networkId) throws AAIServiceException;
    public boolean postL3NetworkData(String networkId, L3Network request) throws AAIServiceException;
    public boolean deleteL3NetworkData(String networkId, String resourceVersion) throws AAIServiceException;

    // Vpn Bindings
    public VpnBinding requestVpnBindingData(String vpnId) throws AAIServiceException;
//    public boolean postVpnBindingData(String vpnId, VpnBinding request) throws AAIServiceException;
    public boolean deleteVpnBindingData(String vpnId, String resourceVersion) throws AAIServiceException;

    // UBB Notify
    public boolean sendNotify(NotifyEvent event, String serviceInstanceId, String pathCode) throws AAIServiceException;

    // Node Query - 1602
    public SearchResults requestNodeQuery(String type, String entityIdentifier, String entityName) throws AAIServiceException;
    public String requestDataByURL(URL url) throws AAIServiceException;
    public GenericVnf requestGenericVnfeNodeQuery(String vnfName) throws AAIServiceException;

    public QueryStatus backup(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;
    public QueryStatus restore(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;

    public void logKeyError(String keys);

    public QueryStatus processResponseData(String rv, String resource, AAIRequest request, String prefix,  SvcLogicContext ctx, HashMap<String, String> nameValues, String modifier) throws JsonParseException, JsonMappingException, IOException, AAIServiceException ;
    public String getPathTemplateForResource(String resoourceName, String join, SvcLogicContext ctx) throws MalformedURLException;
    public boolean isDeprecatedFormat(String resource, HashMap<String, String> nameValues);

    String query(AAIRequest request) throws AAIServiceException;
    String save(AAIRequest request) throws AAIServiceException;
    boolean delete(AAIRequest request, String resourceVersion) throws AAIServiceException;
    boolean update(AAIRequest request, String resourceVersion) throws AAIServiceException;


}
