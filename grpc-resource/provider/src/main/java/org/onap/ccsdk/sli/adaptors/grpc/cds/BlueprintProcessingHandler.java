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

import com.google.common.collect.Maps;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Struct;
import com.google.protobuf.Struct.Builder;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.onap.ccsdk.apps.controllerblueprints.common.api.ActionIdentifiers;
import org.onap.ccsdk.apps.controllerblueprints.common.api.CommonHeader;
import org.onap.ccsdk.apps.controllerblueprints.common.api.Flag;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.BluePrintProcessingServiceGrpc.BluePrintProcessingServiceStub;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceInput;
import org.onap.ccsdk.apps.controllerblueprints.processing.api.ExecutionServiceOutput;
import org.onap.ccsdk.sli.adaptors.grpc.JsonFormat;
import org.onap.ccsdk.sli.adaptors.grpc.Utils;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.core.slipluginutils.SliPluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BlueprintProcessingHandler {

    private static final Logger log = LoggerFactory.getLogger(BlueprintProcessingHandler.class);

    private static final int DEFAULT_TTL = 180;
    private static final String CCSDK_ORIGINATOR = "CCSDK";
    private static final String IS_FORCE_PROP = "is_force";
    private static final String TTL_PROP = "ttl";
    private static final String BLUEPRINT_NAME_PROP = "blueprint_name";
    private static final String BLUEPRINT_VERSION_PROP = "blueprint_version";
    private static final String ACTION_PROP = "action";
    private static final String MODE_PROP = "mode";
    private static final String PAYLOAD_PROP = "payload";
    private static final String PREFIX_PROP = "prefix";

    QueryStatus process(final Map<String, String> parameters, final ManagedChannel channel, final SvcLogicContext ctx) {
        try {
            SliPluginUtils.checkParameters(parameters,
                new String[]{BLUEPRINT_NAME_PROP, BLUEPRINT_VERSION_PROP, ACTION_PROP, MODE_PROP, PREFIX_PROP}, log);
        } catch (SvcLogicException e) {
            return QueryStatus.FAILURE;
        }

        final boolean isForce = Boolean.getBoolean(parameters.get(IS_FORCE_PROP));
        int ttl = Integer.parseInt(parameters.get(TTL_PROP));
        if (ttl == 0) {
            ttl = DEFAULT_TTL;
        }
        final String blueprintName = parameters.get(BLUEPRINT_NAME_PROP);
        final String blueprintVersion = parameters.get(BLUEPRINT_VERSION_PROP);
        final String action = parameters.get(ACTION_PROP);
        final String mode = parameters.get(MODE_PROP);
        final String payload = parameters.get(PAYLOAD_PROP);
        final String prefix = parameters.get(PREFIX_PROP);

        log.info("Processing blueprint({}:{}) for action({})", blueprintVersion, blueprintName, action);

        final AtomicReference<QueryStatus> responseStatus = new AtomicReference<>();
        final CountDownLatch finishLatch = new CountDownLatch(1);

        final BluePrintProcessingServiceStub asyncStub = BluePrintProcessingServiceGrpc.newStub(channel);

        final StreamObserver<ExecutionServiceOutput> responseObserver = new StreamObserver<ExecutionServiceOutput>() {
            @Override
            public void onNext(ExecutionServiceOutput output) {
                log.info("onNext: {}", output);

                Map<String, String> jsonToCtx = Maps.newHashMap();
                String json = "";
                try {
                    json = JsonFormat.printer().print(output);
                } catch (InvalidProtocolBufferException e) {
                    log.error("Failed to parse received message. blueprint({}:{}) for action({}). {}", blueprintVersion,
                        blueprintName, action, output, e);
                    responseStatus.compareAndSet(null, QueryStatus.FAILURE);
                    finishLatch.countDown();
                }

                ctx.setAttribute("BlueprintProcessingHandler_process", json);
                jsonToCtx.put("source", "BlueprintProcessingHandler_process");
                jsonToCtx.put("outputPath", prefix);
                jsonToCtx.put("isEscaped", Boolean.FALSE.toString());

                try {
                    SliPluginUtils.jsonStringToCtx(jsonToCtx, ctx);
                } catch (SvcLogicException e) {
                    log.error("Failed to put jsonStringToCtx. blueprint({}:{}) for action({}). {}", blueprintVersion,
                        blueprintName, action, output, e);
                    responseStatus.compareAndSet(null, QueryStatus.FAILURE);
                    finishLatch.countDown();
                }
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                log.error("Failed processing blueprint({}:{}) for action({}). {}", blueprintVersion, blueprintName,
                    action, status);
                responseStatus.compareAndSet(null, QueryStatus.FAILURE);
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("Completed blueprint({}:{}) for action({})", blueprintVersion, blueprintName, action);
                responseStatus.compareAndSet(null, QueryStatus.SUCCESS);
                finishLatch.countDown();
            }
        };

        final CommonHeader commonHeader = CommonHeader.newBuilder()
            .setOriginatorId(CCSDK_ORIGINATOR)
            .setRequestId(UUID.randomUUID().toString())
            .setTimestamp(Utils.timestamp())
            .setFlag(Flag.newBuilder()
                .setIsForce(isForce)
                .setTtl(ttl)
                .build())
            .build();
        final ActionIdentifiers actionIdentifiers = ActionIdentifiers.newBuilder()
            .setActionName(action)
            .setBlueprintName(blueprintName)
            .setBlueprintVersion(blueprintVersion)
            .setMode(mode)
            .build();

        Builder struct = Struct.newBuilder();
        try {
            JsonFormat.parser().merge(payload, struct);
        } catch (InvalidProtocolBufferException e) {
            log.error("Failed converting payload for blueprint({}:{}) for action({}). {}", blueprintVersion,
                blueprintName, action, e);
            return QueryStatus.FAILURE;
        }

        final ExecutionServiceInput request = ExecutionServiceInput.newBuilder()
            .setActionIdentifiers(actionIdentifiers)
            .setPayload(struct.build())
            .setCommonHeader(commonHeader).build();

        final StreamObserver<ExecutionServiceInput> requestObserver = asyncStub.process(responseObserver);

        try {
            requestObserver.onNext(request);
        } catch (RuntimeException e) {
            requestObserver.onError(e);
            return QueryStatus.FAILURE;
        }

        requestObserver.onCompleted();
        try {
            finishLatch.await(ttl, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Failed processing blueprint({}:{}) for action({}). {}", blueprintVersion, blueprintName, action,
                e);
            Thread.currentThread().interrupt();
            return QueryStatus.FAILURE;
        }

        return responseStatus.get();
    }
}