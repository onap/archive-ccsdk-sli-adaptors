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
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.adaptors.aai.data.notify.NotifyEvent;
import org.onap.ccsdk.sli.adaptors.aai.data.v1507.VServer;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface AAIClient extends SvcLogicResource, SvcLogicJavaPlugin {

    // VCE
    public boolean postNetworkVceData(String vnfId, Vce request) throws AAIServiceException;
    public Vce requestNetworkVceData(String vnfId) throws AAIServiceException;
    public boolean deleteNetworkVceData(String vnfId, String resourceVersion) throws AAIServiceException;

    // Service Inteface
    public ServiceInstance requestServiceInterfaceData(String customerId, String serviceType, String svc_instanceId) throws AAIServiceException;
    public boolean postServiceInterfaceData(String customerId, String serviceType, String svcInstanceId, ServiceInstance request)    throws AAIServiceException;
    public SearchResults requestServiceInstanceURL(String svcInstanceId) throws AAIServiceException;

    // VServers
    public Vserver requestVServerData(String tenantId, String vserverId, String cloudOwner, String cloudRegionId) throws AAIServiceException;
    public boolean postVServerData(String tenantId, String vserverId, String cloudOwner, String cloudRegionId, Vserver request) throws AAIServiceException;
    public boolean deleteVServerData(String tenantId, String vserverId, String cloudOwner, String cloudRegionId, String resourceVersion) throws AAIServiceException;

    public URL requestVserverURLNodeQuery(String vserver_name) throws AAIServiceException;
    public String getTenantIdFromVserverUrl(URL url);
    public String getCloudOwnerFromVserverUrl(URL url);
    public String getCloudRegionFromVserverUrl(URL url);
    public String getVServerIdFromVserverUrl(URL url, String tennantId);
    public Vserver requestVServerDataByURL(URL url) throws AAIServiceException;

    // VPLS-PE
    public VplsPe requestNetworkVplsPeData(String equipmentName) throws AAIServiceException;
    public boolean postNetworkVplsPeData(String vnfId, VplsPe request) throws AAIServiceException;
    public boolean deleteNetworkVplsPeData(String vnfId, String resourceVersion) throws AAIServiceException;


    // Complexes
    public Complex   requestNetworkComplexData(String vnfId) throws AAIServiceException;
    public boolean postNetworkComplexData(String vnfId, Complex request) throws AAIServiceException;
    public boolean deleteNetworkComplexData(String vnfId, String resourceVersion) throws AAIServiceException;

    // CTag Pool
    public CtagPool requestCtagPoolData(String physicalLocationId, String targetPe, String availabilityZoneName) throws AAIServiceException;

    // --------------------------------- 1507 ---------------------------
    // Data Change
    public VServer  dataChangeRequestVServerData(URL url) throws AAIServiceException;

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

    // ----------------- Release 1510 ----------------------
    //    // GenericVNF
    public GenericVnf requestGenericVnfData(String vnfId) throws AAIServiceException;
    public boolean postGenericVnfData(String vnfId, GenericVnf request) throws AAIServiceException;
    public boolean deleteGenericVnfData(String vnfId, String resourceVersion) throws AAIServiceException;

    //    DvsSwitch
    public DvsSwitch requestDvsSwitchData(String vnfId) throws AAIServiceException;
    public boolean postDvsSwitchData(String vnfId, DvsSwitch request) throws AAIServiceException;
    public boolean deleteDvsSwitchData(String vnfId, String resourceVersion) throws AAIServiceException;

    //    PInterface
    public PInterface requestPInterfaceData(String hostname, String interfaceName) throws AAIServiceException;
    public boolean postPInterfaceData(String hostname, String interfaceName, PInterface request) throws AAIServiceException;
    public boolean deletePInterfaceData(String hostname, String interfaceName, String resourceVersion) throws AAIServiceException;

    // Physical Link
    public PhysicalLink requestPhysicalLinkData(String vnfId) throws AAIServiceException;
    public boolean postPhysicalLinkData(String vnfId, PhysicalLink request) throws AAIServiceException;
    public boolean deletePhysicalLinkData(String vnfId, String resourceVersion) throws AAIServiceException;

    // PServers
    public Pserver requestPServerData(String hostname) throws AAIServiceException;
    public boolean postPServerData(String hostname, Pserver server) throws AAIServiceException;
    public boolean deletePServerData(String hostname, String resourceVersion) throws AAIServiceException;

    // L3Networks
    public L3Network requestL3NetworkData(String networkId) throws AAIServiceException;
    public L3Network requestL3NetworkQueryByName(String networkId) throws AAIServiceException;
    public boolean postL3NetworkData(String networkId, L3Network request) throws AAIServiceException;
    public boolean deleteL3NetworkData(String networkId, String resourceVersion) throws AAIServiceException;

    // Vpn Bindings
    public VpnBinding requestVpnBindingData(String vpnId) throws AAIServiceException;
//    public boolean postVpnBindingData(String vpnId, VpnBinding request) throws AAIServiceException;
    public boolean deleteVpnBindingData(String vpnId, String resourceVersion) throws AAIServiceException;

    //VnfImage
    public VnfImage requestVnfImageData(String vpnId) throws AAIServiceException;
    public VnfImage requestVnfImageDataByVendorModel(String vendor, String model) throws AAIServiceException;
    public VnfImage requestVnfImageDataByVendorModelVersion(String vendor, String model, String version) throws AAIServiceException;

    // UBB Notify
    public boolean sendNotify(NotifyEvent event, String serviceInstanceId, String pathCode) throws AAIServiceException;

    // 1512
    // Site Pair Site
    public SitePairSet requestSitePairSetData(String sitePairSetId) throws AAIServiceException;
    public boolean postSitePairSetData(String sitePairSetId, SitePairSet request) throws AAIServiceException;
    public boolean deleteSitePairSetData(String sitePairSetId, String resourceVersion) throws AAIServiceException;

    // Services
    public Service requestServiceData(String serviceId) throws AAIServiceException;
    public boolean postServiceData(String serviceId, Service request) throws AAIServiceException;
    public boolean deleteServiceData(String serviceId, String resourceVersion) throws AAIServiceException;

    // Node Query - 1602
    public SearchResults requestNodeQuery(String type, String entityIdentifier, String entityName) throws AAIServiceException;
    public String requestDataByURL(URL url) throws AAIServiceException;
//    public Object requestDataInstanceNodeQuery(String type, String vnf_name) throws AAIServiceException;
    public GenericVnf requestGenericVnfeNodeQuery(String vnf_name) throws AAIServiceException;

    //    // tenant
    public Tenant requestTenantData(String tenant_id, String cloudOwner, String cloudRegionId) throws AAIServiceException;
    public Tenant requestTenantDataByName(String tenant_name, String cloudOwner, String cloudRegionId) throws AAIServiceException;
    public boolean postTenantData(String tenant_id, String cloudOwner, String cloudRegionId, Tenant request) throws AAIServiceException;
//    public boolean deleteGenericVnfData(String vnfId, String resourceVersion) throws AAIServiceException;


    public QueryStatus backup(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;
    public QueryStatus restore(Map<String, String> params, SvcLogicContext ctx) throws SvcLogicException;

    public void logKeyError(String keys);

    public QueryStatus processResponseData(String rv, String resource, AAIRequest request, String prefix,  SvcLogicContext ctx, HashMap<String, String> nameValues, String modifier) throws JsonParseException, JsonMappingException, IOException, AAIServiceException ;
    public String getPathTemplateForResource(String resoourceName, String join, SvcLogicContext ctx) throws MalformedURLException;
    public boolean isDeprecatedFormat(String resource, HashMap<String, String> nameValues);

    String query(AAIRequest request) throws AAIServiceException;
    String save(AAIRequest request) throws AAIServiceException;
    boolean delete(AAIRequest request, String resourceVersion) throws AAIServiceException;


}
