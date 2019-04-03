/*
 * ============LICENSE_START==========================================
 * Copyright (c) 2019 PANTHEON.tech s.r.o.
 * ===================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END============================================
 *
 */
package org.onap.ccsdk.sli.adaptors.saltstack.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.saltstack.SaltstackAdapter;
import org.onap.ccsdk.sli.adaptors.saltstack.impl.SaltstackAdapterImpl;
import org.onap.ccsdk.sli.adaptors.saltstack.impl.SaltstackAdapterPropertiesProviderImplLighty;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that manages and provides services from
 * the saltstack-adapter-provider artifact.
 */
public class SaltstackAdapterModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(SaltstackAdapterModule.class);

    private SaltstackAdapterPropertiesProviderImplLighty salstackPropertiesProvider;
    private SaltstackAdapterImpl saltstackAdapter;

    @Override
    protected boolean initProcedure() {
        this.salstackPropertiesProvider = new SaltstackAdapterPropertiesProviderImplLighty();
        try {
            this.saltstackAdapter = new SaltstackAdapterImpl(salstackPropertiesProvider);
        } catch (SvcLogicException e) {
            LOG.error("Exception thrown while initializing {} in {}!", SaltstackAdapterImpl.class, this.getClass(), e);
        }
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public SaltstackAdapterPropertiesProviderImplLighty getSalstackPropertiesProvider() {
        return this.salstackPropertiesProvider;
    }

    public SaltstackAdapter getSaltstackAdapter() {
        return saltstackAdapter;
    }
}
