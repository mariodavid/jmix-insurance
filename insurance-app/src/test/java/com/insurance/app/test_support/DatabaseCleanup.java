package com.insurance.app.test_support;

import com.insurance.account.core.entity.Account;
import com.insurance.account.core.entity.AccountDocument;
import com.insurance.partner.core.entity.Partner;
import com.insurance.policy.core.entity.Policy;
import com.insurance.quote.core.entity.Quote;
import io.jmix.core.Metadata;
import io.jmix.core.MetadataTools;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseCleanup {

  @Autowired private DataSource dataSource;

  @Autowired private Metadata metadata;

  @Autowired private MetadataTools metadataTools;

  public void removeAllEntities() {
    JdbcTemplate jdbc = new JdbcTemplate(dataSource);
    delete(AccountDocument.class, jdbc);
    delete(Account.class, jdbc);
    delete(Policy.class, jdbc);
    delete(Quote.class, jdbc);
    delete(Partner.class, jdbc);
  }

  private <T> void delete(Class<T> entityClass, JdbcTemplate jdbc) {
    String table = metadataTools.getDatabaseTable(metadata.getClass(entityClass));
    jdbc.update("DELETE FROM " + table);
  }
}
