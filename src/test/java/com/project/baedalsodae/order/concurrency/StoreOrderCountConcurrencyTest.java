package com.project.baedalsodae.order.concurrency;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.baedalsodae.store.entity.Store;
import com.project.baedalsodae.store.entity.StoreCategory;
import com.project.baedalsodae.store.repository.StoreCategoryRepository;
import com.project.baedalsodae.store.repository.StoreRepository;
import com.project.baedalsodae.global.common.entity.Address;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import com.project.baedalsodae.store.service.StoreCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

/**
 * 동시성 테스트 - 로컬 PostgreSQL 환경에서만 실행
 * GitHub Actions CI 환경에서는 @Disabled 처리
 * 실행 전 로컬 PostgreSQL 서버가 실행 중이어야 합니다.
 * application-test-concurrency.yml 설정 필요
 */
@Disabled("동시성 테스트 - 로컬 PostgreSQL 환경에서만 수동 실행")
@SpringBootTest
@ActiveProfiles("test-concurrency")
class StoreOrderCountConcurrencyTest {

	@Autowired
	private StoreCommandService storeService;
	@Autowired
	private StoreRepository storeRepository;
	@Autowired
	private StoreCategoryRepository storeCategoryRepository;

	private UUID storeId1;

	@BeforeEach
	void setUp() {
		// StoreCategory 직접 생성
		StoreCategory category = StoreCategory.createStoreCategory("한식", "한식 카테고리");
		StoreCategory savedCategory = storeCategoryRepository.save(category);

		Store store1 = Store.createStore(
				UUID.randomUUID(),
				savedCategory,
				"테스트 가게1",
				"123-45-67890",
				"010-1234-5678",
				Address.createAddress(
						"11", "서울특별시",
						"11680", "강남구",
						"1168010100", "역삼동",
						"서울 강남구 테헤란로 1", "1층"),
				"테스트 가게1 설명");

		storeId1 = storeRepository.save(store1).getId();
	}

	@Test
	@DisplayName("배달 완료 동시 요청 10건 - 낙관적 락 재시도로 orderCount 정합성 보장")
	void incrementOrderCount_concurrency_optimisticLock_success() throws InterruptedException {
		// given
		int threadCount = 10;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);

		// when
		for (int i = 0; i < threadCount; i++) {
			executor.submit(() -> {
				try {
					storeService.incrementOrderCount(storeId1);
					successCount.incrementAndGet();
				} catch (ObjectOptimisticLockingFailureException e) {
					// EventPoller MAX_RETRY 초과 시 발생
					failCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}
		latch.await();
		executor.shutdown();

		// then
		Store store = storeRepository.findById(storeId1).orElseThrow();

		System.out.println("성공: " + successCount.get() + ", 실패: " + failCount.get());
		System.out.println("최종 orderCount: " + store.getOrderCount());

		// 성공 + 실패 = 전체 요청
		assertThat(successCount.get() + failCount.get()).isEqualTo(threadCount);
		// 최종 orderCount는 성공 횟수와 일치
		assertThat(store.getOrderCount()).isEqualTo(successCount.get());
	}
}