package com.insurance.account.ui.theme;

import com.insurance.theme.ModuleTheme;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;

@org.springframework.stereotype.Component
public class AccountModuleTheme implements ModuleTheme {
  @Override
  public String getModulePrefix() {
    return "account";
  }

  @Override
  public String getThemeClass() {
    return "account-theme";
  }

  @Override
  public Component getHeaderIcon() {
    return VaadinIcon.INSTITUTION.create();
  }
}
