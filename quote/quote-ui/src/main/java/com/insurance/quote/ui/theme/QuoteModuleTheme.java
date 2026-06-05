package com.insurance.quote.ui.theme;

import com.insurance.theme.ModuleTheme;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;

@org.springframework.stereotype.Component
public class QuoteModuleTheme implements ModuleTheme {
  @Override
  public String getModulePrefix() {
    return "quote";
  }

  @Override
  public String getThemeClass() {
    return "quote-theme";
  }

  @Override
  public Component getHeaderIcon() {
    return VaadinIcon.FILE_TEXT.create();
  }
}
