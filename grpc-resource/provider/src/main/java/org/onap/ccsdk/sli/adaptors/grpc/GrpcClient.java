/*
 * Copyright (C) 2019 Bell Canada.
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
package org.onap.ccsdk.sli.adaptors.grpc;

import java.util.Map;
import org.onap.ccsdk.sli.core.sli.SvcLogicContext;
import org.onap.ccsdk.sli.core.sli.SvcLogicJavaPlugin;
import org.onap.ccsdk.sli.core.sli.SvcLogicResource.QueryStatus;

public interface GrpcClient extends SvcLogicJavaPlugin {

    /**
     * Send a request to process to a gRPC server.
     *
     * @param parameters HashMap<String,String> of parameters passed by the DG to this function
     * <table border="1">
     * <thead><th>parameter</th><th>Mandatory/Optional</th><th>description</th></thead>
     * <tbody>
     * <tr><td>host</td><td>Mandatory</td><td>The gRPC server hostname or ip address.</td></tr>
     * <tr><td>port</td><td>Mandatory</td><td>The gRPC server port</td></tr>
     * <tr><td>prefix_id</td><td>Mandatory</td><td>The prefix from which to get next available IP.</td></tr>
     * </tbody>
     * </table>
     */
    QueryStatus sendRequest(Map<String, String> parameters, SvcLogicContext ctx);
}
