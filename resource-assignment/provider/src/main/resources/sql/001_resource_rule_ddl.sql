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

CREATE TABLE resource_rule (
  resource_rule_id SERIAL PRIMARY KEY,
  resource_name VARCHAR(50) NOT NULL,
  end_point_position VARCHAR(50) NOT NULL,
  service_expression VARCHAR(2000) NOT NULL,
  equipment_level VARCHAR(50) NOT NULL,
  equipment_expression VARCHAR(2000) NOT NULL,
  allocation_expression VARCHAR(2000) NOT NULL,
  soft_limit_expression VARCHAR(2000) NOT NULL,
  hard_limit_expression VARCHAR(2000) NOT NULL
);
