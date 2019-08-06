package org.onap.ccsdk.sli.adaptors.base.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicAuthFilter implements ClientRequestFilter {
    private final String basicAuthValue;
    private static final Logger logger = LoggerFactory.getLogger(BasicAuthFilter.class);

    public BasicAuthFilter(String user, String password) {
        this.basicAuthValue = getBasicAuthValue(user, password);
    }

    public void filter(ClientRequestContext requestContext) throws IOException {
        MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.add("Authorization", basicAuthValue);
    }

    protected String getBasicAuthValue(String userName, String password) {
        String token = userName + ":" + password;
        try {
            return "BASIC " + DatatypeConverter.printBase64Binary(token.getBytes("UTF-8"));
        } catch (Exception e) {
            logger.error("getBasicAuthValue threw an exception, credentials will be null", e);
        }
        return null;
    }
}
