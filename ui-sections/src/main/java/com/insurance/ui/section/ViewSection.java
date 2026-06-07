package com.insurance.ui.section;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.fragment.FragmentOwner;

public interface ViewSection<C> {

  String titleMessageKey();

  Component createContent(C context, FragmentOwner fragmentOwner);
}
