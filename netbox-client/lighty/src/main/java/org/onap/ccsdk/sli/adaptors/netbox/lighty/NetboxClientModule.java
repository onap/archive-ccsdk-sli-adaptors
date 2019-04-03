package org.onap.ccsdk.sli.adaptors.netbox.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import java.io.IOException;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetboxClientModule extends AbstractLightyModule {

    private static final Logger LOG = LoggerFactory.getLogger(NetboxClientModule.class);

    private final DbLibService dbLibService;

    private NetboxPropertiesLighty netboxProperties;
    private NetboxHttpClientLighty netboxHttpClient;
    private NetboxClientImplLighty netboxClient;

    public NetboxClientModule(DbLibService dbLibService) {
        this.dbLibService = dbLibService;
    }

    @Override
    protected boolean initProcedure() {
        this.netboxProperties = new NetboxPropertiesLighty();
        this.netboxHttpClient = new NetboxHttpClientLighty(netboxProperties);
        this.netboxClient = new NetboxClientImplLighty(netboxHttpClient, dbLibService);
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

    public NetboxPropertiesLighty getNetboxProperties() {
        return this.netboxProperties;
    }

    public NetboxHttpClientLighty getNetboxHttpClient() {
        return netboxHttpClient;
    }

    public NetboxClientImplLighty getNetboxClientImpl() {
        return netboxClient;
    }
}
