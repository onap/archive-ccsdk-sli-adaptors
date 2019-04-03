package org.onap.ccsdk.sli.adaptors.saltstack.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.saltstack.impl.SaltstackAdapterImpl;
import org.onap.ccsdk.sli.adaptors.saltstack.impl.SaltstackAdapterPropertiesProviderImpl;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaltstackAdapterModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(SaltstackAdapterModule.class);

    private SaltstackAdapterPropertiesProviderImpl salstackPropertiesProvider;
    private SaltstackAdapterImpl saltstackAdapter;

    @Override
    protected boolean initProcedure() {
        this.salstackPropertiesProvider = new SaltstackAdapterPropertiesProviderImpl();
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

    public SaltstackAdapterPropertiesProviderImpl getSalstackPropertiesProvider() {
        return this.salstackPropertiesProvider;
    }

    public SaltstackAdapterImpl getSaltstackAdapter() {
        return saltstackAdapter;
    }
}
