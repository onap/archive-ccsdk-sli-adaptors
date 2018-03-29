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

import time, datetime, json, os, sys, subprocess, re
import uuid
import tarfile
import shutil

import requests

import cherrypy
from cherrypy.lib.httputil import parse_query_string
from cherrypy.lib import auth_basic

from multiprocessing import Process, Manager

from AnsibleModule import ansibleSysCall

import AnsibleSql
from AnsibleSql import readPlaybook, readCredentials

from os import listdir
from os.path import isfile, join

TestRecord = Manager().dict()
ActiveProcess = {}

def sys_call (cmd):
    p = subprocess.Popen(cmd, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    output = p.stdout.readlines()
    retval = p.wait()
    if len (output) > 0:
        for i in range(len(output)):
            output[i] = output[i].strip()
    return retval, output

def callback (Id, Result, Output, Log, returncode):
    
    print "***> in RestServer.callback"

    if Id in TestRecord:
        time_now = datetime.datetime.utcnow()
        delta_time = (time_now - TestRecord[Id]['Time']).total_seconds()
        Result['PlaybookName'] = TestRecord[Id]['PlaybookName']
        Result['Version'] = TestRecord[Id]['Version']
        if returncode == 137:
            Result['StatusCode'] = 500
            Result['StatusMessage'] = "TERMINATED"
        else:
            Result['StatusCode'] = 200
            Result['StatusMessage'] = "FINISHED"

        # Need to update the whole data structure for key=Id otherwise Manager is not updated
        TestRecord[Id] = {'PlaybookName': TestRecord[Id]['PlaybookName'],
                          'LCM': TestRecord[Id]['LCM'],
                          'Version': TestRecord[Id]['Version'],
                          'NodeList': TestRecord[Id]['NodeList'],
                          'HostGroupList': TestRecord[Id]['HostGroupList'],
                          'HostNameList': TestRecord[Id]['HostNameList'],
                          'Time': TestRecord[Id]['Time'],
                          'Timeout': TestRecord[Id]['Timeout'],
                          'Duration': str(delta_time),
                          'EnvParameters': TestRecord[Id]['EnvParameters'],
                          'LocalParameters': TestRecord[Id]['LocalParameters'],
                          'FileParameters': TestRecord[Id]['FileParameters'],
                          'CallBack': TestRecord[Id]['CallBack'],
                          'Result': Result,
                          'Log': Log, 
                          'Output': Output, 
                          'Path': TestRecord[Id]['Path'],
                          'Mandatory': TestRecord[Id]['Path']}

        if not TestRecord[Id]['CallBack'] == None:
            
            # Posting results to callback server

            data = {"StatusCode": 200,
                    "StatusMessage": "FINISHED",
                    "PlaybookName": TestRecord[Id]["PlaybookName"],
                    "Version": TestRecord[Id]["Version"],
                    "Duration": TestRecord[Id]["Duration"],
                    "Results": TestRecord[Id]['Result']['Results']}

            if not TestRecord[Id]['Output']['Output'] == {}:
                for key in data["Results"]:
                    if key in TestRecord[Id]['Output']['Output']:
                        data["Results"][key]["Output"] = TestRecord[Id]['Output']['Output'][key]

            print "     Posting to", TestRecord[Id]['CallBack']
            
            s = requests.Session()
            r = s.post(TestRecord[Id]['CallBack'], data = json.dumps(data),
                       headers = {'content-type': 'application/json'})
            print  "     Response", r.status_code, r.text

def RunAnsible_Playbook (callback, Id, Inventory, Playbook, NodeList, TestRecord,
                         Path, ArchiveFlag):

    print "***> in RestServer.RunAnsible_Playbook"

    # Run test in playbook for given target
    Result = ''

    retval, log, returncode = ansibleSysCall (Inventory, Playbook, NodeList,
                                              TestRecord[Id]['Mandatory'],
                                              TestRecord[Id]['EnvParameters'],
                                              TestRecord[Id]['LocalParameters'],
                                              TestRecord[Id]['LCM'],
                                              TestRecord[Id]['Timeout'])


    print "   returncode:", returncode
    print "   retval:    ", retval
    print "   log:       ", log
    
    Log = ''.join(log)
    Output = {'Output': {}}
    
    onlyfiles = [f for f in listdir(Path)
                 if isfile(join(Path, f))]

    for file in onlyfiles:
        if "results.txt" in file:
            f = open(Path + "/" + file, "r")
            key = file.split("_")[0]
            Output['Output'][key] = f.read()
            f.close()

    Result = {'Results': {}}
    if 'could not be found' in Log:
        Result['Results'] = {"StatusCode": 101,
                             "StatusMessage": "PLAYBOOK NOT FOUND"}
    if returncode == 137:
        Result['Results'] = {"StatusCode": 500,
                             "StatusMessage": "TERMINATED"}

    elif TestRecord[Id]['NodeList'] == []:
        
        host_index = None
        
        if 'TargetNode' in TestRecord[Id]['EnvParameters']:
            targetlist = TestRecord[Id]['EnvParameters']['TargetNode'].split(' ')
        else:
            targetlist = ["localhost"]
            
        for key in retval:
            for i in range (len(targetlist)):
                if key in targetlist[i]:
                    host_index = i
    
            if int(retval[key][0]) > 0 and int(retval[key][2]) == 0 and \
                   int(retval[key][3]) == 0:

                if host_index:
                    Result['Results'][targetlist[host_index]] = \
                             {"GroupName": 'na', "StatusCode": 200, \
                              "StatusMessage": "SUCCESS"}
                else:
                    Result['Results'][key] = \
                             {"GroupName": 'na', "StatusCode": 200, \
                              "StatusMessage": "SUCCESS"}                    
            elif int(retval[key][2]) > 0:
                if host_index:
                    Result['Results'][targetlist[host_index]] = \
                       {"GroupName": 'na', "StatusCode": 400, \
                        "StatusMessage": "NOT REACHABLE"}
                else:
                    Result['Results'][key] = \
                       {"GroupName": 'na', "StatusCode": 400, \
                        "StatusMessage": "NOT REACHABLE"}                    
            elif int(retval[key][3]) > 0:
                if host_index:                
                    Result['Results'][targetlist[host_index]] = \
                       {"GroupName": 'na', "StatusCode": 400, \
                        "StatusMessage": "FAILURE"}
                else:
                    Result['Results'][key] = \
                       {"GroupName": 'na', "StatusCode": 400, \
                        "StatusMessage": "FAILURE"}                    
    else:
        
        for key in retval:

            if len(TestRecord[Id]['HostNameList']) > 0:

                host_index = []
                for i in range (len(TestRecord[Id]['HostNameList'])):
                    if key in TestRecord[Id]['HostNameList'][i]:
                        host_index.append(i)

                if int(retval[key][0]) > 0 and int(retval[key][2]) == 0 and \
                       int(retval[key][3]) == 0:

                    if len(host_index) > 0:
                        Result['Results'][TestRecord[Id]['HostNameList'][host_index[0]]] = \
                          {"GroupName": TestRecord[Id]['HostGroupList'][host_index[0]],
                           "StatusCode": 200, "StatusMessage": "SUCCESS"}
                    
                        for i in range (1, len(host_index)):
                            Result['Results'][TestRecord[Id]['HostNameList'][host_index[i]]]["GroupName"]+=\
                             "," + TestRecord[Id]['HostGroupList'][host_index[i]]
                    else:
                       Result['Results'][key] = \
                          {"GroupName": key,
                           "StatusCode": 200, "StatusMessage": "SUCCESS"}  

                elif int(retval[key][2]) > 0:

                    if len(host_index) > 0:
                        Result['Results'][TestRecord[Id]['HostNameList'][host_index[0]]] = \
                          {"GroupName": TestRecord[Id]['HostGroupList'][host_index[0]],
                           "StatusCode": 400, "StatusMessage": "NOT REACHABLE"}
                    
                        for i in range (1, len(host_index)):
                            Result['Results'][TestRecord[Id]['HostNameList'][host_index[i]]]["GroupName"]+=\
                             "," + TestRecord[Id]['HostGroupList'][host_index[i]]
                    else:
                       Result['Results'][key] = \
                          {"GroupName": key,
                           "StatusCode": 200, "StatusMessage": "NOT REACHABLE"}  
                    
                elif int(retval[key][3]) > 0:

                    if len(host_index) > 0:
                        Result['Results'][TestRecord[Id]['HostNameList'][host_index[0]]] = \
                          {"GroupName": TestRecord[Id]['HostGroupList'][host_index[0]],
                           "StatusCode": 400, "StatusMessage": "FAILURE"}
                    
                        for i in range (1, len(host_index)):
                            Result['Results'][TestRecord[Id]['HostNameList'][host_index[i]]]["GroupName"]+=\
                             "," + TestRecord[Id]['HostGroupList'][host_index[i]]
                    else:
                       Result['Results'][key] = \
                          {"GroupName": key,
                           "StatusCode": 200, "StatusMessage": "FAILURE"}                          
            else:
                host_index = None
                for i in range (len(TestRecord[Id]['NodeList'])):
                    if key in TestRecord[Id]['NodeList'][i]:
                        host_index = i
    
                if int(retval[key][0]) > 0 and int(retval[key][2]) == 0 and \
                       int(retval[key][3]) == 0:
                    Result['Results'][TestRecord[Id]['NodeList'][host_index]] = \
                             {"GroupName": 'na', "StatusCode": 200, \
                             "StatusMessage": "SUCCESS"}
                elif int(retval[key][2]) > 0:
                    Result['Results'][TestRecord[Id]['NodeList'][host_index]] = \
                       {"GroupName": 'na', "StatusCode": 400, "StatusMessage": "NOT REACHABLE"}
                elif int(retval[key][3]) > 0:
                    Result['Results'][TestRecord[Id]['NodeList'][host_index]] = \
                       {"GroupName": 'na', "StatusCode": 400, "StatusMessage": "FAILURE"}
    
    callback (Id, Result, Output, Log, returncode)

class TestManager (object):

    @cherrypy.expose
    @cherrypy.tools.json_out()
    @cherrypy.tools.json_in()
    @cherrypy.tools.allow(methods=['POST', 'GET', 'DELETE'])

    def Dispatch(self, **kwargs):

        # Let cherrypy error handler deal with malformed requests
        # No need for explicit error handler, we use default ones

        time_now = datetime.datetime.utcnow()

        # Erase old test results (2x timeout)
        if TestRecord:
            for key in TestRecord.copy():
                delta_time = (time_now - TestRecord[key]['Time']).seconds
                if delta_time > 2*TestRecord[key]['Timeout']:
                    print "Deleted history for test", key
                    if os.path.exists(TestRecord[key]['Path']):
                        shutil.rmtree (TestRecord[key]['Path'])
                    del TestRecord[key]
                    
        print "***> in RestServer.Dispatch:", cherrypy.request.method

        HomeDir = os.path.dirname(os.path.realpath("~/"))

        if 'POST' in cherrypy.request.method:
            
            input_json = cherrypy.request.json
            print "   Payload:      ", input_json

            if 'Id' in input_json and 'PlaybookName' in input_json:

                if True:

                    if not input_json['Id'] in TestRecord:
                    
                        Id = input_json['Id']
                        PlaybookName = input_json['PlaybookName']

                        version = None
                        if 'Version' in input_json:
                            version = input_json['Version']
                    
                        AnsibleInvFail = True
                        AnsiblePlaybookFail = True

                        MySqlConFail = True
                        MySqlCause = ''

                        LocalNodeList = None

                        str_uuid = str (uuid.uuid4())
                        
                        LCM = PlaybookName.split(".")[0].split('_')[-1]
                        PlaybookDir = HomeDir + "/" + ansible_temp + "/" + \
                                      PlaybookName.split(".")[0] + "_" + str_uuid
                        AnsibleInv = LCM + "_" + "inventory"
                        ArchiveFlag = False

                        print "   LCM:          ", LCM
                        print "   PlaybookDir:  ", ansible_temp + PlaybookDir.split(ansible_temp)[1]
                        print "   AnsibleInv:   ", AnsibleInv
                        print "   ansible_temp: ", ansible_temp

                        if not os.path.exists(HomeDir + "/" + ansible_temp):
                            os.makedirs(HomeDir + "/" + ansible_temp)

                        os.mkdir(PlaybookDir)

                        # Process inventory file for target
                    
                        hostgrouplist = []
                        hostnamelist = []

                        NodeList = []
                        if 'NodeList' in input_json:
                            NodeList = input_json['NodeList']

                        print "   NodeList:     ", NodeList

                        if NodeList == []:
                                # By default set to local host
                                AnsibleInvFail = False

                                LocalNodeList = "host"
                                LocalCredentials = "localhost   ansible_connection=local"
                                f = open(PlaybookDir + "/" + AnsibleInv, "w")
                                f.write("[" + LocalNodeList + "]\n")
                                f.write(LocalCredentials)
                                f.close()

                        else:

                                if from_files:
                                    
                                    # Get credentials from file

                                    data_inventory_orig = {}
                                    data_inventory_target = {}
                                    curr_group = None

                                    print "***>", ansible_path + "/" + ansible_inv
                                    f = open(ansible_path + "/" + ansible_inv, "r")
                                    for line in f:
                                        line = line.rstrip()
                                        
                                        if len(line)> 0:
                                            if '#' not in line:
                                                if "[" in line and "]" in line:
                                                    data_inventory_orig[line] = []
                                                    curr_group = line
                                                else:
                                                    data_inventory_orig[curr_group].append(line)
                                    f.close()

                                    for node in NodeList:
                                        Fail = True
                                        if "[" + node + "]" in data_inventory_orig:
                                            if not "[" + node + "]" in data_inventory_target:

                                                print "RESET", "[" + node + "]"
                                                data_inventory_target["[" + node + "]"] = []
                                            else:
                                                print "OK", "[" + node + "]"
                                            Fail = False
                                            for cred in data_inventory_orig["[" + node + "]"]:
                                                data_inventory_target["[" + node + "]"].append(cred)
                                                
                                        else:
                                            for key in data_inventory_orig:
                                                if node in " ".join(data_inventory_orig[key]):
                                                    if not key in data_inventory_target:
                                                        data_inventory_target[key] = []
                                                    for cred in data_inventory_orig[key]:
                                                        if node in cred:
                                                            data_inventory_target[key].append(cred)
                                                            Fail = False

                                        if Fail:
                                            data_inventory_target["["+node+"]"] = \
                                                   [node + " ansible_connection=ssh ansible_ssh_user=na ansible_ssh_private_key_file=na"]
                                    
                                    AnsibleInvFail = False

                                    f = open(PlaybookDir + "/" + AnsibleInv, "w")
                                    for key in data_inventory_target:
                                        f.write(key + "\n")
                                        for rec in data_inventory_target[key]:
                                            hostgrouplist.append(key.replace("[", '').replace("]", ''))
                                            hostnamelist.append(rec.split(' ')[0])
                                            f.write(rec + "\n")
                                    f.close()

                                else:
                                    
                                    # Get credentials from mySQL
                            
                                    sqlintf = AnsibleSql.mySql (host, user, passwd,
                                                                db)
                                    if sqlintf.con:
                                        MySqlConFail = False
                                        errorCode, diag = readCredentials (sqlintf,
                                                                           NodeList)
                                    
                                        print errorCode, diag 
                                        if len (diag) > 0:
                                            f = open(PlaybookDir + "/" + AnsibleInv,
                                                     "w")
                                            AnsibleInvFail = False
                                            # [hostgroup, hostname, credentials]
                                            for i in range(len(diag)):
                                                f.write('[' +  diag[i][0] + ']' + "\n")
                                                f.write(diag[i][1]+ " " +  diag[i][2] + "\n\n")
                                                hostgrouplist.append(diag[i][0])
                                                hostnamelist.append(diag[i][1])
                                            f.close()
                                        else:
                                            MySqlConFailCause = sqlintf.error
                                    sqlintf.Close()

                        timeout = timeout_seconds
                        if 'Timeout' in input_json:
                            timeout = int (input_json['Timeout'])

                        EnvParam = {}
                        if 'EnvParameters' in input_json:
                            EnvParam = input_json['EnvParameters']

                        LocalParam = {}
                        if 'LocalParameters' in input_json:
                            LocalParam = input_json['LocalParameters']

                        FileParam = {}
                        if 'FileParameters' in input_json:
                            FileParam = input_json['FileParameters']
                    
                        callback_flag = None
                        if 'CallBack' in input_json:
                            callback_flag = input_json['CallBack']

                        TestRecord[Id] = {'PlaybookName': PlaybookName,
                                          'LCM': LCM,
                                          'Version': version,
                                          'NodeList': NodeList,
                                          'HostGroupList': hostgrouplist,
                                          'HostNameList': hostnamelist,
                                          'Time': time_now,
                                          'Duration': timeout,
                                          'Timeout': timeout,
                                          'EnvParameters': EnvParam,
                                          'LocalParameters': LocalParam,
                                          'FileParameters': FileParam,
                                          'CallBack': callback_flag,
                                          'Result': {"StatusCode": 100,
                                                     "StatusMessage": 'PENDING',
                                                     "ExpectedDuration": str(timeout) + "sec"},
                                          'Log': '',
                                          'Output': {},
                                          'Path': PlaybookDir,
                                          'Mandatory': None}

                        # Write files
                        
                        if not TestRecord[Id]['FileParameters'] == {}:
                            for key in TestRecord[Id]['FileParameters']:
                                filename = key
                                filecontent = TestRecord[Id]['FileParameters'][key]
                                f = open(PlaybookDir + "/" + filename, "w")
                                f.write(filecontent)
                                f.close()
                                
                        
                        # Process playbook

                        if from_files:
                            
                            # Get playbooks from files

                            MySqlConFail = False
                            
                            version = None
                            target_PlaybookName = None
                            
                            if '@' in PlaybookName:
                                version = PlaybookName.split("@")[1]
                                version = version.replace('.yml','')
                                version = version.replace('.tar.gz','')

                            onlyfiles = [f for f in listdir(ansible_path)
                                         if isfile(join(ansible_path, f))]

                            version_max = '0.00'
                            version_target = ''
                            
                            for file in onlyfiles:
                                if LCM in file:
                                    temp_version = file.split("@")[1]
                                    temp_version = temp_version.replace('.yml','')
                                    temp_version = temp_version.replace('.tar.gz','')
                                    if version_max < temp_version:
                                        version_max = temp_version
                                    
                                    if not version == None:
                                        if version in PlaybookName:
                                            version_target = version
                                            target_PlaybookName = file
                                            
                            if target_PlaybookName == None:
                                for file in onlyfiles:
                                    if LCM in file and version_max in file:
                                        target_PlaybookName = file
                                        version_target = version_max
                                        
                            if target_PlaybookName:
                                AnsiblePlaybookFail = False
                                readversion = version_target
                                src  = ansible_path + "/" + target_PlaybookName
                                if ".tar.gz" in target_PlaybookName:
                                    dest = PlaybookDir + "/" + LCM + ".tar.gz"
                                    shutil.copy2(src, dest)
                                    retcode = subprocess.call(['tar', '-xvzf',
                                                   dest, "-C", PlaybookDir])
                                    ArchiveFlag = True
                                else:
                                    dest = PlaybookDir + "/" + LCM + ".yml"
                                    shutil.copy2(src, dest)
                            
                        else:
                            # Get playbooks from mySQL

                            sqlintf = AnsibleSql.mySql (host, user, passwd, db)
                            if sqlintf.con:
                                MySqlConFail = False
                                
                                name, readversion, AnsiblePlaybookFail, diag = \
                                  readPlaybook (sqlintf, PlaybookName.split(".")[0],
                                                version)
                                
                                if not AnsiblePlaybookFail:

                                    f = open(PlaybookDir + "/" + LCM + diag[1], "w")
                                    f.write(diag[0])
                                    f.close()
                                    
                                    if ".tar.gz" in diag[1]:
                                        retcode = subprocess.call(['tar', '-xvzf',
                                         PlaybookDir + "/" + LCM + diag[1], "-C", PlaybookDir])
                                        f.close()
                                        ArchiveFlag = True
                            else:
                                MySqlConFailCause = sqlintf.error
                            sqlintf.Close()

                        if MySqlConFail:
                            if os.path.exists(PlaybookDir):
                                shutil.rmtree (PlaybookDir)
                            del TestRecord[Id]
                            return {"StatusCode": 101,
                                    "StatusMessage": "CANNOT CONNECT TO MYSQL: " \
                                    + MySqlConFailCause}
                        elif AnsiblePlaybookFail:
                            if os.path.exists(PlaybookDir):
                                shutil.rmtree (PlaybookDir)
                            del TestRecord[Id]
                            return {"StatusCode": 101,
                                    "StatusMessage": "PLAYBOOK NOT FOUND"}
                        elif AnsibleInvFail:
                            if os.path.exists(PlaybookDir):
                                shutil.rmtree (PlaybookDir)
                            del TestRecord[Id]
                            return {"StatusCode": 101,
                                    "StatusMessage": "NODE LIST CREDENTIALS NOT FOUND"}
                        else:

                            # Test EnvParameters
                            playbook_path = None
                            if ArchiveFlag:
                                for dName, sdName, fList in os.walk(PlaybookDir):
                                    if LCM+".yml" in fList:
                                        playbook_path = dName
                            else:
                                playbook_path = PlaybookDir

                            # Store local vars
                            if not os.path.exists(playbook_path + "/vars"):
                                os.mkdir(playbook_path + "/vars")
                            if not os.path.isfile(playbook_path + "/vars/defaults.yml"):
                                os.mknod(playbook_path + "/vars/defaults.yml")

                            for key in TestRecord[Id]['LocalParameters']:
                                host_index = []
                                for i in range(len(TestRecord[Id]['HostNameList'])):
                                    if key in TestRecord[Id]['HostNameList'][i]:
                                        host_index.append(i)
                                if len(host_index) == 0:
                                    for i in range(len(TestRecord[Id]['HostGroupList'])):
                                        if key in TestRecord[Id]['HostGroupList'][i]:
                                            host_index.append(i)
                                if len(host_index) > 0:
                                    for i in range(len(host_index)):
                                        f = open(playbook_path + "/vars/" +
                                                 TestRecord[Id]['HostNameList'][host_index[i]] +
                                                 ".yml", "a")
                                        for param in TestRecord[Id]['LocalParameters'][key]:
                                            f.write(param + ": " +
                                             str (TestRecord[Id]['LocalParameters'][key][param]) +
                                                  "\n")
                                        f.close()

                            # Get mandatory parameters from playbook
                            Mandatory = []
                            with open(playbook_path + "/" + LCM + ".yml") as origin_file:
                                for line in origin_file:
                                    if "Mandatory" in line:
                                        temp = line.split(":")[1].strip().replace(' ', '')
                                        if len(temp) > 0:
                                            Mandatory = temp.split(",")

                            TestRecord[Id] = {'PlaybookName': TestRecord[Id]['PlaybookName'],
                                              'LCM': TestRecord[Id]['LCM'],
                                              'Version': readversion,
                                              'NodeList': TestRecord[Id]['NodeList'],
                                              'HostGroupList': TestRecord[Id]['HostGroupList'],
                                              'HostNameList': TestRecord[Id]['HostNameList'],
                                              'Time': TestRecord[Id]['Time'],
                                              'Timeout': TestRecord[Id]['Timeout'],
                                              'Duration': TestRecord[Id]['Duration'],
                                              'EnvParameters': TestRecord[Id]['EnvParameters'],
                                              'LocalParameters': TestRecord[Id]['LocalParameters'],
                                              'FileParameters': TestRecord[Id]['FileParameters'],
                                              'CallBack': TestRecord[Id]['CallBack'],
                                              'Result': TestRecord[Id]['Result'],
                                              'Log': TestRecord[Id]['Log'],
                                              'Output': TestRecord[Id]['Output'],
                                              'Path': TestRecord[Id]['Path'],
                                              'Mandatory': Mandatory}

                            TestKey = False

                            if Mandatory:
                                for val in Mandatory:
                                    if EnvParam:
                                        if val in EnvParam:
                                            TestKey = True
                                        else:
                                            if LocalParam:
                                                for key in TestRecord[Id]['NodeList']:
                                                    if key in LocalParam:
                                                        if val in LocalParam[key]:
                                                            TestKey = True
                                    else:
                                        if LocalParam:
                                            for key in TestRecord[Id]['NodeList']:
                                                if key in LocalParam:
                                                    if val in LocalParam[key]:
                                                        TestKey = True
                                                
                                if not TestKey:
                                    if os.path.exists(PlaybookDir):
                                        shutil.rmtree (PlaybookDir)
                                    del TestRecord[Id]
                                    return {"StatusCode": 101,
                                            "StatusMessage": "MISSING MANDATORY PARAMETER: " + \
                                            " ".join(str(x) for x in Mandatory)}
                            
    
                            # Cannot use thread because ansible module uses
                            # signals which are only supported in main thread.
                            # So use multiprocess with shared object

                            p = Process(target = RunAnsible_Playbook,
                                        args = (callback, Id,  PlaybookDir + "/" + AnsibleInv,
                                                playbook_path + "/" + LCM + ".yml",
                                                NodeList, TestRecord, PlaybookDir,
                                                ArchiveFlag))
                            p.start()
                            ActiveProcess[Id] = p
                            return TestRecord[Id]['Result']
                    else:
                        return {"StatusCode": 101, "StatusMessage": "TEST ID ALREADY DEFINED"}

                else:
                    return {"StatusCode": 500, "StatusMessage": "REQUEST MUST INCLUDE: NODELIST"}
                
            else:
                return {"StatusCode": 500, "StatusMessage": "JSON OBJECT MUST INCLUDE: ID, PLAYBOOKNAME"}

        elif 'GET' in cherrypy.request.method:
            
            input_data = parse_query_string(cherrypy.request.query_string)
            
            print "***> in RestServer.GET"
            print "   Payload: ", input_data, input_data['Type']
            
            if 'Id' in input_data and 'Type' in input_data:
                if not ('GetResult' in input_data['Type'] or 'GetOutput' in input_data['Type'] or 'GetLog' in input_data['Type']):
                    return {"StatusCode": 500, "StatusMessage": "RESULTS TYPE UNDEFINED"}
                if input_data['Id'] in TestRecord:
                    
                    if 'GetResult' in input_data['Type']:
                        
                        print "Result:", TestRecord[input_data['Id']]['Result']

                        if 'StatusMessage' in TestRecord[input_data['Id']]['Result'] and getresults_block:

                            print "*** Request blocked", input_data['Id']

                            while ActiveProcess[input_data['Id']].is_alive():
                                time.sleep(5)

                            print "*** Request released ", input_data['Id']

                        print TestRecord[input_data['Id']]['Result']
                        if TestRecord[input_data['Id']]['Result']['StatusCode'] == 500:
                            out_obj = TestRecord[input_data['Id']]['Result']['Results']
                        else:
                            out_obj = {"StatusCode": 200,
                                   "StatusMessage": "FINISHED",
                                   "PlaybookName": TestRecord[input_data['Id']]["PlaybookName"],
                                   "Version": TestRecord[input_data['Id']]["Version"],
                                   "Duration": TestRecord[input_data['Id']]["Duration"],
                                   "Results": TestRecord[input_data['Id']]['Result']['Results']}
                        if not TestRecord[input_data['Id']]['Output']['Output'] == {}:
                            for key in out_obj["Results"]:
                                if key in TestRecord[input_data['Id']]['Output']['Output']:
                                    out_obj["Results"][key]["Output"] = TestRecord[input_data['Id']]['Output']['Output'][key]

                        return out_obj

                    elif 'GetOutput' in input_data['Type']:

                        if TestRecord[input_data['Id']]['Output'] == {} and \
                               getresults_block:

                            print "*** Request blocked", input_data['Id']
                            
                            while TestRecord[input_data['Id']]['Output'] == {} \
                                      or 'StatusMessage' in TestRecord[input_data['Id']]['Result']:
                                time.sleep(5)

                            print "*** Request released ", input_data['Id']
                        
                        print "Output:", TestRecord[input_data['Id']]['Output']
                        return {"Output": TestRecord[input_data['Id']]['Output']['Output']}
                    else:
                        # GetLog

                        if TestRecord[input_data['Id']]['Log'] == '' and \
                               getresults_block:

                            print "*** Request blocked", input_data['Id']
                            
                            while TestRecord[input_data['Id']]['Log'] == '' \
                                      or 'StatusMessage' in TestRecord[input_data['Id']]['Result']:
                                time.sleep(5)

                            print "*** Request released ", input_data['Id']
                            
                        print "Log:", TestRecord[input_data['Id']]['Log']
                        return {"Log": TestRecord[input_data['Id']]['Log']}
                else:
                    return {"StatusCode": 500, "StatusMessage": "TEST ID UNDEFINED"}
            else:
                return {"StatusCode": 500, "StatusMessage": "MALFORMED REQUEST"}
        elif 'DELETE' in cherrypy.request.method:
            input_data = parse_query_string(cherrypy.request.query_string)
            
            print "***> in RestServer.DELETE"
            print "   Payload: ", input_data
            
            if input_data['Id'] in TestRecord:
                if not 'PENDING' in TestRecord[input_data['Id']]['Result']:
                    print "   Path:", TestRecord[input_data['Id']]['Path']
                    if os.path.exists(TestRecord[input_data['Id']]['Path']):
                        shutil.rmtree (TestRecord[input_data['Id']]['Path'])
                    TestRecord.pop (input_data['Id'])
                    if input_data['Id'] in ActiveProcess:
                        ActiveProcess.pop (input_data['Id'])

                    return {"StatusCode": 200, "StatusMessage": "PLAYBOOK EXECUTION RECORDS DELETED"}
                else:
                    return {"StatusCode": 200, "StatusMessage": "PENDING"}
            else:
                return {"StatusCode": 500, "StatusMessage": "TEST ID UNDEFINED"}


if __name__ == '__main__':

    # Read configuration

    config_file_path = "RestServer_config"

    if not os.path.exists(config_file_path):
        print '[INFO] The config file does not exist'
        sys.exit(0)

    ip = 'na'
    port = 'na'
    tls = False
    auth = False
    pub = 'na'
    id = 'na'
    priv = 'na'
    psswd = 'na'
    timeout_seconds = 'na'
    ansible_path = 'na'
    ansible_inv = 'na'
    ansible_temp = 'na'    
    host = 'na'
    user = 'na'
    passwd = 'na'
    db = 'na'
    getresults_block = False
    from_files = False
    
    file = open(config_file_path, 'r')
    for line in file.readlines():
        if '#' not in line:
            if 'ip:' in line:
                ip = line.split(':')[1].strip()
            elif 'port:' in line:
                port = line.split(':')[1].strip()
            elif 'tls:' in line:
                tls = 'YES' in line.split(':')[1].strip().upper()
            elif 'auth:' in line:
                auth = 'YES' in line.split(':')[1].strip().upper()
            if tls and 'priv:' in line:
                priv = line.split(':')[1].strip()
            if tls and 'pub:' in line:
                pub = line.split(':')[1].strip()
            if auth and 'id:' in line:
                id = line.split(':')[1].strip()
            if auth and 'psswd:' in line:
                psswd = line.split(':')[1].strip()
            if 'timeout_seconds' in line:
                timeout_seconds = int (line.split(':')[1].strip())
            if 'ansible_path' in line:
                ansible_path = line.split(':')[1].strip()
            if 'ansible_inv' in line:
                ansible_inv = line.split(':')[1].strip()
                if not os.path.exists(ansible_path + "/" + ansible_inv):
                    print '[INFO] The ansible_inv file does not exist'
                    sys.exit(0)
            if 'ansible_temp' in line:
                ansible_temp = line.split(':')[1].strip()
            if 'host' in line:
                host = line.split(':')[1].strip()
            if 'user' in line:
                user = line.split(':')[1].strip()
            if 'passwd' in line:
                passwd = line.split(':')[1].strip()
            if 'db' in line:
                db = line.split(':')[1].strip()
            if 'getresults_block' in line:
                getresults_block = 'YES' in line.split(':')[1].strip().upper()
            if 'from_files' in line:
                from_files = 'YES' in line.split(':')[1].strip().upper()
    file.close()

    # Initialization
    
    global_conf = {
        'global': {
            'server.socket_host': ip,
            'server.socket_port': int(port),
            'server.protocol_version': 'HTTP/1.1'
            }
        }

    if tls:
        # Use pythons built-in SSL
        cherrypy.server.ssl_module = 'builtin'

        # Point to certificate files

        if not os.path.exists(pub):
            print '[INFO] The public certificate does not exist'
            sys.exit(0)

        if not os.path.exists(priv):
            print '[INFO] The private key does not exist'
            sys.exit(0)            
        
        cherrypy.server.ssl_certificate = pub
        cherrypy.server.ssl_private_key = priv

    if auth:
        userpassdict = {id: psswd}
        checkpassword = cherrypy.lib.auth_basic.checkpassword_dict(userpassdict)

        app_conf = {'/':
                    {'tools.auth_basic.on': True,
                     'tools.auth_basic.realm': 'earth',
                     'tools.auth_basic.checkpassword': checkpassword,
                     }
                    }

        cherrypy.tree.mount(TestManager(), '/', app_conf)
    else:
        cherrypy.tree.mount(TestManager(), '/')
        
    cherrypy.config.update(global_conf)

    # Start server
    
    cherrypy.engine.start()
    cherrypy.engine.block()
