package org.onap.ccsdk.sli.adaptors.ansible.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import io.lighty.core.controller.api.LightyModule;
import org.onap.ccsdk.sli.adaptors.ansible.impl.AnsibleAdapterImpl;
import org.onap.ccsdk.sli.adaptors.ansible.impl.AnsibleAdapterPropertiesProviderImpl;

public class AnsibleAdapterModule extends AbstractLightyModule implements LightyModule {

    private final AnsibleAdapterPropertiesProviderImpl ansibleProviderImpl;
    private final AnsibleAdapterImpl ansibleAdapterImpl;

    public AnsibleAdapterModule() {
        this.ansibleProviderImpl = new AnsibleAdapterPropertiesProviderImpl();
        this.ansibleAdapterImpl = new AnsibleAdapterImpl(ansibleProviderImpl);
    }

    public AnsibleAdapterPropertiesProviderImpl getAnsibleAdapterPropertiesProviderImpl() {
        return this.ansibleProviderImpl;
    }

    public AnsibleAdapterImpl getAnsibleAdapterImpl() {
        return ansibleAdapterImpl;
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