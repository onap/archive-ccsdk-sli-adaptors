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

package org.onap.ccsdk.sli.adaptors.rm.comp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.onap.ccsdk.sli.adaptors.lock.comp.LockHelper;
import org.onap.ccsdk.sli.adaptors.lock.comp.ResourceLockedException;
import org.onap.ccsdk.sli.adaptors.lock.comp.SynchronizedFunction;
import org.onap.ccsdk.sli.adaptors.rm.dao.ResourceDao;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationOutcome;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationStatus;
import org.onap.ccsdk.sli.adaptors.rm.data.LabelAllocationOutcome;
import org.onap.ccsdk.sli.adaptors.rm.data.LabelAllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.LabelResource;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitAllocationOutcome;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitAllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitResource;
import org.onap.ccsdk.sli.adaptors.rm.data.MultiAssetAllocationOutcome;
import org.onap.ccsdk.sli.adaptors.rm.data.MultiAssetAllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.MultiResourceAllocationOutcome;
import org.onap.ccsdk.sli.adaptors.rm.data.MultiResourceAllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.Range;
import org.onap.ccsdk.sli.adaptors.rm.data.RangeAllocationOutcome;
import org.onap.ccsdk.sli.adaptors.rm.data.RangeAllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.RangeResource;
import org.onap.ccsdk.sli.adaptors.rm.data.Resource;
import org.onap.ccsdk.sli.adaptors.rm.data.ResourceKey;
import org.onap.ccsdk.sli.adaptors.rm.data.ResourceType;
import org.onap.ccsdk.sli.adaptors.rm.util.LabelUtil;
import org.onap.ccsdk.sli.adaptors.rm.util.LimitUtil;
import org.onap.ccsdk.sli.adaptors.rm.util.RangeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class AllocationFunction extends SynchronizedFunction {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(AllocationFunction.class);

    private ResourceDao resourceDao;

    private AllocationRequest request;
    private AllocationOutcome outcome;

    private List<Resource> updateList = new ArrayList<>();

    public AllocationFunction(LockHelper lockHelper, ResourceDao resourceDao, AllocationRequest request,
            int lockTimeout) {
        super(lockHelper, getLockNames(request), lockTimeout);
        this.resourceDao = resourceDao;
        this.request = request;
    }

    private static Collection<String> getLockNames(AllocationRequest request) {
        Set<String> lockResourceNames = new HashSet<>();
        addLockNames(lockResourceNames, request);
        return lockResourceNames;
    }

    private static void addLockNames(Set<String> lockResourceNames, AllocationRequest request) {
        if (request instanceof MultiAssetAllocationRequest) {
            MultiAssetAllocationRequest req = (MultiAssetAllocationRequest) request;
            if (req.assetIdList != null) {
                lockResourceNames.addAll(req.assetIdList);
            }
        } else if (request instanceof MultiResourceAllocationRequest) {
            MultiResourceAllocationRequest req = (MultiResourceAllocationRequest) request;
            if (req.allocationRequestList != null) {
                for (AllocationRequest request1 : req.allocationRequestList) {
                    addLockNames(lockResourceNames, request1);
                }
            }
        } else if (request.assetId != null) {
            lockResourceNames.add(request.assetId);
        }
    }

    @Override
    public void _exec() throws ResourceLockedException {
        outcome = allocate(request);
        if (outcome.status == AllocationStatus.Success) {
            for (Resource r : updateList) {
                resourceDao.saveResource(r);
            }
        }
    }

    private AllocationOutcome allocate(AllocationRequest allocationRequest) throws ResourceLockedException {
        if (allocationRequest instanceof MultiAssetAllocationRequest) {
            return allocateMultiAsset((MultiAssetAllocationRequest) allocationRequest);
        }
        if (allocationRequest instanceof MultiResourceAllocationRequest) {
            return allocateMultiResource((MultiResourceAllocationRequest) allocationRequest);
        }
        if (allocationRequest instanceof LimitAllocationRequest) {
            return allocateLimit((LimitAllocationRequest) allocationRequest);
        }
        if (allocationRequest instanceof LabelAllocationRequest) {
            return allocateLabel((LabelAllocationRequest) allocationRequest);
        }
        if (allocationRequest instanceof RangeAllocationRequest) {
            return allocateRange((RangeAllocationRequest) allocationRequest);
        }
        return null;
    }

    private MultiAssetAllocationOutcome allocateMultiAsset(MultiAssetAllocationRequest req) {
        // TODO Auto-generated method stub
        return null;
    }

    private MultiResourceAllocationOutcome allocateMultiResource(MultiResourceAllocationRequest req) {
        MultiResourceAllocationOutcome out = new MultiResourceAllocationOutcome();
        out.request = req;
        out.allocationOutcomeList = new ArrayList<>();
        out.status = AllocationStatus.Success;

        if (req.allocationRequestList != null) {
            for (AllocationRequest req1 : req.allocationRequestList) {
                AllocationOutcome out1 = allocate(req1);
                out.allocationOutcomeList.add(out1);
                if (out1.status != AllocationStatus.Success) {
                    out.status = AllocationStatus.Failure;
                }
            }
        }

        return out;
    }

    private LimitAllocationOutcome allocateLimit(LimitAllocationRequest req) {
        LimitAllocationOutcome out = new LimitAllocationOutcome();
        out.request = req;

        Resource r = resourceDao.getResource(req.assetId, req.resourceName);
        if (r == null) {
            r = new LimitResource();
            r.resourceKey = new ResourceKey();
            r.resourceKey.assetId = req.assetId;
            r.resourceKey.resourceName = req.resourceName;
            r.resourceType = ResourceType.Limit;
        } else {
            if (r.resourceType != ResourceType.Limit) {
                out.status = AllocationStatus.ResourceNotFound;
                return out;
            }
            LimitUtil.recalculate((LimitResource) r);
        }

        LimitResource l = (LimitResource) r;
        if (LimitUtil.checkLimit(l, req)) {
            out.status = AllocationStatus.Success;
            if (req.allocateCount > 0) {
                out.allocatedCount = LimitUtil.allocateLimit(l, req);
                updateList.add(l);
            }
        } else {
            out.status = AllocationStatus.Failure;
        }

        out.used = l.used;
        out.limit = req.checkLimit;

        return out;
    }

    private LabelAllocationOutcome allocateLabel(LabelAllocationRequest req) {
        LabelAllocationOutcome out = new LabelAllocationOutcome();

        out.request = req;

        Resource r = resourceDao.getResource(req.assetId, req.resourceName);
        if (r == null) {
            r = new LabelResource();
            r.resourceKey = new ResourceKey();
            r.resourceKey.assetId = req.assetId;
            r.resourceKey.resourceName = req.resourceName;
            r.resourceType = ResourceType.Label;
        } else {
            if (r.resourceType != ResourceType.Label) {
                out.status = AllocationStatus.ResourceNotFound;
                return out;
            }
            LabelUtil.recalculate((LabelResource) r);
        }

        LabelResource l = (LabelResource) r;
        if (LabelUtil.checkLabel(l, req)) {
            out.status = AllocationStatus.Success;
            out.currentLabel = l.label;
            if (req.allocate) {
                out.allocatedLabel = LabelUtil.allocateLabel(l, req);
                updateList.add(l);
            }
        } else {
            out.status = AllocationStatus.Failure;
        }

        return out;
    }

    private RangeAllocationOutcome allocateRange(RangeAllocationRequest req) {
        RangeAllocationOutcome out = new RangeAllocationOutcome();

        out.request = req;

        Resource r = resourceDao.getResource(req.assetId, req.resourceName);
        if (r == null) {
            r = new RangeResource();
            r.resourceKey = new ResourceKey();
            r.resourceKey.assetId = req.assetId;
            r.resourceKey.resourceName = req.resourceName;
            r.resourceType = ResourceType.Range;
        } else {
            if (r.resourceType != ResourceType.Range) {
                out.status = AllocationStatus.ResourceNotFound;
                return out;
            }
            RangeUtil.recalculate((RangeResource) r);
        }

        RangeResource rr = (RangeResource) r;
        SortedSet<Integer> foundNumbers = null;
        if (!req.check) {
            out.status = AllocationStatus.Success;
            foundNumbers = req.requestedNumbers;
        } else {
            if (req.requestedNumbers != null && req.requestedNumbers.size() > 0) {
                foundNumbers = req.requestedNumbers;
                out.status = AllocationStatus.Success;
                for (int n : foundNumbers) {
                    if (!RangeUtil.checkRange(rr, req, n)) {
                        out.status = AllocationStatus.Failure;
                        break;
                    }
                }
            } else {
                foundNumbers = new TreeSet<>();
                int foundCount = 0;

                // First try to reuse the numbers already taken by the same resource union
                SortedSet<Integer> uu = RangeUtil.getUsed(rr, req.resourceUnionId);
                if (uu != null && !uu.isEmpty() && req.replace) {
                    if (uu.size() >= req.requestedCount) {
                        // Just take the first req.requestedCount numbers from uu
                        Iterator<Integer> i = uu.iterator();
                        while (foundCount < req.requestedCount) {
                            foundNumbers.add(i.next());
                            foundCount++;
                        }
                    } else {
                        // Additional numbers are requested. Try to find them starting from
                        // the minimum we have in uu (the first element) towards the min
                        // parameter, and then starting from the maximum in uu (the last
                        // element) towards the max parameter.
                        // NOTE: In case of request for sequential numbers, the parameters
                        // alignBlockSize and alignModulus are ignored. It would be harder
                        // to take them into account, and currently it is not needed.

                        // Request may contain multiple ranges. We will find the range from the request
                        // that contains the currently used numbers (the first one). We will only look
                        // for additional numbers in that range.

                        Range range = null;
                        if (req.rangeList != null) {
                            for (Range range1 : req.rangeList) {
                                if (uu.first() >= range1.min && uu.first() <= range1.max) {
                                    range = range1;
                                    break;
                                }
                            }
                        }

                        if (range != null) {
                            int uumin = uu.first() - 1;
                            int uumax = uu.last() + 1;
                            foundNumbers.addAll(uu);
                            foundCount = uu.size();
                            for (int n = uumin; foundCount < req.requestedCount && n >= range.min; n--) {
                                if (RangeUtil.checkRange(rr, req, n)) {
                                    foundNumbers.add(n);
                                    foundCount++;
                                } else if (req.sequential) {
                                    break;
                                }
                            }
                            for (int n = uumax; foundCount < req.requestedCount && n <= range.max; n++) {
                                if (RangeUtil.checkRange(rr, req, n)) {
                                    foundNumbers.add(n);
                                    foundCount++;
                                } else if (req.sequential) {
                                    break;
                                }
                            }
                        }

                        // If we could not find enough numbers trying to reuse currently
                        // allocated, reset foundNumbers and foundCount, continue with
                        // the normal allocation of new numbers.
                        if (foundCount < req.requestedCount) {
                            foundNumbers = new TreeSet<>();
                            foundCount = 0;
                        }
                    }
                }

                if (req.rangeList != null) {
                    if (req.reverseOrder) {
                        for (int i = req.rangeList.size() - 1; i >= 0; i--) {
                            Range range = req.rangeList.get(i);
                            for (int n = range.max; foundCount < req.requestedCount && n >= range.min; n--) {
                                if (RangeUtil.checkRange(rr, req, n)) {
                                    foundNumbers.add(n);
                                    foundCount++;
                                } else if (req.sequential) {
                                    foundCount = 0;
                                }
                            }
                        }
                    } else {
                        for (Range range : req.rangeList) {
                            for (int n = range.min; foundCount < req.requestedCount && n <= range.max; n++) {
                                if (RangeUtil.checkRange(rr, req, n)) {
                                    foundNumbers.add(n);
                                    foundCount++;
                                } else if (req.sequential) {
                                    foundCount = 0;
                                }
                            }
                        }
                    }
                }

                out.status = foundCount == req.requestedCount ? AllocationStatus.Success : AllocationStatus.Failure;
            }
        }

        if (out.status == AllocationStatus.Success) {
            out.allocated = foundNumbers;
            if (req.allocate) {
                RangeUtil.allocateRange(rr, out.allocated, req);
                updateList.add(rr);
            }
        } else {
            out.allocated = new TreeSet<>();
        }

        out.used = rr.used;

        return out;
    }

    public AllocationOutcome getAllocationOutcome() {
        return outcome;
    }
}
