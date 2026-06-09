package com.insurance.common.test_support.architecture.rules.jmix;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.insurance.common.test_support.architecture.project.ArchitectureLayer;
import com.tngtech.archunit.core.domain.JavaCall;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

/**
 * Event listener safety rules.
 *
 * <p>Ensures that core event listeners executing business logic or persistence actions run in an
 * authenticated context, that lifecycle event listeners do not trigger recursive saves, and that
 * public API events do not expose core internal entities.
 */
public class JmixEventListenerSafetyRules {

  private static ArchCondition<JavaMethod> beAuthenticatedIfUsingDataManagerOrServices() {
    return new ArchCondition<JavaMethod>(
        "be annotated with @Authenticated if they use DataManager or services") {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        if (method.isAnnotatedWith("io.jmix.core.security.Authenticated")) {
          return;
        }
        boolean usesDataManagerOrService = false;
        for (JavaCall<?> call : method.getCallsFromSelf()) {
          JavaClass target = call.getTarget().getOwner();
          if (target.getName().equals("io.jmix.core.DataManager")
              || target.isAnnotatedWith("org.springframework.stereotype.Service")
              || target.getPackageName().contains(".core.service")) {
            usesDataManagerOrService = true;
            break;
          }
        }
        if (usesDataManagerOrService) {
          String message =
              String.format(
                  "Method %s in core module uses DataManager or service but is not @Authenticated",
                  method.getFullName());
          events.add(SimpleConditionEvent.violated(method, message));
        }
      }
    };
  }

  private static ArchCondition<JavaMethod> notCallDataManagerSave() {
    return new ArchCondition<JavaMethod>("not call DataManager.save()") {
      @Override
      public void check(JavaMethod method, ConditionEvents events) {
        for (JavaCall<?> call : method.getCallsFromSelf()) {
          JavaClass target = call.getTarget().getOwner();
          if (target.getName().equals("io.jmix.core.DataManager")
              && call.getTarget().getName().equals("save")) {
            String message =
                String.format("Method %s calls DataManager.save()", method.getFullName());
            events.add(SimpleConditionEvent.violated(method, message));
          }
        }
      }
    };
  }

  @ArchTest
  public static final ArchRule eventListenersInCoreAreAuthenticated =
      methods()
          .that()
          .areAnnotatedWith("org.springframework.context.event.EventListener")
          .and()
          .areDeclaredInClassesThat()
          .resideInAPackage(ArchitectureLayer.CORE.anyInsurancePackage())
          .should(beAuthenticatedIfUsingDataManagerOrServices())
          .as(
              "event listeners in core modules using DataManager or services must be authenticated");

  @ArchTest
  public static final ArchRule entitySavingEventsDoNotTriggerSave =
      methods()
          .that()
          .areAnnotatedWith("org.springframework.context.event.EventListener")
          .and()
          .haveRawParameterTypes("io.jmix.core.event.EntitySavingEvent")
          .should(notCallDataManagerSave())
          .as("EntitySavingEvent listeners should not call DataManager.save()")
          .allowEmptyShould(true);

  @ArchTest
  public static final ArchRule entityLoadingEventsDoNotTriggerSave =
      methods()
          .that()
          .areAnnotatedWith("org.springframework.context.event.EventListener")
          .and()
          .haveRawParameterTypes("io.jmix.core.event.EntityLoadingEvent")
          .should(notCallDataManagerSave())
          .as("EntityLoadingEvent listeners should not call DataManager.save()")
          .allowEmptyShould(true);

  @ArchTest
  public static final ArchRule apiEventClassesDoNotDependOnCoreEntities =
      noClasses()
          .that()
          .resideInAPackage(ArchitectureLayer.API.anyInsuranceSubpackage("event.."))
          .should()
          .dependOnClassesThat()
          .resideInAPackage(ArchitectureLayer.CORE.anyInsuranceSubpackage("entity.."))
          .as("API event classes should not depend on core entities");
}
