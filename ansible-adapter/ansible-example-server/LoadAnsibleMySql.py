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

#!/usr/bin/python
import pymysql
from os import listdir
from os.path import isfile, join

class mySql():
    
    def __init__(self, myhost, myuser, mypasswd, mydb):
        self.db = pymysql.connect(host=myhost,
                                  user=myuser,
                                  passwd=mypasswd,
                                  db=mydb)
        self.cur = self.db.cursor()

    def Query (self, myquery, val = None):
        results = None
        error = None
        try:
            if val:
                self.cur.execute(myquery, val)
            else:
                self.cur.execute(myquery)
            self.db.commit()
            results = self.cur.fetchall()
        except Exception, e:
            error = str (e)
        return results, error

    def Close (self):
        self.db.close()

def loadPlaybook (value, version, ext = '.yml'):

    errorCode = 0
    diag = ''
    
    # Test if primary key already defined
    query = "SELECT name FROM playbook WHERE name='" + value +"'"
    results, error = sqlintf.Query (query)
    if results:
        # print "Primary key already defined: Updating playbook"
        pass
    else:
        # print "Primary key not defined: Insert new playbook"
        query = "INSERT INTO playbook (name) VALUES ('" + value + "')"
        results, error = sqlintf.Query (query)
        if error:
            errorCode = 1
            diag = error

    # Load playbook
    file = open(playbook_path + value + ext, 'r')
    load_file = file.read()
        
    # Load playbook

    if not errorCode:
        sql = "UPDATE playbook SET value=%s, version=%s, type=%s WHERE name=%s"

        results, error = sqlintf.Query(sql, (load_file, version, ext, value))

        if error:
            # Error loading playbook
            errorCode = 1
            diag = error
            
    return errorCode, diag

def loadCredentials (hostgroup, hostname, cred):
    errorCode = 0
    diag = ''
    
    # Load credentials

    query = "SELECT hostname,hostgroup FROM inventory WHERE hostname='" + hostname +"'"
    results = sqlintf.Query (query)

    print '==>', results
    
    if hostname in str(results):

        results_hostgroups = results[0][0][1]

        # print "Record already defined: Updating inventory"
        if hostgroup in results_hostgroups.split(','):
            query = "UPDATE inventory SET hostname='" + hostname + "',credentials='" +\
                    cred +\
                    "' WHERE hostname='" + hostname + "'"
        else:
            
            results_hostgroups = results_hostgroups + ',' + hostgroup

            query = "UPDATE inventory SET hostname='" + hostname + "',credentials='" +\
                    cred + "',hostgroup='" + results_hostgroups + \
                    "' WHERE hostname='" + hostname + "'"

        results, error = sqlintf.Query (query)
        
    else:
        
        query = "INSERT INTO inventory (hostgroup, hostname, credentials) VALUES ('" + \
                hostgroup + "','" + hostname + "','" + cred + "')"
        results, error = sqlintf.Query (query)

    if error:
        # Error loading credentials
        errorCode = 1
        diag = results

    return errorCode, diag
    

if __name__ == '__main__':

    ################################################################
    # Change below
    ################################################################
    host="localhost"                    # your host, usually localhost
    user="mysql_user_id"                # your username
    passwd="password_4_mysql_user_id"   # your password
    db="ansible"                        # name of the data base

    playbook_path = "/home/ubuntu/RestServerOpenSource/"
    inventory = "/home/ubuntu/RestServerOpenSource/Ansible_inventory"
    ################################################################
    
    onlyfiles = [f for f in listdir(playbook_path)
                 if isfile(join(playbook_path, f))]

    sqlintf = mySql (host, user, passwd, db)

    # Load playbooks
    print "Loading playbooks"
    for file in onlyfiles:
        if "yml" in file:
            name = file.split (".yml")[0]
            print "  Loading:", name
            version = name.split("@")[1]
            errorCode, diag = loadPlaybook (name, version)
            if errorCode:
                print "  Results: Failed - ", diag
            else:
                print "  Results: Success"
        if "tar.gz" in file:
            name = file.split (".tar.gz")[0]
            print "  Loading:", name
            version = name.split("@")[1]
            errorCode, diag = loadPlaybook (name, version, ".tar.gz")

    print "\nLoading inventory"
    
    # Load inventory
    hostgroup = None
    inv = {}
    file = open(inventory, 'r')

    for line in file:

        if '[' in line and ']' in line:
            hostgroup = line.strip().replace('[','').replace(']','')
            inv[hostgroup] = {}
        elif hostgroup and len(line.strip())>0:
            host = line.strip().split(" ")[0]
            credentials = line.replace(host,"")
            inv[hostgroup][host] = credentials
                               
    file.close()

    for hostgroup in inv:
        print "  Loading:", hostgroup
        hostfqdn = ''
        cred = ''
        for hostname in inv[hostgroup]:
            cred = inv[hostgroup][hostname]
            errorCode, diag = loadCredentials (hostgroup, hostname, cred)
            if errorCode:
                print "  Results: Failed - ", diag
            else:
                print "  Results: Success"
                
    sqlintf.Close()
