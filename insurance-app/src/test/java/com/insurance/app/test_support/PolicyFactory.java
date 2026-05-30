package com.insurance.app.test_support;

import com.insurance.policy.api.dto.CreatePolicyRequestDto;
import com.insurance.policy.api.dto.PolicyDto;
import com.insurance.policy.api.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PolicyFactory {

    @Autowired
    private PolicyService policyService;

    public PolicyData.Builder defaultData() {
        return PolicyData.builder();
    }

    public PolicyDto create(PolicyData data) {
        return policyService.createPolicy(new CreatePolicyRequestDto(
                "QT-FACTORY",
                data.partnerNo(),
                data.insuranceProductId(),
                data.effectiveDate(),
                data.premium(),
                data.paymentFrequencyId()
        ));
    }

    public PolicyDto createDefault() {
        return create(defaultData().build());
    }
}
