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

package org.onap.appc.adapter.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.saltstack.SaltstackAdapterPropertiesProvider;
import org.onap.ccsdk.sli.adaptors.saltstack.impl.SaltstackAdapterImpl;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class TestSaltstackAdapterPropertiesProviderImpl {

    private SaltstackAdapterImpl adapter;
    private Properties params;


    @Before
    public void setup() throws IllegalArgumentException {
        params = new Properties();
    }

    @After
    public void tearDown() {
        adapter = null;
        params = null;
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_setPropertiesBasicPortNull() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.clientType", "BASIC");
        params.put("User", "test");
        params.put("Password", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_setPropertiesBasicPortString() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.clientType", "BASIC");
        params.put("org.onap.appc.adapter.saltstack.host", "test");
        params.put("org.onap.appc.adapter.saltstack.port", "test");
        params.put("org.onap.appc.adapter.saltstack.userName", "test");
        params.put("org.onap.appc.adapter.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adapter.saltstack.sshKey", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesBasicSuccess() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.clientType", "BASIC");
        params.put("org.onap.appc.adapter.saltstack.host", "test");
        params.put("org.onap.appc.adapter.saltstack.port", "10");
        params.put("org.onap.appc.adapter.saltstack.userName", "test");
        params.put("org.onap.appc.adapter.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adapter.saltstack.sshKey", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_setPropertiesSSH_CERTPortNull() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.clientType", "SSH_CERT");
        params.put("User", "test");
        params.put("Password", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_setPropertiesSSH_CERTPortString() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.clientType", "SSH_CERT");
        params.put("org.onap.appc.adapter.saltstack.host", "test");
        params.put("org.onap.appc.adapter.saltstack.port", "test");
        params.put("org.onap.appc.adapter.saltstack.userName", "test");
        params.put("org.onap.appc.adapter.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adapter.saltstack.sshKey", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesSSH_CERTSuccess() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.clientType", "SSH_CERT");
        params.put("org.onap.appc.adapter.saltstack.host", "test");
        params.put("org.onap.appc.adapter.saltstack.port", "10");
        params.put("org.onap.appc.adapter.saltstack.userName", "test");
        params.put("org.onap.appc.adapter.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adapter.saltstack.sshKey", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_setPropertiesBOTHPortNull() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.clientType", "BOTH");
        params.put("User", "test");
        params.put("Password", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }

    @Test(expected = SvcLogicException.class)
    public void reqExecCommand_setPropertiesBOTHPortString() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.clientType", "BOTH");
        params.put("org.onap.appc.adapter.saltstack.host", "test");
        params.put("org.onap.appc.adapter.saltstack.port", "test");
        params.put("org.onap.appc.adapter.saltstack.userName", "test");
        params.put("org.onap.appc.adapter.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adapter.saltstack.sshKey", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesBOTHSuccess() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.clientType", "BOTH");
        params.put("org.onap.appc.adapter.saltstack.host", "test");
        params.put("org.onap.appc.adapter.saltstack.port", "10");
        params.put("org.onap.appc.adapter.saltstack.userName", "test");
        params.put("org.onap.appc.adapter.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adapter.saltstack.sshKey", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesNonePortNull() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.clientType", "NONE");
        params.put("User", "test");
        params.put("Password", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesNonePortString() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.clientType", "NONE");
        params.put("org.onap.appc.adapter.saltstack.host", "test");
        params.put("org.onap.appc.adapter.saltstack.port", "test");
        params.put("org.onap.appc.adapter.saltstack.userName", "test");
        params.put("org.onap.appc.adapter.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adapter.saltstack.sshKey", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesNoneSuccess() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.clientType", "NONE");
        params.put("org.onap.appc.adapter.saltstack.host", "test");
        params.put("org.onap.appc.adapter.saltstack.port", "10");
        params.put("org.onap.appc.adapter.saltstack.userName", "test");
        params.put("org.onap.appc.adapter.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adapter.saltstack.sshKey", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }



    @Test
    public void reqExecCommand_setPropertiesElsePortNull() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("User", "test");
        params.put("Password", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesElsePortString() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.host", "test");
        params.put("org.onap.appc.adapter.saltstack.port", "test");
        params.put("org.onap.appc.adapter.saltstack.userName", "test");
        params.put("org.onap.appc.adapter.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adapter.saltstack.sshKey", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
    }

    @Test
    public void reqExecCommand_setPropertiesElseSuccess() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        params.put("org.onap.appc.adapter.saltstack.host", "test");
        params.put("org.onap.appc.adapter.saltstack.port", "10");
        params.put("org.onap.appc.adapter.saltstack.userName", "test");
        params.put("org.onap.appc.adapter.saltstack.userPasswd", "test");
        params.put("org.onap.appc.adapter.saltstack.sshKey", "test");
        SaltstackAdapterPropertiesProvider propProvider = new SaltstackAdapterPropertiesProvider() {
            @Override
            public Properties getProperties() {
                return params;
            }
        };
        adapter = new SaltstackAdapterImpl(propProvider);
        String adaptorName = adapter.getAdapterName();
        assertEquals("Saltstack Adapter", adaptorName);
        adapter.setExecTimeout(10);
    }

    @Test
    public void reqExecCommand_setPropertiesDefault() throws SvcLogicException,
            IllegalStateException, IllegalArgumentException {
        adapter = new SaltstackAdapterImpl();
    }
}
