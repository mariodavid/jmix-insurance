package com.insurance.common.test_support.architecture.lang;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.AbstractClassesTransformer;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ClassesTransformer;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

public final class ArchitectureConditions {

  private ArchitectureConditions() {}

  public static <T> ArchCondition<T> notExist() {
    return new ArchCondition<>("not exist") {

      @Override
      public void check(T item, ConditionEvents events) {
        events.add(SimpleConditionEvent.violated(item, item.toString()));
      }
    };
  }

  public static <T> ArchCondition<T> checkCondition(
      String description, java.util.function.BiConsumer<T, ConditionEvents> checkFunction) {
    return new ArchCondition<>(description) {
      @Override
      public void check(T item, ConditionEvents events) {
        checkFunction.accept(item, events);
      }
    };
  }

  public static <T> ClassesTransformer<T> transformClasses(
      String description, java.util.function.Function<JavaClasses, Iterable<T>> transformFunction) {
    return new AbstractClassesTransformer<>(description) {
      @Override
      public Iterable<T> doTransform(JavaClasses classes) {
        return transformFunction.apply(classes);
      }
    };
  }
}
