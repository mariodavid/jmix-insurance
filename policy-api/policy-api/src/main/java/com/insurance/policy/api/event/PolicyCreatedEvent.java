package com.insurance.policy.api.event;

import org.springframework.context.ApplicationEvent;

public class PolicyCreatedEvent extends ApplicationEvent {
    
    private final String policyId;
    private final String policyNo;

    public PolicyCreatedEvent(Object source, String policyId, String policyNo) {
        super(source);
        this.policyId = policyId;
        this.policyNo = policyNo;
    }

    public String getPolicyId() {
        return policyId;
    }

    public String getPolicyNo() {
        return policyNo;
    }
}
