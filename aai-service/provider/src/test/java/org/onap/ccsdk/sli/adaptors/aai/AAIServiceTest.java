/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.aai;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.HttpMethod;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.openecomp.aai.inventory.v13.*;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;
import org.onap.ccsdk.sli.adaptors.aai.AAIService.TransactionIdTracker;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;

@RunWith(MockitoJUnitRunner.class)
public class AAIServiceTest {
    private static AAIService aaiService = new AAIService(
            AAIService.class.getResource(AAIService.AAICLIENT_PROPERTIES));

    @Spy private AAIService aaiServiceSpy = new AAIService(
            AAIService.class.getResource(AAIService.AAICLIENT_PROPERTIES));

    @Mock private HttpsURLConnection connMock;

    // @Test
    public void existsInvalidResource_shouldReturnFailure() throws MalformedURLException, Exception {
        QueryStatus queryStatus = aaiServiceSpy.exists("InvalidResource", null, null, null);
        assertEquals(QueryStatus.FAILURE, queryStatus);
    }

//    @Test
    public void existsGetPserverByCallBackUrl_shouldReturnSuccess() throws MalformedURLException, Exception {
        String key = "https://aai.api.simpledemo.openecomp.org:8443/aai/v11/cloud-infrastructure/pservers/pserver/chcil129snd";
        String fileLocation = "json/pserverJson.txt";
        SvcLogicContext ctx = new SvcLogicContext();
        setConnMock();

        when(aaiServiceSpy.getConfiguredConnection(new URL(key), HttpMethod.GET)).thenReturn(connMock);
        when(connMock.getResponseCode()).thenReturn(200);
        when(connMock.getInputStream()).thenReturn(getClass().getClassLoader().getResourceAsStream(fileLocation));

        QueryStatus queryStatus = aaiServiceSpy.exists("pserver", key, "prefix.", ctx);

        assertEquals(QueryStatus.SUCCESS, queryStatus);
    }

//    @Test
    public void existsGetPserverByCallBackUrl_throwsExceptionAndReturnsFailure()
            throws MalformedURLException, Exception {
        String key = "https://aai.api.simpledemo.openecomp.org:8443/aai/v11/cloud-infrastructure/pservers/pserver/chcil129snd";
        String fileLocation = "json/pserverJson.txt";
        SvcLogicContext ctx = new SvcLogicContext();
        setConnMock();

        when(aaiServiceSpy.getConfiguredConnection(new URL(key), HttpMethod.GET)).thenReturn(connMock);
        when(connMock.getResponseCode()).thenReturn(200);
        when(connMock.getInputStream()).thenReturn(getClass().getClassLoader().getResourceAsStream(fileLocation));

        when(aaiServiceSpy.dataChangeRequestAaiData(key, Pserver.class)).thenThrow(
                new AAIServiceException("testException"));

        QueryStatus queryStatus = aaiServiceSpy.exists("pserver", key, "prefix.", ctx);

        assertEquals(QueryStatus.FAILURE, queryStatus);
    }

//    @Test
    public void pserverDataChangeRequestData_shouldSucceed() throws Exception {
        String fileLocation = "json/pserverJson.txt";
        String url = "https://aai.api.simpledemo.openecomp.org:8443/aai/v11/cloud-infrastructure/pservers/pserver/chcil129snd";
        setConnMock();

        when(aaiServiceSpy.getConfiguredConnection(new URL(url), HttpMethod.GET)).thenReturn(connMock);
        when(connMock.getResponseCode()).thenReturn(200);
        when(connMock.getInputStream()).thenReturn(getClass().getClassLoader().getResourceAsStream(fileLocation));

        Pserver pserver = aaiServiceSpy.dataChangeRequestAaiData(url, Pserver.class);

        assertEquals("chcil129snd", pserver.getHostname());
    }

//    @Test
    public void pserverDataChangeRequestData_shouldReturnNullFor404() throws Exception {
        String fileLocation = "json/pserverJson.txt";
        String url = "https://aai.api.simpledemo.openecomp.org:8443/aai/v11/cloud-infrastructure/pservers/pserver/chcil129snd";
        setConnMock();

        when(aaiServiceSpy.getConfiguredConnection(new URL(url), HttpMethod.GET)).thenReturn(connMock);
        when(connMock.getResponseCode()).thenReturn(404);
        when(connMock.getErrorStream()).thenReturn(getClass().getClassLoader().getResourceAsStream(fileLocation));

        Pserver pserver = aaiServiceSpy.dataChangeRequestAaiData(url, Pserver.class);

        assertEquals(null, pserver);
    }

    @Test(expected = AAIServiceException.class)
    public void dataChangeRequestData_throwsAAIServiceException() throws Exception {
        String fileLocation = "json/pserverJson.txt";
        String url = "https://aai.api.simpledemo.openecomp.org:8443/aai/v11/cloud-infrastructure/pservers/pserver/chcil129snd";
        setConnMock();

        when(aaiServiceSpy.getConfiguredConnection(new URL(url), HttpMethod.GET)).thenReturn(connMock);
        when(connMock.getResponseCode()).thenReturn(500);
        when(connMock.getInputStream()).thenReturn(getClass().getClassLoader().getResourceAsStream(fileLocation));

        aaiServiceSpy.dataChangeRequestAaiData(url, Class.class);
    }

    public String readFileToString(String fileName) throws IOException, URISyntaxException {
        URL url = AAIServiceTest.class.getResource(fileName);
        Path resPath = Paths.get(url.toURI());

        return new String(Files.readAllBytes(resPath), "UTF8");
    }

    public <T> T getObjectFromJson(String text, Class<T> type)
            throws JsonParseException, JsonMappingException, IOException {
        ObjectMapper mapper = AAIService.getObjectMapper();

        return type.cast(mapper.readValue(text, type));
    }

    private void setConnMock() throws ProtocolException {
        // Set up the connection properties
        connMock.setRequestProperty("Connection", "close");
        connMock.setDoInput(true);
        connMock.setDoOutput(true);
        connMock.setUseCaches(false);
        connMock.setConnectTimeout(1000);
        connMock.setReadTimeout(1000);
        connMock.setRequestMethod(HttpMethod.GET);
        connMock.setRequestProperty("Accept", "application/json");
        connMock.setRequestProperty("Content-Type", "application/json");
        connMock.setRequestProperty("X-FromAppId", "testId");
        connMock.setRequestProperty("X-TransactionId", TransactionIdTracker.getNextTransactionId());
    }

    @Test
    public void testSetStatusMessage_shouldSucceed() throws SvcLogicException, MalformedURLException {
        SvcLogicContext ctx = new SvcLogicContext();
        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put("key1", "ActivateSubnet failure, need to manually activate in EIPAM.");
        aaiService.setStatusMethod(parameters, ctx);

        Pattern r8601 = Pattern.compile(
                "(\\d{4})-(\\d{2})-(\\d{2})T((\\d{2}):(\\d{2}):(\\d{2}))Z");
        Matcher isoDate = r8601.matcher(ctx.getAttribute("aai-summary-status-message"));

        assertTrue(isoDate.lookingAt());

        assertTrue(ctx.getAttribute("aai-summary-status-message")
                .contains("ActivateSubnet failure, need to manually activate in EIPAM."));
    }

     @Test(expected = SvcLogicException.class)
    public void testSetStatusMessage_nullContext() throws SvcLogicException, MalformedURLException {
        SvcLogicContext ctx = null;
        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put("key1", "ActivateSubnet failure, need to manually activate in EIPAM.");
        aaiService.setStatusMethod(parameters, ctx);
    }
}
