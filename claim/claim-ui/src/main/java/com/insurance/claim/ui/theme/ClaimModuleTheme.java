package com.insurance.claim.ui.theme;

import com.insurance.theme.ModuleTheme;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;

@org.springframework.stereotype.Component
public class ClaimModuleTheme implements ModuleTheme {
  @Override
  public String getModulePrefix() {
    return "claim";
  }

  @Override
  public String getThemeClass() {
    return "claim-theme";
  }

  @Override
  public Component getHeaderIcon() {
    return VaadinIcon.FILE_TEXT.create();
  }
}
