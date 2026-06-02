package com.insurance.policy.ui.view.policy;

import com.insurance.policy.core.entity.Policy;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "policies", layout = DefaultMainViewParent.class)
@ViewController(id = "policy_Policy.list")
@ViewDescriptor(path = "policy-list-view.xml")
@LookupComponent("policiesDataGrid")
@DialogMode(width = "64em")
public class PolicyListView extends StandardListView<Policy> {
}
