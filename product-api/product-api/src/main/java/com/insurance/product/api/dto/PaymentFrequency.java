package com.insurance.product.api.dto;

import io.jmix.core.metamodel.datatype.EnumClass;
import org.springframework.lang.Nullable;

public enum PaymentFrequency implements EnumClass<String> {

    YEARLY("YEARLY", 1),
    QUARTERLY("QUARTERLY", 4),
    MONTHLY("MONTHLY", 12);

    private final String id;
    private final int frequency;

    PaymentFrequency(String id, int frequency) {
        this.id = id;
        this.frequency = frequency;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static PaymentFrequency fromId(String id) {
        for (PaymentFrequency at : PaymentFrequency.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }

    public int getFrequency() {
        return frequency;
    }
}
