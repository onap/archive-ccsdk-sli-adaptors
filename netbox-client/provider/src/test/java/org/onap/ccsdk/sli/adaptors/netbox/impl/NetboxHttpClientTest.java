package org.onap.ccsdk.sli.adaptors.netbox.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import java.util.concurrent.CompletionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.adaptors.netbox.api.IpamException;

@RunWith(MockitoJUnitRunner.class)
public class NetboxHttpClientTest {

    private static final String APPLICATION_JSON = "application/json";

    @Rule
    public WireMockRule wm = new WireMockRule(wireMockConfig().dynamicPort());

    private NetboxHttpClient httpClient;

    private String token = "token";

    @Before
    public void setup() {
        String baseUrl = "http://localhost:" + wm.port();

        httpClient = new NetboxHttpClient(baseUrl, token);
        httpClient.init();

        wm.addMockServiceRequestListener(
            (request, response) -> {
                System.out.println("Request URL :" + request.getAbsoluteUrl());
                System.out.println("Request body :" + request.getBodyAsString());
                System.out.println("Response status :" + response.getStatus());
                System.out.println("Response body :" + response.getBodyAsString());
            });
    }

    @After
    public void tearDown() throws IOException {
        httpClient.close();
    }

    @Test
    public void postTest() {
        String expectedUrl = "/testPost";
        givenThat(post(urlEqualTo(expectedUrl)).willReturn(ok()));

        httpClient.post(expectedUrl, "");

        verify(postRequestedFor(urlEqualTo(expectedUrl))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo("Token " + token)));
    }

    @Test
    public void postTestException() {
        String expectedUrl = "/testPost";
        givenThat(post(urlEqualTo(expectedUrl)).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

        try {
            httpClient.post(expectedUrl, "").toCompletableFuture().join();
        } catch (CompletionException e) {
            Assert.assertEquals(IpamException.class, e.getCause().getClass());
            Assert.assertEquals("Netbox request failed", e.getCause().getMessage());
            return;
        }
        Assert.fail();
    }

    @Test
    public void deleteTest() {
        String expectedUrl = "/testDelete";
        givenThat(delete(urlEqualTo(expectedUrl)).willReturn(ok()));

        httpClient.delete(expectedUrl);

        verify(deleteRequestedFor(urlEqualTo(expectedUrl))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo("Token " + token)));
    }

    @Test
    public void deleteTestException() {
        String expectedUrl = "/testDelete";
        givenThat(delete(urlEqualTo(expectedUrl)).willReturn(aResponse().withFault(Fault.MALFORMED_RESPONSE_CHUNK)));

        try {
            httpClient.delete(expectedUrl).toCompletableFuture().join();
        } catch (CompletionException e) {
            Assert.assertEquals(IpamException.class, e.getCause().getClass());
            Assert.assertEquals("Netbox request failed", e.getCause().getMessage());
            return;
        }
        Assert.fail();
    }
}
