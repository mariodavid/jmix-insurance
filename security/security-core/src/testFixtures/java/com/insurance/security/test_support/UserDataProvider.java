package com.insurance.security.test_support;

import com.insurance.common.test_support.TestDataProvider;
import com.insurance.security.entity.User;

public class UserDataProvider implements TestDataProvider<User> {

  @Override
  public Class<User> getEntityClass() {
    return User.class;
  }

  @Override
  public void accept(User user) {
    user.setUsername("testuser");
    user.setPassword("secret");
    user.setFirstName("John");
    user.setLastName("Doe");
    user.setEmail("john.doe@example.com");
    user.setActive(true);
  }
}
