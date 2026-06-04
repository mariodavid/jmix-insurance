package com.insurance.partner.core.test_support;

import com.insurance.common.test_support.TestDataProvider;
import com.insurance.partner.core.entity.Partner;

public class PartnerDataProvider implements TestDataProvider<Partner> {

  @Override
  public Class<Partner> getEntityClass() {
    return Partner.class;
  }

  @Override
  public void accept(Partner partner) {
    partner.setPartnerNo("PT-99999");
    partner.setFirstName("Max");
    partner.setLastName("Mustermann");
  }
}
