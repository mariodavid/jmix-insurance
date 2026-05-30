package com.insurance.common.mdc;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum CommonMdc implements EnumClass<String> {

    POLICY_ID("policy_id"),
    POLICY_NO("policy_no"),
    QUOTE_ID("quote_id"),
    QUOTE_NO("quote_no");

    private final String id;

    CommonMdc(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Nullable
    public static CommonMdc fromId(String id) {
        for (CommonMdc at : CommonMdc.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
