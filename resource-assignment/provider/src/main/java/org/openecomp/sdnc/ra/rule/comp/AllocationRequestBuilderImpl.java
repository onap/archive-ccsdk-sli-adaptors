/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 ONAP Intellectual Property. All rights
 * reserved.
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

package org.openecomp.sdnc.ra.rule.comp;

import java.util.ArrayList;
import java.util.List;

import org.openecomp.sdnc.ra.comp.ServiceData;
import org.openecomp.sdnc.ra.equip.data.EquipmentData;
import org.openecomp.sdnc.ra.rule.dao.RangeRuleDao;
import org.openecomp.sdnc.ra.rule.dao.ResourceRuleDao;
import org.openecomp.sdnc.ra.rule.data.RangeRule;
import org.openecomp.sdnc.ra.rule.data.ResourceRule;
import org.openecomp.sdnc.ra.rule.data.ResourceThreshold;
import org.openecomp.sdnc.ra.rule.data.ThresholdStatus;
import org.openecomp.sdnc.rm.data.AllocationAction;
import org.openecomp.sdnc.rm.data.AllocationRequest;
import org.openecomp.sdnc.rm.data.LimitAllocationOutcome;
import org.openecomp.sdnc.rm.data.LimitAllocationRequest;
import org.openecomp.sdnc.rm.data.MultiResourceAllocationRequest;
import org.openecomp.sdnc.rm.data.RangeAllocationRequest;
import org.openecomp.sdnc.util.expr.ExpressionEvaluator;
import org.openecomp.sdnc.util.str.StrUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AllocationRequestBuilderImpl implements AllocationRequestBuilder {

    private static final Logger log = LoggerFactory.getLogger(AllocationRequestBuilderImpl.class);

    private ResourceRuleDao resourceRuleDao;
    private RangeRuleDao rangeRuleDao;

    @Override
    public AllocationRequest buildAllocationRequest(
            ServiceData serviceData,
            EquipmentData equipmentData,
            boolean checkOnly,
            boolean change) {
        List<ResourceRule> resourceRuleList = resourceRuleDao.getResourceRules(serviceData.serviceModel,
                serviceData.endPointPosition, equipmentData.equipmentLevel);
        List<RangeRule> rangeRuleList = rangeRuleDao.getRangeRules(serviceData.serviceModel,
                serviceData.endPointPosition, equipmentData.equipmentLevel);
        if (resourceRuleList.isEmpty() && rangeRuleList.isEmpty())
            return null;
        if (resourceRuleList.size() == 1 && rangeRuleList.isEmpty())
            return buildAllocationRequest(resourceRuleList.get(0), serviceData, equipmentData, checkOnly, change);

        if (resourceRuleList.isEmpty() && rangeRuleList.size() == 1)
            return buildAllocationRequest(rangeRuleList.get(0), serviceData, equipmentData, checkOnly, change);

        MultiResourceAllocationRequest ar = new MultiResourceAllocationRequest();
        ar.stopOnFirstFailure = false;
        ar.allocationRequestList = new ArrayList<AllocationRequest>();
        for (ResourceRule rr : resourceRuleList) {
            AllocationRequest ar1 = buildAllocationRequest(rr, serviceData, equipmentData, checkOnly, change);
            ar.allocationRequestList.add(ar1);
        }
        for (RangeRule rr : rangeRuleList) {
            AllocationRequest ar1 = buildAllocationRequest(rr, serviceData, equipmentData, checkOnly, change);
            ar.allocationRequestList.add(ar1);
        }
        return ar;
    }

    private AllocationRequest buildAllocationRequest(
            ResourceRule resourceRule,
            ServiceData serviceData,
            EquipmentData equipmentData,
            boolean checkOnly,
            boolean change) {
        StrUtil.info(log, resourceRule);

        LimitAllocationRequest ar = new LimitAllocationRequest();
        ar.resourceSetId = serviceData.resourceSetId;
        ar.resourceUnionId = serviceData.resourceUnionId;
        ar.resourceName = resourceRule.resourceName;
        ar.assetId = equipmentData.equipmentId;
        ar.missingResourceAction = AllocationAction.Succeed_Allocate;
        ar.expiredResourceAction = AllocationAction.Succeed_Allocate;
        ar.replace = true;
        ar.strict = false;
        ar.checkLimit = ExpressionEvaluator.evalLong(
                change ? resourceRule.hardLimitExpression : resourceRule.softLimitExpression, equipmentData.data);
        ar.checkCount = ExpressionEvaluator.evalLong(resourceRule.allocationExpression, serviceData.data);
        ar.allocateCount = checkOnly ? 0 : ar.checkCount;
        return ar;
    }

    private AllocationRequest buildAllocationRequest(
            RangeRule rangeRule,
            ServiceData serviceData,
            EquipmentData equipmentData,
            boolean checkOnly,
            boolean change) {
        StrUtil.info(log, rangeRule);

        RangeAllocationRequest ar = new RangeAllocationRequest();
        ar.resourceSetId = serviceData.resourceSetId;
        ar.resourceUnionId = serviceData.resourceUnionId;
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

    @Override
    public ThresholdStatus getThresholdStatus(
            ServiceData serviceData,
            EquipmentData equipmentData,
            LimitAllocationOutcome limitAllocationOutcome) {
        ResourceRule rr = resourceRuleDao.getResourceRule(serviceData.serviceModel, serviceData.endPointPosition,
                equipmentData.equipmentLevel, limitAllocationOutcome.request.resourceName);
        if (rr == null || rr.thresholdList == null || rr.thresholdList.isEmpty())
            return null;

        ThresholdStatus thresholdStatus = null;
        long maxThresholdValue = 0;
        for (ResourceThreshold th : rr.thresholdList) {
            long thresholdValue = ExpressionEvaluator.evalLong(th.expression, equipmentData.data);

            if (thresholdValue > maxThresholdValue) {
                maxThresholdValue = thresholdValue;

                if (limitAllocationOutcome.used >= thresholdValue) {
                    thresholdStatus = new ThresholdStatus();
                    thresholdStatus.resourceRule = rr;
                    thresholdStatus.resourceThreshold = th;
                    thresholdStatus.limitValue = limitAllocationOutcome.limit;
                    thresholdStatus.thresholdValue = thresholdValue;
                    thresholdStatus.used = limitAllocationOutcome.used;
                    thresholdStatus.lastAdded = limitAllocationOutcome.allocatedCount;
                }
            }
        }

        return thresholdStatus;
    }

    public void setResourceRuleDao(ResourceRuleDao resourceRuleDao) {
        this.resourceRuleDao = resourceRuleDao;
    }

    public void setRangeRuleDao(RangeRuleDao rangeRuleDao) {
        this.rangeRuleDao = rangeRuleDao;
    }
}
