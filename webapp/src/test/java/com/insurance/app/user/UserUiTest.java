package com.insurance.app.user;

import static com.insurance.security.core.test_support.Assertions.assertThat;

import com.insurance.app.WebappApplication;
import com.insurance.common.test_support_ui.DataGridInteractions;
import com.insurance.common.test_support_ui.FormInteractions;
import com.insurance.common.test_support_ui.ViewInteractions;
import com.insurance.security.core.entity.User;
import io.jmix.core.DataManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@UiTest
@SpringBootTest(classes = {WebappApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
class UserUiTest {

  private static final String PASSWORD = "test-passwd";

  @Autowired private DataManager dataManager;

  @Autowired private ViewNavigators viewNavigators;

  @Autowired private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    removeTestUsers();
  }

  @Test
  void given_userListView_when_userCreated_then_userIsPersistedWithEncodedPassword() {
    // given
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    View<?> userListView = viewInteractions.navigate("security_User.list");
    String username = "test-user-" + System.currentTimeMillis();

    // when
    DataGridInteractions.of(userListView, User.class, "usersDataGrid")
        .actionPerform("createAction");

    View<?> userDetailView = viewInteractions.findOpenView("security_User.detail");
    FormInteractions form = FormInteractions.of(userDetailView);
    form.setTextFieldValue("usernameField", username);
    form.setPasswordFieldValue("passwordField", PASSWORD);
    form.setPasswordFieldValue("confirmPasswordField", PASSWORD);
    form.click("saveAndCloseButton");

    // then
    userListView = viewInteractions.findOpenView("security_User.list");
    DataGridInteractions<User> usersDataGrid =
        DataGridInteractions.of(userListView, User.class, "usersDataGrid");
    usersDataGrid.items().stream()
        .filter(u -> u.getUsername().equals(username))
        .findFirst()
        .orElseThrow();

    User savedUser = dataManager.load(User.class).query("e.username = ?1", username).one();
    assertThat(savedUser.getPassword()).isNotEqualTo(PASSWORD);
    assertThat(passwordEncoder.matches(PASSWORD, savedUser.getPassword())).isTrue();
    assertThat(savedUser).isActive();
  }

  @Test
  void given_passwordMismatch_when_userSaved_then_detailViewStaysOpenAndUserIsNotPersisted() {
    // given
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    View<?> userListView = viewInteractions.navigate("security_User.list");
    String username = "test-user-" + System.currentTimeMillis();

    // when
    DataGridInteractions.of(userListView, User.class, "usersDataGrid")
        .actionPerform("createAction");

    View<?> userDetailView = viewInteractions.findOpenView("security_User.detail");
    FormInteractions form = FormInteractions.of(userDetailView);
    form.setTextFieldValue("usernameField", username);
    form.setPasswordFieldValue("passwordField", PASSWORD);
    form.setPasswordFieldValue("confirmPasswordField", "different-passwd");
    form.click("saveAndCloseButton");

    // then
    View<?> currentView = UiTestUtils.getCurrentView();
    assertThat(currentView.getId()).contains("security_User.detail");
    assertThat(dataManager.load(User.class).query("e.username = ?1", username).optional())
        .isEmpty();
  }

  @AfterEach
  void tearDown() {
    removeTestUsers();
  }

  private void removeTestUsers() {
    dataManager
        .load(User.class)
        .query("e.username like ?1", "test-user-%")
        .list()
        .forEach(u -> dataManager.remove(u));
  }
}
