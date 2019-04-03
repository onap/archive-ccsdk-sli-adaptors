package org.onap.ccsdk.sli.grpc.client.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.grpc.cds.BlueprintProcessingClientLighty;
import org.onap.ccsdk.sli.adaptors.grpc.cds.GrpcPropertiesLighty;

public class GrpcResourceModule extends AbstractLightyModule {

    private GrpcPropertiesLighty grpcProperties;
    private BlueprintProcessingClientLighty blueprintProcessingClient;

    @Override
    protected boolean initProcedure() {
        this.grpcProperties = new GrpcPropertiesLighty();
        this.blueprintProcessingClient = new BlueprintProcessingClientLighty(grpcProperties);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public GrpcPropertiesLighty getGrpcProperties() {
        return this.grpcProperties;
    }

    public BlueprintProcessingClientLighty getBlueprintProcessingClient() {
        return blueprintProcessingClient;
    }
}
