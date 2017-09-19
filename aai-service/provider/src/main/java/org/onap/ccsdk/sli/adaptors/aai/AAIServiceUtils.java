/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 *             reserved.
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

import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;
import org.openecomp.aai.inventory.v11.Relationship;
import org.openecomp.aai.inventory.v11.RelationshipData;
import org.openecomp.aai.inventory.v11.RelationshipList;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AAIServiceUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AAIService.class);

    public static String getPrimaryIdFromClass(Class<? extends AAIDatum> resourceClass){
        // 1. find class
        getLogger().debug(resourceClass.getName());
        AAIDatum instance = null;

        try {
            instance = resourceClass.newInstance();

            Annotation[] annotations = resourceClass.getAnnotations();
            for(Annotation annotation : annotations) {
                Class<? extends Annotation> anotationType = annotation.annotationType();
                String annotationName = anotationType.getName();

                // 2. find string property setters and getters for the lists
                if("javax.xml.bind.annotation.XmlType".equals(annotationName)){
                    XmlType order = (XmlType)annotation;
                    String[]  values = order.propOrder();
                    for(String value : values) {
                        String id = camelCaseToDashedString(value);
                        return id;
                    }
                }
            }
        } catch(Exception exc) {

        }
        return null;
    }

    public static String getSecondaryIdFromClass(Class<? extends AAIDatum> resourceClass){
        // 1. find class
        getLogger().debug(resourceClass.getName());
        AAIDatum instance = null;

        try {
            instance = resourceClass.newInstance();

            Annotation[] annotations = resourceClass.getAnnotations();
            for(Annotation annotation : annotations) {
                Class<? extends Annotation> anotationType = annotation.annotationType();
                String annotationName = anotationType.getName();

                // 2. find string property setters and getters for the lists
                if("javax.xml.bind.annotation.XmlType".equals(annotationName)){
                    boolean primaryIdFound = false;
                    XmlType order = (XmlType)annotation;
                    String[]  values = order.propOrder();
                    for(String value : values) {
                        String id = camelCaseToDashedString(value);
                        if(primaryIdFound) {
                            return id;
                        } else {
                            primaryIdFound = true;
                        }
                    }
                }
            }
        } catch(Exception exc) {

        }
        return null;
    }


    private static Logger getLogger() {
        return LOG;
    }


    private static final String regex = "([A-Z][a-z,0-9]+)";
    private static final String replacement = "-$1";

    public static String camelCaseToDashedString(String propOrder) {
        return propOrder.replaceAll(regex, replacement).toLowerCase();
    }

    public static HashMap<String,String> keyToHashMap(String key,    SvcLogicContext ctx) {
        if (key == null) {
            return (null);
        }

        getLogger().debug("Converting key [" + key + "] to where clause");

        if (key.startsWith("'") && key.endsWith("'")) {
            key = key.substring(1, key.length() - 1);

            getLogger().debug("Stripped outer single quotes - key is now [" + key + "]");
        }

        String[] keyTerms = key.split("\\s+");

        StringBuffer whereBuff = new StringBuffer();
        String term1 = null;
        String op = null;
        String term2 = null;
        HashMap<String, String> results = new HashMap<String, String>();

        for (int i = 0; i < keyTerms.length; i++) {
            if (term1 == null) {
                if ("and".equalsIgnoreCase(keyTerms[i])
                        || "or".equalsIgnoreCase(keyTerms[i])) {
                    // Skip over ADD/OR
                } else {
                    term1 = resolveTerm(keyTerms[i], ctx);
                }
            } else if (op == null) {
                if ("==".equals(keyTerms[i])) {
                    op = "=";
                } else {
                    op = keyTerms[i];
                }
            } else {
                term2 = resolveTerm(keyTerms[i], ctx);
                term2 = term2.trim().replace("'", "").replace("$", "").replace("'", "");
                results.put(term1,  term2);

                term1 = null;
                op = null;
                term2 = null;
            }
        }

        return (results);
    }

    private static String resolveTerm(String term, SvcLogicContext ctx) {
        if (term == null) {
            return (null);
        }

        getLogger().debug("resolveTerm: term is " + term);

        if (term.startsWith("$") && (ctx != null)) {
            // Resolve any index variables.

            return ("'" + resolveCtxVariable(term.substring(1), ctx) + "'");
        } else if (term.startsWith("'") || term.startsWith("\"")) {
            return (term);
        } else {
            return (term.replaceAll("-", "_"));

        }
    }

    private static String resolveCtxVariable(String ctxVarName, SvcLogicContext ctx) {

        if (ctxVarName.indexOf('[') == -1) {
            // Ctx variable contains no arrays
            return (ctx.getAttribute(ctxVarName));
        }

        // Resolve any array references
        StringBuffer sbuff = new StringBuffer();
        String[] ctxVarParts = ctxVarName.split("\\[");
        sbuff.append(ctxVarParts[0]);
        for (int i = 1; i < ctxVarParts.length; i++) {
            if (ctxVarParts[i].startsWith("$")) {
                int endBracketLoc = ctxVarParts[i].indexOf("]");
                if (endBracketLoc == -1) {
                    // Missing end bracket ... give up parsing
                    getLogger().warn("Variable reference " + ctxVarName
                            + " seems to be missing a ']'");
                    return (ctx.getAttribute(ctxVarName));
                }

                String idxVarName = ctxVarParts[i].substring(1, endBracketLoc);
                String remainder = ctxVarParts[i].substring(endBracketLoc);

                sbuff.append("[");
                sbuff.append(ctx.getAttribute(idxVarName));
                sbuff.append(remainder);

            } else {
                // Index is not a variable reference
                sbuff.append("[");
                sbuff.append(ctxVarParts[i]);
            }
        }

        return (ctx.getAttribute(sbuff.toString()));
    }

    public static void populateRelationshipDataFromPath(RelationshipList rl) throws URISyntaxException {
        List<Relationship> list =  rl.getRelationship();
        if(list != null && !list.isEmpty()) {
            for(Relationship relationship : list) {
                if(relationship.getRelationshipData().isEmpty()){
                    String link = relationship.getRelatedLink();
                    URI uri = new URI(link);
                        link = uri.getPath();
                    HashMap<String,String> contributors = pathToHashMap(link);
                    for(String key : contributors.keySet()) {
                        RelationshipData rd = new RelationshipData();
                        rd.setRelationshipKey(key);
                        rd.setRelationshipValue(contributors.get(key));
                        relationship.getRelationshipData().add(rd);
                    }
                }
            }
        }
    }

    protected static HashMap<String,String> pathToHashMap(String path) {
        HashMap<String, String> nameValues = new  HashMap<String, String>();

        String[] split = path.split("/");

        LinkedList<String> list = new LinkedList<String>( Arrays.asList(split));
        Iterator<String> it = list.iterator();

        while(it.hasNext()) {
            String tag = it.next();
            if(!tag.isEmpty()) {
                if(AAIRequest.getResourceNames().contains(tag)){
                    LOG.info(tag);
                    // get the class from tag
                    Class<? extends AAIDatum> clazz = null;
                    try {
                        clazz = AAIRequest.getClassFromResource(tag);
                        String fieldName = AAIServiceUtils.getPrimaryIdFromClass(clazz);

                        String value = it.next();
                        if(!StringUtils.isEmpty(value)){
                            nameValues.put(String.format("%s.%s", tag, fieldName), value);
                            switch(tag) {
                            case "cloud-region":
                            case "entitlement":
                            case "license":
                            case "route-target":
                            case "service-capability":
                            case "ctag-pool":
                                String secondaryFieldName = AAIServiceUtils.getSecondaryIdFromClass(clazz);
                                if(secondaryFieldName != null) {
                                    value = it.next();
                                    nameValues.put(String.format("%s.%s", tag, secondaryFieldName), value);
                                }
                                break;
                            default:
                                break;
                            }
                        }
                    } catch (ClassNotFoundException exc) {
                        LOG.info("Caught exception", exc);
                    }
                }
            }
        }
        return nameValues;
    }

    public static String getPathForResource(String resource, String key, SvcLogicContext ctx ) throws MalformedURLException{
        HashMap<String, String> nameValues = AAIServiceUtils.keyToHashMap(key, ctx);
        AAIRequest request = AAIRequest.createRequest(resource, nameValues);

        for(String name : nameValues.keySet()) {
            request.addRequestProperty(name, nameValues.get(name));
        }
        return request.getRequestPath();
    }

    public static boolean isValidFormat(String resource, HashMap<String, String> nameValues) {

        switch(resource){
        case "formatted-query":
        case "generic-query":
        case "named-query":
        case "nodes-query":
        case "linterface":
        case "l2-bridge-sbg":
        case "l2-bridge-bgf":
        case "echo":
        case "test":
            return true;
        }
        if(resource.contains(":")) {
            resource = resource.substring(0, resource.indexOf(":"));
        }

        Set<String> keys = nameValues.keySet();
        for(String key : keys) {
            if(!key.contains(".")) {
                if("depth".equals(key) || "related-to".equals(key) || "related_to".equals(key) || "related-link".equals(key) || "related_link".equals(key) || "selflink".equals(key))
                    continue;
                else {
                    getLogger().warn(String.format("key %s is incompatible with resource type '%s'", key, resource));
                }
            }
        }
        return true;
    }

    public static boolean containsResource(String resource, HashMap<String, String> nameValues) {
        if(resource.contains(":")) {
            return true;
        }

        switch(resource){
        case "custom-query":
        case "formatted-query":
        case "generic-query":
        case "named-query":
        case "nodes-query":
        case "linterface":
        case "l2-bridge-sbg":
        case "l2-bridge-bgf":
        case "echo":
        case "test":
            return true;

        default:
            if(nameValues.containsKey("selflink")) {
                return true;
            }
        }

        Set<String> tags = new HashSet<>();

        for(String key : nameValues.keySet()) {
            key = key.replace("_", "-");
            if(key.contains(".")) {
                String[] split = key.split("\\.");
                tags.add(split[0]);
            } else {
                tags.add(key);
            }
        }
        return tags.contains(resource);
    }
}
