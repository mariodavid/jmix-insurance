package com.insurance.common.test_support;

import com.insurance.common.entity.Address;
import com.insurance.common.entity.AddressAssert;
import com.insurance.common.entity.CommonEntity;
import com.insurance.common.entity.CommonEntityAssert;

public class Assertions extends org.assertj.core.api.Assertions {

    public static AddressAssert assertThat(Address actual) {
        return new AddressAssert(actual);
    }

    public static CommonEntityAssert assertThat(CommonEntity actual) {
        return new CommonEntityAssert(actual);
    }

    protected Assertions() {
    }
}
