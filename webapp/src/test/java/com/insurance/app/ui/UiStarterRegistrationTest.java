package com.insurance.app.ui;

import static org.assertj.core.api.Assertions.assertThat;

import com.insurance.account.ui.view.account.AccountListView;
import com.insurance.app.WebappApplication;
import com.insurance.app.test_support.AuthenticatedAsAdmin;
import com.insurance.partner.ui.view.partner.PartnerListView;
import com.insurance.policy.ui.view.policy.PolicyListView;
import com.insurance.quote.ui.view.quote.QuoteListView;
import com.insurance.security.ui.view.user.UserListView;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@UiTest
@SpringBootTest(classes = {WebappApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
@ExtendWith(AuthenticatedAsAdmin.class)
@DisplayName("UI starter registration")
class UiStarterRegistrationTest {

  @Autowired private ViewRegistry viewRegistry;

  @Autowired private ViewNavigators viewNavigators;

  @ParameterizedTest(name = "{0}")
  @MethodSource("centralListViews")
  @DisplayName("Central list views from UI starters are registered and routable")
  void centralListViewsFromUiStartersAreRegisteredAndRoutable(
      String viewId, Class<? extends View<?>> viewClass) {
    assertThat(viewRegistry.hasView(viewId))
        .as("%s must be registered by its UI starter", viewId)
        .isTrue();

    ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
    assertThat(viewInfo.getControllerClass().getName())
        .as("%s must resolve to the expected view controller", viewId)
        .isEqualTo(viewClass.getName());

    viewNavigators.view(UiTestUtils.getCurrentView(), viewClass).navigate();
    assertThat(UiTestUtils.getCurrentView().getClass().getName())
        .as("%s must be navigable", viewId)
        .isEqualTo(viewClass.getName());
  }

  private static Stream<Object[]> centralListViews() {
    return Stream.of(
        view("quote_Quote.list", QuoteListView.class),
        view("account_Account.list", AccountListView.class),
        view("policy_Policy.list", PolicyListView.class),
        view("partner_Partner.list", PartnerListView.class),
        view("security_User.list", UserListView.class));
  }

  private static Object[] view(String viewId, Class<? extends View<?>> viewClass) {
    return new Object[] {viewId, viewClass};
  }
}
