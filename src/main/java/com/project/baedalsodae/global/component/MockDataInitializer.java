package com.project.baedalsodae.global.component;

import java.sql.Connection;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile({"prod", "local"})
@RequiredArgsConstructor
@Order(1)
public class MockDataInitializer implements ApplicationRunner {

    private final DataSource dataSource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(
                    conn,
                    new EncodedResource(new ClassPathResource("data.sql")),
                    false,
                    false,
                    "--",
                    ";",
                    "/*",
                    "*/");
            log.info("[MockDataInitializer] data.sql 실행 완료");
        } catch (Exception e) {
            log.warn("[MockDataInitializer] data.sql 실행 중 오류 (이미 데이터 있을 수 있음): {}", e.getMessage());
        }
    }
}
