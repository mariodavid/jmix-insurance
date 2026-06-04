package com.insurance.security.security;

import com.insurance.security.api.security.FullAccessRole;
import com.insurance.security.entity.User;
import io.jmix.securitydata.user.AbstractDatabaseUserRepository;
import java.util.Collection;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Primary
@Component("security_DatabaseUserRepository")
public class DatabaseUserRepository extends AbstractDatabaseUserRepository<User> {

  @Override
  protected Class<User> getUserClass() {
    return User.class;
  }

  @Override
  protected void initSystemUser(final User systemUser) {
    final Collection<GrantedAuthority> authorities =
        getGrantedAuthoritiesBuilder().addResourceRole(FullAccessRole.CODE).build();
    systemUser.setAuthorities(authorities);
  }

  @Override
  protected void initAnonymousUser(final User anonymousUser) {}
}
