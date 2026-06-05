package com.insurance.common.test_support_ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HtmlContainer;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.data.provider.DataProvider;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datepicker.TypedDatePicker;
import io.jmix.flowui.component.select.JmixSelect;
import io.jmix.flowui.component.textfield.JmixIntegerField;
import io.jmix.flowui.component.textfield.JmixPasswordField;
import io.jmix.flowui.component.textfield.TypedTextField;
import io.jmix.flowui.data.items.ContainerDataProvider;
import io.jmix.flowui.util.OperationResult;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.View;
import java.time.LocalDate;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unchecked")
public class FormInteractions {

  private final View<?> view;

  public FormInteractions(View<?> view) {
    this.view = view;
  }

  public static FormInteractions of(View<?> view) {
    return new FormInteractions(view);
  }

  @Nullable
  public TypedTextField<Object> textField(String componentId) {
    return (TypedTextField<Object>) getComponent(componentId);
  }

  @Nullable
  public JmixIntegerField integerField(String componentId) {
    return (JmixIntegerField) getComponent(componentId);
  }

  @Nullable
  public JmixPasswordField passwordField(String componentId) {
    return (JmixPasswordField) getComponent(componentId);
  }

  @Nullable
  public TypedDatePicker<LocalDate> datePickerField(String componentId) {
    return (TypedDatePicker<LocalDate>) getComponent(componentId);
  }

  @Nullable
  public JmixSelect<Object> selectField(String componentId) {
    return (JmixSelect<Object>) getComponent(componentId);
  }

  @Nullable
  public HtmlContainer htmlContainerField(String componentId) {
    return (HtmlContainer) getComponent(componentId);
  }

  public <T> EntityComboBox<T> entityComboBoxField(String componentId, Class<T> entityClass) {
    return (EntityComboBox<T>) getComponent(componentId);
  }

  @Nullable
  private Component getComponent(String componentId) {
    return UiComponentUtils.findComponent(view, componentId).orElse(null);
  }

  @NotNull
  private Component getRequiredComponent(String componentId) {
    return UiComponentUtils.findComponent(view, componentId)
        .orElseThrow(
            () ->
                new IllegalArgumentException(
                    "Component '%s' not found in view '%s'".formatted(componentId, view.getId())));
  }

  @Nullable
  public Button button(String buttonId) {
    return (Button) getComponent(buttonId);
  }

  public void setTextFieldValue(String componentId, Object value) {
    ((TypedTextField<Object>) getRequiredComponent(componentId)).setTypedValue(value);
  }

  public void setIntegerFieldValue(String componentId, Integer value) {
    ((JmixIntegerField) getRequiredComponent(componentId)).setValue(value);
  }

  public void setPasswordFieldValue(String componentId, String value) {
    ((JmixPasswordField) getRequiredComponent(componentId)).setValue(value);
  }

  public void setDatePickerValue(String componentId, LocalDate value) {
    ((TypedDatePicker<LocalDate>) getRequiredComponent(componentId)).setValue(value);
  }

  public void setSelectValue(String componentId, Object value) {
    ((JmixSelect<Object>) getRequiredComponent(componentId)).setValue(value);
  }

  public OperationResult saveForm() {
    if (view instanceof StandardDetailView<?> detailView) {
      return detailView.closeWithSave();
    }
    throw new UnsupportedOperationException(
        "View is not a StandardDetailView, cannot saveForm directly.");
  }

  public <T> List<T> getEntityComboBoxValues(String componentId, Class<T> entityClass) {
    EntityComboBox<T> entityComboBox = entityComboBoxField(componentId, entityClass);
    return getEntityComboBoxValues(entityComboBox);
  }

  @NotNull
  private <T> List<T> getEntityComboBoxValues(EntityComboBox<T> entityComboBox) {
    DataProvider<T, ?> dataProvider = entityComboBox.getDataProvider();
    if (dataProvider instanceof ContainerDataProvider containerDataProvider) {
      return containerDataProvider.getContainer().getItems();
    } else {
      throw new UnsupportedOperationException("Unsupported DataProvider: " + dataProvider);
    }
  }

  public <T> void setEntityComboBoxFieldValue(String componentId, T entity, Class<T> entityClass) {
    EntityComboBox<T> comboBox = entityComboBoxField(componentId, entityClass);
    T entityFromComboBox =
        getEntityComboBoxValues(comboBox).stream()
            .filter(t -> t.equals(entity))
            .findFirst()
            .orElseThrow();
    comboBox.setValue(entityFromComboBox);
  }

  public <T> void setEntityComboBoxValue(String componentId, T value, Class<T> entityClass) {
    ((EntityComboBox<T>) getRequiredComponent(componentId)).setValue(value);
  }

  public boolean isVisible(String componentId) {
    return UiComponentUtils.isComponentVisible(UiComponentUtils.getComponent(view, componentId));
  }

  public void click(String componentId) {
    Button btn = (Button) getRequiredComponent(componentId);
    btn.click();
  }

  public void setFieldValueByLabel(String labelText, Object value) {
    Component component = findComponentByLabel(view, labelText);
    if (component == null) {
      throw new IllegalArgumentException("Field with label '%s' not found".formatted(labelText));
    }
    if (component instanceof TypedTextField<?> textField) {
      ((TypedTextField<Object>) textField).setTypedValue(value);
    } else if (component instanceof JmixIntegerField integerField) {
      integerField.setValue((Integer) value);
    } else if (component instanceof JmixPasswordField passwordField) {
      passwordField.setValue((String) value);
    } else if (component instanceof TypedDatePicker<?> datePicker) {
      datePicker.setValue((LocalDate) value);
    } else if (component instanceof JmixSelect<?> select) {
      ((JmixSelect<Object>) select).setValue(value);
    } else if (component instanceof EntityComboBox<?> comboBox) {
      ((EntityComboBox<Object>) comboBox).setValue(value);
    } else {
      throw new UnsupportedOperationException(
          "Component of type %s does not support direct setFieldValueByLabel"
              .formatted(component.getClass().getName()));
    }
  }

  @Nullable
  public Object getFieldValueByLabel(String labelText) {
    Component component = findComponentByLabel(view, labelText);
    if (component == null) {
      throw new IllegalArgumentException("Field with label '%s' not found".formatted(labelText));
    }
    if (component instanceof TypedTextField<?> textField) {
      return textField.getTypedValue();
    } else if (component instanceof JmixIntegerField integerField) {
      return integerField.getValue();
    } else if (component instanceof JmixPasswordField passwordField) {
      return passwordField.getValue();
    } else if (component instanceof TypedDatePicker<?> datePicker) {
      return datePicker.getValue();
    } else if (component instanceof JmixSelect<?> select) {
      return select.getValue();
    } else if (component instanceof EntityComboBox<?> comboBox) {
      return comboBox.getValue();
    } else {
      throw new UnsupportedOperationException(
          "Component of type %s does not support direct getFieldValueByLabel"
              .formatted(component.getClass().getName()));
    }
  }

  @Nullable
  private Component findComponentByLabel(Component parent, String labelText) {
    if (parent instanceof com.vaadin.flow.component.HasLabel hasLabel
        && labelText.equals(hasLabel.getLabel())) {
      return parent;
    }
    for (Component child : parent.getChildren().toList()) {
      Component found = findComponentByLabel(child, labelText);
      if (found != null) {
        return found;
      }
    }
    return null;
  }
}
