package com.insurance.account.ui.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Account UI: Read-only", code = AccountUiReadRole.CODE)
public interface AccountUiReadRole {
    String CODE = "account-ui-read";

    @ViewPolicy(viewIds = {"account_Account.list"})
    @MenuPolicy(menuIds = {"account_Account.list"})
    void accountUi();
}
