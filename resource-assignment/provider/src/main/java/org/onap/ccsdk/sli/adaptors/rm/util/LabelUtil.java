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

package org.onap.ccsdk.sli.adaptors.rm.util;

import java.util.ArrayList;
import java.util.Date;

import org.onap.ccsdk.sli.adaptors.rm.data.AllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.LabelAllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.LabelAllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.LabelResource;
import org.onap.ccsdk.sli.adaptors.rm.data.ResourceKey;
import org.onap.ccsdk.sli.adaptors.rm.data.ResourceType;

public class LabelUtil {

    public static boolean checkLabel(LabelResource l, LabelAllocationRequest req) {
        if (req.check && req.label != null && l.allocationItems != null && !l.allocationItems.isEmpty()) {
            for (AllocationItem ai : l.allocationItems) {
                LabelAllocationItem lai = (LabelAllocationItem) ai;
                if (!eq(req.resourceUnionId, lai.resourceUnionId) && !eq(req.label, lai.label))
                    return false;
            }
        }
        return true;
    }

    public static String allocateLabel(LabelResource l, LabelAllocationRequest req, String applicationId) {
        if (!req.allocate)
            return null;

        LabelAllocationItem lai = (LabelAllocationItem) ResourceUtil.getAllocationItem(l, req.resourceSetId);
        if (lai == null) {
            lai = new LabelAllocationItem();
            lai.resourceType = ResourceType.Label;
            lai.resourceKey = new ResourceKey();
            lai.resourceKey.assetId = req.assetId;
            lai.resourceKey.resourceName = req.resourceName;
            lai.applicationId = applicationId;
            lai.resourceSetId = req.resourceSetId;
            lai.resourceUnionId = req.resourceUnionId;
            lai.resourceShareGroupList = req.resourceShareGroupList;

            if (l.allocationItems == null)
                l.allocationItems = new ArrayList<AllocationItem>();
            l.allocationItems.add(lai);
        }

        lai.label = req.label;
        lai.allocationTime = new Date();

        recalculate(l);

        return lai.label;
    }

    public static void recalculate(LabelResource l) {
        l.label = null;
        l.referenceCount = 0;
        if (l.allocationItems != null)
            for (AllocationItem ai : l.allocationItems) {
                LabelAllocationItem lai = (LabelAllocationItem) ai;
                if (lai.label != null) {
                    l.referenceCount++;
                    if (l.label == null)
                        l.label = lai.label;
                    else if (!l.label.equals(lai.label))
                        l.label = "__BLOCKED__";
                }
            }
    }

    private static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }
}
