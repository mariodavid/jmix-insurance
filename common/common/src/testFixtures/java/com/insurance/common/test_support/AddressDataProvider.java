package com.insurance.common.test_support;

import com.insurance.common.entity.Address;

public class AddressDataProvider implements TestDataProvider<Address> {

    @Override
    public Class<Address> getEntityClass() {
        return Address.class;
    }

    @Override
    public void accept(Address address) {
        address.setStreet("Hauptstraße");
        address.setHouseNumber("10a");
        address.setPostalCode("12345");
        address.setCity("Musterstadt");
    }
}
