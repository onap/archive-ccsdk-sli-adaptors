/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
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

package org.onap.ccsdk.sli.adaptors.util.expr;

import java.util.Map;

public class ExpressionEvaluator {

    public static long evalLong(String expr, Map<String, Object> vars) {
        return (long) evalFloat(expr, vars);
    }

    public static float evalFloat(String expr, Map<String, Object> vars) {
        expr = expr.trim();
        int sl = expr.length();
        if (sl == 0)
            throw new IllegalArgumentException("Cannot interpret empty string.");

        // Remove parentheses if any
        if (expr.charAt(0) == '(' && expr.charAt(sl - 1) == ')')
            return evalFloat(expr.substring(1, sl - 1), vars);

        // Look for operators in the order of least priority
        String[] sss = findOperator(expr, "-", true);
        if (sss != null)
            return evalFloat(sss[0], vars) - evalFloat(sss[1], vars);

        sss = findOperator(expr, "+", true);
        if (sss != null)
            return evalFloat(sss[0], vars) + evalFloat(sss[1], vars);

        sss = findOperator(expr, "/", true);
        if (sss != null)
            return evalFloat(sss[0], vars) / evalFloat(sss[1], vars);

        sss = findOperator(expr, "*", true);
        if (sss != null)
            return evalFloat(sss[0], vars) * evalFloat(sss[1], vars);

        // Check if expr is a number
        try {
            return Float.valueOf(expr);
        } catch (Exception e) {
        }

        // Must be a variable
        Object v = vars.get(expr);
        if (v != null) {
            if (v instanceof Float)
                return (Float) v;
            if (v instanceof Long)
                return (Long) v;
            if (v instanceof Integer)
                return (Integer) v;
        }
        return 0;
    }

    public static boolean evalBoolean(String expr, Map<String, Object> vars) {
        expr = expr.trim();
        int sl = expr.length();
        if (sl == 0)
            throw new IllegalArgumentException("Cannot interpret empty string.");

        if (expr.equalsIgnoreCase("true"))
            return true;

        if (expr.equalsIgnoreCase("false"))
            return false;

        // Remove parentheses if any
        if (expr.charAt(0) == '(' && expr.charAt(sl - 1) == ')')
            return evalBoolean(expr.substring(1, sl - 1), vars);

        // Look for operators in the order of least priority
        String[] sss = findOperator(expr, "or", true);
        if (sss != null)
            return evalBoolean(sss[0], vars) || evalBoolean(sss[1], vars);

        sss = findOperator(expr, "and", true);
        if (sss != null)
            return evalBoolean(sss[0], vars) && evalBoolean(sss[1], vars);

        sss = findOperator(expr, "not", true);
        if (sss != null)
            return !evalBoolean(sss[1], vars);

        sss = findOperator(expr, "!=", false);
        if (sss == null)
            sss = findOperator(expr, "<>", false);
        if (sss != null)
            return evalLong(sss[0], vars) != evalLong(sss[1], vars);

        sss = findOperator(expr, "==", false);
        if (sss == null)
            sss = findOperator(expr, "=", false);
        if (sss != null)
            return evalLong(sss[0], vars) == evalLong(sss[1], vars);

        sss = findOperator(expr, ">=", false);
        if (sss != null)
            return evalLong(sss[0], vars) >= evalLong(sss[1], vars);

        sss = findOperator(expr, ">", false);
        if (sss != null)
            return evalLong(sss[0], vars) > evalLong(sss[1], vars);

        sss = findOperator(expr, "<=", false);
        if (sss != null)
            return evalLong(sss[0], vars) <= evalLong(sss[1], vars);

        sss = findOperator(expr, "<", false);
        if (sss != null)
            return evalLong(sss[0], vars) < evalLong(sss[1], vars);

        throw new IllegalArgumentException("Cannot interpret '" + expr + "': Invalid expression.");
    }

    private static String[] findOperator(String s, String op, boolean delimiterRequired) {
        int opl = op.length();
        int sl = s.length();
        String delimiters = " \0\t\r\n()";
        int pcount = 0, qcount = 0;
        for (int i = 0; i < sl; i++) {
            char c = s.charAt(i);
            if (c == '(' && qcount == 0)
                pcount++;
            else if (c == ')' && qcount == 0) {
                pcount--;
                if (pcount < 0)
                    throw new IllegalArgumentException("Cannot interpret '" + s + "': Parentheses do not match.");
            } else if (c == '\'')
                qcount = (qcount + 1) % 2;
            else if (i <= sl - opl && pcount == 0 && qcount == 0) {
                String ss = s.substring(i, i + opl);
                if (ss.equalsIgnoreCase(op)) {
                    boolean found = true;
                    if (delimiterRequired) {
                        // Check for delimiter before and after to make sure it is not part of another word
                        char chbefore = '\0';
                        if (i > 0)
                            chbefore = s.charAt(i - 1);
                        char chafter = '\0';
                        if (i < sl - opl)
                            chafter = s.charAt(i + opl);
                        found = delimiters.indexOf(chbefore) >= 0 && delimiters.indexOf(chafter) >= 0;
                    }
                    if (found) {
                        // We've found the operator, split the string
                        String[] sss = new String[2];
                        sss[0] = s.substring(0, i);
                        sss[1] = s.substring(i + opl);
                        return sss;
                    }
                }
            }
        }
        if (pcount > 0)
            throw new IllegalArgumentException("Cannot interpret '" + s + "': Parentheses do not match.");
        if (qcount > 0)
            throw new IllegalArgumentException("Cannot interpret '" + s + "': No closing '.");
        return null;
    }

    private static Object parseObject(String s) {
        s = s.trim();
        int sl = s.length();
        if (sl == 0)
            throw new IllegalArgumentException("Cannot interpret empty string.");
        if (s.equalsIgnoreCase("null"))
            return null;
        if (s.charAt(0) == '\'') {
            if (sl < 2 || s.charAt(sl - 1) != '\'')
                throw new IllegalArgumentException("Cannot interpret '" + s + "': No closing '.");
            return s.substring(1, sl - 1);
        }
        // Not in quotes - must be a number
        try {
            return Long.valueOf(s);
        } catch (Exception e) {
        }
        try {
            return Double.valueOf(s);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot interpret '" + s + "': Invalid number.");
        }
    }
}
