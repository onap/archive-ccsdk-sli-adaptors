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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class SynchronizedFunction {

    private Set<String> synchset;
    private String lockRequester;
    private int lockTimeout; // Seconds
    private LockHelper lockHelper;

    protected SynchronizedFunction(LockHelper lockHelper, Collection<String> synchset, int lockTimeout) {
        this.lockHelper = lockHelper;
        this.synchset = new HashSet<String>(synchset);
        this.lockRequester = generateLockRequester();
        this.lockTimeout = lockTimeout;
    }

    protected abstract void _exec();

    public void exec() {
        lockHelper.lock(synchset, lockRequester, lockTimeout);
        try {
            _exec();
        } finally {
            lockHelper.unlock(synchset, true);
        }
    }

    private static String generateLockRequester() {
        return "SynchronizedFunction-" + (int) (Math.random() * 1000000);
    }
}
