package org.onap.ccsdk.sli.grpc.client.lighty;

import io.lighty.core.controller.api.AbstractLightyModule;
import org.onap.ccsdk.sli.adaptors.grpc.GrpcProperties;
import org.onap.ccsdk.sli.adaptors.grpc.cds.BlueprintProcessingClient;

public class GrpcResourceModule extends AbstractLightyModule {

    private GrpcProperties grpcProperties;
    private BlueprintProcessingClient blueprintProcessingClient;

    @Override
    protected boolean initProcedure() {
        this.grpcProperties = new GrpcProperties();
        this.blueprintProcessingClient = new BlueprintProcessingClient(grpcProperties);
        return true;
    }

    @Override
    protected boolean stopProcedure() {
        return true;
    }

    public GrpcProperties getGrpcProperties() {
        return this.grpcProperties;
    }

    public BlueprintProcessingClient getBlueprintProcessingClient() {
        return blueprintProcessingClient;
    }
}
