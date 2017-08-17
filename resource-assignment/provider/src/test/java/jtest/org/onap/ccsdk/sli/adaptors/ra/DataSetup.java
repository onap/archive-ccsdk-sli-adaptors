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

package jtest.org.onap.ccsdk.sli.adaptors.ra;

import java.util.Date;

import jtest.util.org.onap.ccsdk.sli.adaptors.ra.TestDb;
import jtest.util.org.onap.ccsdk.sli.adaptors.ra.TestTable;

public class DataSetup {

    private TestDb testDb;

    private TestTable vpePool = null;
    private TestTable vplspePool = null;
    private TestTable pserver = null;
    private TestTable serviceResource = null;
    private TestTable resource = null;
    private TestTable allocationItem = null;

    private static final String[] VPE_POOL_COLUMNS = {
            "vpe_name", "ipv4_oam_address", "loopback0_ipv4_address", "provisioning_status", "aic_site_id",
            "availability_zone", "vlan_id_outer", "vendor", "physical_intf_name", "physical_intf_speed",
            "physical_intf_units", "vpe_uuid", "vpe_id", "image_filename" };

    private static final String[] VPLSPE_POOL_COLUMNS = {
            "vplspe_name", "aic_site_id", "availability_zone", "physical_intf_name", "physical_intf_speed",
            "physical_intf_units", "loopback0_ipv4_address", "vlan_id_outer", "vplspe_uuid", "image_filename",
            "provisioning_status", "vendor" };

    private static final String[] PSERVER_COLUMNS = {
            "hostname", "ptnii_equip_name", "number_of_cpus", "disk_in_gigabytes", "ram_in_megabytes", "equip_type",
            "equip_vendor", "equip_model", "fqdn", "pserver_selflink", "ipv4_oam_address", "serial_number",
            "pserver_id", "internet_topology", "aic_site_id", "in_maint", "pserver_name2", "purpose" };

    private static final String[] SERVICE_RESOURCE_COLUMNS = {
            "service_instance_id", "service_status", "service_change_number", "resource_set_id", "resource_union_id" };

    private static final String[] RESOURCE_COLUMNS = { "asset_id", "resource_name", "resource_type", "lt_used" };

    private static final String[] ALLOCATION_ITEM_COLUMNS = {
            "resource_id", "application_id", "resource_set_id", "resource_union_id", "resource_share_group_list",
            "lt_used", "allocation_time" };

    private void initTables() {
        if (vpePool == null)
            vpePool = testDb.table("VPE_POOL", "vpe_name", VPE_POOL_COLUMNS);
        if (vplspePool == null)
            vplspePool = testDb.table("VPLSPE_POOL", "vplspe_name", VPLSPE_POOL_COLUMNS);
        if (pserver == null)
            pserver = testDb.table("PSERVER", "hostname", PSERVER_COLUMNS);
        if (serviceResource == null)
            serviceResource = testDb.table("SERVICE_RESOURCE", "service_resource_id", SERVICE_RESOURCE_COLUMNS);
        if (resource == null)
            resource = testDb.table("RESOURCE", "resource_id", RESOURCE_COLUMNS);
        if (allocationItem == null)
            allocationItem = testDb.table("ALLOCATION_ITEM", "allocation_item_id", ALLOCATION_ITEM_COLUMNS);
    }

    public void cleanup() {
        initTables();
        vpePool.delete("true");
        vplspePool.delete("true");
        pserver.delete("true");
        serviceResource.delete("true");
        allocationItem.delete("true");
        resource.delete("true");
    }

    public void setupVpePort(
            String aicSiteId,
            String vpeId,
            String interfaceName,
            String provStatus,
            String imageFileName) {
        initTables();
        vpePool.add(vpeId, "127.0.0.1", "107.134.152.139", provStatus, aicSiteId, "mtanj-esx-az01", "3501",
                "JUNIPER", interfaceName, "1", "GBPS", "vpe002", "VPESAT-auttx200me6", imageFileName);
    }

    public void setupVplspePort(
            String aicSiteId,
            String vplspeId,
            String interfaceName,
            String provStatus,
            String imageFileName) {
        initTables();
        vplspePool.add(vplspeId, aicSiteId, "mtanj-esx-az01", interfaceName, "100", "GBPS", "192.168.119.32", "3501",
                "vpls002", imageFileName, provStatus, "JUNIPER");
    }

    public void setupPserver(String hostname, String aicSiteId) {
        initTables();
        pserver.add(hostname, hostname + "srv1", 4, 1000, 16000, "equip_type", "equip_vendor", "equip_model", "fqdn",
                "pserver_selflink", "123.123.123.123", "serial_number", "pserver_id", "internet_topology", aicSiteId,
                "N", hostname, "purpose");
    }

