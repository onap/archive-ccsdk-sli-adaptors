package org.onap.ccsdk.sli.adaptors.grpc.cds;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import org.apache.http.HttpHeaders;
import org.onap.ccsdk.sli.adaptors.grpc.GrpcProperties;

public class BasicAuthClientInterceptor implements ClientInterceptor {

    private GrpcProperties props;

    public BasicAuthClientInterceptor(GrpcProperties props) {
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
