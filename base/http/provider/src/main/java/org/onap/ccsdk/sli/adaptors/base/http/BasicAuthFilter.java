package org.onap.ccsdk.sli.adaptors.base.http;

import java.io.IOException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

public class BasicAuthFilter implements ClientRequestFilter {
    private final String basicAuthValue;

    public BasicAuthFilter(String basicAuthValue) {
        this.basicAuthValue = basicAuthValue;
    }

    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.add("Authorization", basicAuthValue);
    }


}
