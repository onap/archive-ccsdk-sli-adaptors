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
/**
 * @author Rich Tabedzki
 *
 */
package org.onap.ccsdk.sli.adaptors.aai;

/**
 * THIS CLASS IS A COPY OF {@link AAIExecutorInterface} WITH REMOVED OSGi DEPENDENCIES
 */
public interface AAIExecutorInterfaceLighty {
	public String get(AAIRequestLighty request) throws AAIServiceException;
	public String post(AAIRequestLighty request) throws AAIServiceException;
	public Boolean delete(AAIRequestLighty request, String resourceVersion) throws AAIServiceException;
	public Object query(AAIRequestLighty request, Class clas) throws AAIServiceException;
	public Boolean patch(AAIRequestLighty request, String resourceVersion) throws AAIServiceException;
}
