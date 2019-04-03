package org.onap.ccsdk.sli.adaptors.aai.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import io.lighty.core.controller.api.LightyModule;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.onap.ccsdk.sli.adaptors.aai.AAIServiceProvider;

public class AaaServiceModule extends AbstractLightyModule implements LightyModule {

    private final AAIServiceProvider aaiServiceProvider;
    private final AAIService aaiService;

    public AaaServiceModule() {
        this.aaiServiceProvider = new AAIServiceProvider();
        this.aaiService = new AAIService(aaiServiceProvider);
    }

    public AAIServiceProvider getAAIServiceProvider() {
        return this.aaiServiceProvider;
    }

    public AAIService getAAIService() {
        return aaiService;
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