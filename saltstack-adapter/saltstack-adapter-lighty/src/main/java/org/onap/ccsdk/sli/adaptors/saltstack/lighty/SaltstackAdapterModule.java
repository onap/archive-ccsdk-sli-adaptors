package org.onap.ccsdk.sli.adaptors.saltstack.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import io.lighty.core.controller.api.LightyModule;
import org.onap.ccsdk.sli.adaptors.saltstack.impl.SaltstackAdapterImpl;
import org.onap.ccsdk.sli.adaptors.saltstack.impl.SaltstackAdapterPropertiesProviderImpl;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;

public class SaltstackAdapterModule extends AbstractLightyModule implements LightyModule {

    private final SaltstackAdapterPropertiesProviderImpl salstackPropertiesProvider;
    private final SaltstackAdapterImpl saltstackAdapter;

    public SaltstackAdapterModule() throws SvcLogicException {
        this.salstackPropertiesProvider = new SaltstackAdapterPropertiesProviderImpl();
        this.saltstackAdapter = new SaltstackAdapterImpl(salstackPropertiesProvider);
    }

    public SaltstackAdapterPropertiesProviderImpl getSalstackPropertiesProvider() {
        return this.salstackPropertiesProvider;
    }

    public SaltstackAdapterImpl getSaltstackAdapter() {
        return saltstackAdapter;
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