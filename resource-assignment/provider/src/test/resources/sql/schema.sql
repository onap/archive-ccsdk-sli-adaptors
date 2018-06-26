---
-- ============LICENSE_START=======================================================
-- openECOMP : SDN-C
-- ================================================================================
-- Copyright (C) 2017 AT&T Intellectual Property. All rights
--                         reserved.
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

CREATE TABLE RESOURCE_LOCK (
  resource_lock_id SERIAL PRIMARY KEY,
  resource_name VARCHAR(50) NOT NULL UNIQUE,
  lock_holder VARCHAR(100) NOT NULL,
  lock_count SMALLINT NOT NULL,
  lock_time DATETIME NOT NULL,
  expiration_time DATETIME NOT NULL
);

CREATE TABLE RESOURCE (
    resource_id SERIAL PRIMARY KEY,
    asset_id VARCHAR(50) NOT NULL,
    resource_name VARCHAR(50) NOT NULL,
    resource_type VARCHAR(10) NOT NULL,
    lt_used BIGINT,
    ll_label VARCHAR(50),
    ll_reference_count SMALLINT,
    rr_used VARCHAR(4000)
);

ALTER TABLE RESOURCE ADD CONSTRAINT c1_resource CHECK (resource_type IN ('Limit', 'Label', 'Range'));
CREATE UNIQUE INDEX ak1_resource ON RESOURCE (asset_id, resource_name);

CREATE TABLE RESOURCE_LOAD (
    resource_load_id SERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL REFERENCES resource (resource_id),
    application_id VARCHAR(20) NOT NULL,
    resource_load_time DATETIME NOT NULL,
    resource_expiration_time DATETIME
);

CREATE INDEX i1_resource_load ON RESOURCE_LOAD (resource_id);
CREATE UNIQUE INDEX ak1_resource_load ON RESOURCE_LOAD (resource_id, application_id);

CREATE TABLE ALLOCATION_ITEM (
    allocation_item_id SERIAL PRIMARY KEY,
    resource_id BIGINT NOT NULL REFERENCES resource (resource_id),
    application_id VARCHAR(50) NOT NULL,
    resource_set_id VARCHAR(50) NOT NULL,
    resource_union_id VARCHAR(50) NOT NULL,
    resource_share_group_list VARCHAR(200),
    lt_used BIGINT,
    ll_label VARCHAR(50),
    rr_used VARCHAR(200),
    allocation_time DATETIME NOT NULL
);

CREATE INDEX i1_allocation_item ON allocation_item (resource_id);
CREATE UNIQUE INDEX ak1_allocation_item ON allocation_item (resource_id, resource_set_id);

CREATE TABLE RESOURCE_RULE (
  resource_rule_id SERIAL PRIMARY KEY,
  resource_name VARCHAR(50) NOT NULL,
  service_model VARCHAR(50) NOT NULL,
  end_point_position VARCHAR(50) NOT NULL,
  service_expression VARCHAR(2000) NOT NULL,
  equipment_level VARCHAR(50) NOT NULL,
  equipment_expression VARCHAR(2000) NOT NULL,
  allocation_expression VARCHAR(2000) NOT NULL,
  soft_limit_expression VARCHAR(2000) NOT NULL,
  hard_limit_expression VARCHAR(2000) NOT NULL
);

CREATE TABLE RESOURCE_THRESHOLD (
  resource_threshold_id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  resource_rule_id bigint(20) NOT NULL,
  threshold_expression varchar(2000) NOT NULL,
  threshold_message varchar(2000) NOT NULL,
  PRIMARY KEY (resource_threshold_id)
);

CREATE TABLE RANGE_RULE (
  range_rule_id SERIAL PRIMARY KEY,
  range_name VARCHAR(50) NOT NULL,
  service_model VARCHAR(50) NOT NULL,
  end_point_position VARCHAR(50) NOT NULL,
  equipment_level VARCHAR(50) NOT NULL,
  equipment_expression VARCHAR(2000) NOT NULL,
  ranges VARCHAR(100) NOT NULL
);

CREATE TABLE MAX_PORT_SPEED (
  max_port_speed_id SERIAL PRIMARY KEY,
  image_file_name VARCHAR(50) NOT NULL,
  end_point_position VARCHAR(50) NOT NULL,
  interface_name VARCHAR(100) NOT NULL,
  max_speed BIGINT NOT NULL,
  unit VARCHAR(10) NOT NULL
);

