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
package org.onap.ccsdk.sli.adaptors.resource.mdsal.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.resource.mdsal.ConfigResource;
import org.onap.ccsdk.sli.adaptors.resource.mdsal.MdsalResourcePropertiesProviderImpl;
import org.onap.ccsdk.sli.adaptors.resource.mdsal.OperationalResource;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that manages and provides services from
 * the mdsal-resource-provider artifact.
 */
public class MdsalResourceModule extends AbstractLightyModule {

    private MdsalResourcePropertiesProviderImpl mdsalResourcePropertiesProvider;
    private ConfigResource configResource;
    private OperationalResource operationalResource;

    @Override
    protected boolean initProcedure() {
        this.mdsalResourcePropertiesProvider = new MdsalResourcePropertiesProviderImpl();
        this.configResource = new ConfigResource(mdsalResourcePropertiesProvider);
        this.operationalResource = new OperationalResource(mdsalResourcePropertiesProvider);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public MdsalResourcePropertiesProviderImpl getMdsalResourcePropertiesProviderImpl() {
        return this.mdsalResourcePropertiesProvider;
    }

    public ConfigResource getConfigResource() {
        return configResource;
    }

    public OperationalResource getOperationalResource() {
        return this.operationalResource;
    }
}
