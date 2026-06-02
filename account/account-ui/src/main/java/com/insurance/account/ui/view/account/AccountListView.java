package com.insurance.account.ui.view.account;

import com.insurance.account.core.entity.Account;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "accounts", layout = DefaultMainViewParent.class)
@ViewController(id = "account_Account.list")
@ViewDescriptor(path = "account-list-view.xml")
@LookupComponent("accountsDataGrid")
@DialogMode(width = "64em")
public class AccountListView extends StandardListView<Account> {
}
