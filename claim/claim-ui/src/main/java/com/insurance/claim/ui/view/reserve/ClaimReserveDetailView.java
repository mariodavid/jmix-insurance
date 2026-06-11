package com.insurance.claim.ui.view.reserve;

import com.insurance.claim.core.entity.ClaimReserve;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "claims/reserves/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "claim_ClaimReserve.detail")
@ViewDescriptor(path = "claim-reserve-detail-view.xml")
@EditedEntityContainer("claimReserveDc")
public class ClaimReserveDetailView extends StandardDetailView<ClaimReserve> {}