    public void setupService(
            String serviceInstanceId,
            String status,
            int changeNumber,
            long speedKbps,
            String vpeId,
            String vplspeId,
            String serverId) {
        initTables();

        String resourceSetId = serviceInstanceId + "/" + changeNumber;
        String resourceUnionId = serviceInstanceId;

        serviceResource.add(serviceInstanceId, status, changeNumber, resourceSetId, resourceUnionId);

        Long rid = resource.getId("asset_id = '" + vpeId + "/ae0' AND resource_name = 'Bandwidth'");
        if (rid == null) {
            resource.add(vpeId + "/ae0", "Bandwidth", "Limit", 1);
            rid = resource.getLastId();
        }
        allocationItem.add(rid, "SDNC", resourceSetId, resourceUnionId, null, speedKbps, new Date());

        rid = resource.getId("asset_id = '" + vplspeId + "' AND resource_name = 'Bandwidth'");
        if (rid == null) {
            resource.add(vplspeId, "Bandwidth", "Limit", 1);
            rid = resource.getLastId();
        }
        allocationItem.add(rid, "SDNC", resourceSetId, resourceUnionId, null, speedKbps, new Date());

        rid = resource.getId("asset_id = '" + serverId + "' AND resource_name = 'Bandwidth'");
        if (rid == null) {
            resource.add(serverId, "Bandwidth", "Limit", 1);
            rid = resource.getLastId();
        }
        allocationItem.add(rid, "SDNC", resourceSetId, resourceUnionId, null, speedKbps, new Date());

        rid = resource.getId("asset_id = '" + serverId + "' AND resource_name = 'Connection'");
        if (rid == null) {
            resource.add(serverId, "Connection", "Limit", 1);
            rid = resource.getLastId();
        }
        allocationItem.add(rid, "SDNC", resourceSetId, resourceUnionId, null, 1, new Date());
    }

    public boolean serviceNotInDb(String serviceInstanceId, String status, Integer changeNumber) {
        String where = "service_instance_id = '" + serviceInstanceId + "'";
        if (status != null)
            where += " AND service_status = '" + status + "'";
        if (changeNumber != null)
            where += " AND service_change_number = " + changeNumber;

        if (serviceResource.exists(where))
            return false;

        where = "resource_union_id = '" + serviceInstanceId + "'";
        if (changeNumber != null)
            where += " AND resource_set_id = '" + serviceInstanceId + "/" + changeNumber + "'";

        if (allocationItem.exists(where))
            return false;

        return true;
    }

    public boolean serviceCorrectInDb(String serviceInstanceId, String status, int changeNumber, long speedKbps) {
        String where = "service_instance_id = '" + serviceInstanceId + "' AND service_status = '" + status +
                "' AND service_change_number = " + changeNumber;
        if (!serviceResource.exists(where))
            return false;

        where = "resource_union_id = '" + serviceInstanceId + "' AND resource_set_id = '" + serviceInstanceId + "/" +
                changeNumber + "' AND lt_used = " + speedKbps;
        if (!allocationItem.exists(where))
            return false;

        return true;
    }

    public boolean serviceCorrectInDb(
            String vpeId,
            String aicSiteId,
            String serviceInstanceId,
            String status,
            int changeNumber,
            long speedKbps) {

        String where = "service_instance_id = '" + serviceInstanceId + "' AND service_status = '" + status +
                "' AND service_change_number = " + changeNumber;
        if (!serviceResource.exists(where))
            return false;

        Long vpebwrid = resource.getId("asset_id = '" + vpeId + "/ae0' AND resource_name = 'Bandwidth'");
        if (vpebwrid == null)
            return false;

        where = "resource_id = " + vpebwrid + " AND resource_union_id = '" + serviceInstanceId +
                "' AND resource_set_id = '" + serviceInstanceId + "/" + changeNumber + "' AND lt_used = " + speedKbps;
        if (!allocationItem.exists(where))
            return false;

        Long srvbwrid = resource.getId("asset_id = '" + aicSiteId + "/Server1' AND resource_name = 'Bandwidth'");
        if (srvbwrid == null)
            return false;

        where = "resource_id = " + srvbwrid + " AND resource_union_id = '" + serviceInstanceId +
                "' AND resource_set_id = '" + serviceInstanceId + "/" + changeNumber + "' AND lt_used = " + speedKbps;
        if (!allocationItem.exists(where))
            return false;

        Long srvconrid = resource.getId("asset_id = '" + aicSiteId + "/Server1' AND resource_name = 'Connection'");
        if (srvconrid == null)
            return false;

        where = "resource_id = " + srvconrid + " AND resource_union_id = '" + serviceInstanceId +
                "' AND resource_set_id = '" + serviceInstanceId + "/" + changeNumber + "' AND lt_used = 1";
        if (!allocationItem.exists(where))
            return false;

        return true;
    }

    public boolean serviceCorrectInDb(
            String serviceInstanceId,
            String endPointPosition,
            String status,
            int changeNumber,
            long speedKbps) {
        String where = "service_instance_id = '" + serviceInstanceId + "' AND service_status = '" + status +
                "' AND service_change_number = " + changeNumber;
        if (!serviceResource.exists(where))
            return false;

        where = "resource_union_id = '" + serviceInstanceId + "/" + endPointPosition + "' AND resource_set_id = '" +
                serviceInstanceId + "/" + endPointPosition + "/" + changeNumber + "' AND lt_used = " + speedKbps;
        if (!allocationItem.exists(where))
            return false;

        return true;
    }

    public void setTestDb(TestDb testDb) {
        this.testDb = testDb;
    }
}
