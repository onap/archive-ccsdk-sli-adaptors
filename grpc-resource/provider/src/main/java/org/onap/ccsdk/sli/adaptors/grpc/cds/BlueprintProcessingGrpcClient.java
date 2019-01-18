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
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc.BluePrintProcessingServiceStub;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlueprintProcessingGrpcClient {

    private static final Logger log = LoggerFactory.getLogger(BlueprintProcessingGrpcClient.class)

    private ManagedChannel channel;
    private BluePrintProcessingServiceStub asyncStub;

    public BlueprintProcessingGrpcClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    private BlueprintProcessingGrpcClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        asyncStub = BluePrintProcessingServiceGrpc.newStub(channel);
    }

    public void start() {
        try {
            process();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process() throws Exception {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<ExecutionServiceInput> requestObserver =
            asyncStub.process(new StreamObserver<ExecutionServiceOutput>() {
                @Override
                public void onNext(ExecutionServiceOutput output) {
                    log.info("Got result {}", output);
                }

                @Override
                public void onError(Throwable t) {
                    Status status = Status.fromThrowable(t);
                    log.error("Failed: {}", status, t);
                    finishLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    finishLatch.countDown();
                }
            });

        try {
            ExecutionServiceInput request = ExecutionServiceInput.newBuilder().build();
            requestObserver.onNext(request);
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            throw e;
        }

        // Mark the end of requests
        requestObserver.onCompleted();

        // Receiving happens asynchronously
        finishLatch.await(1, TimeUnit.MINUTES);
    }


}
