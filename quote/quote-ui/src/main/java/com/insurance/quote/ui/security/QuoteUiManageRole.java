package com.insurance.quote.ui.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Quote UI: Manage", code = QuoteUiManageRole.CODE)
public interface QuoteUiManageRole {
  String CODE = "quote-ui-manage";

  @ViewPolicy(viewIds = {"quote_Quote.list", "quote_Quote.detail"})
  @MenuPolicy(menuIds = {"quote_Quote.list"})
  void quoteUi();
}
