package org.onap.ccsdk.sli.adaptors.aai.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.aai.AAIServiceLighty;
import org.onap.ccsdk.sli.adaptors.aai.AAIServiceProvider;

public class AaaServiceModule extends AbstractLightyModule {

    private AAIServiceProvider aaiServiceProvider;
    private AAIServiceLighty aaiService;

    @Override
    protected boolean initProcedure() {
        this.aaiServiceProvider = new AAIServiceProvider();
        this.aaiService = new AAIServiceLighty(aaiServiceProvider);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public AAIServiceProvider getAAIServiceProvider() {
        return this.aaiServiceProvider;
    }

    public AAIServiceLighty getAAIService() {
        return aaiService;
    }
}
