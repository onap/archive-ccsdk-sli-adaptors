This source repository contains the code for the CCSDK plugins.

To compile this code:

1. Make sure your local Maven settings file ($HOME/.m2/settings.xml) contains references to the ONAP repositories and OpenDaylight repositories.  See example-settings.xml for an example.

2. To compile, run "mvn clean install".


***SaltStack Adaptor:*** CCSDK SLI ADAPTORS to support SaltStack server:

***Connection from CCSDK SLI ADAPTOR Adaptor to SaltStack server:***

Create an Adaptor to communicate with the SaltStack server:
1) SaltStack server doesn’t expose any REST API unlike Chef.
2) SSH based communication with the SaltStack server, one command at a time (preferred). This will mean that SaltStack server should have it’s SSH enabled.
3) Create a REST-wrap around SaltStack server like is done for Ansible server.

***SSH based communication:***
1) Adaptor can execute commands on the Salt Master and bring back the result and put to the context memory for DG based analysis. (https://docs.saltstack.com/en/latest/ref/modules/all/index.html#all-salt-modules).
2) This can be useful for several reasons, for instance it might be useful to know the interfaces in the minions before executing certain network config based commands. This can simple be done by running, 'salt '*' network.interfaces' on server.
3) SaltStack Server, Output module support: The json-out outputter can be used to display the return data in JSON format. So the DG can put this onto context memory for execution. https://docs.saltstack.com/en/latest/ref/output/all/index.html#all-salt-output
4) Since the command execution on server might take time, a thread can be spawn to make a single SSH command execution in a SYNC manner. The thread executes the command and brings back the result and puts to the context memory for DG’s access.
5) For some specific executions operations like configure and upgrade, each configuration execution on the server will be handled by 2 or more SSH command execution. (1 for sending configuration and another for verifying the result). This will give the DGs and Saltstack adaptor with more control on the SaltStack server.

***SaltState (SLS) file for execution on the SaltStack server:***
 The desired SLS file can be executed by one of the following three ways:
1) The SLS file for VNF configuration can be assumed to be already on the server, similar to Ansible. In this case, no addition requirements are necessary. We would already know the name of SLS file to execute so the configuration is performed on the VNF. 
2) SLS file creation using DG: Create a DG to parse the configuration and create an SLS file using adaptors such as FileRecorder. Then this SLS file can be passed to the adaptor, so the adaptor can send the configuration to server. The adaptor can also send a SLS file to the Saltstack server and then run the command to execute it. 
3) Third option is for the configuration SLS file that is to be sent to the VNF after instantiation is attached at the design time. This SLS formula- SaltStack file can be picked up and stored in the DB, as part of UEB listener. This can then be sent to adaptor using DGs.

***Requirements and benefits of the chosen SSH method:***
1) The SaltStack server should have it’s SSH enabled.
2) Such execution method will give the DGs and adaptor with more refined control on the SaltStack server.
==================================================================================================================


***Defining Saltstack server properties:*** Can be done with 2 different methods. 
1) Saltstack server details are found in the property file named saltstack-adapter.properties. Param has to be given with following types. 
    "org.onap.appc.adapter.saltstack.clientType"; -> Supported types are (BASIC || SSH_CERT || BOTH).
    "org.onap.appc.adapter.saltstack.host"; ->  Saltstack server's host name IP address.
    "org.onap.appc.adapter.saltstack.port"; ->  Saltstack server's port to make SSH connection to.
    "org.onap.appc.adapter.saltstack.userName"; ->  Saltstack server's SSH UserName.
    "org.onap.appc.adapter.saltstack.userPasswd"; ->  Saltstack server's SSH Password.
    "org.onap.appc.adapter.saltstack.sshKey"; ->  Saltstack server's SSH KEY file location.
2) All the server related details can also be passed as param to the adaptor from the Directed Graphs. Param has to be given with following types. 
    "HostName";  ->  Saltstack server's host name IP address.
    "Port"; ->  Saltstack server's port to make SSH connection to.
    "Password"; ->  Saltstack server's SSH UserName.
    "User"; ->  Saltstack server's SSH Password.
  Note: SSH_CERT based Auth is not supported in this method.
  
