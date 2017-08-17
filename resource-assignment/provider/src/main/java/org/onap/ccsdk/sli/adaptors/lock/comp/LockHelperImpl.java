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

package org.onap.ccsdk.sli.adaptors.lock.comp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.onap.ccsdk.sli.adaptors.lock.dao.ResourceLockDao;
import org.onap.ccsdk.sli.adaptors.lock.data.ResourceLock;

public class LockHelperImpl implements LockHelper {

    private ResourceLockDao resourceLockDao;
    private int retryCount = 10;
    private int lockWait = 5; // Seconds

    @Override
    public void lock(String resourceName, String lockRequester, int lockTimeout /* Seconds */) {
        lock(Collections.singleton(resourceName), lockRequester, lockTimeout);
    }

    @Override
    public void unlock(String resourceName, boolean force) {
        unlock(Collections.singleton(resourceName), force);
    }

    @Override
    public void lock(Collection<String> resourceNameList, String lockRequester, int lockTimeout /* Seconds */) {
        for (int i = 0; true; i++) {
            try {
                tryLock(resourceNameList, lockRequester, lockTimeout);
                return;
            } catch (ResourceLockedException e) {
                if (i > retryCount)
                    throw e;
                try {
                    Thread.sleep(lockWait * 1000);
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    @Override
    public void unlock(Collection<String> lockNames, boolean force) {
        if (lockNames == null || lockNames.size() == 0)
            return;

        resourceLockDao.lockTable();

        try {
            for (String name : lockNames) {
                ResourceLock l = resourceLockDao.getByResourceName(name);
                if (l != null)
                    if (force || l.lockCount == 1)
                        resourceLockDao.delete(l.id);
                    else
                        resourceLockDao.decrementLockCount(l.id);
            }
        } finally {
            resourceLockDao.unlockTable();
        }
    }

    public void tryLock(Collection<String> resourceNameList, String lockRequester, int lockTimeout /* Seconds */) {
        if (resourceNameList == null || resourceNameList.size() == 0)
            return;

        lockRequester = generateLockRequester(lockRequester, 100);

        resourceLockDao.lockTable();

        try {
            // First check if all requested records are available to lock

            Date now = new Date();

            List<ResourceLock> dbLockList = new ArrayList<ResourceLock>();
            List<String> insertLockNameList = new ArrayList<String>();
            for (String name : resourceNameList) {
                ResourceLock l = resourceLockDao.getByResourceName(name);

                boolean canLock =
                        l == null || now.getTime() > l.expirationTime.getTime() || lockRequester != null &&
                                lockRequester.equals(l.lockHolder) || l.lockCount <= 0;
                if (!canLock)
                    throw new ResourceLockedException(l.resourceName, l.lockHolder, lockRequester);

                if (l != null)
                    dbLockList.add(l);
                else
                    insertLockNameList.add(name);
            }

            // Update the lock info in DB
            for (ResourceLock l : dbLockList)
                resourceLockDao.update(l.id, now, new Date(now.getTime() + lockTimeout * 1000), l.lockCount + 1);

            // Insert records for those that are not yet there
            for (String lockName : insertLockNameList) {
                ResourceLock l = new ResourceLock();
                l.resourceName = lockName;
                l.lockHolder = lockRequester;
                l.lockTime = now;
                l.expirationTime = new Date(now.getTime() + lockTimeout * 1000);
                l.lockCount = 1;
                resourceLockDao.add(l);
            }
        } finally {
            resourceLockDao.unlockTable();
        }
    }

    private static String generateLockRequester(String name, int maxLength) {
        if (name == null)
            name = "";
        int l1 = name.length();
        String tname = Thread.currentThread().getName();
        int l2 = tname.length();
        if (l1 + l2 + 1 > maxLength) {
            int maxl1 = maxLength / 2;
            if (l1 > maxl1) {
                name = name.substring(0, maxl1);
                l1 = maxl1;
            }
            int maxl2 = maxLength - l1 - 1;
            if (l2 > maxl2)
                tname = tname.substring(0, 6) + "..." + tname.substring(l2 - maxl2 + 9);
        }
        return tname + '-' + name;
    }

    public void setResourceLockDao(ResourceLockDao resourceLockDao) {
        this.resourceLockDao = resourceLockDao;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setLockWait(int lockWait /* Seconds */) {
        this.lockWait = lockWait;
    }
}