CREATE TABLE MAX_SERVER_SPEED (
  max_server_speed_id SERIAL PRIMARY KEY,
  server_model VARCHAR(50) NOT NULL,
  evc_count SMALLINT NOT NULL,
  max_speed BIGINT NOT NULL,
  unit VARCHAR(10) NOT NULL,
  description VARCHAR(500)
);

CREATE TABLE SERVICE_RESOURCE (
  service_resource_id SERIAL PRIMARY KEY,
  service_instance_id VARCHAR(80) NOT NULL,
  service_status VARCHAR(10) NOT NULL,
  service_change_number SMALLINT NOT NULL,
    resource_set_id VARCHAR(100) NOT NULL,
    resource_union_id VARCHAR(100) NOT NULL,
);

ALTER TABLE SERVICE_RESOURCE ADD CONSTRAINT C1_SERVICE_RESOURCE CHECK (service_status IN ('Active', 'Pending'));
CREATE INDEX i1_service_resource ON SERVICE_RESOURCE (service_instance_id);
CREATE UNIQUE INDEX ak1_service_resource ON SERVICE_RESOURCE (service_instance_id, service_change_number);

CREATE TABLE VPE_POOL (
  vpe_name VARCHAR(20) NOT NULL,
  ipv4_oam_address VARCHAR(20) NOT NULL,
  loopback0_ipv4_address VARCHAR(20) NOT NULL,
  provisioning_status VARCHAR(10) NOT NULL,
  aic_site_id VARCHAR(100) NOT NULL,
  availability_zone VARCHAR(100) NOT NULL,
  vlan_id_outer VARCHAR(20) NOT NULL,
  vendor VARCHAR(20) NOT NULL,
  physical_intf_name VARCHAR(40) NOT NULL,
  physical_intf_speed VARCHAR(20) NOT NULL,
  physical_intf_units VARCHAR(20) NOT NULL,
  vpe_uuid VARCHAR(80) DEFAULT NULL,
  vpe_id VARCHAR(80) DEFAULT NULL,
  image_filename VARCHAR(100) DEFAULT NULL,
  PRIMARY KEY (aic_site_id, vpe_name, availability_zone)
);

CREATE TABLE VPLSPE_POOL (
  vplspe_name varchar(20) NOT NULL,
  aic_site_id varchar(100) NOT NULL,
  availability_zone varchar(100) NOT NULL,
  physical_intf_name varchar(40) NOT NULL,
  physical_intf_speed varchar(20) NOT NULL,
  physical_intf_units varchar(20) NOT NULL,
  loopback0_ipv4_address varchar(20) NOT NULL,
  vlan_id_outer varchar(20) NOT NULL,
  vplspe_uuid varchar(80) DEFAULT NULL,
  image_filename varchar(100) DEFAULT NULL,
  provisioning_status varchar(10) DEFAULT NULL,
  vendor varchar(20) DEFAULT NULL,
  PRIMARY KEY (vplspe_name, aic_site_id, availability_zone, physical_intf_name)
);

CREATE TABLE VPE_LOCK (
  vpe_name varchar(20) NOT NULL,
  vpn_lock varchar(20) NOT NULL,
  PRIMARY KEY (vpe_name)
);

CREATE TABLE PARAMETERS (
  name varchar(100) PRIMARY KEY,
  value varchar(24) NOT NULL,
  category varchar(24) NOT NULL,
  memo varchar(128)
);

CREATE TABLE PSERVER (
  hostname varchar(255) NOT NULL,
  ptnii_equip_name varchar(255),
  number_of_cpus varchar(255),
  disk_in_gigabytes varchar(255),
  ram_in_megabytes varchar(255),
  equip_type varchar(255),
  equip_vendor varchar(255),
  equip_model varchar(255),
  fqdn varchar(255),
  pserver_selflink varchar(255),
  ipv4_oam_address varchar(15),
  serial_number varchar(255),
  pserver_id varchar(255),
  internet_topology varchar(40),
  aic_site_id varchar(100),
  in_maint varchar(5),
  pserver_name2 varchar(255),
  purpose varchar(255),
  PRIMARY KEY (hostname)
);
