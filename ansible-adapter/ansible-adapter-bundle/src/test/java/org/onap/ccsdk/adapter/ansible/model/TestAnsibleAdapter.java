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
package org.onap.ccsdk.adapter.ansible.model;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleMessageParser;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleResult;
import org.onap.ccsdk.sli.adaptors.ansible.model.AnsibleServerEmulator;

public class TestAnsibleAdapter {

    private Class[] parameterTypes;
    private AnsibleMessageParser ansibleMessageParser;
    private Method m;
    private String name;

    @Test
    public void callPrivateConstructorsMethodsForCodeCoverage() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {

          /* test constructors */
          Class<?>[] classesOne = {AnsibleMessageParser.class};
          for(Class<?> clazz : classesOne) {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                name = constructor.getName();
                constructor.setAccessible(true);
                assertNotNull(constructor.newInstance());
          }
          Class<?>[] classesTwo = {AnsibleServerEmulator.class};
          for(Class<?> clazz : classesTwo) {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                name = constructor.getName();
                constructor.setAccessible(true);
                assertNotNull(constructor.newInstance());
          }
          Class<?>[] classesThree = {AnsibleResult.class};
          for(Class<?> clazz : classesThree) {
                Constructor<?> constructor = clazz.getDeclaredConstructor();
                name = constructor.getName();
                constructor.setAccessible(true);
                assertNotNull(constructor.newInstance());
          }

          /* test methods */
          ansibleMessageParser = new AnsibleMessageParser();
          parameterTypes = new Class[1];
          parameterTypes[0] = java.lang.String.class;

          m = ansibleMessageParser.getClass().getDeclaredMethod("getFilePayload", parameterTypes);
          m.setAccessible(true);
          assertNotNull(m.invoke(ansibleMessageParser,"{\"test\": test}"));

    }
}
