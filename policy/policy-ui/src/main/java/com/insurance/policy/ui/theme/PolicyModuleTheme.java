package com.insurance.policy.ui.theme;

import com.insurance.theme.ModuleTheme;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;

@org.springframework.stereotype.Component
public class PolicyModuleTheme implements ModuleTheme {
  @Override
  public String getModulePrefix() {
    return "policy";
  }

  @Override
  public String getThemeClass() {
    return "policy-theme";
  }

  @Override
  public Component getHeaderIcon() {
    return VaadinIcon.SHIELD.create();
  }
}
