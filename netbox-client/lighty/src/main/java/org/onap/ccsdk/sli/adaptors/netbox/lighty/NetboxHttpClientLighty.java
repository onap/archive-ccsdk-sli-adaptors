/*
 * Copyright (C) 2018 Bell Canada.
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
package org.onap.ccsdk.sli.adaptors.netbox.lighty;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

import java.io.IOException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

public class NetboxHttpClientLighty implements AutoCloseable {

    private static final String APPLICATION_JSON = "application/json";

    private final CloseableHttpClient client;
    private final String url;
    private final String token;

    // Used by the blueprint container
    public NetboxHttpClientLighty(NetboxPropertiesLighty properties) {
        this(properties.getHost(), properties.getApiKey());
    }

    NetboxHttpClientLighty(final String url, final String token) {
        this.url = url;
        this.token = token;

        final TrustStrategy acceptingTrustStrategy = (certificate, authType) -> true;
        final SSLContext sslContext;
        try {
            sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy).build();
        } catch (final NoSuchAlgorithmException | KeyManagementException | KeyStoreException e) {
            throw new IllegalStateException("Can't create http client", e);
        }
        client = HttpClientBuilder.create()
            .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
            .setSSLContext(sslContext)
            .build();
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    HttpResponse post(final String uri, final String requestBody) throws IOException {
        final HttpPost request = new HttpPost(url + uri);
        setHeaders(request);
        request.setEntity(new StringEntity(requestBody, Charset.forName("UTF-8")));
        return client.execute(request);
    }

    HttpResponse delete(final String uri) throws IOException {
        final HttpDelete request = new HttpDelete(url + uri);
        setHeaders(request);
        return client.execute(request);
    }

    private void setHeaders(final HttpRequestBase request) {
        request.addHeader(ACCEPT, APPLICATION_JSON);
        request.addHeader(CONTENT_TYPE, APPLICATION_JSON);
        request.addHeader(AUTHORIZATION, "Token " + token);
    }
}
