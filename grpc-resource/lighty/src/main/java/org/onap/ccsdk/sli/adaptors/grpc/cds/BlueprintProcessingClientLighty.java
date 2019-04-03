/*
 * Copyright (C) 2019 Bell Canada.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onap.ccsdk.sli.adaptors.grpc.cds;

import io.grpc.ManagedChannel;
import io.grpc.internal.DnsNameResolverProvider;
import io.grpc.internal.PickFirstLoadBalancerProvider;
import io.grpc.netty.NettyChannelBuilder;
import java.util.Map;
import org.onap.ccsdk.sli.adaptors.grpc.GrpcClient;
import org.onap.ccsdk.sli.adaptors.grpc.GrpcPropertiesLighty;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * THIS CLASS IS A COPY OF {@link BlueprintProcessingClient} WITH REMOVED OSGi DEPENDENCIES
 */
public class BlueprintProcessingClientLighty implements GrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BlueprintProcessingClientLighty.class);

    private ManagedChannel channel;
    private BlueprintProcessingHandler handler;

    public BlueprintProcessingClientLighty(GrpcPropertiesLighty props) {
        this.channel = NettyChannelBuilder
            .forAddress(props.getUrl(), props.getPort())
            .nameResolverFactory(new DnsNameResolverProvider())
            .loadBalancerFactory(new PickFirstLoadBalancerProvider())
            .intercept(new BasicAuthClientInterceptorLighty(props))
            .usePlaintext()
            .build();
        this.handler = new BlueprintProcessingHandler();
    }

    public BlueprintProcessingClientLighty(ManagedChannel channel, BlueprintProcessingHandler handler) {
        this.channel = channel;
        this.handler = handler;
    }

    // Used by blueprint
    public void start() {
        log.info("BlueprintProcessingClient started");
    }

    // Used by blueprint
    public void stop() {
        if (channel != null) {
            channel.shutdown();
        }
        log.info("BlueprintProcessingClient stopped");
    }

    /*
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function.
     * <table border="1">
     * <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     * <tbody>
     * <tr><td>is_force</td><td>Optional</td><td>Whether to force or not the request.</td></tr>
     * <tr><td>ttl</td><td>Optional</td><td>Duration of the request.</td></tr>
     * <tr><td>blueprint_name</td><td>Mandatory</td><td>Name of the blueprint to process.</td></tr>
     * <tr><td>blueprint_version</td><td>Mandatory</td><td>Version of the blueprint to process.</td></tr>
     * <tr><td>action</td><td>Mandatory</td><td>Action of the blueprint to process.</td></tr>
     * <tr><td>mode</td><td>Mandatory</td><td>Mode to operate the transaction.</td></tr>
     * <tr><td>payload</td><td>Mandatory</td><td>Payload.</td></tr>
     * <tr><td>prefix</td><td>Mandatory</td><td>Prefix string to put response in context.</td></tr>
     * </tbody>
     * </table>
     */
    @Override
    public QueryStatus sendRequest(Map<String, String> parameters, SvcLogicContext ctx) {
        return handler.process(parameters, channel, ctx);
    }
}
