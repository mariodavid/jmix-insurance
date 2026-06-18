package com.insurance.claim.ui.view.claim;

import com.insurance.claim.core.entity.Reserve;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.DialogMode;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "claims/reserves/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "claim_Reserve.detail")
@ViewDescriptor(path = "reserve-detail-view.xml")
@EditedEntityContainer("reserveDc")
@DialogMode(width = "40em")
public class ReserveDetailView extends StandardDetailView<Reserve> {}
