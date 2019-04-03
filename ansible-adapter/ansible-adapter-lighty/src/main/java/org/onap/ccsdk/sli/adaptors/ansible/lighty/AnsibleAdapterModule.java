package org.onap.ccsdk.sli.adaptors.ansible.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.ansible.impl.AnsibleAdapterImpl;
import org.onap.ccsdk.sli.adaptors.ansible.impl.AnsibleAdapterPropertiesProviderImpl;

public class AnsibleAdapterModule extends AbstractLightyModule {

    private AnsibleAdapterPropertiesProviderImpl ansibleProviderImpl;
    private AnsibleAdapterImpl ansibleAdapterImpl;

    @Override
    protected boolean initProcedure() {
        this.ansibleProviderImpl = new AnsibleAdapterPropertiesProviderImpl();
        this.ansibleAdapterImpl = new AnsibleAdapterImpl(ansibleProviderImpl);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public AnsibleAdapterPropertiesProviderImpl getAnsibleAdapterPropertiesProviderImpl() {
        return this.ansibleProviderImpl;
    }

    public AnsibleAdapterImpl getAnsibleAdapterImpl() {
        return ansibleAdapterImpl;
    }
}
