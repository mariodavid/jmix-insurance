package com.insurance.policy.api.service;

import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;

public interface PolicyService {
    PolicyDto createPolicy(CreatePolicyRequestDto request);
    PolicyDto findPolicyById(String id);
}
