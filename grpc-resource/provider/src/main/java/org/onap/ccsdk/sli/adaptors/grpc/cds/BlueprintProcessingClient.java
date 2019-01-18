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
import io.grpc.ManagedChannelBuilder;
import java.util.Map;
import org.onap.ccsdk.sli.adaptors.grpc.GrpcClient;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlueprintProcessingClient implements GrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BlueprintProcessingClient.class);

    private ManagedChannel channel;
    private BlueprintProcessingHandler handler;

    public BlueprintProcessingClient(String host, int port, BlueprintProcessingHandler handler) {
        this.channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
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

    @Override
    public QueryStatus sendRequest(Map<String, String> parameters, SvcLogicContext ctx) {
        return handler.process(parameters, channel);
    }
}