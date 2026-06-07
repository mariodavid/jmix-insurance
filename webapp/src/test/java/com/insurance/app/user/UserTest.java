package com.insurance.app.user;

import static com.insurance.security.core.test_support.Assertions.assertThat;

import com.insurance.app.test_support.BaseIntegrationTest;
import com.insurance.common.test_support.EntityTestData;
import com.insurance.security.core.entity.User;
import com.insurance.security.core.test_support.UserDataProvider;
import io.jmix.core.DataManager;
import io.jmix.core.security.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserTest extends BaseIntegrationTest {

  @Autowired private DataManager dataManager;

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private UserRepository userRepository;

  @Autowired private EntityTestData entityTestData;

  private User savedUser;

  @Test
  void given_userCreatedWithDefaults_when_saved_then_canBeLoadedByIdAndUsername() {
    // given
    String username = "test-user-" + System.currentTimeMillis();

    // when
    savedUser =
        entityTestData.saveWithDefaults(
            new UserDataProvider(),
            user -> {
              user.setUsername(username);
              user.setPassword(passwordEncoder.encode("test-passwd"));
            });

    // then
    User loadedUser = dataManager.load(User.class).id(savedUser.getId()).one();
    UserDetails userDetails = userRepository.loadUserByUsername(username);
    assertThat(loadedUser).hasId(savedUser.getId()).hasUsername(username).isActive();
    assertThat(userDetails.getUsername()).isEqualTo(username);
  }

  @AfterEach
  void tearDown() {
    if (savedUser != null) {
      dataManager.load(User.class).id(savedUser.getId()).optional().ifPresent(dataManager::remove);
      savedUser = null;
    }
  }
}
