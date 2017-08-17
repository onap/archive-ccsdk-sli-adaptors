/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All
 *                         rights
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

package jtest.org.onap.ccsdk.sli.adaptors.rm;

import java.util.Date;

import jtest.util.org.onap.ccsdk.sli.adaptors.ra.TestDb;
import jtest.util.org.onap.ccsdk.sli.adaptors.ra.TestTable;

public class DataSetup {

    private TestDb testDb;

    private TestTable resource = null;
    private TestTable allocationItem = null;

    private static final String[] RESOURCE_COLUMNS =
            { "asset_id", "resource_name", "resource_type", "lt_used", "ll_label", "ll_reference_count", "rr_used" };

    private static final String[] ALLOCATION_ITEM_COLUMNS = {
            "resource_id", "application_id", "resource_set_id", "resource_union_id", "resource_share_group_list",
            "lt_used", "ll_label", "rr_used", "allocation_time" };

    private void initTables() {
        if (resource == null)
            resource = testDb.table("RESOURCE", "resource_id", RESOURCE_COLUMNS);
        if (allocationItem == null)
            allocationItem = testDb.table("ALLOCATION_ITEM", "allocation_item_id", ALLOCATION_ITEM_COLUMNS);
    }

    public void cleanup() {
        allocationItem.delete("true");
        resource.delete("true");
    }

    public void setupLimitItem(
            String resourceName,
            String assetId,
            String resourceSetId,
            String resourceUnionId,
            long used) {
        initTables();

        Long rid = resource.getId("asset_id = '" + assetId + "' AND resource_name = '" + resourceName + "'");
        if (rid == null) {
            resource.add(assetId, resourceName, "Limit", 1, null, null, null);
            rid = resource.getLastId();
        }
        allocationItem.add(rid, "SDNC", resourceSetId, resourceUnionId, null, used, null, null, new Date());
    }

    public void setupRangeItem(
            String resourceName,
            String assetId,
            String resourceSetId,
            String resourceUnionId,
            String used) {
        initTables();

        Long rid = resource.getId("asset_id = '" + assetId + "' AND resource_name = '" + resourceName + "'");
        if (rid == null) {
            resource.add(assetId, resourceName, "Range", null, null, null, used);
            rid = resource.getLastId();
        }
        allocationItem.add(rid, "SDNC", resourceSetId, resourceUnionId, null, null, null, used, new Date());
    }

    public void setupLabelItem(
            String resourceName,
            String assetId,
            String resourceSetId,
            String resourceUnionId,
            String label) {
        initTables();

        Long rid = resource.getId("asset_id = '" + assetId + "' AND resource_name = '" + resourceName + "'");
        if (rid == null) {
            resource.add(assetId, resourceName, "Label", null, label, 1, null);
            rid = resource.getLastId();
        }
        allocationItem.add(rid, "SDNC", resourceSetId, resourceUnionId, null, null, label, null, new Date());
    }

    public void setTestDb(TestDb testDb) {
        this.testDb = testDb;
    }
}
