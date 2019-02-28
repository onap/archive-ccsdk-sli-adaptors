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
import java.util.Map;
import org.junit.Test;
import org.onap.ccsdk.sli.adaptors.grpc.JsonFormat;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.slipluginutils.SliPluginUtils;

public class GrpcClientTest {

    @Test
    public void testPayload() throws InvalidProtocolBufferException {

        String payload = "{\n"
            + "    \"commonHeader\": {\n"
            + "        \"timestamp\": \"2019-02-27T22:08:39.587Z\",\n"
            + "        \"originatorId\": \"System\",\n"
            + "        \"requestId\": \"1234\",\n"
            + "        \"subRequestId\": \"1234-12234\"\n"
            + "    },\n"
            + "    \"actionIdentifiers\": {\n"
            + "        \"blueprintName\": \"test\",\n"
            + "        \"blueprintVersion\": \"1.0.0\",\n"
            + "        \"actionName\": \"resource-assignment\",\n"
            + "        \"mode\": \"sync\"\n"
            + "    },\n"
            + "    \"status\": {\n"
            + "        \"code\": 200,\n"
            + "        \"eventType\": \"EVENT-COMPONENT-EXECUTED\",\n"
            + "        \"timestamp\": \"2019-02-27T22:08:39.981Z\",\n"
            + "        \"message\": \"success\"\n"
            + "    },\n"
            + "    \"payload\": {\n"
            + "        \"resource-assignment-params\": {\n"
            + "            \"test\": \"THIS IS A TEST: service-capability-resolved-status-test\"\n"
            + "        },\n"
            + "        \"status\": \"success\"\n"
            + "    }\n"
            + "}";

        Map<String, String> jsonToCtx = Maps.newHashMap();
        jsonToCtx.put("source", "blueprint_processing_result");
        jsonToCtx.put("outputPath", "t");
        jsonToCtx.put("isEscaped", Boolean.FALSE.toString());

        SvcLogicContext svcLogicContext = new SvcLogicContext();
        svcLogicContext.setAttribute("blueprint_processing_result", payload);

        try {
            SliPluginUtils.jsonStringToCtx(jsonToCtx, svcLogicContext);
        } catch (SvcLogicException e) {
            e.printStackTrace();
        }

        Builder t = Struct.newBuilder();

        JsonFormat.parser().merge(payload, t);

        System.out.println(((Builder) t).build().toString());

        System.out.println(JsonFormat.printer().print(t));
    }

}