package com.insurance.policy.ui.api;

import java.time.LocalDate;
import java.util.UUID;

public interface PolicyViewContext {

  UUID policyId();

  String policyNo();

  String partnerNo();

  LocalDate coverageStart();

  LocalDate coverageEnd();
}
