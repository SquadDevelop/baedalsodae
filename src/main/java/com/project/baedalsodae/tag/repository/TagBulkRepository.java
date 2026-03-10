package com.project.baedalsodae.tag.repository;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class TagBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void bulkInsertIgnore(List<String> tagNames) {
        return;

        //        if (tagNames == null || tagNames.isEmpty()) return;
        //
        //        String sql =
        //                """
        //            INSERT INTO baedalsodae.p_tag (name)
        //            VALUES (?)
        //            ON CONFLICT (name) DO NOTHING
        //        """;
        //
        //        jdbcTemplate.batchUpdate(
        //                sql, tagNames, tagNames.size(), (ps, name) -> ps.setString(1, name));
    }
}
