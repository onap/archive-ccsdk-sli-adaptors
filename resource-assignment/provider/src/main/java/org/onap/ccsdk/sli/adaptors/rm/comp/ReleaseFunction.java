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

package org.onap.ccsdk.sli.adaptors.rm.comp;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.onap.ccsdk.sli.adaptors.lock.comp.LockHelper;
import org.onap.ccsdk.sli.adaptors.lock.comp.ResourceLockedException;
import org.onap.ccsdk.sli.adaptors.lock.comp.SynchronizedFunction;
import org.onap.ccsdk.sli.adaptors.rm.dao.ResourceDao;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationItem;
import org.onap.ccsdk.sli.adaptors.rm.data.Resource;
import org.onap.ccsdk.sli.adaptors.rm.util.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ReleaseFunction extends SynchronizedFunction {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(ReleaseFunction.class);

    private ResourceDao resourceDao;

    private String resourceSetId, resourceUnionId;

    public ReleaseFunction(LockHelper lockHelper, ResourceDao resourceDao, String resourceSetId,
            String resourceUnionId, Collection<String> lockNames, int lockTimeout) {
        super(lockHelper, lockNames, lockTimeout);
        this.resourceDao = resourceDao;
        this.resourceSetId = resourceSetId;
        this.resourceUnionId = resourceUnionId;
    }

    @Override
    public void _exec() throws ResourceLockedException {
        List<Resource> resourceList =
                resourceSetId != null
                        ? resourceDao.getResourceSet(resourceSetId) : resourceDao.getResourceUnion(resourceUnionId);
        for (Resource r : resourceList) {
            boolean updated = false;
            if (r.allocationItems != null) {
                Iterator<AllocationItem> i = r.allocationItems.iterator();
                while (i.hasNext()) {
                    AllocationItem ai = i.next();
                    if (resourceSetId != null) {
                        if (resourceSetId.equals(ai.resourceSetId)) {
                            i.remove();
                            updated = true;
                        }

                    } else if (resourceUnionId != null) {

                        if (resourceUnionId.equals(ai.resourceUnionId)) {
                            i.remove();
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
