/*-
 * ============LICENSE_START=======================================================
 * ONAP : APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Copyright (C) 2017 Amdocs
 * =============================================================================
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
 * 
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 * ============LICENSE_END=========================================================
 */


package org.onap.appc.test;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;

/**
 * This class is used as an intercept logger that can be used in testing to intercept and record all messages that are
 * logged, thus allowing a junit test case to examine the log output and make assertions.
 */
public class InterceptLogger implements org.slf4j.Logger {

    /**
     * This inner class represents an intercepted log event
     */
    public class LogRecord {
        private Level level;
        private String message;
        private long timestamp;
        private Throwable t;

        public LogRecord(Level level, String message) {
            setLevel(level);
            setTimestamp(System.currentTimeMillis());
            setMessage(message);
        }

        public LogRecord(Level level, String message, Throwable t) {
            this(level, message);
            setThrowable(t);
        }

        /**
         * @return the value of level
         */
        public Level getLevel() {
            return level;
        }

        /**
         * @return the value of message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return the value of timestamp
         */
        public long getTimestamp() {
            return timestamp;
        }

        /**
         * @param level
         *            the value for level
         */
        public void setLevel(Level level) {
            this.level = level;
        }

        /**
         * @param message
         *            the value for message
         */
        public void setMessage(String message) {
            this.message = message;
        }

        /**
         * @param timestamp
         *            the value for timestamp
         */
        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * @return the value of t
         */
        public Throwable getThrowable() {
            return t;
        }

        /**
         * @param t
         *            the value for t
         */
        public void setThrowable(Throwable t) {
            this.t = t;
        }

    }

    /**
     * The list of all intercepted log events
     */
    private List<LogRecord> events;

    /**
     * Create the intercept logger
     */
    public InterceptLogger() {
        events = new ArrayList<LogRecord>(1000);
    }

    /**
     * @return Returns all intercepted log events
     */
    public List<LogRecord> getLogRecords() {
        return events;
    }

    /**
     * Clears all log events
     */
    public void clear() {
        events.clear();
    }

    @Override
    public void debug(Marker marker, String msg) {
        debug(msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        debug(MessageFormat.format(format, arg));
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        debug(MessageFormat.format(format, arguments));
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        debug(MessageFormat.format(format, arg1, arg2));
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        debug(msg, t);
    }

    @Override
    public void debug(String msg) {
        events.add(new LogRecord(Level.DEBUG, msg));
    }

    @Override
    public void debug(String format, Object arg) {
        events.add(new LogRecord(Level.DEBUG, MessageFormat.format(format, arg)));
    }

    @Override
    public void debug(String format, Object... arguments) {
        events.add(new LogRecord(Level.DEBUG, MessageFormat.format(format, arguments)));
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        events.add(new LogRecord(Level.DEBUG, MessageFormat.format(format, arg1, arg2)));
    }

    @Override
    public void debug(String msg, Throwable t) {
        events.add(new LogRecord(Level.DEBUG, msg, t));
    }

    @Override
    public void error(Marker marker, String msg) {
        error(msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        error(format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        error(format, arguments);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        error(format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        events.add(new LogRecord(Level.ERROR, msg, t));
    }

    @Override
    public void error(String msg) {
        events.add(new LogRecord(Level.ERROR, msg));
    }

    @Override
    public void error(String format, Object arg) {
        events.add(new LogRecord(Level.ERROR, MessageFormat.format(format, arg)));
    }

    @Override
    public void error(String format, Object... arguments) {
        events.add(new LogRecord(Level.ERROR, MessageFormat.format(format, arguments)));
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        events.add(new LogRecord(Level.ERROR, MessageFormat.format(format, arg1, arg2)));
    }

    @Override
    public void error(String msg, Throwable t) {
        events.add(new LogRecord(Level.ERROR, msg, t));
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void info(Marker marker, String msg) {
        info(msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        info(format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        info(format, arguments);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        info(format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        events.add(new LogRecord(Level.INFO, msg, t));
    }

    @Override
    public void info(String msg) {
        events.add(new LogRecord(Level.INFO, msg));
    }

    @Override
    public void info(String format, Object arg) {
        events.add(new LogRecord(Level.INFO, MessageFormat.format(format, arg)));
    }

    @Override
    public void info(String format, Object... arguments) {
        events.add(new LogRecord(Level.INFO, MessageFormat.format(format, arguments)));
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        events.add(new LogRecord(Level.INFO, MessageFormat.format(format, arg1, arg2)));
    }

    @Override
    public void info(String msg, Throwable t) {
        events.add(new LogRecord(Level.INFO, msg, t));
    }

    @Override
    public boolean isDebugEnabled() {
        return true;
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return true;
    }

    @Override
    public void trace(Marker marker, String msg) {
        trace(msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        trace(format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        trace(format, argArray);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        trace(format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        trace(msg, t);
    }

    @Override
    public void trace(String msg) {
        events.add(new LogRecord(Level.TRACE, msg));
    }

    @Override
    public void trace(String format, Object arg) {
        events.add(new LogRecord(Level.TRACE, MessageFormat.format(format, arg)));
    }

    @Override
    public void trace(String format, Object... arguments) {
        events.add(new LogRecord(Level.TRACE, MessageFormat.format(format, arguments)));
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        events.add(new LogRecord(Level.TRACE, MessageFormat.format(format, arg1, arg2)));
    }

    @Override
    public void trace(String msg, Throwable t) {
        events.add(new LogRecord(Level.TRACE, msg, t));
    }

    @Override
    public void warn(Marker marker, String msg) {
        warn(msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        warn(format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        warn(format, arguments);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        warn(format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        events.add(new LogRecord(Level.WARN, msg, t));
    }

    @Override
    public void warn(String msg) {
        events.add(new LogRecord(Level.WARN, msg));
    }

    @Override
    public void warn(String format, Object arg) {
        events.add(new LogRecord(Level.WARN, MessageFormat.format(format, arg)));
    }

    @Override
    public void warn(String format, Object... arguments) {
        events.add(new LogRecord(Level.WARN, MessageFormat.format(format, arguments)));
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        events.add(new LogRecord(Level.WARN, MessageFormat.format(format, arg1, arg2)));
    }

    @Override
    public void warn(String msg, Throwable t) {
        events.add(new LogRecord(Level.WARN, msg, t));
    }
}
