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
package org.onap.ccsdk.sli.adaptors.aai.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.aai.AAIClientLighty;
import org.onap.ccsdk.sli.adaptors.aai.AAIServiceLighty;
import org.onap.ccsdk.sli.adaptors.aai.AAIServiceProviderLighty;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that manages and provides services from
 * the aai-service-provider artifact.
 */
public class AaaServiceModule extends AbstractLightyModule {

    private AAIServiceProviderLighty aaiServiceProvider;
    private AAIServiceLighty aaiService;

    @Override
    protected boolean initProcedure() {
        this.aaiServiceProvider = new AAIServiceProviderLighty();
        this.aaiService = new AAIServiceLighty(aaiServiceProvider);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public AAIServiceLighty getAAIClient() {
        return aaiService;
    }

    // FIXME original blueprint is exposing AAIClient interface but it contains dependencies on AAIRequest which
    //  contains dependencies on OSGi - rewrite this AAIRequest into interface or remove the OSGi dependency
    public AAIClientLighty getAAIService() {
        return aaiService;
    }

}
