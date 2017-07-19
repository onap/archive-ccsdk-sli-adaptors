---
-- ============LICENSE_START=======================================================
-- openECOMP : SDN-C
-- ================================================================================
-- Copyright (C) 2017 ONAP Intellectual Property. All rights
-- 						reserved.
-- ================================================================================
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
-- 
--      http://www.apache.org/licenses/LICENSE-2.0
-- 
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
-- ============LICENSE_END=========================================================
---

CREATE TABLE sdnctl.VNF (
vnf_id varchar(80) NOT NULL,
vnf_name varchar(80) NOT NULL,
vnf_type varchar(10) NOT NULL,
orchestration_status varchar(20),
heat_stack_id varchar(80),
mso_catalog_key varchar(80),
availability_zone varchar(80),
aic_site_id varchar(80),
oam_ipv4_address varchar(20),
CONSTRAINT P_VNF PRIMARY KEY(vnf_id));

CREATE TABLE sdnctl.VNF_NETWORK_CONNECTION (
connection_uuid varchar(80) NOT NULL,
vnf_id varchar(80) NOT NULL,
connection_id varchar(80),
connection_type varchar(20),
neutron_network_id varchar(80),
neutron_network_name varchar(80),
orchestration_status varchar(20),
switch_id varchar(40),
heat_stack_id varchar(80),
mso_catalog_key varchar(80),
provider_network varchar(80),
port_group_id varchar(80),
port_group_name varchar(80),
CONSTRAINT P_NETWORK_CONNECTION PRIMARY KEY(connection_uuid));

CREATE TABLE sdnctl.VNF_NETWORK_CONNECTION_VLAN (
vlan_uuid varchar(80) NOT NULL,
vlan_id numeric(4) NOT NULL,
vlan_type varchar(5) NOT NULL,
connection_uuid varchar(80),
CONSTRAINT P_VNF_NETWORK_CONNECTION_VLAN PRIMARY KEY(vlan_uuid));

CREATE TABLE sdnctl.VLAN_ID_POOL (
vlan_id numeric(4) NOT NULL,
universe varchar(40) NOT NULL,
status varchar(40) NOT NULL,
vlan_uuid varchar(80),
CONSTRAINT P_VLAN_ID_POOL PRIMARY KEY(vlan_id,universe));

CREATE TABLE sdnctl.IPV4_ADDRESS_POOL (
ipv4_addr varchar(20) NOT NULL,
universe varchar(40) NOT NULL,
status varchar(40) NOT NULL,
CONSTRAINT P_IPV4_ADDRESS_POOL PRIMARY KEY(ipv4_addr, universe));


