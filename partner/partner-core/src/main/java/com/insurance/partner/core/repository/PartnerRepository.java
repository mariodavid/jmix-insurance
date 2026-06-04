package com.insurance.partner.core.repository;

import com.insurance.partner.core.entity.Partner;
import io.jmix.core.repository.JmixDataRepository;
import java.util.Optional;
import java.util.UUID;

public interface PartnerRepository extends JmixDataRepository<Partner, UUID> {
  Optional<Partner> findByPartnerNo(String partnerNo);
}
