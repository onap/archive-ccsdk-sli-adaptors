package org.onap.ccsdk.sli.grpc.client.lighty;


import io.lighty.core.controller.api.AbstractLightyModule;
import io.lighty.core.controller.api.LightyModule;
import org.onap.ccsdk.sli.adaptors.grpc.GrpcProperties;
import org.onap.ccsdk.sli.adaptors.grpc.cds.BlueprintProcessingClient;

public class GrpcResourceModule extends AbstractLightyModule implements LightyModule {

    private final GrpcProperties grpcProperties;
    private final BlueprintProcessingClient blueprintProcessingClient;

    public GrpcResourceModule() {
        this.grpcProperties = new GrpcProperties();
        this.blueprintProcessingClient = new BlueprintProcessingClient(grpcProperties);
    }

    public GrpcProperties getGrpcProperties() {
        return this.grpcProperties;
    }

    public BlueprintProcessingClient getBlueprintProcessingClient() {
        return blueprintProcessingClient;
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