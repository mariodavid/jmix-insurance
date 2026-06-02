package com.insurance.partner.core.entity;

import com.insurance.common.entity.CommonEntity;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@JmixEntity
@Table(name = "PARTNER_PARTNER")
@Entity(name = "partner_Partner")
public class Partner extends CommonEntity {

    @Column(name = "PARTNER_NO", nullable = false, unique = true)
    @NotNull
    private String partnerNo;

    @Column(name = "FIRST_NAME", nullable = false)
    @NotNull
    private String firstName;

    @Column(name = "LAST_NAME", nullable = false)
    @NotNull
    private String lastName;

    public String getPartnerNo() {
        return partnerNo;
    }

    public void setPartnerNo(String partnerNo) {
        this.partnerNo = partnerNo;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @InstanceName
    public String instanceName() {
        return "%s - %s %s".formatted(partnerNo, firstName, lastName);
    }
}
