package com.insurance.common;

import com.insurance.common.entity.Address;
import com.insurance.common.test_support.AddressDataProvider;
import com.insurance.common.test_support.EntityTestData;
import io.jmix.core.DataManager;
import io.jmix.core.security.SystemAuthenticator;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static com.insurance.common.test_support.Assertions.assertThat;
import static com.insurance.common.test_support.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
class CommonTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private EntityTestData entityTestData;

    @Autowired
    private SystemAuthenticator systemAuthenticator;

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

        assertThatThrownBy(() -> systemAuthenticator.withUser("admin", () -> dataManager.save(address)))
                .isInstanceOf(ConstraintViolationException.class)
                .satisfies(error -> assertThat(((ConstraintViolationException) error).getConstraintViolations())
                        .anySatisfy(violation -> assertThat(violation.getPropertyPath().toString()).isEqualTo("street")));
    }

    @Test
    void given_address_when_savedAndUpdated_then_auditFieldsAndVersionAreSetByJmix() {
        // given
        savedAddress = systemAuthenticator.withUser("admin",
                () -> entityTestData.saveWithDefaults(new AddressDataProvider()));

        assertThat(savedAddress.getCreatedDate()).isNotNull();
        assertThat(savedAddress.getCreatedBy()).isEqualTo("admin");
        assertThat(savedAddress.getLastModifiedDate()).isNotNull();
        assertThat(savedAddress).hasVersion(1);

        // when
        savedAddress.setCity("Updated City");
        savedAddress = systemAuthenticator.withUser("admin", () -> dataManager.save(savedAddress));

        // then
        Address reloaded = dataManager.load(Address.class).id(savedAddress.getId()).one();
        assertThat(reloaded.getCreatedDate()).isEqualTo(savedAddress.getCreatedDate());
        assertThat(reloaded.getCreatedBy()).isEqualTo("admin");
        assertThat(reloaded.getLastModifiedDate()).isNotNull();
        assertThat(reloaded.getLastModifiedBy()).isEqualTo("admin");
        assertThat(reloaded).hasVersion(2);
    }

    @Test
    void given_validAddress_when_saved_then_canBeLoadedAgain() {
        Address address = dataManager.create(Address.class);
        address.setStreet("Valid Street");
        address.setHouseNumber("3");
        address.setPostalCode("10115");
        address.setCity("Berlin");
        savedAddress = systemAuthenticator.withUser("admin", () -> dataManager.save(address));

        Address loaded = dataManager.load(Address.class).id(savedAddress.getId()).one();

        assertThat(loaded)
                .hasStreet("Valid Street")
                .hasHouseNumber("3")
                .hasPostalCode("10115")
                .hasCity("Berlin");
    }

    @AfterEach
    void tearDown() {
        if (savedAddress != null) {
            systemAuthenticator.runWithUser("admin", () -> dataManager.remove(savedAddress));
            savedAddress = null;
        }
    }
}
