package org.onap.ccsdk.sli.adaptors.netbox.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import io.lighty.core.controller.api.LightyModule;
import java.io.IOException;
import org.onap.ccsdk.sli.adaptors.netbox.impl.NetboxClientImpl;
import org.onap.ccsdk.sli.adaptors.netbox.impl.NetboxHttpClient;
import org.onap.ccsdk.sli.adaptors.netbox.property.NetboxProperties;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

public class NetboxClientModule extends AbstractLightyModule implements LightyModule {

    private final NetboxProperties netboxProperties;
    private final NetboxHttpClient netboxHttpClient;
    private final NetboxClientImpl netboxClient;

    public NetboxClientModule(final DbLibService dbLibService) {
        this.netboxProperties = new NetboxProperties();
        this.netboxHttpClient = new NetboxHttpClient(netboxProperties);
        this.netboxClient = new NetboxClientImpl(netboxHttpClient, dbLibService);
    }

    public NetboxProperties getNetboxProperties() {
        return this.netboxProperties;
    }

    public NetboxHttpClient getNetboxHttpClient() {
        return netboxHttpClient;
    }

    public NetboxClientImpl getNetboxClientImpl() {
        return netboxClient;
    }

    @Override
    protected boolean initProcedure() {
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        try {
            netboxHttpClient.close();
        } catch (IOException e) {

            return false;
        }
        return true;
    }
}