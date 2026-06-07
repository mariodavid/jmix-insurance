package com.insurance.policy.api.service;

import com.insurance.policy.api.dto.PartnerPolicySummaryDto;
import java.util.List;

/**
 * Read-only policy overview API for partner-context screens.
 *
 * <p>Consumers use this API instead of querying policy persistent entities across module
 * boundaries.
 */
public interface PartnerPolicyOverviewService {

  List<PartnerPolicySummaryDto> findPoliciesForPartner(String partnerNo);
}
