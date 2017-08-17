/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All
 *                         rights
 *                         reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.sli.adaptors.ra.alloc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.onap.ccsdk.sli.adaptors.ra.comp.AllocationRule;
import org.onap.ccsdk.sli.adaptors.ra.comp.ServiceData;
import org.onap.ccsdk.sli.adaptors.ra.equip.data.EquipmentData;
import org.onap.ccsdk.sli.adaptors.ra.rule.dao.RangeRuleDao;
import org.onap.ccsdk.sli.adaptors.ra.rule.dao.ResourceRuleDao;
import org.onap.ccsdk.sli.adaptors.ra.rule.data.RangeRule;
import org.onap.ccsdk.sli.adaptors.ra.rule.data.ResourceRule;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationAction;
import org.onap.ccsdk.sli.adaptors.rm.data.AllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.LimitAllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.MultiResourceAllocationRequest;
import org.onap.ccsdk.sli.adaptors.rm.data.RangeAllocationRequest;
import org.onap.ccsdk.sli.adaptors.util.expr.ExpressionEvaluator;
import org.onap.ccsdk.sli.adaptors.util.str.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbAllocationRule implements AllocationRule {

    private static final Logger log = LoggerFactory.getLogger(DbAllocationRule.class);

    private ResourceRuleDao resourceRuleDao;
    private RangeRuleDao rangeRuleDao;

    @Override
    public AllocationRequest buildAllocationRequest(
            String resourceUnionId,
            String resourceSetId,
            String endPointPosition,
            ServiceData serviceData,
            EquipmentData equipmentData,
            boolean checkOnly,
            boolean change) {
        List<ResourceRule> resourceRuleList = resourceRuleDao.getResourceRules(serviceData.serviceModel,
                endPointPosition, equipmentData.equipmentLevel);
        List<RangeRule> rangeRuleList =
                rangeRuleDao.getRangeRules(serviceData.serviceModel, endPointPosition, equipmentData.equipmentLevel);

        List<AllocationRequest> arlist = new ArrayList<AllocationRequest>();

        for (ResourceRule rr : resourceRuleList) {
            if (serviceData.resourceName != null && !serviceData.resourceName.equals(rr.resourceName))
                continue;
            AllocationRequest ar1 = buildAllocationRequest(rr, resourceUnionId, resourceSetId, serviceData,
                    equipmentData, checkOnly, change);
            arlist.add(ar1);
        }
        for (RangeRule rr : rangeRuleList) {
            if (serviceData.resourceName != null && !serviceData.resourceName.equals(rr.rangeName))
                continue;
            AllocationRequest ar1 = buildAllocationRequest(rr, resourceUnionId, resourceSetId, serviceData,
                    equipmentData, checkOnly, change);
            arlist.add(ar1);
        }

        if (arlist.isEmpty())
            return null;

        if (arlist.size() == 1)
            return arlist.get(0);

        MultiResourceAllocationRequest ar = new MultiResourceAllocationRequest();
        ar.stopOnFirstFailure = false;
        ar.allocationRequestList = arlist;
        return ar;
    }

    private AllocationRequest buildAllocationRequest(
            ResourceRule resourceRule,
            String resourceUnionId,
            String resourceSetId,
            ServiceData serviceData,
            EquipmentData equipmentData,
            boolean checkOnly,
            boolean change) {
        StrUtil.info(log, resourceRule);

        LimitAllocationRequest ar = new LimitAllocationRequest();
        ar.resourceSetId = resourceSetId;
        ar.resourceUnionId = resourceUnionId;
        ar.resourceName = resourceRule.resourceName;
        if (serviceData.resourceShareGroup != null)
            ar.resourceShareGroupList = Collections.singleton(serviceData.resourceShareGroup);
        ar.assetId = equipmentData.equipmentId;
        ar.missingResourceAction = AllocationAction.Succeed_Allocate;
        ar.expiredResourceAction = AllocationAction.Succeed_Allocate;
        ar.replace = true;
        ar.strict = false;
        ar.checkLimit = ExpressionEvaluator.evalLong(
                change ? resourceRule.hardLimitExpression : resourceRule.softLimitExpression, equipmentData.data);;
        ar.checkCount = ExpressionEvaluator.evalLong(resourceRule.allocationExpression, serviceData.data);
        ar.allocateCount = checkOnly ? 0 : ar.checkCount;
        return ar;
    }

    private AllocationRequest buildAllocationRequest(
            RangeRule rangeRule,
            String resourceUnionId,
            String resourceSetId,
            ServiceData serviceData,
            EquipmentData equipmentData,
            boolean checkOnly,
            boolean change) {
        StrUtil.info(log, rangeRule);

        RangeAllocationRequest ar = new RangeAllocationRequest();
        ar.resourceSetId = resourceSetId;
        ar.resourceUnionId = resourceUnionId;
        ar.resourceName = rangeRule.rangeName;
        ar.assetId = equipmentData.equipmentId;
        ar.missingResourceAction = AllocationAction.Succeed_Allocate;
        ar.expiredResourceAction = AllocationAction.Succeed_Allocate;
        ar.replace = true;
        ar.check = true;
        ar.allocate = !checkOnly;
        ar.checkMin = rangeRule.minValue;
        ar.checkMax = rangeRule.maxValue;
        return ar;
    }

    public void setResourceRuleDao(ResourceRuleDao resourceRuleDao) {
        this.resourceRuleDao = resourceRuleDao;
    }

    public void setRangeRuleDao(RangeRuleDao rangeRuleDao) {
        this.rangeRuleDao = rangeRuleDao;
    }
}
