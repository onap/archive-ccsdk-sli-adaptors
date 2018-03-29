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

import pymysql, sys
from os import listdir
from os.path import isfile, join

class mySql():
    
    def __init__(self, myhost, myuser, mypasswd, mydb):
        self.con = True
        self.error = ''
        self.db = None
        try:
            self.db = pymysql.connect(host=myhost,
                                      user=myuser,
                                      passwd=mypasswd,
                                      db=mydb)
            self.cur = self.db.cursor()
        except Exception as e:
            self.error = e[1]
            self.con = False

    def Query (self, myquery, val = None):
        results = None
        try:
            if val:
                self.cur.execute(myquery, val)
            else:
                self.cur.execute(myquery)
            self.db.commit()
            results = self.cur.fetchall()
        except Exception, e:
            results = repr(e)
        return results
    
    def Close (self):
        if self.db:
            self.db.close()

def loadPlaybook (sqlintf, value, version, ext = '.yml'):

    errorCode = 0
    diag = ''
    
    # Test if primary key already defined
    query = "SELECT name FROM playbook WHERE name='" + value +"'"
    results = sqlintf.Query (query)
    if len(results) > 0:
        pass
    else:
        query = "INSERT INTO playbook (name) VALUES ('" + value + "')"
        results = sqlintf.Query (query)
        if len(results) > 0:
            errorCode = 1
            diag = results

    # Load playbook
    file = open(playbook_path + value + ext, 'r')
    load_file = file.read()
        
    if not errorCode:
        sql = "UPDATE playbook SET value=%s, version=%s, type=%s WHERE name=%s"

        results = sqlintf.Query(sql, (load_file, version, ext, value))

        if len (results) > 0:
            # Error loading playbook
            errorCode = 1
            diag = results

    return errorCode, diag

def loadCredentials (sqlintf, hostgroup, hostname, cred):
    errorCode = 0
    diag = ''
    
    # Load credentials

    query = "SELECT hostname,hostgroup FROM inventory WHERE hostname='" + hostname +"'"
    results = sqlintf.Query (query)

    if hostname in str (results):

        results_hostgroups = results[0][1]
        
        if hostgroup in results_hostgroups.split(','):
            query = "UPDATE inventory SET hostname='" + hostname + "',credentials='" +\
                    cred +\
                    "' WHERE hostname='" + hostname + "'"
        else:

            results_hostgroups = results_hostgroups + ',' + hostgroup
            
            query = "UPDATE inventory SET hostname='" + hostname + "',credentials='" +\
                    cred + "',hostgroup='" + results_hostgroups + \
                    "' WHERE hostname='" + hostname + "'"
                        
        results = sqlintf.Query (query)
        
    else:
        
        query = "INSERT INTO inventory (hostgroup, hostname, credentials) VALUES ('" + \
                hostgroup + "','" + hostname + "','" + cred + "')"
        results = sqlintf.Query (query)

    if len (results) > 0:
        # Error loading playbook
        errorCode = 1
        diag = results

    return errorCode, diag
    

def readPlaybook (sqlintf, value, version=None):

    errorCode = 0
    diag = ''

    print "***> in AnsibleSql.readPlaybook"
    
    if not version:
        query = "SELECT MAX(version) FROM playbook WHERE name like'" + value + "%'"
        print "   Query:", query
        results = sqlintf.Query (query)
        version = results[0][0]

    print "   Provided playbook name:", value 
    print "   Used version:", version

    results = []
    if version:
        query = "SELECT value,type FROM playbook WHERE name='" + value + "@" + version + "'"
        results = sqlintf.Query (query)

        print "Query:", query
        print "Results:", results
    
    if len(results) == 0:
        errorCode = 1
    else:
        if len(results[0]) == 0:
            errorCode = 1
            diag = results[0]
        else:
            diag = results[0]

    return value, version, errorCode, diag

def readCredentials (sqlintf, tag):
    errorCode = []
    diag = []

    print "***> in AnsibleSql.readCredential"
    
    # Load credentials

    for rec in tag:

        # Try hostgroup
        query = "SELECT hostgroup, hostname, credentials FROM inventory WHERE hostgroup LIKE '%" + \
                rec +"%'"
        query_results = sqlintf.Query (query)

        results = ()
        for q in query_results:
            if rec in q[0].split(','):
                l = list(q)
                l[0] = rec
                q = tuple(l)
                results = (q,) + results

        if len(results) == 0:
            # Try hostname
            query = "SELECT hostgroup, hostname, credentials FROM inventory WHERE hostname='" + \
                    rec +"'"
            results = sqlintf.Query (query)

        print "   Query:", query
        print "   Results:", len(results), results

        if len(results) == 0:
            errorCode = 1
            hostgroup = rec
            hostname = rec
            credentials = 'ansible_connection=ssh ansible_ssh_user=na ansible_ssh_private_key_file=na\n'
            diag.append([hostgroup, hostname, credentials])
        else:
            errorCode = 0
            for i in range(len (results)):
                for h in results[i][0].split(','):
                    hostgroup = h
                    hostname = results[i][1]
                    credentials = results[i][2]
                    diag.append([hostgroup, hostname, credentials])

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
            errorCode, diag = loadPlaybook (sqlintf, name, version, '.yml')
            if errorCode:
                print "  Results: Failed - ", diag
            else:
                print "  Results: Success"

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
            errorCode, diag = loadCredentials (sqlintf, hostgroup, hostname, cred)
            if errorCode:
                print "  Results: Failed - ", diag
            else:
                print "  Results: Success"
                
    print "\nReading playbook"
    
    # Read playbook

    if not sqlintf.con:
        print "Cannot connect to MySql:", sqlintf.error
        sys.exit()
        
    name = "ansible_sleep"
    print "Reading playbook:", name
    value, version, errorCode, diag = readPlaybook (sqlintf, name)
    if errorCode:
        print "Results: Failed - ", diag
    else:
        print "Results: Success"
        print value
        print version
        print diag

    print "\nReading inventory"

    # Read inventory

    tag = ["your_inventory_test_group_name"]
    print "Reading inventory tag:", tag
    errorCode, diag = readCredentials (sqlintf, tag)
    if errorCode:
        print "Results: Failed - ", diag
    else:
        print "Results: Success"
        print diag
                
    sqlintf.Close()

