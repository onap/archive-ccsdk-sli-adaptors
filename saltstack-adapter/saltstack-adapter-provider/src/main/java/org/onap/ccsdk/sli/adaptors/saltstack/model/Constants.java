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

package org.onap.ccsdk.sli.adaptors.saltstack.model;

public class Constants {
    public static final String NETCONF_SCHEMA = "sdnctl";
    public static final String SDNCTL_SCHEMA = "sdnctl";
    public static final String DEVICE_AUTHENTICATION_TABLE_NAME = "DEVICE_AUTHENTICATION";
    public static final String CONFIGFILES_TABLE_NAME = "CONFIGFILES";
    public static final String DEVICE_INTERFACE_LOG_TABLE_NAME = "DEVICE_INTERFACE_LOG";
    public static final String FILE_CONTENT_TABLE_FIELD_NAME = "FILE_CONTENT";
    public static final String FILE_NAME_TABLE_FIELD_NAME = "FILE_NAME";
    public static final String USER_NAME_TABLE_FIELD_NAME = "USER_NAME";
    public static final String PASSWORD_TABLE_FIELD_NAME = "PASSWORD";
    public static final String PORT_NUMBER_TABLE_FIELD_NAME = "PORT_NUMBER";
    public static final String VNF_TYPE_TABLE_FIELD_NAME = "VNF_TYPE";
    public static final String SERVICE_INSTANCE_ID_FIELD_NAME = "SERVICE_INSTANCE_ID";
    public static final String REQUEST_ID_FIELD_NAME = "REQUEST_ID";
    public static final String CREATION_DATE_FIELD_NAME = "CREATION_DATE";
    public static final String LOG_FIELD_NAME = "LOG";
    public static final String SDC_ARTIFACTS_TABLE_NAME = "ASDC_ARTIFACTS";
    public static final String PAYLOAD = "payload";
    public static final String CONNECTION_RETRY_DELAY = "org.onap.appc.ssh.connection.retry.delay";
    public static final String CONNECTION_RETRY_COUNT = "org.onap.appc.ssh.connection.retry.count";
    public static final int DEFAULT_CONNECTION_RETRY_DELAY = 60;
    public static final int DEFAULT_CONNECTION_RETRY_COUNT = 5;
    public static final int DEFAULT_SSH_COMMAND_RETRY_COUNT = 3;
    public static final int DEFAULT_CHECKACTIVE_RETRY_COUNT = 3;
    public static final int DEFAULT_CHECKACTIVE_RETRY_DELAY = 30;
    public static final int DEFAULT_STOP_RETRY_COUNT = 3;
    public static final int DEFAULT_STOP_RETRY_DELAY = 30;
    public static final String PARAM_IN_CONNECTION_DETAILS = "connection-details";
    public static final String PARAM_IN_NODE_NAME = "node-name";
    public static final String PARAM_IN_NODE_STATUS = "node-status";
    public static final String PARAM_IN_VM_URL = "vm-url";
    public static final String SKIP_EXECUTION_INSTALLER_BIN_FILE = "Skip-execution-installer-bin-file";
    public static final String SKIP_DEPLOY = "Skip-deploy";
    public static final String UPGRADE_VERSION = "upgrade-version";
    public static final String STATE_COMMAND = "/opt/jnetx/skyfall-scp/asp-state.sh | grep -o UP | wc -l";
    public static final String VNFC_STATE_COMMAND = "/opt/jnetx/skyfall-scp/asp-state.sh";
    public static final String RESTART_NODE_COMMAND = "/opt/jnetx/skyfall-scp/asp-stop.sh --restart -f --nodes";
    public static final String START_NODE_COMMAND = "/opt/jnetx/skyfall-scp/asp-start.sh -f --nodes";
    public static final String STOP_NODE_COMMAND = "/opt/jnetx/skyfall-scp/asp-stop.sh -f --nodes";
    public static final int STATE_COMMAND_RESULT = 18;
    public static final String FE_STATE_TRUE_TEST_COMMAND = "ssh -t -q fe1 /opt/omni/bin/swmml -e display-platform-status | grep -o TRUE | wc -l";
    public static final int FE_STATE_TRUE_TEST_RESULT = 22;
    public static final String FE_STATE_FALSE_TEST_COMMAND = "ssh -t -q fe1 /opt/omni/bin/swmml -e display-platform-status | grep -o FALSE | wc -l";
    public static final int FE_STATE_FALSE_TEST_RESULT = 2;
    public static final String FE_OPERATIONAL_TEST_COMMAND = "ssh -t -q fe1 /opt/omni/bin/swmml -e display-platform-status | grep -o 'NOT FULLY OPERATIONAL' | wc -l";
    public static final int FE_OPERATIONAL_TEST_RESULT = 2;
    public static final String SMP_CHECK_ACTIVE_STATE_COMMAND = "cat skyfall-scp/runtime/SCP_SMP_*/smp/log/system.log| grep SSS | tail -1";
    public static final String SMP_STATE_ACTIVE = "SMP is active";
    public static final String SMP_STATE_INACTIVE = "SMP is not active";
    public static final String RSYNC_COMMAND = "yes n | /opt/jnetx/skyfall-scp/asp-rsync.sh --check | grep -o 'is active' | wc -l";
    public static final int RSYNC_COMMAND_RESULT = 9;
    public static final String PARAM_IN_TIMEOUT = "timeout";
    public static final String PARAM_IN_FILE_URL = "source-file-url";
    public static final String DOWNLOAD_COMMAND = "wget -N %s";
    public static final String[] VM_NAMES = new String[]{"fe1", "fe2", "be1", "be2", "be3", "be4", "be5", "smp1", "smp2"};
    public static final String DEFAULT_DISK_SPACE = "10240000";
    public static final String DF_COMMAND_TEMPLATE = "ssh %s df | grep vda1 | grep -v grep | tr -s ' '|cut -d ' ' -f4";
    public static final String DG_OUTPUT_STATUS_MESSAGE = "output.status.message";
    public static final String ATTRIBUTE_ERROR_MESSAGE = "error-message";
    public static final String CONNECTION_DETAILS_FIELD_NAME = "connection-details";
    public static final String VNF_HOST_IP_ADDRESS_FIELD_NAME = "vnf-host-ip-address";
    public static final String VNF_HOST_IP2_ADDRESS_FIELD_NAME = "vnf-host-ip2-address";
    public static final String DG_ERROR_FIELD_NAME = "org.openecom.appc.dg.error";

    private Constants() {
    }
}
