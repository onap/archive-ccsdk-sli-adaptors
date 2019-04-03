package org.onap.ccsdk.sli.adaptors.resource.mdsal.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.resource.mdsal.ConfigResource;
import org.onap.ccsdk.sli.adaptors.resource.mdsal.MdsalResourcePropertiesProviderImpl;
import org.onap.ccsdk.sli.adaptors.resource.mdsal.OperationalResource;

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
