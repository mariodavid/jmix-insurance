package com.insurance.quote.ui.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Quote UI: Read-only", code = QuoteUiReadRole.CODE)
public interface QuoteUiReadRole {
    String CODE = "quote-ui-read";

    @ViewPolicy(viewIds = {"quote_Quote.list"})
    @MenuPolicy(menuIds = {"quote_Quote.list"})
    void quoteUi();
}
