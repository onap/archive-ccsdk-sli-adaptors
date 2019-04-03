package org.onap.ccsdk.sli.adaptors.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import io.lighty.core.controller.api.LightyModule;
import java.util.concurrent.ExecutionException;
import org.onap.ccsdk.sli.adaptors.aai.lighty.AaaServiceModule;
import org.onap.ccsdk.sli.adaptors.ansible.lighty.AnsibleAdapterModule;
import org.onap.ccsdk.sli.adaptors.netbox.lighty.NetboxClientModule;
import org.onap.ccsdk.sli.adaptors.resource.lighty.ResourceModule;
import org.onap.ccsdk.sli.adaptors.resource.mdsal.lighty.MdsalResourceModule;
import org.onap.ccsdk.sli.adaptors.resource.sql.lighty.SqlModule;
import org.onap.ccsdk.sli.adaptors.saltstack.lighty.SaltstackAdapterModule;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.grpc.client.lighty.GrpcResourceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        if (!startLightyModule(aaaServiceModule)) {
            LOG.error("Unable to start AaaServiceModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.ansibleAdapterModule = new AnsibleAdapterModule();
        if (!startLightyModule(ansibleAdapterModule)) {
            LOG.error("Unable to start AnsibleAdapterModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.grpcResourceModule = new GrpcResourceModule();
        if (!startLightyModule(grpcResourceModule)) {
            LOG.error("Unable to start GrpcResourceModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.mdsalResourceModule = new MdsalResourceModule();
        if (!startLightyModule(mdsalResourceModule)) {
            LOG.error("Unable to start MdsalResourceModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.netboxClientModule = new NetboxClientModule(dbLibService);
        if (!startLightyModule(netboxClientModule)) {
            LOG.error("Unable to start NetboxClientModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.resourceModule = new ResourceModule(dbLibService);
        if (!startLightyModule(resourceModule)) {
            LOG.error("Unable to start ResourceModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.saltstackAdapterModule = new SaltstackAdapterModule();
        if (!startLightyModule(saltstackAdapterModule)) {
            LOG.error("Unable to start SaltstackAdapterModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        this.sqlModule = new SqlModule(dbLibService);
        if (!startLightyModule(sqlModule)) {
            LOG.error("Unable to start SqlModule in CCSDK Adaptors Lighty module!");
            return false;
        }

        LOG.debug("CCSDK Adaptors Lighty module was initialized successfully");
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        LOG.debug("Stopping CCSDK Adaptors Lighty module...");

        boolean stopSuccessfull = true;

        if (!stopLightyModule(sqlModule)) {
            stopSuccessfull = false;
        }

        if (!stopLightyModule(saltstackAdapterModule)) {
            stopSuccessfull = false;
        }

        if (!stopLightyModule(resourceModule)) {
            stopSuccessfull = false;
        }

        if (!stopLightyModule(netboxClientModule)) {
            stopSuccessfull = false;
        }

        if (!stopLightyModule(mdsalResourceModule)) {
            stopSuccessfull = false;
        }

        if (!stopLightyModule(grpcResourceModule)) {
            stopSuccessfull = false;
        }

        if (!stopLightyModule(ansibleAdapterModule)) {
            stopSuccessfull = false;
        }

        if (!stopLightyModule(aaaServiceModule)) {
            stopSuccessfull = false;
        }

        if (stopSuccessfull) {
            LOG.debug("CCSDK Adaptors Lighty module was stopped successfully");
        } else {
            LOG.error("CCSDK Adaptors Lighty module was not stopped successfully!");
        }
        return stopSuccessfull;
    }

    // TODO move this method somewhere to utils?
    private boolean startLightyModule(LightyModule lightyModule) {
        try {
            return lightyModule.start().get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Exception thrown while initializing {} in CCSDK Adaptors Lighty module!", lightyModule.getClass(),
                    e);
            return false;
        }
    }

    // TODO move this method somewhere to utils?
    private boolean stopLightyModule(LightyModule lightyModule) {
        try {
            if (!lightyModule.shutdown().get()) {
                LOG.error("{} was not stopped successfully in CCSDK Adaptors Lighty module!", lightyModule.getClass());
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            LOG.error("Exception thrown while shutting down {} in CCSDK Adaptors Lighty module!", lightyModule.getClass(),
                    e);
            return false;
        }
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
