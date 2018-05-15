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

package org.onap.ccsdk.sli.adaptors.rm.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitAllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitAllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitResource;
import org.onap.ccsdk.sli.adaptors.rm.data.ResourceKey;
import org.onap.ccsdk.sli.adaptors.rm.data.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LimitUtil {

    private static final Logger log = LoggerFactory.getLogger(LimitUtil.class);

    public static boolean checkLimit(LimitResource l, LimitAllocationRequest req) {
        if (req.checkCount <= 0) {
            return true;
        }

        long checkCount = req.checkCount;
        long currentUsage = 0;
        if (req.resourceSetId != null) {
            LimitAllocationItem lai = (LimitAllocationItem) ResourceUtil.getAllocationItem(l, req.resourceSetId);
            if (lai != null) {
                currentUsage = lai.used;
            }
        }
        if (!req.replace) {
            checkCount += currentUsage;
        }

        long used = calculateLimitUsage(l, 0, null, null);
        long wouldUse = calculateLimitUsage(l, checkCount, req.resourceUnionId, req.resourceShareGroupList);

        // If usage is not increasing by this request, only check the limit if
        // strictCheck is true.
        if (wouldUse <= used && !req.strict) {
            return true;
        }

        return wouldUse <= req.checkLimit;
    }

    private static long calculateLimitUsage(
            LimitResource l,
            long checkCount,
            String resourceUnionId,
            Set<String> resourceShareGroupList) {
        if ((l.allocationItems == null || l.allocationItems.isEmpty()) &&
                (resourceUnionId == null || resourceUnionId.length() == 0)) {
            return 0;
        }

        long t1 = System.currentTimeMillis();
        boolean logit = false;
        String rn = "Resource: " + l.resourceKey.resourceName + " - " + l.resourceKey.assetId;

        // In order to best utilize the resource, we need to take not the sum of all allocation items, but
        // instead the maximum usage that could happen at any moment of time (given not all allocation items are active
        // at the same time), also taking into account possible resource sharing.
        // Thus we need to find all combinations of allocation items that can be active at the same time (allocation
        // items with the same first union cannot be active at the same time), compute the usage for each (again,
        // taking into account resource sharing), and take the maximum.
        //
        // Example:
        // Let's have the following allocation items:
        // ai1: sdid1, vrf1 - usage 5
        // ai2: sdid2, vrf1 - usage 10
        // ai3: sdid3, vrf2 - usage 15
        // ai4: sdid1, vrf3 - usage 20
        // ai5: sdid3, vrf1 - usage 25
        // The following combinations of active allocation items are possible:
        // 1) ai1, ai2, ai3
        // 2) ai1, ai2, ai5
        // 3) ai2, ai3, ai4
        // 4) ai2, ai3, ai5
        // Here is how we calculate the usage for combination 1:
        // ai1 and ai2 contain the same resource union vrf1, so they share the resource - we take the max of usage,
        // so we have:
        // max(5, 10) + 15 = 25
        // Similarly, we calculate the usage of the other combinations:
        // 2) max(5, 10, 25) = 25
        // 3) 10 + 15 + 20 = 45
        // 4) max(10, 25) + 15 = 40
        // So, the result in this case is:
        // max(25, 25, 45, 40) = 45
        //
        // We might have a problem with this approach, if we have a lot of combinations. Assuming we have at most 2
        // allocation items with the same resource union (sdid), the number of combinations would be
        // 2 ^ n
        // where n is the number of allocation items that have the same resource union (sdid). That would be
        // the number of change orders currently in progress.
        //
        // Here is one optimization that we can do:
        // If we have allocation items that have all resource unions the same, we don't need to generate combinations
        // with each of them, we can just take the one of them with the maximum usage, as it is clear that the others
        // will not lead to a bigger usage.
        // For example, if we had the following allocation items:
        // ai1: sdid1, vrf1 - usage 10
        // ai2: sdid1, vrf1 - usage 20
        // We only need to take the combinations with ai2, as they will always lead to bigger usage than the remaining
        // combinations with ai1.

        // First, group the allocation items by the first resource union, using the LimitUsage structure
        int regularChangeCount = 0;
        Map<String/* resourceUnionId */, List<LimitUsage>> limitUsageMap = new HashMap<>();
        if (l.allocationItems != null) {
            for (AllocationItem ai : l.allocationItems) {
                LimitAllocationItem lai = (LimitAllocationItem) ai;
                boolean regularChange =
                        addLimitUsage(limitUsageMap, lai.resourceUnionId, lai.resourceShareGroupList, lai.used);
                if (regularChange) {
                    regularChangeCount++;
                }
            }
        }
        if (checkCount > 0 && resourceUnionId != null) {
            boolean regularChange = addLimitUsage(limitUsageMap, resourceUnionId, resourceShareGroupList, checkCount);
            if (regularChange) {
                regularChangeCount++;
            }
        }

        // Generate all the combinations, containing one LimitUsage object for each firstResourceUnion
        int significantChangeCount = 0;
        List<List<LimitUsage>> allCombinations = new ArrayList<>();
        for (String firstResourceUnion : limitUsageMap.keySet()) {
            List<LimitUsage> limitUsageList = limitUsageMap.get(firstResourceUnion);
            if (limitUsageList.size() > 1) {
                significantChangeCount++;
            }
            if (allCombinations.isEmpty()) {
                for (LimitUsage limitUsage : limitUsageList) {
                    List<LimitUsage> newCombination = new ArrayList<>();
                    newCombination.add(limitUsage);
                    allCombinations.add(newCombination);
                }
            } else {
                if (limitUsageList.size() == 1) {
                    // No new combinations are generated - just add this one to all combinations we have until now
                    for (List<LimitUsage> combination : allCombinations) {
                        combination.add(limitUsageList.get(0));
                    }
                } else {
                    // We have to duplicate each of the current combinations for each element of limitUsageList
                    List<List<LimitUsage>> newAllCombinations = new ArrayList<>();
                    for (List<LimitUsage> combination : allCombinations) {
                        for (LimitUsage limitUsage : limitUsageList) {
                            List<LimitUsage> newCombination = new ArrayList<>(combination);
                            newCombination.add(limitUsage);
                            newAllCombinations.add(newCombination);
                        }
                    }
                    allCombinations = newAllCombinations;
                }
            }
        }

        // Now, go through all combinations and calculate its usage, get the maximum
        long maxUsage = 0;
        for (List<LimitUsage> combination : allCombinations) {
            long usage = calculateUsage(combination);
            if (usage > maxUsage) {
                maxUsage = usage;
            }
        }

        long t2 = System.currentTimeMillis();
        if (logit) {
            log.debug(rn + ": Calculating usage completed:");
            log.debug(rn + ":     Regular changes: " + regularChangeCount);
            log.debug(rn + ":     Significant changes: " + significantChangeCount);
            log.debug(rn + ":     Combinations: " + allCombinations.size());
            log.debug(rn + ":     Usage: " + maxUsage);
            log.debug(rn + ":     Time: " + (t2 - t1));
        }

        return maxUsage;
    }

    private static boolean addLimitUsage(
            Map<String/* resourceUnionId */, List<LimitUsage>> limitUsageMap,
            String resourceUnionId,
            Set<String> resourceShareGroupList,
            long used) {
        List<LimitUsage> limitUsageList = limitUsageMap.get(resourceUnionId);
        if (limitUsageList == null) {
            limitUsageList = new ArrayList<>();
            limitUsageMap.put(resourceUnionId, limitUsageList);
        }
        // See if we already have the same shareResourceUnionSet in the list. In such case just update the usage
        // to the bigger value.
        LimitUsage limitUsage = null;
        for (LimitUsage limitUsage1 : limitUsageList) {
            if ((limitUsage1.resourceShareGroupList == null || limitUsage1.resourceShareGroupList.isEmpty()) &&
                    (resourceShareGroupList == null || resourceShareGroupList.isEmpty())) {
                limitUsage = limitUsage1;
                break;
            }
            if (limitUsage1.resourceShareGroupList != null &&
                    limitUsage1.resourceShareGroupList.equals(resourceShareGroupList)) {
                limitUsage = limitUsage1;
                break;
            }
        }
        if (limitUsage != null) {
            if (limitUsage.usage < used) {
                limitUsage.usage = used;
            }
            return true;
        }

        limitUsage = new LimitUsage();
        limitUsage.resourceUnion = resourceUnionId;
        limitUsage.resourceShareGroupList = resourceShareGroupList;
        limitUsage.usage = used;
        limitUsageList.add(limitUsage);
        return false;
    }

    private static class LimitUsage {

        @SuppressWarnings("unused")
        public String resourceUnion;
        public Set<String> resourceShareGroupList;
        public long usage;
    }

    private static boolean hasCommonSharedResource(LimitUsage limitUsage1, LimitUsage limitUsage2) {
        if (limitUsage1.resourceShareGroupList == null || limitUsage1.resourceShareGroupList.isEmpty()) {
            return false;
        }
        if (limitUsage2.resourceShareGroupList == null || limitUsage2.resourceShareGroupList.isEmpty()) {
            return false;
        }

        for (String resourceUnion : limitUsage1.resourceShareGroupList) {
            if (limitUsage2.resourceShareGroupList.contains(resourceUnion)) {
                return true;
            }
        }

        return false;
    }

    private static long calculateUsage(List<LimitUsage> combination) {
        // All LimitUsage objects that have a common value in their sharedResourceUnionSet reuse the resource, so
        // split the combination in sets that have common value. Then the usage of each set will be the maximum of
        // the usages of the LimitUsage objects in the set. The usage of the combination will be the sum of the usages
        // of all sets.
        List<List<LimitUsage>> sharedSets = new ArrayList<>();
        for (LimitUsage limitUsage : combination) {
            // See if we can put limitUsage in any of the existing sets - is it has a common resource union with
            // any of the LimitUsage objects in a set.
            boolean found = false;
            for (List<LimitUsage> sharedSet : sharedSets) {
                for (LimitUsage limitUsage1 : sharedSet) {
                    if (hasCommonSharedResource(limitUsage, limitUsage1)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    sharedSet.add(limitUsage);
                    break;
                }
            }
            if (!found) {
                // Start a new set
                List<LimitUsage> newSharedSet = new ArrayList<>();
                newSharedSet.add(limitUsage);
                sharedSets.add(newSharedSet);
            }
        }

        long sum = 0;
        for (List<LimitUsage> sharedSet : sharedSets) {
            float max = 0;
            for (LimitUsage limitUsage : sharedSet) {
                if (max < limitUsage.usage) {
                    max = limitUsage.usage;
                }
            }
            sum += max;
        }

        return sum;
    }

    public static long allocateLimit(LimitResource l, LimitAllocationRequest req) {
        if (req.allocateCount <= 0) {
            return 0;
        }
        long uu = l.used;

        LimitAllocationItem lai = (LimitAllocationItem) ResourceUtil.getAllocationItem(l, req.resourceSetId);
        if (lai == null) {
            lai = new LimitAllocationItem();
            lai.resourceType = ResourceType.Limit;
            lai.resourceKey = new ResourceKey();
            lai.resourceKey.assetId = req.assetId;
            lai.resourceKey.resourceName = req.resourceName;
            lai.applicationId = req.applicationId;
            lai.resourceSetId = req.resourceSetId;
            lai.resourceUnionId = req.resourceUnionId;
            lai.resourceShareGroupList = req.resourceShareGroupList;
            lai.used = req.allocateCount;

            if (l.allocationItems == null) {
                l.allocationItems = new ArrayList<>();
            }
            l.allocationItems.add(lai);
        } else {
            lai.used = req.replace ? req.allocateCount : lai.used + req.allocateCount;
        }

        lai.allocationTime = new Date();

        recalculate(l);

        return l.used - uu;
    }

    public static void recalculate(LimitResource l) {
        l.used = calculateLimitUsage(l, 0, null, null);
    }
}
