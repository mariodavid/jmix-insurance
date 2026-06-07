package com.insurance.account.api.service;

import com.insurance.account.api.dto.PartnerAccountSummaryDto;
import java.util.Optional;

/**
 * Read-only account overview API for partner-context screens.
 *
 * <p>Consumers use this API instead of joining account and policy persistent entities outside the
 * owning modules.
 */
public interface PartnerAccountOverviewService {

  Optional<PartnerAccountSummaryDto> findAccountSummaryForPartner(String partnerNo);
}
