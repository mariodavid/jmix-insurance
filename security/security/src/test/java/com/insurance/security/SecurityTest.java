package com.insurance.security;

import com.insurance.common.test_support.AuthenticatedAsAdmin;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.security.entity.User;
import com.insurance.security.security.FullAccessRole;
import com.insurance.security.test_support.UserDataProvider;
import io.jmix.core.DataManager;
import io.jmix.core.security.UserRepository;
import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Method;

import static com.insurance.security.test_support.Assertions.assertThat;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ExtendWith(AuthenticatedAsAdmin.class)
class SecurityTest {

    @Autowired
    private DataManager dataManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityTestData entityTestData;

    private User savedUser;

    @Test
    void given_userCreatedWithDataManager_when_saved_then_canBeLoadedByIdAndUsername() {
        savedUser = entityTestData.saveWithDefaults(new UserDataProvider(), user -> {
            user.setUsername("security-test-user-" + System.currentTimeMillis());
            user.setPassword(passwordEncoder.encode("test-passwd"));
        });

        User loaded = dataManager.load(User.class).id(savedUser.getId()).one();
        UserDetails userDetails = userRepository.loadUserByUsername(savedUser.getUsername());

        assertThat(loaded)
                .hasId(savedUser.getId())
                .hasUsername(savedUser.getUsername())
                .isActive();
        assertThat(userDetails.getUsername()).isEqualTo(savedUser.getUsername());
    }

    @Test
    void fullAccessRoleDeclaresWildcardEntityAttributeViewMenuAndSpecificPolicies() throws NoSuchMethodException {
        Method method = FullAccessRole.class.getDeclaredMethod("fullAccess");

        EntityPolicy entityPolicy = method.getAnnotation(EntityPolicy.class);
        EntityAttributePolicy attributePolicy = method.getAnnotation(EntityAttributePolicy.class);
        ViewPolicy viewPolicy = method.getAnnotation(ViewPolicy.class);
        MenuPolicy menuPolicy = method.getAnnotation(MenuPolicy.class);
        SpecificPolicy specificPolicy = method.getAnnotation(SpecificPolicy.class);

        assertThat(entityPolicy.entityName()).isEqualTo("*");
        assertThat(entityPolicy.actions()).containsExactly(EntityPolicyAction.ALL);
        assertThat(attributePolicy.entityName()).isEqualTo("*");
        assertThat(attributePolicy.attributes()).containsExactly("*");
        assertThat(attributePolicy.action()).isEqualTo(EntityAttributePolicyAction.MODIFY);
        assertThat(viewPolicy.viewIds()).containsExactly("*");
        assertThat(menuPolicy.menuIds()).containsExactly("*");
        assertThat(specificPolicy.resources()).containsExactly("*");
    }

    @AfterEach
    void tearDown() {
        if (savedUser != null) {
            User user = savedUser;
            dataManager.load(User.class)
                    .id(user.getId())
                    .optional()
                    .ifPresent(dataManager::remove);
            savedUser = null;
        }
    }
}
