package com.project.baedalsodae.cart.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.baedalsodae.BaedalsodaeApplication;
import com.project.baedalsodae.cart.entity.Cart;
import com.project.baedalsodae.cart.entity.QCart;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Disabled("임시 비활성화")
@DataJpaTest(showSql = true)
@Import({BaedalsodaeApplication.class, CartQueryDslSmokeTest.QueryDslConfig.class})
@ActiveProfiles("test")
class CartQueryDslSmokeTest {

    @Autowired JPAQueryFactory queryFactory;

    @Autowired EntityManager em;

    @Autowired CartRepository cartRepository;

    UUID savedUserId;

    @BeforeEach
    void setUp() {
        savedUserId = UUID.randomUUID();
    }

    @Test
    @DisplayName("QueryDSL - JPAQueryFactory 정상 동작 확인 (빈 결과 반환)")
    void querydsl_smoke_test() {
        QCart cart = QCart.cart;

        List<Cart> result =
                queryFactory.selectFrom(cart).where(cart.userId.eq(savedUserId)).fetch();

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("QueryDSL - fetchOne 정상 동작 확인")
    void querydsl_fetch_one_smoke_test() {
        QCart cart = QCart.cart;

        Cart result = queryFactory.selectFrom(cart).where(cart.userId.eq(savedUserId)).fetchOne();

        assertThat(result).isNull();
    }

    @Configuration
    static class QueryDslConfig {
        @Bean
        JPAQueryFactory jpaQueryFactory(EntityManager em) {
            return new JPAQueryFactory(em);
        }
    }
}
