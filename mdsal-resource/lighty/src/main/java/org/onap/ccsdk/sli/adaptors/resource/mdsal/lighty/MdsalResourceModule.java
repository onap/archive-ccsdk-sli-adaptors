package org.onap.ccsdk.sli.adaptors.resource.mdsal.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import io.lighty.core.controller.api.LightyModule;
import org.onap.ccsdk.sli.adaptors.resource.mdsal.ConfigResource;
import org.onap.ccsdk.sli.adaptors.resource.mdsal.MdsalResourcePropertiesProviderImpl;
import org.onap.ccsdk.sli.adaptors.resource.mdsal.OperationalResource;

public class MdsalResourceModule extends AbstractLightyModule implements LightyModule {

    private final MdsalResourcePropertiesProviderImpl mdsalResourcePropertiesProvider;
    private final ConfigResource configResource;
    private final OperationalResource operationalResource;

    public MdsalResourceModule() {
        this.mdsalResourcePropertiesProvider = new MdsalResourcePropertiesProviderImpl();
        this.configResource = new ConfigResource(mdsalResourcePropertiesProvider);
        this.operationalResource = new OperationalResource(mdsalResourcePropertiesProvider);
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

    @Override
    protected boolean initProcedure() {
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }
}