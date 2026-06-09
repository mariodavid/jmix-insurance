package com.insurance.common.test_support_ui.architecture.ui;

import com.insurance.common.test_support.architecture.lang.ArchitectureConditions;
import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.ViewDescriptor;

public final class JmixViewDescriptorRuleParts {

  private JmixViewDescriptorRuleParts() {}

  public static DescribedPredicate<JavaClass> jmixViewOrFragmentControllers() {
    return DescribedPredicate.describe(
        "Jmix view or fragment controllers",
        javaClass ->
            javaClass.isAnnotatedWith(ViewDescriptor.class)
                || javaClass.isAnnotatedWith(FragmentDescriptor.class));
  }

  public static ArchCondition<JavaClass> referenceExistingDescriptorFiles() {
    return ArchitectureConditions.checkCondition(
        "reference existing descriptor files",
        (javaClass, events) -> {
          if (!JmixUiDescriptorScanner.descriptorFileExists(javaClass)) {
            String path = JmixUiDescriptorScanner.getDescriptorPath(javaClass);
            events.add(
                SimpleConditionEvent.violated(
                    javaClass,
                    String.format("Descriptor file \"%s\" not found in resources", path)));
          }
        });
  }

  public static ArchCondition<JavaClass> referenceExistingDescriptorTargets() {
    return ArchitectureConditions.checkCondition(
        "reference existing descriptor targets",
        (javaClass, events) ->
            JmixUiDescriptorScanner.validateBindings(javaClass)
                .forEach(error -> events.add(SimpleConditionEvent.violated(javaClass, error))));
  }
}
