package com.insurance.partner.core.view.partner;

import com.insurance.partner.core.entity.Partner;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "partners/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "partner_Partner.detail")
@ViewDescriptor(path = "partner-detail-view.xml")
@EditedEntityContainer("partnerDc")
public class PartnerDetailView extends StandardDetailView<Partner> {
}
