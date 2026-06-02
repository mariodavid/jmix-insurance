package com.insurance.security;

import com.insurance.common.test_support.AuthenticatedAsAdmin;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.security.entity.User;
import com.insurance.security.test_support.UserDataProvider;
import io.jmix.core.DataManager;
import io.jmix.core.security.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

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
