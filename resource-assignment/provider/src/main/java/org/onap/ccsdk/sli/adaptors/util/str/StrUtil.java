/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *                         reserved.
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

package org.onap.ccsdk.sli.adaptors.util.str;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrUtil {

    private static final Logger log = LoggerFactory.getLogger(StrUtil.class);

    public static final String INDENT_STR = "    ";

    public static void indent(StringBuilder ss, int ind) {
        for (int i = 0; i < ind; i++)
            ss.append(INDENT_STR);
    }

    public static void info(Logger log, Object o) {
        if (log.isInfoEnabled()) {
            StringBuilder ss = new StringBuilder();
            struct(ss, o);
            log.info(ss.toString());
        }
    }

    public static void debug(Logger log, Object o) {
        if (log.isDebugEnabled()) {
            StringBuilder ss = new StringBuilder();
            struct(ss, o);
            log.debug(ss.toString());
        }
    }

    public static void struct(StringBuilder ss, Object o) {
        struct(ss, o, 0);
    }

    public static void struct(StringBuilder ss, Object o, int ind) {
        if (o == null) {
            ss.append("null");
            return;
        }

        if (isSimple(o)) {
            ss.append(o);
            return;
        }

        Class<? extends Object> cls = o.getClass();

        if (cls.isEnum()) {
            ss.append(o);
            return;
        }

        if (cls.isArray()) {
            int n = Array.getLength(o);
            if (n == 0) {
                ss.append("[]");
                return;
            }

            Object o1 = Array.get(o, 0);
            if (isSimple(o1)) {
                ss.append('[').append(o1);
                for (int i = 1; i < n; i++) {
                    o1 = Array.get(o, i);
                    ss.append(", ").append(o1);
                }
                ss.append(']');
                return;
            }

            ss.append('\n');
            indent(ss, ind + 1);
            ss.append('[');
            struct(ss, o1, ind + 1);
            for (int i = 1; i < n; i++) {
                o1 = Array.get(o, i);
                struct(ss, o1, ind + 1);
            }
            ss.append('\n');
            indent(ss, ind + 1);
            ss.append(']');
            return;
        }

        if (o instanceof Collection<?>) {
            Collection<?> ll = (Collection<?>) o;

            int n = ll.size();
            if (n == 0) {
                ss.append("[]");
                return;
            }

            Iterator<?> ii = ll.iterator();
            Object o1 = ii.next();
            if (isSimple(o1)) {
                ss.append('[').append(o1);
                while (ii.hasNext()) {
                    o1 = ii.next();
                    ss.append(", ").append(o1);
                }
                ss.append(']');
                return;
            }

            ss.append('\n');
            indent(ss, ind + 1);
            ss.append('[');
            struct(ss, o1, ind + 1);
            while (ii.hasNext()) {
                o1 = ii.next();
                struct(ss, o1, ind + 1);
            }
            ss.append('\n');
            indent(ss, ind + 1);
            ss.append(']');
            return;

        }

        if (o instanceof Map<?, ?>) {
            Map<?, ?> mm = (Map<?, ?>) o;

            int n = mm.size();
            if (n == 0) {
                ss.append("{}");
                return;
            }

            ss.append('{');

            for (Object k : mm.keySet()) {
                ss.append('\n');
                indent(ss, ind + 1);
                ss.append(k).append(": ");

                Object o1 = mm.get(k);
                struct(ss, o1, ind + 2);
            }

            ss.append('\n');
            indent(ss, ind);
            ss.append('}');

            return;
        }

        Field[] fields = cls.getFields();

        if (fields.length == 0) {
            ss.append(o);
            return;
        }

        ss.append('\n');
        indent(ss, ind + 1);
        ss.append('<').append(cls.getSimpleName()).append("> {");
        for (Field f : fields) {
            ss.append('\n');
            indent(ss, ind + 2);
            ss.append(f.getName()).append(": ");
            Object v = null;
            try {
                v = f.get(o);
            } catch (IllegalAccessException e) {
                v = "*** Cannot obtain value *** : " + e.getMessage();
            }
            struct(ss, v, ind + 2);
        }
        ss.append('\n');
        indent(ss, ind + 1);
        ss.append('}');
    }

    public static SortedSet<Integer> listInt(String ss, String warning) {
        if (ss == null || ss.length() == 0)
            return null;

        SortedSet<Integer> ll = new TreeSet<Integer>();
        String[] str = ss.split(",");
        for (String s : str) {
            try {
                int i1 = s.indexOf('-');
                int start, end;
                if (i1 > 0) {
                    String s1 = s.substring(0, i1);
                    String s2 = s.substring(i1 + 1);
                    start = Integer.parseInt(s1);
                    end = Integer.parseInt(s2);
                } else
                    start = end = Integer.parseInt(s);
                for (int i = start; i <= end; i++)
                    ll.add(i);
            } catch (NumberFormatException e) {
                // Skip this - bad data in DB
                log.warn(warning + " [" + s + "].", e);
            }
        }
        return ll;
    }

    public static String listInt(SortedSet<Integer> ll) {
        if (ll == null || ll.size() == 0)
            return null;

        StringBuilder sb = new StringBuilder(2000);
        Iterator<Integer> i = ll.iterator();
        int n = i.next();
        int start = n;
        int end = n;
        boolean first = true;
        while (i.hasNext()) {
            n = i.next();
            if (n != end + 1) {
                if (!first)
                    sb.append(',');
                first = false;

                if (start == end)
                    sb.append(start);
                else if (start == end - 1)
                    sb.append(start).append(',').append(end);
                else
                    sb.append(start).append('-').append(end);

                start = n;
            }
            end = n;
        }

        if (!first)
            sb.append(',');

        if (start == end)
            sb.append(start);
        else if (start == end - 1)
            sb.append(start).append(',').append(end);
        else
            sb.append(start).append('-').append(end);

        return sb.toString();
    }

    public static List<String> listStr(String s) {
        if (s == null || s.length() == 0)
            return null;
        String[] ss = s.split(",");
        return Arrays.asList(ss);
    }

    public static String listStr(Collection<String> ll) {
        if (ll == null || ll.isEmpty())
            return null;
        StringBuilder ss = new StringBuilder(1000);
        Iterator<String> i = ll.iterator();
        ss.append(i.next());
        while (i.hasNext())
            ss.append(',').append(i.next());
        return ss.toString();
    }

    private static boolean isSimple(Object o) {
        if (o == null)
            return true;

        if (o instanceof Number || o instanceof String || o instanceof Boolean || o instanceof Date)
            return true;

        return false;
    }
}
