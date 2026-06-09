package com.insurance.app.ui.view.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.AbstractLogin.LoginEvent;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import io.jmix.core.CoreProperties;
import io.jmix.core.MessageTools;
import io.jmix.core.security.AccessDeniedException;
import io.jmix.flowui.component.loginform.JmixLoginForm;
import io.jmix.flowui.kit.component.ComponentUtils;
import io.jmix.flowui.kit.component.loginform.JmixLoginI18n;
import io.jmix.flowui.view.MessageBundle;
import io.jmix.flowui.view.StandardView;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.securityflowui.authentication.AuthDetails;
import io.jmix.securityflowui.authentication.LoginViewSupport;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;

@SuppressWarnings("PMD.GuardLogStatement")
@Route("login")
@ViewController(id = "app_LoginView")
@ViewDescriptor(path = "login-view.xml")
public class LoginView extends StandardView implements LocaleChangeObserver {

  private static final Logger log = LoggerFactory.getLogger(LoginView.class);

  private final CoreProperties coreProperties;
  private final LoginViewSupport loginViewSupport;
  private final MessageTools messageTools;
  private final String defaultUsername;
  private final String defaultPassword;

  public LoginView(
      CoreProperties coreProperties,
      LoginViewSupport loginViewSupport,
      MessageTools messageTools,
      @Value("${ui.login.defaultUsername:}") String defaultUsername,
      @Value("${ui.login.defaultPassword:}") String defaultPassword) {
    this.coreProperties = coreProperties;
    this.loginViewSupport = loginViewSupport;
    this.messageTools = messageTools;
    this.defaultUsername = defaultUsername;
    this.defaultPassword = defaultPassword;
  }

  @ViewComponent private JmixLoginForm login;

  @ViewComponent private MessageBundle messageBundle;

  @Subscribe
  public void onInit(final InitEvent event) {
    initLocales();
    initDefaultCredentials();
  }

  private void initLocales() {
    Map<Locale, String> locales = new LinkedHashMap<>();
    for (Locale locale : coreProperties.getAvailableLocales()) {
      locales.putIfAbsent(locale, messageTools.getLocaleDisplayName(locale));
    }

    ComponentUtils.setItemsMap(login, locales);

    login.setSelectedLocale(VaadinSession.getCurrent().getLocale());
  }

  private void initDefaultCredentials() {
    if (StringUtils.isNotBlank(defaultUsername)) {
      login.setUsername(defaultUsername);
    }

    if (StringUtils.isNotBlank(defaultPassword)) {
      login.setPassword(defaultPassword);
    }
  }

  @Subscribe("login")
  public void onLogin(final LoginEvent event) {
    try {
      loginViewSupport.authenticate(
          AuthDetails.of(event.getUsername(), event.getPassword())
              .withLocale(login.getSelectedLocale())
              .withRememberMe(login.isRememberMe()));
    } catch (final AuthenticationException | AccessDeniedException e) {
      log.warn("Login failed for user '{}': {}", event.getUsername(), e.toString());
      event.getSource().setError(true);
    }
  }

  @Override
  public void localeChange(final LocaleChangeEvent event) {
    UI.getCurrent().getPage().setTitle(messageBundle.getMessage("LoginView.title"));

    final JmixLoginI18n loginI18n = JmixLoginI18n.createDefault();

    final JmixLoginI18n.JmixForm form = new JmixLoginI18n.JmixForm();
    form.setTitle("");
    form.setUsername(messageBundle.getMessage("loginForm.username"));
    form.setPassword(messageBundle.getMessage("loginForm.password"));
    form.setSubmit(messageBundle.getMessage("loginForm.submit"));
    form.setForgotPassword(messageBundle.getMessage("loginForm.forgotPassword"));
    form.setRememberMe(messageBundle.getMessage("loginForm.rememberMe"));
    loginI18n.setForm(form);

    final LoginI18n.ErrorMessage errorMessage = new LoginI18n.ErrorMessage();
    errorMessage.setTitle(messageBundle.getMessage("loginForm.errorTitle"));
    errorMessage.setMessage(messageBundle.getMessage("loginForm.badCredentials"));
    errorMessage.setUsername(messageBundle.getMessage("loginForm.errorUsername"));
    errorMessage.setPassword(messageBundle.getMessage("loginForm.errorPassword"));
    loginI18n.setErrorMessage(errorMessage);

    login.setI18n(loginI18n);
  }
}
