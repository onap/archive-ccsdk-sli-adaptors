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
package org.onap.ccsdk.sli.adaptors.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.aai.lighty.AaaServiceModule;
import org.onap.ccsdk.sli.adaptors.ansible.lighty.AnsibleAdapterModule;
import org.onap.ccsdk.sli.adaptors.netbox.lighty.NetboxClientModule;
import org.onap.ccsdk.sli.adaptors.resource.lighty.ResourceModule;
import org.onap.ccsdk.sli.adaptors.resource.mdsal.lighty.MdsalResourceModule;
import org.onap.ccsdk.sli.adaptors.resource.sql.lighty.SqlModule;
import org.onap.ccsdk.sli.adaptors.saltstack.lighty.SaltstackAdapterModule;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.lighty.common.CcsdkLightyUtils;
import org.onap.ccsdk.sli.grpc.client.lighty.GrpcResourceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the {@link io.lighty.core.controller.api.LightyModule} that groups all other LightyModules
 * from the ccsdk-sli-adaptors repository so they can be all treated as one component (for example started/stopped
 * at once).
 * For more information about the lighty.io visit the website https://lighty.io.
 */
public class CcsdkAdaptorsLightyModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(CcsdkAdaptorsLightyModule.class);

    private DbLibService dbLibService;

    private AaaServiceModule aaaServiceModule;
    private AnsibleAdapterModule ansibleAdapterModule;
    private GrpcResourceModule grpcResourceModule;
    private MdsalResourceModule mdsalResourceModule;
    private NetboxClientModule netboxClientModule;
    private ResourceModule resourceModule;
    private SaltstackAdapterModule saltstackAdapterModule;
    private SqlModule sqlModule;

    public CcsdkAdaptorsLightyModule(DbLibService dbLibService) {
        this.dbLibService = dbLibService;
    }

    @Override
    protected boolean initProcedure() {
        LOG.debug("Initializing CCSDK Adaptors Lighty module...");

        this.aaaServiceModule = new AaaServiceModule();
        if (!CcsdkLightyUtils.startLightyModule(aaaServiceModule)) {
            LOG.error("Unable to start AaaServiceModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.ansibleAdapterModule = new AnsibleAdapterModule();
        if (!CcsdkLightyUtils.startLightyModule(ansibleAdapterModule)) {
            LOG.error("Unable to start AnsibleAdapterModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.grpcResourceModule = new GrpcResourceModule();
        if (!CcsdkLightyUtils.startLightyModule(grpcResourceModule)) {
            LOG.error("Unable to start GrpcResourceModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.mdsalResourceModule = new MdsalResourceModule();
        if (!CcsdkLightyUtils.startLightyModule(mdsalResourceModule)) {
            LOG.error("Unable to start MdsalResourceModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.netboxClientModule = new NetboxClientModule(dbLibService);
        if (!CcsdkLightyUtils.startLightyModule(netboxClientModule)) {
            LOG.error("Unable to start NetboxClientModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.resourceModule = new ResourceModule(dbLibService);
        if (!CcsdkLightyUtils.startLightyModule(resourceModule)) {
            LOG.error("Unable to start ResourceModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.saltstackAdapterModule = new SaltstackAdapterModule();
        if (!CcsdkLightyUtils.startLightyModule(saltstackAdapterModule)) {
            LOG.error("Unable to start SaltstackAdapterModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.sqlModule = new SqlModule(dbLibService);
        if (!CcsdkLightyUtils.startLightyModule(sqlModule)) {
            LOG.error("Unable to start SqlModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        LOG.debug("CCSDK Adaptors Lighty module was initialized successfully");
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        LOG.debug("Stopping CCSDK Adaptors Lighty module...");

        boolean stopSuccessful = true;

        if (!CcsdkLightyUtils.stopLightyModule(sqlModule)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(saltstackAdapterModule)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(resourceModule)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(netboxClientModule)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(mdsalResourceModule)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(grpcResourceModule)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(ansibleAdapterModule)) {
            stopSuccessful = false;
        }

        if (!CcsdkLightyUtils.stopLightyModule(aaaServiceModule)) {
            stopSuccessful = false;
        }

        if (stopSuccessful) {
            LOG.debug("CCSDK Adaptors Lighty module was stopped successfully");
        } else {
            LOG.error("CCSDK Adaptors Lighty module was not stopped successfully!");
        }
        return stopSuccessful;
    }

    public AaaServiceModule getAaaServiceModule() {
        return aaaServiceModule;
    }

    public AnsibleAdapterModule getAnsibleAdapterModule() {
        return ansibleAdapterModule;
    }

    public GrpcResourceModule getGrpcResourceModule() {
        return grpcResourceModule;
    }

    public MdsalResourceModule getMdsalResourceModule() {
        return mdsalResourceModule;
    }

    public NetboxClientModule getNetboxClientModule() {
        return netboxClientModule;
    }

    public ResourceModule getResourceModule() {
        return resourceModule;
    }

    public SaltstackAdapterModule getSaltstackAdapterModule() {
        return saltstackAdapterModule;
    }

    public SqlModule getSqlModule() {
        return sqlModule;
    }
}
