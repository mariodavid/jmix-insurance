package com.insurance.app.test_support;

import com.insurance.partner.core.entity.Partner;
import io.jmix.core.DataManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PartnerFactory {

    @Autowired
    private DataManager dataManager;

    public PartnerData.Builder defaultData() {
        return PartnerData.builder();
    }

    public Partner save(PartnerData data) {
        Partner partner = dataManager.create(Partner.class);
        if (data.partnerNo() != null) {
            partner.setPartnerNo(data.partnerNo());
        }
        partner.setFirstName(data.firstName());
        partner.setLastName(data.lastName());
        return dataManager.save(partner);
    }

    public Partner saveDefault() {
        return save(defaultData().build());
    }
}
