package com.insurance.claim.ui.view.claim;

import com.insurance.claim.core.entity.Claim;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.LookupComponent;
import io.jmix.flowui.view.StandardListView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "claims", layout = DefaultMainViewParent.class)
@ViewController(id = "claim_Claim.list")
@ViewDescriptor(path = "claim-list-view.xml")
@LookupComponent("claimsDataGrid")
@DialogMode(width = "64em")
public class ClaimListView extends StandardListView<Claim> {}
