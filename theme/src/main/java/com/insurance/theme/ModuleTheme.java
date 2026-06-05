package com.insurance.theme;

import com.vaadin.flow.component.Component;

public interface ModuleTheme {
  /** The prefix of the view controller IDs in this module (e.g., "partner"). */
  String getModulePrefix();

  /** The theme class to apply to the main layout (e.g., "partner-theme"). */
  String getThemeClass();

  /** The Vaadin icon component representing this module. */
  Component getHeaderIcon();
}
