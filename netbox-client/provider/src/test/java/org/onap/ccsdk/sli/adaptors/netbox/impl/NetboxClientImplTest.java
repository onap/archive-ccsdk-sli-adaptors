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
package org.onap.ccsdk.sli.adaptors.netbox.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.created;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.adaptors.netbox.api.IpamException;
import org.onap.ccsdk.sli.adaptors.netbox.model.IPAddress;
import org.onap.ccsdk.sli.adaptors.netbox.model.Prefix;
import org.onap.ccsdk.sli.adaptors.netbox.model.Status.Values;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

@RunWith(MockitoJUnitRunner.class)
public class NetboxClientImplTest {

    private static final String APPLICATION_JSON = "application/json";

    @Rule
    public WireMockRule wm = new WireMockRule(wireMockConfig().dynamicPort());

    @Mock
    private DbLibService dbLib;

    private String token = "token";
    private String serviceInstanceId = UUID.randomUUID().toString();
    private String vfModuleId = UUID.randomUUID().toString();

    private NetboxHttpClient httpClient;
    private NetboxClientImpl netboxClient;

    @Before
    public void setup() {
        String baseUrl = "http://localhost:" + wm.port();

        httpClient = new NetboxHttpClient(baseUrl, token);
        httpClient.init();

        netboxClient = new NetboxClientImpl(httpClient, dbLib);

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
    public void nextAvailableIpInPrefixTestNoId() throws SQLException {
        Prefix prefix = mock(Prefix.class);
        doReturn(null).when(prefix).getId();
        try {
            netboxClient.assign(prefix, serviceInstanceId, vfModuleId);
        } catch (IpamException e) {
            Assert.assertEquals("Id must be set", e.getMessage());
            return;
        }
        Assert.fail();
    }

    @Test
    public void nextAvailableIpInPrefixTest() throws IOException, IpamException, SQLException {
        Integer id = 3;
        Prefix prefix = mock(Prefix.class);
        doReturn(id).when(prefix).getId();

        URL url = Resources.getResource("nextAvailableIpResponse.json");
        String response = Resources.toString(url, Charsets.UTF_8);

        String expectedUrl = "/api/ipam/prefixes/" + id + "/available-ips/";
        givenThat(post(urlEqualTo(expectedUrl)).willReturn(created().withBody(response)));

        netboxClient.assign(prefix, serviceInstanceId, vfModuleId);

        verify(postRequestedFor(urlEqualTo(expectedUrl))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo("Token " + token)));
        Mockito.verify(dbLib).writeData(anyString(), any(ArrayList.class), eq((null)));
    }

    @Test
    public void deleteIpTestError500() throws SQLException {
        Integer id = 3;
        IPAddress ipAddress = mock(IPAddress.class);
        doReturn(id).when(ipAddress).getId();

        String expectedUrl = "/api/ipam/ip-addresses/" + id + "/";
        givenThat(delete(urlEqualTo(expectedUrl)).willReturn(serverError()));
        try {
            netboxClient.unassign(ipAddress, serviceInstanceId, vfModuleId);
        } catch (IpamException e) {
            Assert.assertEquals(IllegalStateException.class, e.getCause().getClass());
            Assert.assertTrue(e.getMessage().contains(
                "Fail to unassign IP for IPAddress(id= 3). java.lang.IllegalStateException: Netbox request failed with status: HTTP/1.1 500 Server Error"));
            return;
        }
        Assert.fail();
    }

    @Test
    public void deleteIpTest() throws IpamException, SQLException {
        Integer id = 3;
        IPAddress ipAddress = mock(IPAddress.class);
        doReturn(id).when(ipAddress).getId();

        String expectedUrl = "/api/ipam/ip-addresses/" + id + "/";
        givenThat(delete(urlEqualTo(expectedUrl)).willReturn(ok()));
        netboxClient.unassign(ipAddress, serviceInstanceId, vfModuleId);
        verify(deleteRequestedFor(urlEqualTo(expectedUrl))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo("Token " + token)));
        Mockito.verify(dbLib).writeData(anyString(), any(ArrayList.class), eq((null)));
    }


    @Test
    public void getIpAddressTest() throws IOException {
        StatusLine statusLine = mock(StatusLine.class);
        doReturn(201).when(statusLine).getStatusCode();

        URL url = Resources.getResource("nextAvailableIpResponse.json");
        String response = Resources.toString(url, Charsets.UTF_8);
        InputStream stream = new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));

        HttpEntity entity = mock(HttpEntity.class);
        doReturn(stream).when(entity).getContent();

        HttpResponse httpResponse = mock(HttpResponse.class);
        doReturn(statusLine).when(httpResponse).getStatusLine();
        doReturn(entity).when(httpResponse).getEntity();

        IPAddress ipAddress = netboxClient.getIpAddress(httpResponse);

        Assert.assertEquals("192.168.20.7/32", ipAddress.getAddress());
        Assert.assertEquals(Integer.valueOf(8), ipAddress.getId());
        Assert.assertEquals(Values.ACTIVE, ipAddress.getStatus());
    }
}