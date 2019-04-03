package org.onap.ccsdk.sli.adaptors.netbox.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import java.io.IOException;
import org.onap.ccsdk.sli.adaptors.netbox.impl.NetboxClientImpl;
import org.onap.ccsdk.sli.adaptors.netbox.impl.NetboxHttpClient;
import org.onap.ccsdk.sli.adaptors.netbox.property.NetboxProperties;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetboxClientModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(NetboxClientModule.class);

    private final DbLibService dbLibService;

    private NetboxProperties netboxProperties;
    private NetboxHttpClient netboxHttpClient;
    private NetboxClientImpl netboxClient;

    public NetboxClientModule(DbLibService dbLibService) {
        this.dbLibService = dbLibService;
    }

    @Override
    protected boolean initProcedure() {
        this.netboxProperties = new NetboxProperties();
        this.netboxHttpClient = new NetboxHttpClient(netboxProperties);
        this.netboxClient = new NetboxClientImpl(netboxHttpClient, dbLibService);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        try {
            netboxHttpClient.close();
        } catch (IOException e) {
            LOG.error("Exception thrown while closing {}!", netboxHttpClient.getClass(), e);
            return false;
        }
        return true;
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
}
