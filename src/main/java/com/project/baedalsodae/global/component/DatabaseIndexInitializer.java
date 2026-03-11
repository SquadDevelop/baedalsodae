package com.project.baedalsodae.global.component;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
@DependsOn("dataSourceScriptDatabaseInitializer")
public class DatabaseIndexInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute(
                """
      CREATE UNIQUE INDEX IF NOT EXISTS uq_menu_category_name_active
      ON baedalsodae.p_menu_category (store_id, name)
      WHERE is_deleted = false
      """);
    }
}
