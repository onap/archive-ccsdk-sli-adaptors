/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 - 2018 AT&T Intellectual Property. All rights
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

package org.onap.ccsdk.sli.adaptors.aai;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.onap.ccsdk.sli.adaptors.aai.AAIRequest;
import org.onap.ccsdk.sli.adaptors.aai.AAIService;
import org.onap.ccsdk.sli.adaptors.aai.AAIServiceUtils;
import org.onap.ccsdk.sli.adaptors.aai.EchoRequest;
import org.onap.ccsdk.sli.adaptors.aai.data.AAIDatum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PathCreationTest {

    private static final Logger LOG = LoggerFactory.getLogger(PathCreationTest.class);
    
    private static AAIService aaiService;

    @BeforeClass
    public static void setUp() throws Exception {
        aaiService = new AAIService(
                AAIService.class.getResource(AAIService.AAICLIENT_PROPERTIES));
        LOG.info("\nTaicAAIResourceTest.setUp\n");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        LOG.info("----------------------- AAIResourceTest.tearDown -----------------------");
    }

    static Set<String> resourceNames = new TreeSet<String>();
    static Map<String, String> tagValues = new LinkedHashMap<String, String>();

    
    @Test
    public void test01()
    {
        LOG.info("----------------------- Test: " + new Object(){}.getClass().getEnclosingMethod().getName() + " -----------------------");

        try
        {
            File file = new File(this.getClass().getResource("/aai-path.properties").getFile());
            if(!file.exists()) {
                fail("File does not exist");
                return;
            }
            FileReader reader = new FileReader(file);
            
            Properties properties = new Properties();
            properties.load(reader);
            LOG.info("loaded " + properties.size());
            
            Set<String> keys = properties.stringPropertyNames();
            
            int index = 0;
            
            for(String key : keys) {
                String[] tags = key.split("\\|");
                for(String tag : tags) {
                    if(!resourceNames.contains(tag)) {
                        resourceNames.add(tag);
                        tagValues.put(tag, Integer.toString(++index));
                    }
                }
                BitSet bs = new BitSet(256);
                for(String tag : tags) {
                    String value = tagValues.get(tag);
                    Integer bitIndex = Integer.parseInt(value) ;
                    bs.set(bitIndex);
                }
                String path = properties.getProperty(key);
                LOG.info(String.format("bitset %s\t\t%s", bs.toString(), path));
            }
        }
        catch (Exception e)
        {
            LOG.error("Caught exception", e);
            fail("Caught exception");
        }
    }
    
    @Test
    public void test02() {
        try {
            Map<String, String> nameValues = new HashMap<String, String> ();
            nameValues.put("pserver.hostname", "USAUTOUFTIL0205UJZZ01");
            
            AAIRequest request = AAIRequest.createRequest("pserver", nameValues);
            request.addRequestProperty("pserver.hostname", "USAUTOUFTIL0205UJZZ01");

            URL url = request.getRequestUrl("GET", null);
            url.getPath();
            LOG.info("Received response");
        } catch(Exception exc) {
            LOG.info("Caught exception", exc);
        }
    }
    
    @Test
    public void test03() {
        HashMap<String, String> nameValues = new  HashMap<String, String>();
        
        String path = 
//                "/aai/v11/network/site-pair-sets/site-pair-set/a3839637-575e-49b3-abb7-a003b0d4cc35/routing-instances/routing-instance/7f08a85e-716f-4bc2-a4f4-70801b07a5e6";
        "/aai/v10/cloud-infrastructure/cloud-regions/cloud-region/att-aic/AAIAIC25";
        
        String[] split = path.split("/");
        
        LinkedList<String> list = new LinkedList<String>( Arrays.asList(split));
        ListIterator<String> it = list.listIterator();
        
//        for(String tag : split) {
        while(it.hasNext()) {
            String tag = it.next();
            if(!tag.isEmpty()) {
                if(resourceNames.contains(tag)){
                    LOG.info(tag);
                    // get the class from tag
                    Class<? extends AAIDatum> clazz = null;
                    try {
                        clazz = AAIRequest.getClassFromResource(tag);
                        String fieldName = AAIServiceUtils.getPrimaryIdFromClass(clazz);
                        int nextIndex = it.nextIndex();

                        String value = list.get(nextIndex);
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
                    } catch (Exception exc) {
                        LOG.info("Caught exception", exc);
                    }
                    // get id from class
                    // read the follwoing field
                    // create relationship data
                }
            }
        }
        LOG.info(nameValues.toString());
    }
}
