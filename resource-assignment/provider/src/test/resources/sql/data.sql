---
-- ============LICENSE_START=======================================================
-- ONAP : CCSDK
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

INSERT INTO RESOURCE_RULE (
    resource_name, service_model, end_point_position, service_expression, equipment_level, equipment_expression,
    allocation_expression, soft_limit_expression, hard_limit_expression)
VALUES (
    'Bandwidth', 'L3SDN', 'IPAG-TOA', 'true', 'Port', 'true',
    'service-speed-kbps', '0.5 * max-port-speed', '0.9 * max-port-speed');

INSERT INTO RESOURCE_RULE (
    resource_name, service_model, end_point_position, service_expression, equipment_level, equipment_expression,
    allocation_expression, soft_limit_expression, hard_limit_expression)
VALUES (
    'Bandwidth', 'L3SDN', 'VCE-Cust', 'true', 'Server', 'true',
    'service-speed-kbps', '0.6 * max-server-speed * number-primary-servers', 'max-server-speed * number-primary-servers');

INSERT INTO RESOURCE_RULE (
  resource_name, service_model, end_point_position, service_expression, equipment_level,
    equipment_expression, allocation_expression, soft_limit_expression, hard_limit_expression)
VALUES (
  'Connection', 'L3SDN', 'VCE-Cust', 'true', 'Server',
    'true', '1', '40', '40');

INSERT INTO RESOURCE_RULE (
     resource_name, service_model, end_point_position, service_expression, equipment_level, equipment_expression,
    allocation_expression, soft_limit_expression, hard_limit_expression)
VALUES (
     'Bandwidth', 'L3SDN', 'VPE-Cust', 'true', 'Port', 'true',
    'service-speed-kbps', '0.5 * max-port-speed', '0.9 * max-port-speed');

INSERT INTO RESOURCE_RULE (
  resource_name, service_model, end_point_position, service_expression,
    equipment_level, equipment_expression, allocation_expression, soft_limit_expression, hard_limit_expression)
VALUES (
  'Bandwidth', 'L3AVPN-EVC', 'VPE-Cust', 'true', 'Port', 'true', 'service-speed-kbps', '8000000', '8000000');

INSERT INTO RESOURCE_RULE (
  resource_name, service_model, end_point_position, service_expression,
    equipment_level, equipment_expression, allocation_expression, soft_limit_expression, hard_limit_expression)
VALUES (
  'Connection', 'L3AVPN-EVC', 'VPE-Cust', 'true', 'Port', 'true', '1', '200', '200');

INSERT INTO RESOURCE_RULE (
  resource_name, service_model, end_point_position, service_expression,
    equipment_level, equipment_expression, allocation_expression, soft_limit_expression, hard_limit_expression)
VALUES (
  'Bandwidth', 'L3AVPN-PORT', 'VPE-Cust', 'true', 'Port', 'true', 'service-speed-kbps', '8000000', '8000000');

insert into RESOURCE_THRESHOLD (
  resource_rule_id, threshold_expression, threshold_message)
values (
  (select resource_rule_id from RESOURCE_RULE where resource_name = 'Bandwidth' and equipment_level = 'Server'),
    '0.5 * max-server-speed * number-primary-servers',
    'The provisioned access bandwidth is at or exceeds 50% of the total server capacity.');

insert into RESOURCE_THRESHOLD (
  resource_rule_id, threshold_expression, threshold_message)
values (
  (select resource_rule_id from RESOURCE_RULE where resource_name = 'Bandwidth' and equipment_level = 'Server'),
    '0.7 * max-server-speed * number-primary-servers',
    'The provisioned access bandwidth is at or exceeds 70% of the total server capacity.');

INSERT INTO RANGE_RULE (
  range_name, service_model, end_point_position, equipment_level, min_value, max_value)
VALUES (
  'subinterface-id', 'L3AVPN-EVC', 'VPE-Cust', 'Port', 100, 3999);

INSERT INTO RANGE_RULE (
  range_name, service_model, end_point_position, equipment_level, min_value, max_value)
VALUES (
  'vlan-id-inner', 'L3AVPN-EVC', 'VPE-Cust', 'Port', 2, 4091);

INSERT INTO RANGE_RULE (
  range_name, service_model, end_point_position, equipment_level, min_value, max_value)
VALUES (
  'bundle-id', 'L3AVPN-PORT', 'VPE-Cust', 'Port', 1, 99999);

INSERT INTO MAX_PORT_SPEED (
     image_file_name, end_point_position, interface_name, max_speed, unit)
VALUES (
     'JUNIPER_VPE_IMAGE_FILENAME', 'VPE-Cust', 'ae0', 5000, 'Mpbs');

INSERT INTO MAX_SERVER_SPEED (server_model, evc_count, max_speed, unit, description)
VALUES ('ALL', 5, 1600, 'Mbps', 'Max speed, when there are <=5 EVC on server');

INSERT INTO MAX_SERVER_SPEED (server_model, evc_count, max_speed, unit, description)
VALUES ('ALL', 10, 1400, 'Mbps', 'Max speed, when there are 6 to 10 (including 10) EVC on server');

INSERT INTO MAX_SERVER_SPEED (server_model, evc_count, max_speed, unit, description)
VALUES ('ALL', 15, 1000, 'Mbps', 'Max speed, when there are 11 to 15 (including 15) EVC on server');

INSERT INTO MAX_SERVER_SPEED (server_model, evc_count, max_speed, unit, description)
VALUES ('ALL', 20, 700, 'Mbps', 'Max speed, when there are 16 to 20 (including 20) EVC on server');

INSERT INTO MAX_SERVER_SPEED (server_model, evc_count, max_speed, unit, description)
VALUES ('ALL', 10000, 500, 'Mbps', 'Max speed, when there are 21 or more EVC on server');

INSERT INTO PARAMETERS (name, value, category, memo)
VALUES ('homing.pserver.sparing.ratio', '1:1', 'homing',
  'Ratio of primary to backup servers within any of the AIC sites. Used in RA to calculate the max allowed bw in an AIC site.');

