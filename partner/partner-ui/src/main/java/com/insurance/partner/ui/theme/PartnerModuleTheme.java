package com.insurance.partner.ui.theme;

import com.insurance.theme.ModuleTheme;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;

@org.springframework.stereotype.Component
public class PartnerModuleTheme implements ModuleTheme {
  @Override
  public String getModulePrefix() {
    return "partner";
  }

  @Override
  public String getThemeClass() {
    return "partner-theme";
  }

  @Override
  public Component getHeaderIcon() {
    return VaadinIcon.USER.create();
  }
}
