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
package org.onap.ccsdk.adapter.model;

import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.saltstack.model.SaltstackMessageParser;
import org.onap.ccsdk.sli.adaptors.saltstack.model.SaltstackResult;
import org.onap.ccsdk.sli.adaptors.saltstack.model.SaltstackServerEmulator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertNotNull;

public class TestSaltstackAdapter {

    private Class[] parameterTypes;
    private SaltstackMessageParser saltstackMessageParser;
    private Method m;

    @Test
    public void callPrivateConstructorsMethodsForCodeCoverage() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {

          /* test constructors */
          Class<?>[] classesOne = {SaltstackMessageParser.class};
          for(Class<?> clazz : classesOne) {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                assertNotNull(constructor.newInstance());
          }
          Class<?>[] classesTwo = {SaltstackServerEmulator.class};
          for(Class<?> clazz : classesTwo) {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                assertNotNull(constructor.newInstance());
          }
          Class<?>[] classesThree = {SaltstackResult.class};
          for(Class<?> clazz : classesThree) {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                constructor.setAccessible(true);
                assertNotNull(constructor.newInstance());
          }

          /* test methods */
          saltstackMessageParser = new SaltstackMessageParser();
          parameterTypes = new Class[1];
          parameterTypes[0] = String.class;

          m = saltstackMessageParser.getClass().getDeclaredMethod("getFilePayload", parameterTypes);
          m.setAccessible(true);
          assertNotNull(m.invoke(saltstackMessageParser,"{\"test\": test}"));

    }
}
