package com.insurance.policy.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreatePolicyRequestDto(
    String quoteNo,
    String partnerNo,
    String insuranceProductId,
    LocalDate effectiveDate,
    BigDecimal premium,
    String paymentFrequencyId) {}
