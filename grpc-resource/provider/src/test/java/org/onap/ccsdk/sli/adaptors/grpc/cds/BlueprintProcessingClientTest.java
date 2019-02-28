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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.Maps;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import io.grpc.util.MutableHandlerRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.onap.ccsdk.apps.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc.BluePrintProcessingServiceImplBase;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.ccsdk.sli.adaptors.grpc.GrpcProperties;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

@RunWith(JUnit4.class)
public class BlueprintProcessingClientTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private BlueprintProcessingClient client;

    private final SvcLogicContext svcLogicContext = new SvcLogicContext();
    private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
    private final List<String> messagesDelivered = new ArrayList<>();
    private final CountDownLatch allRequestsDelivered = new CountDownLatch(1);
    private final AtomicReference<StreamObserver<ExecutionServiceOutput>> responseObserverRef = new AtomicReference<>();

    @Before
    public void setUp() throws Exception {

        String serverName = InProcessServerBuilder.generateName();
        grpcCleanup.register(InProcessServerBuilder.forName(serverName)
            .fallbackHandlerRegistry(serviceRegistry).directExecutor().build().start());

        BlueprintProcessingHandler handler = new BlueprintProcessingHandler();

        client =
            new BlueprintProcessingClient(InProcessChannelBuilder.forName(serverName).directExecutor().build(),
                handler);

        final BluePrintProcessingServiceImplBase routeChatImpl =
            new BluePrintProcessingServiceImplBase() {
                @Override
                public StreamObserver<ExecutionServiceInput> process(
                    StreamObserver<ExecutionServiceOutput> responseObserver) {

                    responseObserverRef.set(responseObserver);

                    StreamObserver<ExecutionServiceInput> requestObserver = new StreamObserver<ExecutionServiceInput>() {
                        @Override
                        public void onNext(ExecutionServiceInput message) {
                            messagesDelivered.add(message.getActionIdentifiers().getActionName());
                        }

                        @Override
                        public void onError(Throwable t) {

                        }

                        @Override
                        public void onCompleted() {
                            allRequestsDelivered.countDown();
                        }
                    };

                    return requestObserver;
                }
            };

        serviceRegistry.addService(routeChatImpl);
    }

    @After
    public void tearDown() {
        client.stop();
    }

    @Test
    public void testClientCst() {
        GrpcProperties props = Mockito.mock(GrpcProperties.class);
        doReturn(999).when(props).getPort();
        doReturn("localhost").when(props).getUrl();
        new BlueprintProcessingClient(props);
    }


    @Test
    public void testSendMessageFail() throws Exception {
        Map<String, String> input = Maps.newHashMap();
        input.put("is_force", "true");
        input.put("ttl", "1");
        input.put("blueprint_name", "test");
        input.put("blueprint_version", "1.0.0");
        input.put("action", "test-action");
        input.put("mode", "sync");
        input.put("payload", "");
        input.put("prefix", "res");

        QueryStatus status = client.sendRequest(input, svcLogicContext);

        Assert.assertEquals(QueryStatus.FAILURE, status);

    }

    @Test
    public void testSendMessage() throws Exception {
        ExecutionServiceOutput fakeResponse1 = ExecutionServiceOutput.newBuilder().setActionIdentifiers(
            ActionIdentifiers.newBuilder().setActionName("response1").build()).build();

        ExecutionServiceOutput fakeResponse2 = ExecutionServiceOutput.newBuilder().setActionIdentifiers(
            ActionIdentifiers.newBuilder().setActionName("response2").build()).build();

        Map<String, String> input = Maps.newHashMap();
        input.put("is_force", "true");
        input.put("ttl", "1");
        input.put("blueprint_name", "test");
        input.put("blueprint_version", "1.0.0");
        input.put("action", "test-action");
        input.put("mode", "sync");
        input.put("payload", "{}");
        input.put("prefix", "res");

        client.sendRequest(input, svcLogicContext);

        // request message sent and delivered for one time
        assertTrue(allRequestsDelivered.await(1, TimeUnit.SECONDS));
        assertEquals(Collections.singletonList("test-action"), messagesDelivered);

        // let server complete.
        responseObserverRef.get().onCompleted();
    }

}