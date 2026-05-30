package com.insurance.partner.core.view.partner;

import com.insurance.partner.core.entity.Partner;
import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;

@Route(value = "partners", layout = DefaultMainViewParent.class)
@ViewController(id = "partner_Partner.list")
@ViewDescriptor(path = "partner-list-view.xml")
@LookupComponent("partnersDataGrid")
@DialogMode(width = "64em")
public class PartnerListView extends StandardListView<Partner> {
}
