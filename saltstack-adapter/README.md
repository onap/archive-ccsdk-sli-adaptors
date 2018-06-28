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