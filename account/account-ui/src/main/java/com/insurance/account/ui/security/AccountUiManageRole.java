package com.insurance.account.ui.security;

import io.jmix.security.role.annotation.ResourceRole;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;

@ResourceRole(name = "Account UI: Manage", code = AccountUiManageRole.CODE)
public interface AccountUiManageRole {
    String CODE = "account-ui-manage";

    @ViewPolicy(viewIds = {"account_Account.list", "account_Account.detail"})
    @MenuPolicy(menuIds = {"account_Account.list"})
    void accountUi();
}
