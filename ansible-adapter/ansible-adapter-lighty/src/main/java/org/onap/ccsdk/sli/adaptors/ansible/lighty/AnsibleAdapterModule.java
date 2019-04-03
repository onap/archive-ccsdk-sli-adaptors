package org.onap.ccsdk.sli.adaptors.ansible.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.ansible.impl.AnsibleAdapterImpl;

public class AnsibleAdapterModule extends AbstractLightyModule {

    private AnsibleAdapterPropertiesProviderImplLighty ansibleProviderImpl;
    private AnsibleAdapterImpl ansibleAdapterImpl;

    @Override
    protected boolean initProcedure() {
        this.ansibleProviderImpl = new AnsibleAdapterPropertiesProviderImplLighty();
        this.ansibleAdapterImpl = new AnsibleAdapterImpl(ansibleProviderImpl);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public AnsibleAdapterPropertiesProviderImplLighty getAnsibleAdapterPropertiesProviderImpl() {
        return this.ansibleProviderImpl;
    }

    public AnsibleAdapterImpl getAnsibleAdapterImpl() {
        return ansibleAdapterImpl;
    }
}
