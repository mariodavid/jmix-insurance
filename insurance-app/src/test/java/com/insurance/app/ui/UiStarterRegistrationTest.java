package com.insurance.app.ui;

import com.insurance.account.ui.view.account.AccountListView;
import com.insurance.app.InsuranceAppApplication;
import com.insurance.app.test_support.AuthenticatedAsAdmin;
import com.insurance.partner.ui.view.partner.PartnerListView;
import com.insurance.policy.ui.view.policy.PolicyListView;
import com.insurance.quote.ui.view.quote.QuoteListView;
import io.jmix.flowui.ViewNavigators;
import io.jmix.flowui.testassist.FlowuiTestAssistConfiguration;
import io.jmix.flowui.testassist.UiTest;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewInfo;
import io.jmix.flowui.view.ViewRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@UiTest
@SpringBootTest(classes = {InsuranceAppApplication.class, FlowuiTestAssistConfiguration.class})
@ActiveProfiles("test")
@ExtendWith(AuthenticatedAsAdmin.class)
@DisplayName("UI starter registration")
class UiStarterRegistrationTest {

    @Autowired
    private ViewRegistry viewRegistry;

    @Autowired
    private ViewNavigators viewNavigators;

    @ParameterizedTest(name = "{0}")
    @MethodSource("centralListViews")
    @DisplayName("Central list views from UI starters are registered and routable")
    void centralListViewsFromUiStartersAreRegisteredAndRoutable(String viewId, Class<? extends View<?>> viewClass) {
        assertThat(viewRegistry.hasView(viewId))
                .as("%s must be registered by its UI starter", viewId)
                .isTrue();

        ViewInfo viewInfo = viewRegistry.getViewInfo(viewId);
        assertThat(viewInfo.getControllerClass())
                .as("%s must resolve to the expected view controller", viewId)
                .isEqualTo(viewClass);

        viewNavigators.view(UiTestUtils.getCurrentView(), viewClass).navigate();
        assertThat((Object) UiTestUtils.getCurrentView())
                .as("%s must be navigable", viewId)
                .isInstanceOf(viewClass);
    }

    private static Stream<Object[]> centralListViews() {
        return Stream.of(
                view("quote_Quote.list", QuoteListView.class),
                view("account_Account.list", AccountListView.class),
                view("policy_Policy.list", PolicyListView.class),
                view("partner_Partner.list", PartnerListView.class)
        );
    }

    private static Object[] view(String viewId, Class<? extends View<?>> viewClass) {
        return new Object[]{viewId, viewClass};
    }
}
