package com.insurance.common.entity;

import io.jmix.core.MetadataTools;
import io.jmix.core.metamodel.annotation.DependsOnProperties;
import io.jmix.core.metamodel.annotation.InstanceName;
import io.jmix.core.metamodel.annotation.JmixEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@JmixEntity
@Table(name = "COMMON_ADDRESS")
@Entity(name = "common_Address")
public class Address extends CommonEntity {

    @Column(name = "STREET", nullable = false)
    @NotNull
    private String street;

    @Column(name = "HOUSE_NUMBER", nullable = false)
    @NotNull
    private String houseNumber;

    @Column(name = "POSTAL_CODE", nullable = false)
    @NotNull
    private String postalCode;

    @Column(name = "CITY", nullable = false)
    @NotNull
    private String city;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @InstanceName
    @DependsOnProperties({"street", "houseNumber", "postalCode", "city"})
    public String getInstanceName(MetadataTools metadataTools) {
        return String.format("%s %s %s %s",
                metadataTools.format(street),
                metadataTools.format(houseNumber),
                metadataTools.format(postalCode),
                metadataTools.format(city));
    }
}
