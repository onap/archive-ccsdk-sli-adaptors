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
package org.onap.ccsdk.sli.adaptors.netbox.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import java.io.IOException;
import org.onap.ccsdk.sli.adaptors.netbox.api.NetboxClient;
import org.onap.ccsdk.sli.adaptors.netbox.impl.NetboxClientImplLighty;
import org.onap.ccsdk.sli.adaptors.netbox.impl.NetboxHttpClientLighty;
import org.onap.ccsdk.sli.adaptors.netbox.property.NetboxPropertiesLighty;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that manages and provides services from
 * the netbox-client-provider artifact.
 */
public class NetboxClientModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(NetboxClientModule.class);

    private final DbLibService dbLibService;

    private NetboxPropertiesLighty netboxProperties;
    private NetboxHttpClientLighty netboxHttpClient;
    private NetboxClientImplLighty netboxClient;

    public NetboxClientModule(DbLibService dbLibService) {
        this.dbLibService = dbLibService;
    }

    @Override
    protected boolean initProcedure() {
        this.netboxProperties = new NetboxPropertiesLighty();
        this.netboxHttpClient = new NetboxHttpClientLighty(netboxProperties);
        this.netboxClient = new NetboxClientImplLighty(netboxHttpClient, dbLibService);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        try {
            netboxHttpClient.close();
        } catch (IOException e) {
            LOG.error("Exception thrown while closing {}!", netboxHttpClient.getClass(), e);
            return false;
        }
        return true;
    }

    public NetboxClient getNetboxClient() {
        return netboxClient;
    }

}
