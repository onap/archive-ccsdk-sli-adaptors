'''
/*-
* ============LICENSE_START=======================================================
* ONAP : APPC
* ================================================================================
* Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
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
'''

import os, subprocess
import sys
from collections import namedtuple
import json

import uuid

def ansibleSysCall (inventory_path, playbook_path, nodelist, mandatory,
                    envparameters, localparameters, lcm, timeout):

    print "***> in AnsibleModule.ansibleSysCall"
    print "   EnvParameters:  ", envparameters
    print "   LocalParameters:", localparameters
    print "   Inventory:      ", inventory_path
    print "   Playbook:       ", playbook_path
    print "   NodeList:       ", nodelist
    print "   Mandatory:      ", mandatory
    print "   Timeout:        ", timeout
    log = []

    str_parameters = ''
                
    if not envparameters == {}:
        for key in envparameters:
            if str_parameters == '':
                str_parameters = '"'  + str(key) + '=\'' + str(envparameters[key])  + '\''
            else:
                str_parameters += ' '  + str(key) + '=\'' + str(envparameters[key])  + '\''
        str_parameters += '"'
                        
    if len(str_parameters) > 0:
        cmd = 'timeout --signal=KILL ' + str(timeout) + \
              ' ansible-playbook -v --extra-vars ' + str_parameters + ' -i ' + \
              inventory_path + ' ' + playbook_path
    else:
        cmd = 'timeout --signal=KILL ' + str(timeout) + \
              ' ansible-playbook -v -i ' + inventory_path + ' ' + playbook_path

    print "   CMD:            ", cmd

    print "\n =================ANSIBLE STDOUT BEGIN============================================\n"
    p = subprocess.Popen(cmd, shell=True,
                         stdout=subprocess.PIPE,
                         stderr=subprocess.STDOUT) 
    # p.wait()
    (stdout_value, err) = p.communicate()
    
    stdout_value_cleanup = ''
    for line in stdout_value:
        stdout_value_cleanup += line.replace('  ', ' ')
    stdout_value = stdout_value_cleanup.splitlines() 

    ParseFlag = False
    retval = {}
    returncode = p.returncode

    if returncode == 137:
        
        print "   ansible-playbook system call timed out"
        # ansible-playbook system call timed out
        for line in stdout_value: # p.stdout.readlines():
            log.append (line)
            
            
    elif 'ping' in lcm:

        targetnode = envparameters['TargetNode'].split(' ')
        str_json = None
        for line in stdout_value: # p.stdout.readlines():
            print line # line,
            if "PLAY RECAP" in line:
                ParseFlag = False
            if ParseFlag and len(line.strip())>0:
                str_json += line.strip()
            if "TASK [debug]" in line:
                ParseFlag = True
                str_json = ''
            log.append (line)

        if str_json:
            if '=>' in str_json:
                out_json =eval(str_json.split('=>')[1].replace('true','True').replace('false','False'))

                if 'ping.stdout_lines' in out_json:
                    for node in targetnode:
                        ip_address = node
                        ok_flag = '0'
                        changed_flag = '0'
                        unreachable_flag = '0'
                        failed_flag = '1'
                        for rec in out_json['ping.stdout_lines']:
                            if node in rec and "is alive" in rec:
                                ok_flag = '1'
                                changed_flag = '1'
                                unreachable_flag = '0'
                                failed_flag = '0'
                        for rec in out_json['ping.stdout_lines']:
                            if node in rec and "address not found" in rec:
                                ok_flag = '0'
                                changed_flag = '0'
                                unreachable_flag = '1'
                                failed_flag = '0'
                        retval[ip_address]=[ok_flag, changed_flag, unreachable_flag,
                                            failed_flag]
    else:
            
        for line in stdout_value: # p.stdout.readlines():
            print line # line,
            if ParseFlag and len(line.strip())>0:
                ip_address = line.split(':')[0].strip()
                ok_flag = line.split(':')[1].strip().split('=')[1].split('changed')[0].strip()
                changed_flag = line.split(':')[1].strip().split('=')[2].split('unreachable')[0].strip()
                unreachable_flag = line.split(':')[1].strip().split('=')[3].split('failed')[0].strip()
                failed_flag = line.split(':')[1].strip().split('=')[4].strip()
                retval[ip_address]=[ok_flag, changed_flag, unreachable_flag, failed_flag]
            if "PLAY RECAP" in line:
                ParseFlag = True
            log.append (line)
        
    # retval['p'] = p.wait()

    print " =================ANSIBLE STDOUT END==============================================\n"

    return retval, log, returncode

if __name__ == '__main__':

    from multiprocessing import Process, Value, Array, Manager
    import time

    nodelist = 'host'

    playbook_file = 'ansible_sleep@0.00.yml'


    d = Manager().dict()
    
    p = Process(nodelist=ansible_call, args=('ansible_module_config', playbook_file, nodelist,d, ))
    p.start()

    print "Process running"
    print d
    p.join()
    print d
