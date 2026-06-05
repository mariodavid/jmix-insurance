package com.insurance.common.test_support_ui;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.JmixIntegerField;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.grid.DataGridItems;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.testassist.UiTestUtils;
import io.jmix.flowui.view.View;
import java.util.List;

public class UiTestSupport {

  // Recursively search for a button by its exact translated text
  public static JmixButton findButtonByText(Component parent, String text) {
    if (parent instanceof JmixButton button && text.equals(button.getText())) {
      return button;
    }
    for (Component child : parent.getChildren().toList()) {
      JmixButton found = findButtonByText(child, text);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  // Lookup a DataGrid and cast it safely
  @SuppressWarnings("unchecked")
  public static <T> DataGrid<T> getDataGrid(View<?> view, String id) {
    return (DataGrid<T>) UiTestUtils.getComponent(view, id);
  }

  // Lookup a DataGrid and extract all its current items as a list
  public static <T> List<T> getGridItems(View<?> view, String id) {
    DataGrid<T> grid = getDataGrid(view, id);
    DataGridItems<T> items = grid.getItems();
    if (items == null) {
      return List.of();
    }
    return items.getItems().stream().toList();
  }

  // Lookup a TypedTextField and cast it safely
  @SuppressWarnings("unchecked")
  public static <T> TypedTextField<T> getTextField(View<?> view, String id) {
    return (TypedTextField<T>) UiTestUtils.getComponent(view, id);
  }

  // Lookup an EntityComboBox and cast it safely
  @SuppressWarnings("unchecked")
  public static <T> EntityComboBox<T> getComboBox(View<?> view, String id) {
    return (EntityComboBox<T>) UiTestUtils.getComponent(view, id);
  }

  // Lookup a JmixSelect and cast it safely
  @SuppressWarnings("unchecked")
  public static <T> JmixSelect<T> getSelect(View<?> view, String id) {
    return (JmixSelect<T>) UiTestUtils.getComponent(view, id);
  }

  // Lookup a TypedDatePicker and cast it safely
  @SuppressWarnings("unchecked")
  public static <T extends Comparable<? super T>> TypedDatePicker<T> getDatePicker(
      View<?> view, String id) {
    return (TypedDatePicker<T>) UiTestUtils.getComponent(view, id);
  }

  // Lookup a JmixIntegerField and cast it safely
  public static JmixIntegerField getIntegerField(View<?> view, String id) {
    return (JmixIntegerField) UiTestUtils.getComponent(view, id);
  }

  // Lookup a JmixPasswordField and cast it safely
  public static JmixPasswordField getPasswordField(View<?> view, String id) {
    return (JmixPasswordField) UiTestUtils.getComponent(view, id);
  }
}
