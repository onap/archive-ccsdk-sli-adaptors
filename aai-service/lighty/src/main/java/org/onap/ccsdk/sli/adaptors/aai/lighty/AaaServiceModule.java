package org.onap.ccsdk.sli.adaptors.aai.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.onap.ccsdk.sli.adaptors.aai.AAIServiceProvider;

public class AaaServiceModule extends AbstractLightyModule {

    private AAIServiceProvider aaiServiceProvider;
    private AAIService aaiService;

    @Override
    protected boolean initProcedure() {
        this.aaiServiceProvider = new AAIServiceProvider();
        this.aaiService = new AAIService(aaiServiceProvider);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public AAIServiceProvider getAAIServiceProvider() {
        return this.aaiServiceProvider;
    }

    public AAIService getAAIService() {
        return aaiService;
    }
}