***Using Saltstack Adaptor Commands and params to pass in:*** reqExecCommand:
Method to execute a single command on SaltState server and execute a SLS file located on the server. The command entered should request the output in JSON format, this can be done by appending json-out outputter as specified in https://docs.saltstack.com/en/latest/ref/output/all/salt.output.json_out.html#module-salt.output.json_out and https://docs.saltstack.com/en/2017.7/ref/cli/salt-call.html 
The response from Saltstack comes in json format and it is automatically put to context for DGs access, with a certain request-ID as prefix.
If Id is not passed as part of input param, then a random Id will be generated and put to properties in "org.onap.appc.adapter.saltstack.Id" field. All the output message from the execution will be appended with reqId. 
1) Execute a single command on SaltState server : Example command will look like: 
1.1) Command to test if all VNFC are running: "salt '*' test.ping --out=json --static"
1.2) To check Network interfaces on your minions: "salt '*' network.interfaces --out=json --static"
1.3) Restart Minion service after upgrade process: "salt minion1 service.restart salt-minion --out=json --static"
Note: If using --out=json, you will probably want --static as well. Without the static option, you will get a separate JSON string per minion which makes JSON output invalid as a whole. This is due to using an iterative outputter. So if you want to feed it to a JSON parser, use --static as well.

This "reqExecCommand" method gives the Operator/Directed Graphs to execute commands in a fine-tuned manner, which also means the operator/DG-creator should know what to expect as output as a result of command execution (for both success/failure case). 
By this way using DGs, the operator can check for success/failure of the executed comment. 
If the output is not in JSON format, then the adaptor still tries to convert it into properties, in addition, params that will hold the command execution result for DG access are (note: this is just to check if the command was executed successfully on the server, this doesn't check the status of the command on saltstack server): 
Result code at: org.onap.appc.adapter.saltstack.result.code (On success: This will be always be 250, means command execution was success but the result of the execution is unknown and is to be checked from ctx using DGs)
Message at: org.onap.appc.adapter.saltstack.message
Both user inputted/auto generated req Id at:  org.onap.appc.adapter.saltstack.Id
To check the status of the command configuration on saltstack server: the user should exactly know what to look for in the context 
so the user can identify if the configuration execution on the saltstack server succeded or not. 
here for instance, in 1.1) the user should check if $reqId.<minion-name> is set to true in the context memory using DGs. 

2) Execute a SLS file located on the server : Example command will look like:
Knowing the saltstack server has vim.sls file located at "/srv/salt" directory then user can execute the following commands:
1.1) Command to run the vim.sls file on saltstack server: "salt '*' state.apply vim --out=json --static"
1.2) Command to run the nettools.sls file on saltstack server: "salt '*' state.apply nettools --out=json --static"
Important thing to note: If the reqExecCommand is used to execute sls file then along with following, 
    "HostName";  ->  Saltstack server's host name IP address.
    "Port"; ->  Saltstack server's port to make SSH connection to.
    "Password"; ->  Saltstack server's SSH UserName.
    "User"; ->  Saltstack server's SSH Password.
the param should contain,
    "slsExec"; ->  this variable should be set to true.

In this case, params that will hold the command execution result for DG access are
Result code at: org.onap.appc.adapter.saltstack.result.code (On success: This will be 200, this means the command was executed successfully and also configuration change made using the SLS file was also successful) 
Message at: org.onap.appc.adapter.saltstack.message
Both user inputted/auto generated req Id at:  org.onap.appc.adapter.saltstack.Id
The result code here will be the execution of configuration SLS file on the server. 
NOTE: It would be better to use reqExecSLS, where you will only have to specify SLS file name on server.
***Using Saltstack Adaptor Commands and params to pass in:*** reqExecSLS:
Method to execute a single sls on SaltState server and execute a SLS file located on the server. The command entered should request the output in JSON format, this can be done by appending json-out outputter as specified in https://docs.saltstack.com/en/latest/ref/output/all/salt.output.json_out.html#module-salt.output.json_out and https://docs.saltstack.com/en/2017.7/ref/cli/salt-call.html 
The response from Saltstack comes in json format and it is automatically put to context for DGs access, with a certain request-ID as prefix.
If Id is not passed as part of input param, then a random Id will be generated and put to properties in "org.onap.appc.adapter.saltstack.Id" field. All the output message from the execution will be appended with reqId. 
1) Execute a single command on SaltState server : Example command will look like: 
