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

package org.openecomp.sdnc.rm.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openecomp.sdnc.rm.data.AllocationItem;
import org.openecomp.sdnc.rm.data.RangeAllocationItem;
import org.openecomp.sdnc.rm.data.RangeAllocationRequest;
import org.openecomp.sdnc.rm.data.RangeResource;
import org.openecomp.sdnc.rm.data.ResourceKey;
import org.openecomp.sdnc.rm.data.ResourceType;

public class RangeUtil {

    public static void recalculate(RangeResource r) {
        r.used = new TreeSet<Integer>();
        if (r.allocationItems != null)
            for (AllocationItem ai : r.allocationItems) {
                RangeAllocationItem rai = (RangeAllocationItem) ai;
                if (rai.used != null)
                    r.used.addAll(rai.used);
            }
    }

    public static boolean checkRange(RangeResource r, RangeAllocationRequest req, int num) {
        if (num < req.checkMin || num > req.checkMax)
            return false;

        if (r.allocationItems != null)
            for (AllocationItem ai : r.allocationItems) {
                RangeAllocationItem rai = (RangeAllocationItem) ai;
                if (!eq(req.resourceUnionId, rai.resourceUnionId) && rai.used != null && rai.used.contains(num))
                    return false;
            }

        return true;
    }

    private static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    public static SortedSet<Integer> getUsed(RangeResource r, String resourceUnionId) {
        SortedSet<Integer> used = new TreeSet<Integer>();
        if (r.allocationItems != null)
            for (AllocationItem ai : r.allocationItems) {
                RangeAllocationItem rai = (RangeAllocationItem) ai;
                if (eq(resourceUnionId, rai.resourceUnionId) && rai.used != null)
                    used.addAll(rai.used);
            }
        return used;
    }

    public static void allocateRange(
            RangeResource rr,
            SortedSet<Integer> requestedNumbers,
            RangeAllocationRequest req,
            String applicationId) {
        if (!req.allocate)
            return;

        RangeAllocationItem rai = (RangeAllocationItem) ResourceUtil.getAllocationItem(rr, req.resourceSetId);
        if (rai == null) {
            rai = new RangeAllocationItem();
            rai.resourceType = ResourceType.Range;
            rai.resourceKey = new ResourceKey();
            rai.resourceKey.assetId = req.assetId;
            rai.resourceKey.resourceName = req.resourceName;
            rai.applicationId = applicationId;
            rai.resourceSetId = req.resourceSetId;
            rai.resourceUnionId = req.resourceUnionId;
            rai.resourceShareGroupList = req.resourceShareGroupList;
            rai.used = requestedNumbers;

            if (rr.allocationItems == null)
                rr.allocationItems = new ArrayList<AllocationItem>();
            rr.allocationItems.add(rai);
        } else if (req.replace)
            rai.used = requestedNumbers;
        else
            rai.used.addAll(requestedNumbers);

        rai.allocationTime = new Date();

        recalculate(rr);
    }
}
