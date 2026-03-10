package com.project.baedalsodae.order.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.entity.Address;
import com.project.baedalsodae.order.entity.Order;
import com.project.baedalsodae.order.entity.enums.OrderStatus;
import com.project.baedalsodae.order.repository.OrderRepository;
import com.project.baedalsodae.order.service.OrderService;
import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.StoreCategory;
import com.project.baedalsodae.store.repository.StoreCategoryRepository;
import com.project.baedalsodae.store.repository.StoreRepository;
import com.project.baedalsodae.user.entity.UserRole;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

/**
 * 동시성 테스트 - 로컬 PostgreSQL 환경에서만 실행 GitHub Actions CI 환경에서는 @Disabled 처리 실행 전 로컬 PostgreSQL 서버가 실행
 * 중이어야 합니다. application-test-concurrency.yml 설정 필요
 */
@Disabled("동시성 테스트 - 로컬 PostgreSQL 환경에서만 수동 실행")
@SpringBootTest
@ActiveProfiles("test-concurrency")
class OrderCancelConcurrencyTest {

    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private StoreCategoryRepository storeCategoryRepository;

    private UUID orderId;
    private UUID managerId;

    @BeforeEach
    void setUp() {
        StoreCategory category = StoreCategory.createStoreCategory("한식", "한식 카테고리");
        StoreCategory savedCategory = storeCategoryRepository.save(category);

        Store store =
                Store.createStore(
                        UUID.randomUUID(),
                        savedCategory,
                        "테스트 가게",
                        "123-45-67890",
                        "010-1234-5678",
                        Address.createAddress(
                                "11", "서울특별시",
                                "11680", "강남구",
                                "1168010100", "역삼동",
                                "서울 강남구 테헤란로 1", "1층"),
                        "테스트 가게 설명");
        Store savedStore = storeRepository.save(store);

        Order order =
                Order.create(
                        UUID.randomUUID(),
                        "테스트고객",
                        "010-0000-0000",
                        savedStore,
                        UUID.randomUUID(),
                        "서울 강남구 테헤란로 1",
                        "ORD-20260310-0001",
                        null,
                        null,
                        BigDecimal.valueOf(10000),
                        BigDecimal.valueOf(3000),
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(13000));
        order.request(); // CREATED → REQUESTED
        orderId = orderRepository.save(order).getId();

        managerId = UUID.randomUUID();
    }

    @Test
    @DisplayName("주문 취소 대기 동시 요청 10건 - 낙관적 락으로 단 1건만 성공")
    void cancelRequestOrder_concurrency_optimisticLock_onlyOneSucceeds() throws InterruptedException {
        // given
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger optimisticLockFailCount = new AtomicInteger(0);
        AtomicInteger statusConflictFailCount = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            executor.submit(
                    () -> {
                        try {
                            orderService.cancelRequestOrder(
                                    managerId, UserRole.MANAGER, null, orderId, "동시성 테스트");
                            successCount.incrementAndGet();
                        } catch (ObjectOptimisticLockingFailureException e) {
                            // 낙관적 락 충돌: 동일 버전을 읽은 스레드가 먼저 커밋된 변경을 덮어쓰려 할 때 발생
                            optimisticLockFailCount.incrementAndGet();
                        } catch (BusinessException e) {
                            // 상태 충돌: 선행 트랜잭션 커밋 후 DB를 새로 읽은 스레드가 이미 CANCEL_REQUESTED 상태를 감지
                            statusConflictFailCount.incrementAndGet();
                        } finally {
                            latch.countDown();
                        }
                    });
        }
        latch.await();
        executor.shutdown();

        // then
        Order order = orderRepository.findById(orderId).orElseThrow();

        System.out.println("성공: " + successCount.get());
        System.out.println("낙관적 락 실패: " + optimisticLockFailCount.get());
        System.out.println("상태 충돌 실패: " + statusConflictFailCount.get());
        System.out.println("최종 주문 상태: " + order.getStatus());
        System.out.println("최종 version: " + order.getVersion());

        assertThat(successCount.get() + optimisticLockFailCount.get() + statusConflictFailCount.get())
                .isEqualTo(threadCount);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(order.getVersion()).isEqualTo(1L);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCEL_REQUESTED);
    }
}
