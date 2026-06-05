package com.insurance.app.user;

import static com.insurance.security.test_support.Assertions.assertThat;

import com.insurance.app.WebappApplication;
import com.insurance.common.test_support_ui.DataGridInteractions;
import com.insurance.common.test_support_ui.FormInteractions;
import com.insurance.common.test_support_ui.UiTestSupport;
import com.insurance.common.test_support_ui.ViewInteractions;
import com.insurance.security.entity.User;
import com.insurance.security.ui.view.user.UserDetailView;
import com.insurance.security.ui.view.user.UserListView;
import io.jmix.core.DataManager;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

/** Sample UI integration test for the User entity. */
@UiTest
@SpringBootTest(classes = {WebappApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
public class UserUiTest {

  @Autowired DataManager dataManager;

  @Autowired ViewNavigators viewNavigators;

  @Autowired PasswordEncoder passwordEncoder;

  @Test
  void test_createUser() {
    // Navigate to user list view
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    UserListView userListView = viewInteractions.navigate(UserListView.class);

    // click "Create" button
    JmixButton createBtn = UiTestSupport.findButtonByText(userListView, "Create");
    assertThat(createBtn).isNotNull();
    createBtn.click();

    // Get detail view
    UserDetailView userDetailView = viewInteractions.findOpenView(UserDetailView.class);

    // Set username and password in the fields
    FormInteractions form = FormInteractions.of(userDetailView);
    String username = "test-user-" + System.currentTimeMillis();
    form.setFieldValueByLabel("Username", username);
    form.setFieldValueByLabel("Password", "test-passwd");
    form.setFieldValueByLabel("Confirm Password", "test-passwd");

    // Click "OK"
    JmixButton commitAndCloseBtn = UiTestSupport.findButtonByText(userDetailView, "OK");
    assertThat(commitAndCloseBtn).isNotNull();
    commitAndCloseBtn.click();

    // Get navigated user list view
    userListView = viewInteractions.findOpenView(UserListView.class);

    // Check the created user is shown in the table
    DataGridInteractions<User> usersDataGrid =
        DataGridInteractions.of(userListView, User.class, "usersDataGrid");
    usersDataGrid.items().stream()
        .filter(u -> u.getUsername().equals(username))
        .findFirst()
        .orElseThrow();

    User savedUser = dataManager.load(User.class).query("e.username = ?1", username).one();
    assertThat(savedUser.getPassword()).isNotEqualTo("test-passwd");
    assertThat(passwordEncoder.matches("test-passwd", savedUser.getPassword())).isTrue();
    assertThat(savedUser).isActive();
  }

  @Test
  void test_passwordMismatchPreventsSave() {
    ViewInteractions viewInteractions = ViewInteractions.forNavigation(viewNavigators);
    UserListView userListView = viewInteractions.navigate(UserListView.class);
    JmixButton createBtn = UiTestSupport.findButtonByText(userListView, "Create");
    assertThat(createBtn).isNotNull();
    createBtn.click();

    UserDetailView userDetailView = viewInteractions.findOpenView(UserDetailView.class);
    FormInteractions form = FormInteractions.of(userDetailView);
    String username = "test-user-" + System.currentTimeMillis();
    form.setFieldValueByLabel("Username", username);
    form.setFieldValueByLabel("Password", "test-passwd");
    form.setFieldValueByLabel("Confirm Password", "different-passwd");

    JmixButton commitAndCloseBtn = UiTestSupport.findButtonByText(userDetailView, "OK");
    assertThat(commitAndCloseBtn).isNotNull();
    commitAndCloseBtn.click();

    View<?> currentView = UiTestUtils.getCurrentView();
    assertThat(currentView).isInstanceOf(UserDetailView.class);
    assertThat(dataManager.load(User.class).query("e.username = ?1", username).optional())
        .isEmpty();
  }

  @AfterEach
  void tearDown() {
    dataManager
        .load(User.class)
        .query("e.username like ?1", "test-user-%")
        .list()
        .forEach(u -> dataManager.remove(u));
  }
}
