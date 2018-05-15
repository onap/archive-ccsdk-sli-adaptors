/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights
 * 						reserved.
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
import org.onap.ccsdk.sli.adaptors.ra.comp.ResourceEntity;
import org.onap.ccsdk.sli.adaptors.ra.comp.ResourceRequest;
import org.onap.ccsdk.sli.adaptors.ra.comp.ResourceTarget;
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
	public AllocationRequest buildAllocationRequest(String serviceModel, ResourceEntity resourceEntity,
			ResourceTarget resourceTarget, ResourceRequest resourceRequest, boolean checkOnly, boolean change) {
		List<ResourceRule> resourceRuleList = resourceRuleDao.getResourceRules(serviceModel,
				resourceTarget.resourceTargetType);
		List<RangeRule> rangeRuleList = rangeRuleDao.getRangeRules(serviceModel, resourceTarget.resourceTargetType);

		List<AllocationRequest> arlist = new ArrayList<>();

		for (ResourceRule rr : resourceRuleList) {
			if (resourceRequest.resourceName != null && !resourceRequest.resourceName.equals(rr.resourceName)) {
				continue;
			}

			boolean matches = ExpressionEvaluator.evalBoolean(rr.serviceExpression, resourceEntity.data);
			matches = matches && ExpressionEvaluator.evalBoolean(rr.equipmentExpression, resourceTarget.data);

			if (matches) {
				AllocationRequest ar1 = buildAllocationRequest(rr, resourceEntity, resourceTarget, resourceRequest,
						checkOnly, change);
				arlist.add(ar1);
			}
		}

		for (RangeRule rr : rangeRuleList) {
			if (resourceRequest.resourceName != null && !resourceRequest.resourceName.equals(rr.rangeName)) {
				continue;
			}
			if (resourceRequest.endPointPosition != null
					&& !resourceRequest.endPointPosition.equals(rr.endPointPosition)) {
				continue;
			}

			AllocationRequest ar1 = buildAllocationRequest(rr, resourceEntity, resourceTarget, resourceRequest,
					checkOnly, change);
			arlist.add(ar1);
		}

		if (arlist.isEmpty()) {
			return null;
		}

		if (arlist.size() == 1) {
			return arlist.get(0);
		}

		MultiResourceAllocationRequest ar = new MultiResourceAllocationRequest();
		ar.stopOnFirstFailure = false;
		ar.allocationRequestList = arlist;
		return ar;
	}

	private AllocationRequest buildAllocationRequest(ResourceRule resourceRule, ResourceEntity resourceEntity,
			ResourceTarget resourceTarget, ResourceRequest resourceRequest, boolean checkOnly, boolean change) {
		StrUtil.info(log, resourceRule);

		LimitAllocationRequest ar = new LimitAllocationRequest();
		ar.applicationId = resourceRequest.applicationId;
		ar.resourceUnionId = resourceEntity.resourceEntityType + "::" + resourceEntity.resourceEntityId;
		ar.resourceSetId = ar.resourceUnionId + "::" + resourceEntity.resourceEntityVersion;
		ar.resourceName = resourceRule.resourceName;
		if (resourceRequest.resourceShareGroup != null) {
			ar.resourceShareGroupList = Collections.singleton(resourceRequest.resourceShareGroup);
		}
		ar.assetId = resourceTarget.resourceTargetType + "::" + resourceTarget.resourceTargetId;
		ar.missingResourceAction = AllocationAction.Succeed_Allocate;
		ar.expiredResourceAction = AllocationAction.Succeed_Allocate;
		ar.replace = resourceRequest.replace;
		ar.strict = false;
		ar.checkLimit = ExpressionEvaluator.evalLong(
				change ? resourceRule.hardLimitExpression : resourceRule.softLimitExpression, resourceTarget.data);
		ar.checkCount = ExpressionEvaluator.evalLong(resourceRule.allocationExpression, resourceEntity.data);
		ar.allocateCount = checkOnly ? 0 : ar.checkCount;
		return ar;
	}

	private AllocationRequest buildAllocationRequest(RangeRule rangeRule, ResourceEntity resourceEntity,
			ResourceTarget resourceTarget, ResourceRequest resourceRequest, boolean checkOnly, boolean change) {
		StrUtil.info(log, rangeRule);

		RangeAllocationRequest ar = new RangeAllocationRequest();
		ar.applicationId = resourceRequest.applicationId;
		if (resourceRequest.endPointPosition != null) {
			ar.resourceUnionId = resourceEntity.resourceEntityType + "::" + resourceEntity.resourceEntityId + "::"
					+ resourceRequest.endPointPosition;
			ar.endPointPosition = resourceRequest.endPointPosition;
		}else
			ar.resourceUnionId = resourceEntity.resourceEntityType + "::" + resourceEntity.resourceEntityId;
		ar.resourceSetId = ar.resourceUnionId + "::" + resourceEntity.resourceEntityVersion;
		ar.resourceName = rangeRule.rangeName;
		if (resourceRequest.resourceShareGroup != null) {
			ar.resourceShareGroupList = Collections.singleton(resourceRequest.resourceShareGroup);
		}
		ar.assetId = resourceTarget.resourceTargetType + "::" + resourceTarget.resourceTargetId;
		ar.requestedNumbers = StrUtil.listInt(resourceRequest.rangeRequestedNumbers,
				"Invalid value for requested-numbers");
		if (ar.requestedNumbers != null) {
			ar.requestedCount = ar.requestedNumbers.size();
		}
		ar.excludeNumbers = StrUtil.listInt(resourceRequest.rangeExcludeNumbers, "Invalid value for exclude-numbers");
		ar.reverseOrder = resourceRequest.rangeReverseOrder;
		ar.missingResourceAction = AllocationAction.Succeed_Allocate;
		ar.expiredResourceAction = AllocationAction.Succeed_Allocate;
		ar.replace = resourceRequest.replace;
		ar.check = true;
		ar.allocate = !checkOnly;
		ar.checkMin = resourceRequest.rangeMinOverride >= 0 ? resourceRequest.rangeMinOverride : rangeRule.minValue;
		ar.checkMax = resourceRequest.rangeMaxOverride >= 0 ? resourceRequest.rangeMaxOverride : rangeRule.maxValue;
		return ar;
	}

	public void setResourceRuleDao(ResourceRuleDao resourceRuleDao) {
		this.resourceRuleDao = resourceRuleDao;
	}

	public void setRangeRuleDao(RangeRuleDao rangeRuleDao) {
		this.rangeRuleDao = rangeRuleDao;
	}
}
