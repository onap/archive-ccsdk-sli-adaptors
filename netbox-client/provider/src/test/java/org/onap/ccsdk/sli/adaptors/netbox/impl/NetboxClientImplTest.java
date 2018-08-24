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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.onap.ccsdk.sli.adaptors.netbox.model.IPAddress;
import org.onap.ccsdk.sli.adaptors.netbox.model.Prefix;
import org.onap.ccsdk.sli.core.dblib.DbLibService;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class NetboxClientImplTest {

    private static final String APPLICATION_JSON = "application/json";

    @Rule
    public WireMockRule wm = new WireMockRule(wireMockConfig().dynamicPort());
    @Mock
    private DbLibService dbLib;
    @Mock
    private SvcLogicContext svcLogicContext;
    @Mock
    private Appender<ILoggingEvent> appender;
    @Captor
    private ArgumentCaptor<ILoggingEvent> captor;

    private String token = "token";
    private String serviceInstanceId = UUID.randomUUID().toString();
    private String vfModuleId = UUID.randomUUID().toString();

    private NetboxHttpClient httpClient;
    private NetboxClientImpl netboxClient;

    @Mock
    private NetboxHttpClient httpClientMock;
    @Mock
    private NetboxClientImpl netboxClientMock;

    @Before
    public void setup() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
            .getLogger(NetboxClientImpl.class);
        logger.addAppender(appender);

        String baseUrl = "http://localhost:" + wm.port();

        httpClient = new NetboxHttpClient(baseUrl, token);

        netboxClient = new NetboxClientImpl(httpClient, dbLib);

        netboxClientMock = new NetboxClientImpl(httpClientMock, dbLib);

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
    public void unassignIpAddressTestNoId() {
        IPAddress ipAddress = mock(IPAddress.class);
        doReturn(null).when(ipAddress).getId();
        QueryStatus status = netboxClient.unassignIpAddress(ipAddress, serviceInstanceId, vfModuleId, svcLogicContext);
        Assert.assertEquals(QueryStatus.FAILURE, status);
        verifyLogEntry("Id must be set");
    }

    @Test
    public void unassignIpAddressFailedRequest() throws IOException {
        Integer id = 3;
        IPAddress ipAddress = mock(IPAddress.class);
        doReturn(id).when(ipAddress).getId();

        doThrow(new IOException("Failed request")).when(httpClientMock).delete(anyString());
        QueryStatus status = netboxClientMock
            .unassignIpAddress(ipAddress, serviceInstanceId, vfModuleId, svcLogicContext);

        Assert.assertEquals(QueryStatus.FAILURE, status);
        verifyLogEntry("Fail to unassign IP for IPAddress(id= 3). Failed request");
    }

    @Test
    public void unassignIpAddressServerError() {
        Integer id = 3;
        IPAddress ipAddress = mock(IPAddress.class);
        doReturn(id).when(ipAddress).getId();

        String expectedUrl = "/api/ipam/ip-addresses/" + id + "/";
        givenThat(delete(urlEqualTo(expectedUrl)).willReturn(serverError()));

        QueryStatus status = netboxClient.unassignIpAddress(ipAddress, serviceInstanceId, vfModuleId, svcLogicContext);

        Assert.assertEquals(QueryStatus.FAILURE, status);
        verifyLogEntry("Fail to unassign IP for IPAddress(id=3). HTTP code=500.");
    }

    @Test
    public void unassignIpAddressFailSQL() throws IOException, SQLException {
        Integer id = 3;
        IPAddress ipAddress = mock(IPAddress.class);
        doReturn(id).when(ipAddress).getId();

        String response = "{}";

        String expectedUrl = "/api/ipam/ip-addresses/" + id + "/";
        givenThat(delete(urlEqualTo(expectedUrl)).willReturn(created().withBody(response)));

        doThrow(new SQLException("Failed")).when(dbLib).writeData(anyString(), any(ArrayList.class), eq(null));

        QueryStatus status = netboxClient.unassignIpAddress(ipAddress, serviceInstanceId, vfModuleId, svcLogicContext);

        Assert.assertEquals(QueryStatus.FAILURE, status);
        verifyLogEntry("Caught SQL exception");
    }

    @Test
    public void unassignIpAddressSuccess() throws IOException, SQLException {
        Integer id = 3;
        IPAddress ipAddress = mock(IPAddress.class);
        doReturn(id).when(ipAddress).getId();

        String response = "{}";

        String expectedUrl = "/api/ipam/ip-addresses/" + id + "/";
        givenThat(delete(urlEqualTo(expectedUrl)).willReturn(created().withBody(response)));

        QueryStatus status = netboxClient.unassignIpAddress(ipAddress, serviceInstanceId, vfModuleId, svcLogicContext);

        verify(deleteRequestedFor(urlEqualTo(expectedUrl))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo("Token " + token)));

        Mockito.verify(dbLib).writeData(anyString(), any(ArrayList.class), eq(null));
        Assert.assertEquals(QueryStatus.SUCCESS, status);
    }


    @Test
    public void nextAvailableIpInPrefixTestNoId() {
        Prefix prefix = mock(Prefix.class);
        doReturn(null).when(prefix).getId();
        QueryStatus status = netboxClient.assignIpAddress(prefix, serviceInstanceId, vfModuleId, svcLogicContext);
        Assert.assertEquals(QueryStatus.FAILURE, status);
        verifyLogEntry("Id must be set");
    }

    @Test
    public void nextAvailableIpInPrefixFailedRequest() throws IOException {
        Integer id = 3;
        Prefix prefix = mock(Prefix.class);
        doReturn(id).when(prefix).getId();

        doThrow(new IOException("Failed request")).when(httpClientMock).post(anyString(), anyString());
        QueryStatus status = netboxClientMock.assignIpAddress(prefix, serviceInstanceId, vfModuleId, svcLogicContext);

        Assert.assertEquals(QueryStatus.FAILURE, status);
        verifyLogEntry("Fail to assign IP for Prefix(id=3). Failed request");
    }

    @Test
    public void nextAvailableIpInPrefixBadRespPayload() throws IOException {
        Integer id = 3;
        Prefix prefix = mock(Prefix.class);
        doReturn(id).when(prefix).getId();

        URL url = Resources.getResource("badResponse.json");
        String response = Resources.toString(url, Charsets.UTF_8);

        String expectedUrl = "/api/ipam/prefixes/" + id + "/available-ips/";
        givenThat(post(urlEqualTo(expectedUrl)).willReturn(created().withBody(response)));

        QueryStatus status = netboxClient.assignIpAddress(prefix, serviceInstanceId, vfModuleId, svcLogicContext);

        Assert.assertEquals(QueryStatus.FAILURE, status);
        verifyLogEntry("Fail to parse IPAM JSON reponse to IPAddress POJO. IPAM JSON Response={\n"
            + "  \"id\": 8\n"
            + "  \"address\": \"192.168.20.7/32\"\n"
            + "}");
    }

    @Test
    public void nextAvailableIpInPrefixFailSQL() throws IOException, SQLException {
        Integer id = 3;
        Prefix prefix = mock(Prefix.class);
        doReturn(id).when(prefix).getId();

        URL url = Resources.getResource("nextAvailableIpResponse.json");
        String response = Resources.toString(url, Charsets.UTF_8);

        String expectedUrl = "/api/ipam/prefixes/" + id + "/available-ips/";
        givenThat(post(urlEqualTo(expectedUrl)).willReturn(created().withBody(response)));

        doThrow(new SQLException("Failed")).when(dbLib).writeData(anyString(), any(ArrayList.class), eq(null));

        QueryStatus status = netboxClient.assignIpAddress(prefix, serviceInstanceId, vfModuleId, svcLogicContext);

        Assert.assertEquals(QueryStatus.FAILURE, status);
        verifyLogEntry("Caught SQL exception");
    }

    @Test
    public void nextAvailableIpInPrefixError500() throws IOException {
        Integer id = 3;
        Prefix prefix = mock(Prefix.class);
        doReturn(id).when(prefix).getId();

        URL url = Resources.getResource("nextAvailableIpResponse.json");
        String response = Resources.toString(url, Charsets.UTF_8);

        String expectedUrl = "/api/ipam/prefixes/" + id + "/available-ips/";
        givenThat(post(urlEqualTo(expectedUrl)).willReturn(serverError().withBody(response)));

        QueryStatus status = netboxClient.assignIpAddress(prefix, serviceInstanceId, vfModuleId, svcLogicContext);

        Assert.assertEquals(QueryStatus.FAILURE, status);
        verifyLogEntry("Fail to assign IP for Prefix(id=3). HTTP code 201!=500.");
    }

    @Test
    public void nextAvailableIpInPrefixSuccess() throws IOException, SQLException {
        Integer id = 3;
        Prefix prefix = mock(Prefix.class);
        doReturn(id).when(prefix).getId();

        URL url = Resources.getResource("nextAvailableIpResponse.json");
        String response = Resources.toString(url, Charsets.UTF_8);

        String expectedUrl = "/api/ipam/prefixes/" + id + "/available-ips/";
        givenThat(post(urlEqualTo(expectedUrl)).willReturn(created().withBody(response)));

        QueryStatus status = netboxClient.assignIpAddress(prefix, serviceInstanceId, vfModuleId, svcLogicContext);

        verify(postRequestedFor(urlEqualTo(expectedUrl))
            .withHeader(ACCEPT, equalTo(APPLICATION_JSON))
            .withHeader(CONTENT_TYPE, equalTo(APPLICATION_JSON))
            .withHeader(AUTHORIZATION, equalTo("Token " + token)));

        Mockito.verify(dbLib).writeData(anyString(), any(ArrayList.class), eq(null));
        Assert.assertEquals(QueryStatus.SUCCESS, status);
    }

    private void verifyLogEntry(String message) {
        Mockito.verify(appender, times(1)).doAppend(captor.capture());
        List<ILoggingEvent> allValues = captor.getAllValues();
        for (ILoggingEvent loggingEvent : allValues) {
            Assert.assertTrue(loggingEvent.getFormattedMessage().contains(message));
        }
    }

}