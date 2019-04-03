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

import com.google.common.net.HttpHeaders;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import org.onap.ccsdk.sli.adaptors.grpc.GrpcPropertiesLighty;

/**
 * THIS CLASS IS A COPY OF {@link BasicAuthClientInterceptor} WITH REMOVED OSGi DEPENDENCIES
 */
public class BasicAuthClientInterceptorLighty implements ClientInterceptor {

    private GrpcPropertiesLighty props;

    public BasicAuthClientInterceptorLighty(GrpcPropertiesLighty props) {
        this.props = props;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
        MethodDescriptor<ReqT, RespT> method,
        CallOptions callOptions,
        Channel channel) {

        Key<String> authHeader = Key.of(HttpHeaders.AUTHORIZATION, Metadata.ASCII_STRING_MARSHALLER);

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(
            channel.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(authHeader, props.getAuth());
                super.start(responseListener, headers);
            }
        };
    }
}