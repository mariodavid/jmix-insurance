package com.insurance.partner.ui.view.partner;

import com.insurance.partner.core.entity.Partner;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.DefaultMainViewParent;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;

@Route(value = "partners/:id", layout = DefaultMainViewParent.class)
@ViewController(id = "partner_Partner.detail")
@ViewDescriptor(path = "partner-detail-view.xml")
@EditedEntityContainer("partnerDc")
public class PartnerDetailView extends StandardDetailView<Partner> {}
