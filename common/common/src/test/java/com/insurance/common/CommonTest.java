package com.insurance.common;

import com.insurance.common.entity.Address;
import io.jmix.core.DataManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
class CommonTest {

    @Autowired
    private DataManager dataManager;

    private Address savedAddress;

    @Test
    void contextLoads() {
    }

    @Test
    void given_addressMissingRequiredField_when_saved_then_validationFailsDeterministically() {
        Address address = dataManager.create(Address.class);
        address.setHouseNumber("2");
        address.setPostalCode("12345");
        address.setCity("Berlin");

        assertThatThrownBy(() -> dataManager.save(address))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(error -> assertThat(((ConstraintViolationException) error).getConstraintViolations())
                        .anySatisfy(violation -> assertThat(violation.getPropertyPath().toString()).isEqualTo("street")));
    }

    @Test
    void given_validAddress_when_saved_then_canBeLoadedAgain() {
        Address address = dataManager.create(Address.class);
        address.setStreet("Valid Street");
        address.setHouseNumber("3");
        address.setPostalCode("10115");
        address.setCity("Berlin");
        savedAddress = dataManager.save(address);

        Address loaded = dataManager.load(Address.class).id(savedAddress.getId()).one();

        assertThat(loaded.getStreet()).isEqualTo("Valid Street");
        assertThat(loaded.getHouseNumber()).isEqualTo("3");
        assertThat(loaded.getPostalCode()).isEqualTo("10115");
        assertThat(loaded.getCity()).isEqualTo("Berlin");
    }

    @AfterEach
    void tearDown() {
        if (savedAddress != null) {
            dataManager.remove(savedAddress);
            savedAddress = null;
        }
    }
}
