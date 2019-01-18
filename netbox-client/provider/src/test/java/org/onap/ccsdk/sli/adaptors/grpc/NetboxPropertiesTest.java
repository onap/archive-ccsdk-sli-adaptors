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
package org.onap.ccsdk.sli.adaptors.netbox.property;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class NetboxPropertiesTest {

    private NetboxProperties props;

    @Mock
    private Appender<ILoggingEvent> appender;
    @Captor
    private ArgumentCaptor<ILoggingEvent> captor;

    @Before
    public void setup() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
            .getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logger.addAppender(appender);
    }

    @Test
    public void testMissingFile() {
        props = new NetboxProperties();

        verifyLogEntry(
            "Missing configuration properties resource for Netbox: netbox.properties");
    }

    private void verifyLogEntry(String message) {
        verify(appender, times(1)).doAppend(captor.capture());
        List<ILoggingEvent> allValues = captor.getAllValues();
        for (ILoggingEvent loggingEvent : allValues) {
            Assert.assertTrue(loggingEvent.getFormattedMessage().contains(message));
        }
    }
}