package com.insurance.security.api.security;

import static org.assertj.core.api.Assertions.assertThat;

import io.jmix.security.model.EntityAttributePolicyAction;
import io.jmix.security.model.EntityPolicyAction;
import io.jmix.security.role.annotation.EntityAttributePolicy;
import io.jmix.security.role.annotation.EntityPolicy;
import io.jmix.security.role.annotation.SpecificPolicy;
import io.jmix.securityflowui.role.annotation.MenuPolicy;
import io.jmix.securityflowui.role.annotation.ViewPolicy;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;

class FullAccessRoleTest {

  @Test
  void fullAccessRoleDeclaresWildcardEntityAttributeViewMenuAndSpecificPolicies()
      throws NoSuchMethodException {
    Method method = FullAccessRole.class.getDeclaredMethod("fullAccess");

    EntityPolicy entityPolicy = method.getAnnotation(EntityPolicy.class);
    EntityAttributePolicy attributePolicy = method.getAnnotation(EntityAttributePolicy.class);
    ViewPolicy viewPolicy = method.getAnnotation(ViewPolicy.class);
    MenuPolicy menuPolicy = method.getAnnotation(MenuPolicy.class);
    SpecificPolicy specificPolicy = method.getAnnotation(SpecificPolicy.class);

    assertThat(entityPolicy.entityName()).isEqualTo("*");
    assertThat(entityPolicy.actions()).containsExactly(EntityPolicyAction.ALL);
    assertThat(attributePolicy.entityName()).isEqualTo("*");
    assertThat(attributePolicy.attributes()).containsExactly("*");
    assertThat(attributePolicy.action()).isEqualTo(EntityAttributePolicyAction.MODIFY);
    assertThat(viewPolicy.viewIds()).containsExactly("*");
    assertThat(menuPolicy.menuIds()).containsExactly("*");
    assertThat(specificPolicy.resources()).containsExactly("*");
  }
}
