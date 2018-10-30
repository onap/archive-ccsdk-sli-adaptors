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

package org.onap.ccsdk.sli.adaptors.ra;

import java.security.SecureRandom;
import java.util.Map;
import org.onap.ccsdk.sli.adaptors.lock.comp.LockHelper;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceLockNode implements SvcLogicJavaPlugin {

    private static final Logger log = LoggerFactory.getLogger(ResourceLockNode.class);

    private LockHelper lockHelper;

    public void setLockHelper(LockHelper lockHelper) {
        this.lockHelper = lockHelper;
    }

    public void lockResource(Map<String, String> paramMap, SvcLogicContext ctx) throws SvcLogicException {
        String resourceName = getParam(paramMap, "resource-name", true, null);
        String lockRequester = getParam(paramMap, "lock-requester", false, generateLockRequester());
        String lockTimeoutStr = getParam(paramMap, "lock-timeout", false, "600"); // Default lock timeout: 10 min
        int lockTimeout = Integer.parseInt(lockTimeoutStr);
        String lockWaitStr = getParam(paramMap, "lock-wait", false, "5"); // Time waiting before next retry. Default: 5 sec
        int lockWait = Integer.parseInt(lockWaitStr);
        String lockRetryCountStr = getParam(paramMap, "lock-retry-count", false, "10"); // Default: 10 retries
        int lockRetryCount = Integer.parseInt(lockRetryCountStr);

        lockHelper.lock(resourceName, lockRequester, lockTimeout, lockWait, lockRetryCount);
    }

    public void unlockResource(Map<String, String> paramMap, SvcLogicContext ctx) throws SvcLogicException {
        String resourceName = getParam(paramMap, "resource-name", true, null);

        lockHelper.unlock(resourceName, false);
    }

    public void lockResource(String resourceName, String lockRequester, int lockTimeout /* sec */) {
        lockResource(resourceName, lockRequester, lockTimeout, 5, 10);
    }

    public void lockResource(String resourceName, String lockRequester, int lockTimeout /* sec */, int lockWait /* Seconds */, int retryCount) {
        if (lockRequester == null) {
            lockRequester = generateLockRequester();
        }
        if (lockTimeout <= 0) {
            lockTimeout = 600;
        }

        lockHelper.lock(resourceName, lockRequester, lockTimeout, lockWait, retryCount);
    }

    public void unlockResource(String resourceName) {
        lockHelper.unlock(resourceName, false);
    }

    private String getParam(Map<String, String> paramMap, String name, boolean required, String def)
            throws SvcLogicException {
        String v = paramMap.get(name);
        if (v != null && v.trim().length() > 0) {
            log.info("Param: " + name + ": " + v.trim());
            return v.trim();
        }
        if (required) {
            throw new SvcLogicException("The following node parameter is required: " + name);
        }

        log.info("Param: " + name + " not supplied. Using default: " + def);
        return def;
    }

    private static String generateLockRequester() {
        SecureRandom rand = new SecureRandom();
        return "SynchronizedFunction-" + (int) (rand.nextDouble() * 1000000);
    }
}

