/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                         reserved.
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

package org.onap.ccsdk.sli.adaptors.rm.comp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.onap.ccsdk.sli.adaptors.lock.comp.LockHelper;
import org.onap.ccsdk.sli.adaptors.lock.comp.ResourceLockedException;
import org.onap.ccsdk.sli.adaptors.lock.comp.SynchronizedFunction;
import org.onap.ccsdk.sli.adaptors.rm.dao.ResourceDao;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitAllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.RangeAllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.ReleaseRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.Resource;
import org.onap.ccsdk.sli.adaptors.rm.data.ResourceType;
import org.onap.ccsdk.sli.adaptors.rm.util.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReleaseFunction extends SynchronizedFunction {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ReleaseFunction.class);

    private ResourceDao resourceDao;

    private ReleaseRequest releaseRequest;

    public ReleaseFunction(LockHelper lockHelper, ResourceDao resourceDao, ReleaseRequest releaseRequest,
            Collection<String> lockNames, int lockTimeout) {
        super(lockHelper, lockNames, lockTimeout);
        this.resourceDao = resourceDao;
        this.releaseRequest = releaseRequest;
    }

    @Override
    public void _exec() throws ResourceLockedException {
        List<Resource> resourceList = new ArrayList<>();
        if (releaseRequest.assetId != null && releaseRequest.resourceName != null) {
            Resource r = resourceDao.getResource(releaseRequest.assetId, releaseRequest.resourceName);
            if (r != null) {
                resourceList.add(r);
            }
        } else if (releaseRequest.assetId != null) {
            if (releaseRequest.resourceSetId != null) {
                resourceList = resourceDao.getResourceSetForAsset(releaseRequest.resourceSetId, releaseRequest.assetId);
            } else {
                resourceList =
                        resourceDao.getResourceUnionForAsset(releaseRequest.resourceUnionId, releaseRequest.assetId);
            }
        } else {
            if (releaseRequest.resourceSetId != null) {
                resourceList = resourceDao.getResourceSet(releaseRequest.resourceSetId);
            } else {
                resourceList = resourceDao.getResourceUnion(releaseRequest.resourceUnionId);
            }
        }

        for (Resource r : resourceList) {
            boolean updated = false;
            if (r.allocationItems != null) {
                Iterator<AllocationItem> i = r.allocationItems.iterator();
                while (i.hasNext()) {
                    AllocationItem ai = i.next();
                    if (releaseRequest.resourceSetId != null) {

                        if (releaseRequest.resourceSetId.equals(ai.resourceSetId)) {
                            if (r.resourceType == ResourceType.Limit) {
                                LimitAllocationItem lai = (LimitAllocationItem) ai;
                                if (releaseRequest.releaseAmount > 0 && releaseRequest.releaseAmount < lai.used) {
                                    lai.used -= releaseRequest.releaseAmount;
                                } else {
                                    i.remove();
                                }
                            } else if (r.resourceType == ResourceType.Range) {
                                RangeAllocationItem rai = (RangeAllocationItem) ai;
                                if (releaseRequest.releaseNumbers != null && !releaseRequest.releaseNumbers.isEmpty()) {
                                    rai.used.removeAll(releaseRequest.releaseNumbers);
                                    if (rai.used.isEmpty()) {
                                        i.remove();
                                    }
                                } else {
                                    i.remove();
                                }
                            } else {
                                i.remove();
                            }
                            updated = true;
                        }

                    } else if (releaseRequest.resourceUnionId != null) {

                        if (releaseRequest.resourceUnionId.equals(ai.resourceUnionId)) {
                            if (r.resourceType == ResourceType.Limit) {
                                LimitAllocationItem lai = (LimitAllocationItem) ai;
                                if (releaseRequest.releaseAmount > 0 && releaseRequest.releaseAmount < lai.used) {
                                    lai.used -= releaseRequest.releaseAmount;
                                } else {
                                    i.remove();
                                }
                            } else if (r.resourceType == ResourceType.Range) {
                                RangeAllocationItem rai = (RangeAllocationItem) ai;
                                if (releaseRequest.releaseNumbers != null && !releaseRequest.releaseNumbers.isEmpty()) {
                                    rai.used.removeAll(releaseRequest.releaseNumbers);
                                    if (rai.used.isEmpty()) {
                                        i.remove();
                                    }
                                } else {
                                    i.remove();
                                }
                            } else {
                                i.remove();
                            }
                            updated = true;
                        }

                    }
                }
            }
            if (updated) {
                ResourceUtil.recalculate(r);
                resourceDao.saveResource(r);
            }
        }
    }
}
