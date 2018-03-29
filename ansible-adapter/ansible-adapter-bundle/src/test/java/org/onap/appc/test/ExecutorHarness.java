/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 * 
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */


package org.onap.appc.test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onap.appc.test.InterceptLogger;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;

/**
 * This class is used as a test harness to wrap the call to an executor node.
 */

public class ExecutorHarness {

    /**
     * The executor to be tested
     */
    private SvcLogicJavaPlugin executor;

    /**
     * The collection of all exec methods found on the class
     */
    private Map<String, Method> methods;

    /**
     * The field of the class being tested that contains the reference to the logger to be used. This is modified to
     * point to our interception logger for the test.
     */
    private Field contextLogger;

    /**
     * The interception logger that buffers all messages logged and allows us to look at them as part of the test case.
     */
    private InterceptLogger logger;

    /**
     * Create the harness and initialize it
     * 
     * @throws SecurityException
     *             If a security manager, s, is present and any of the following conditions is met:
     *             <ul>
     *             <li>invocation of s.checkMemberAccess(this, Member.DECLARED) denies access to the declared field</li>
     *             <li>the caller's class loader is not the same as or an ancestor of the class loader for the current
     *             class and invocation of s.checkPackageAccess() denies access to the package of this class</li>
     *             </ul>
     * @throws NoSuchFieldException
     *             if a field with the specified name is not found.
     * @throws IllegalAccessException
     *             if this Field object is enforcing Java language access control and the underlying field is either
     *             inaccessible or final.
     * @throws IllegalArgumentException
     *             if the specified object is not an instance of the class or interface declaring the underlying field
     *             (or a subclass or implementor thereof), or if an unwrapping conversion fails.
     */
    @SuppressWarnings("nls")
    public ExecutorHarness() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
                    IllegalAccessException {
        methods = new HashMap<>();
        new SvcLogicContext();

        Class<?> contextClass = SvcLogicContext.class;
        contextLogger = contextClass.getDeclaredField("LOG");
        contextLogger.setAccessible(true);
        logger = new InterceptLogger();
        contextLogger.set(null, logger);
    }

    /**
     * Convenience constructor
     * 
     * @param executor
     *            The executor to be tested by the harness
     * @throws SecurityException
     *             If a security manager, s, is present and any of the following conditions is met:
     *             <ul>
     *             <li>invocation of s.checkMemberAccess(this, Member.DECLARED) denies access to the declared field</li>
     *             <li>the caller's class loader is not the same as or an ancestor of the class loader for the current
     *             class and invocation of s.checkPackageAccess() denies access to the package of this class</li>
     *             </ul>
     * @throws NoSuchFieldException
     *             if a field with the specified name is not found.
     * @throws IllegalAccessException
     *             if this Field object is enforcing Java language access control and the underlying field is either
     *             inaccessible or final.
     * @throws IllegalArgumentException
     *             if the specified object is not an instance of the class or interface declaring the underlying field
     *             (or a subclass or implementor thereof), or if an unwrapping conversion fails.
     */
    public ExecutorHarness(SvcLogicJavaPlugin executor) throws NoSuchFieldException, SecurityException,
                    IllegalArgumentException, IllegalAccessException {
        this();
        setExecutor(executor);
    }

    /**
     * @param executor
     *            The java plugin class to be executed
     */
    public void setExecutor(SvcLogicJavaPlugin executor) {
        this.executor = executor;
        scanExecutor();
    }

    /**
     * @return The java plugin class to be executed
     */
    public SvcLogicJavaPlugin getExecutor() {
        return executor;
    }

    /**
     * @return The set of all methods that meet the signature requirements
     */
    public List<String> getExecMethodNames() {
        List<String> names = new ArrayList<>();
        names.addAll(methods.keySet());
        return names;
    }

    /**
     * Returns an indication if the named method is a valid executor method that could be called from a DG execute node
     * 
     * @param methodName
     *            The method name to be validated
     * @return True if the method name meets the signature requirements, false if the method either does not exist or
     *         does not meet the requirements.
     */
    public boolean isExecMethod(String methodName) {
        return methods.containsKey(methodName);
    }

    /**
     * This method scans the executor class hierarchy to locate all methods that match the required signature of the
     * executor and records these methods in a map.
     */
    private void scanExecutor() {
        methods.clear();
        Class<?> executorClass = executor.getClass();
        Method[] publicMethods = executorClass.getMethods();
        for (Method method : publicMethods) {
            if (method.getReturnType().equals(Void.class)) {
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == 2) {
                    if (Map.class.isAssignableFrom(paramTypes[0])
                        && SvcLogicContext.class.isAssignableFrom(paramTypes[1])) {
                        methods.put(method.getName(), method);
                    }
                }
            }
        }
    }
}
