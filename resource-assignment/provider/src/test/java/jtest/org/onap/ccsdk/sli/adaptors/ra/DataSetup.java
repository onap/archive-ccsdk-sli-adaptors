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

package jtest.org.onap.ccsdk.sli.adaptors.ra;

import java.util.Date;
import jtest.util.org.onap.ccsdk.sli.adaptors.ra.TestDb;
import jtest.util.org.onap.ccsdk.sli.adaptors.ra.TestTable;

public class DataSetup {

    private TestDb testDb;

    private TestTable resource = null;
    private TestTable allocationItem = null;

    private static final String[] RESOURCE_COLUMNS =
        {"asset_id", "resource_name", "resource_type", "lt_used", "rr_used"};

    private static final String[] ALLOCATION_ITEM_COLUMNS = {"resource_id", "application_id", "resource_set_id",
            "resource_union_id", "resource_share_group_list", "lt_used", "rr_used", "allocation_time"};

    private void initTables() {
        if (resource == null) {
            resource = testDb.table("RESOURCE", "resource_id", RESOURCE_COLUMNS);
        }
        if (allocationItem == null) {
            allocationItem = testDb.table("ALLOCATION_ITEM", "allocation_item_id", ALLOCATION_ITEM_COLUMNS);
        }
    }

    public void cleanup() {
        initTables();
        allocationItem.delete("true");
        resource.delete("true");
    }

    public void setupLimitItem(String resourceName, String assetId, String resourceSetId, String resourceUnionId,
            long used) {
        initTables();

        Long rid = resource.getId("asset_id = '" + assetId + "' AND resource_name = '" + resourceName + "'");
        if (rid == null) {
            resource.add(assetId, resourceName, "Limit", used, null);
            rid = resource.getLastId();
        }
        allocationItem.add(rid, "SDNC", resourceSetId, resourceUnionId, null, used, null, new Date());
    }

    public void setupRangeItem(String resourceName, String assetId, String resourceSetId, String resourceUnionId,
            String resourceShareGroup, String used) {
        initTables();

        Long rid = resource.getId("asset_id = '" + assetId + "' AND resource_name = '" + resourceName + "'");
        if (rid == null) {
            resource.add(assetId, resourceName, "Range", null, used);
            rid = resource.getLastId();
        }
        allocationItem.add(rid, "SDNC", resourceSetId, resourceUnionId, resourceShareGroup, null, used, new Date());
    }

    public void setupRangeItem(String resourceName, String assetId, String resourceSetId, String resourceUnionId,
            String used) {
        setupRangeItem(resourceName, assetId, resourceSetId, resourceUnionId, null, used);
    }

    public boolean checkRangeItem(String resourceName, String assetId, String resourceSetId, String used) {
        String where = "resource_id = (SELECT resource_id FROM RESOURCE WHERE resource_name = '" + resourceName
                + "' AND asset_id = '" + assetId + "') AND resource_set_id = '" + resourceSetId + "'";
        Object usedInDb = allocationItem.getColumn("rr_used", where);
        return used.equals(usedInDb);
    }

    public boolean checkLimitItem(String resourceName, String assetId, String resourceSetId, int used) {
        String where = "resource_id = (SELECT resource_id FROM RESOURCE WHERE resource_name = '" + resourceName
                + "' AND asset_id = '" + assetId + "') AND resource_set_id = '" + resourceSetId + "' AND lt_used = "
                + used;
        return allocationItem.exists(where);
    }

    public void setTestDb(TestDb testDb) {
        this.testDb = testDb;
    }
}
