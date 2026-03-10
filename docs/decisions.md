# 기술적 의사결정 상세

---

## 1단계 — 프로젝트 설계 

### 와이어프레임 기반 팀 얼라인 (팀 전체)

**목적**: 개발 착수 전 기능 범위와 화면 흐름을 시각화하여 도메인별 요구사항 오해 방지

**효과**:
- API 설계 기준 공유 및 사전 합의
- 팀원 간 도메인 경계 명확화
- 불필요한 재작업 최소화

---

### RESTful API 설계 의사결정 (팀 전체)

> 상세 회의록: [RESTful API 설계 의사결정 (한병두)](https://www.notion.so/teamsparta/RESTful-API-3142dc3ef51480f68ec9e68ecbf50c92)

**문제**: 초기에 Store 조회 API를 역할(Role) 기준으로 분리 설계

```
/api/v1/customers/stores/{storeId}
/api/v1/owners/stores/{storeId}
```
 "URI는 Actor가 아닌 Resource 기준이어야 한다. MSA로 분리할 때 어떤 도메인 서비스로 나눌 것인가?"
→ `customers`, `owners`는 행위자(Actor)이지 Resource가 아님. Store는 고객이 보든 점주가 보든 동일한 하나의 Resource

**기각된 대안**: `/stores/{storeId}/owner` — `owner`가 Resource가 아닌 Role이므로 RESTful 원칙 위배

**최종 합의**: `/stores` + `/me/stores` 구조로 분리
```
GET /api/v1/stores/{storeId}        ← 고객용 (공개)
GET /api/v1/me/stores/{storeId}     ← 점주용 (현재 로그인 사용자 기준)
PUT /api/v1/me/stores/{storeId}
DELETE /api/v1/me/stores/{storeId}
```
- `/me/`는 현재 인증된 사용자의 소유 리소스를 의미, Role은 URI가 아닌 Authorization으로 처리
- admin 기능은 `/admin/` 네임스페이스로 분리
- 상태 변경 `PATCH`, 전체 교체 `PUT`, 성공 응답 코드 통일 (생성 `201`, 조회 `200`, 삭제 `204`)

---

## 2단계 — 공통 인프라 구축 (개발 초반)

### GlobalException + 공통 응답 처리 (신혜원)

**설계**: `ErrorCode` / `SuccessCode` enum으로 응답 형식 통일

**진화 과정**: 초기에는 `ErrorCode`만 존재 → 성공 응답도 일관된 포맷이 필요해지면서 `SuccessCode`를 추가하여 요청/응답 양방향 포맷을 통일

**효과**:
- 팀원 전체가 동일한 응답 포맷 사용
- 예외 발생 지점과 응답 변환 로직 분리

---

### JWT 기반 인증 설계 (이현빈)

> 정리 문서: [Spring Security + JWT 기반 인증 전략 정리 (이현빈)](https://www.notion.so/teamsparta/Spring-Security-JWT-31c2dc3ef51480f09b44ec0cf1cd2fff)

**처리 방식 분리**:
- JWT 토큰 검증: `JwtAuthorizationFilter` (필터 레이어)
- 로그인 처리: `AuthController` (컨트롤러 레이어)
- Spring Security의 `UsernamePasswordAuthenticationFilter`에 위임하지 않고 명시적 분리

**accessToken 페이로드 구성**: `userId`, `role` 포함 → DB 조회 없이 인가 처리 가능

---

### refreshToken + Redis 블랙리스트 (이현빈)

**무상태성 유지 전략**:
- `accessToken` 단기 만료 (1시간) + `refreshToken` 장기 만료 (7일)
- refreshToken은 Redis에 저장, 재발급 시 검증

**로그아웃 처리**:
- 로그아웃된 `accessToken`을 Redis 블랙리스트에 등록
- 남은 유효기간만큼 TTL 설정 → 만료 후 자동 제거로 메모리 관리

---

### 카카오 주소 API 도입 (이현빈 아이디어 · 신혜원 구현)

**문제**: 사용자가 직접 입력하는 주소는 정합성 보장 불가 (오타, 비표준 형식)

**선택**: 카카오 주소 검색 API로 표준화된 시도·시군구 코드 확보 후 DB 저장

**협업 과정**: 이현빈님이 카카오 API 도입을 제안하고, 신혜원님이 API 연동 및 테스트 코드를 구현

**효과**:
- 전국 지역 기반 서비스 가능 지역 필터링에 활용
- `AllowedRegion`의 `sigunguCode`와 매칭하여 서비스 가능 여부 판단

---

## 3단계 — 도메인 개발 

### 삭제 후 재생성 로직 — 메뉴 태그 / 가게 운영시간 (하지혜)

**문제**: 태그·운영시간 수정 시 변경된 항목만 특정하기 어려움 (순서 변경 + 추가 + 삭제 혼재)

**선택**: 기존 연관 데이터 전체 삭제 후 재삽입 (`orphanRemoval = true` + `cascade = ALL`)

**이유**: 변경 감지 로직 복잡도 대비 트랜잭션 단위가 작아 실질적 성능 차이 미미

관련 PR: [#46](https://github.com/SquadDevelop/baedalsodae/pull/46), [#120](https://github.com/SquadDevelop/baedalsodae/pull/120)

---

### Soft Delete + PostgreSQL Partial Index (하지혜)

**문제**: soft delete(`deleted_at IS NOT NULL`) 환경에서 `UNIQUE` 제약이 삭제된 데이터와 충돌

**JPA 기본 `@UniqueConstraint`의 한계**: 삭제 여부를 조건으로 거는 부분 인덱스 불가

**해결**: PostgreSQL Partial Index 직접 적용

```sql
CREATE UNIQUE INDEX idx_unique_active
ON p_menu_item (menu_category_id, name)
WHERE deleted_at IS NULL;
```

---

### 비관적 락 도입 — 메뉴 순서 변경 (하지혜)

**문제**: 메뉴 카테고리/아이템 순서 변경 시 여러 row를 한 번에 수정 → 동시 요청 시 race condition 발생 가능

**낙관적 락을 선택하지 않은 이유**:
- 여러 row를 동시에 수정하는 구조에서 충돌 발생 시 재시도 로직이 복잡해짐
- 관리자 전용 기능이라 요청 빈도가 낮아 lock 대기로 인한 성능 부담이 허용 가능한 수준

**결론**: 비관적 락(`PESSIMISTIC_WRITE`)으로 조회 시점에 선점, 정합성 보장

관련 PR: [#52](https://github.com/SquadDevelop/baedalsodae/pull/52), [#85](https://github.com/SquadDevelop/baedalsodae/pull/85)

---

### 주문 취소 동시성 문제 (정재빈)

**시나리오**: 고객과 사장이 동시에 같은 주문에 취소 요청

**문제**: 두 요청이 동시에 `REQUESTED` 상태를 읽고 각각 취소 처리 시 중복 상태 전이 발생

**해결**: 주문 상태 변경 시 낙관적 락(`@Version`) 적용
- 충돌 시 `OptimisticLockException` → 클라이언트에 재시도 유도
- 단일 row 변경이라 재시도 로직이 간단해 낙관적 락이 적합

---

## 4단계 — 주문/결제 프로세스

> 관련 문서: [주문, 결제에 관해서](https://www.notion.so/teamsparta/3192dc3ef51480fabc9dc98c57668c9b)

### 아웃박스 패턴 + 이벤트 폴링 스케줄러 (정재빈 · 한병두)

**문제**: 주문 상태 변경 후 결제/알림 이벤트 발행 시 트랜잭션 커밋 전에 이벤트가 처리될 수 있음

**`@TransactionalEventListener`의 한계**:
- `AFTER_COMMIT` 시점에 발행하면 해당 트랜잭션 밖이라 예외 처리·롤백 연동이 어려움

**선택**: 아웃박스 패턴
1. 도메인 이벤트를 DB `event` 테이블에 함께 저장 (같은 트랜잭션)
2. 스케줄러가 주기적으로 미처리 이벤트 폴링 후 발행

**결과**: 이벤트 유실 없이 최소 1회 보장 (at-least-once delivery)

---

### WireMock 기반 가상 PG사 구현 (정재빈 · 한병두)

**문제**: 실제 PG사 연동 없이 결제 흐름(승인·취소)을 개발·테스트 환경에서 검증하기 어려움

**선택**: WireMock을 Docker Compose로 띄워 가상 PG 서버로 운용

```
결제 요청 → WireMockPGClient → WireMock 서버 (포트 8999) → 매핑된 응답 반환
```

- `pg-pay.json` — 금액·결제수단 검증 후 승인 응답 반환
- `pg-cancel.json` — 취소 요청 처리 응답 반환
- `WireMockPGClient`가 `@Primary`로 등록되어 실제 PG 클라이언트와 인터페이스 공유

**효과**: 실제 PG사 계약·비용 없이 결제 승인·취소 전 흐름을 실제와 동일하게 검증 가능

---

### 롱폴링 vs SSE — 주문 상태 실시간 조회 (정재빈, 한병두)

| 방식 | 장점 | 단점 |
|---|---|---|
| 롱폴링 | 구현 단순, HTTP 호환성 우수 | 연결 재수립 오버헤드 |
| SSE | 서버 → 클라이언트 단방향 스트림, 연결 유지 효율적 | 브라우저 연결 수 제한 |

**선택**: 롱폴링 — 현재 배포 환경(HAProxy)에서 SSE 연결 유지 관리보다 단순성 우선

**의사결정 과정**: 정재빈님 주도로 방식 비교 후 팀 전체가 논의에 참여하여 최종 결정

---

## 5단계 — 성능 최적화

> 관련 문서: [QueryDSL 의사결정 과정](https://www.notion.so/teamsparta/QueryDSL-3192dc3ef51480a994b3efb35e973e9f) · [JPQL vs NativeQuery vs QueryDSL](https://www.notion.so/teamsparta/JPQL-vs-NativeQuery-vs-QueryDsl-3192dc3ef51480a5aec7f47dd72bd7eb)

### 커서 기반 페이지네이션 도입 (팀 전체)

| 상황 | 선택 | 이유 |
|---|---|---|
| 단순 CRUD | Spring Data JPA | 보일러플레이트 최소화 |
| 동적 조건 조회 | QueryDSL | 타입 안전, 컴파일 타임 오류 감지 |
| 복잡한 집계·DB 함수 | Native Query | JPA 표현 한계 극복 |

**문제**: offset 방식은 페이지 조회 중 데이터가 추가/삭제되면 중복·누락 발생, 대용량에서 성능 저하

**배경**: 팀 전체가 cursor 기반 페이지네이션 도입에 합의

**적용 도메인**: 가게 목록 조회(신혜원), 주문 목록 조회(정재빈), 허용 지역 목록 조회(하지혜)

**구현 전략**:
- 정렬 전략별 `BooleanExpression` 분리
- 정렬 기준이 같은 경우를 대비해 `id`로 타이브레이킹
- boolean 그룹 경계 처리를 위한 `crossBoundary` OR 조건 패턴

### count 쿼리 최적화 (신혜원)

**문제**: 페이지 조회 시 데이터 쿼리 + count 쿼리 총 2번 실행

**해결**: 첫 페이지(`cursor = null`)에서만 count 쿼리 실행, 이후 페이지는 `hasNext` 플래그로 판단
```
if (cursor == null) {
    long total = queryFactory.select(entity.count()).fetchOne();
}
```

---

## 6단계 — AI 기능 

### pgvector + STT 기반 음성 메뉴 추천 (한병두)

**흐름**:
```
음성 입력 → STT(OpenAI Whisper) → 텍스트 → 임베딩 벡터 생성 → pgvector 유사도 검색 → 메뉴 추천
```

**임베딩 전략**:
- 메뉴 아이템 등록/수정 시 `name + description` 임베딩 벡터 생성 후 pgvector 저장
- 검색 시 코사인 유사도(`cosine_distance`)로 가장 가까운 메뉴 반환

**pgvector 선택 이유**: 별도 벡터 DB(Pinecone 등) 없이 기존 PostgreSQL에서 벡터 검색 가능

---

## 7단계 — 배포 / 운영 (마무리)

### 무중단 배포 전략 (정재빈)

> 관련 문서: [AWS 인프라 배포 가이드](https://www.notion.so/teamsparta/AWS-31d2dc3ef51480809126d91f94bc97c0) · [VPC/서브넷/Gateway 설정](https://www.notion.so/teamsparta/vpc-Internet-Gateway-NAT-Gateway-31d2dc3ef514809292bad6ee9decdd3b) · [HAProxy 초기 설정 가이드](https://www.notion.so/teamsparta/HAProxy-1-31e2dc3ef51480fd8683dcc001e41744) · [AWS 배포 구성 및 트러블슈팅 총정리](https://www.notion.so/teamsparta/AWS-31e2dc3ef514806cadc7f722f666e192)

**선택**: 그레이스풀 셧다운 + 롤링 업데이트 (HAProxy 기반)

| 방식 | 설명 |
|---|---|
| 그레이스풀 셧다운 | `SIGTERM` 후 처리 중인 요청 완료까지 대기 (`spring.lifecycle.timeout-per-shutdown-phase: 30s`) |
| 롤링 업데이트 | HAProxy가 트래픽을 한 인스턴스씩 전환하며 순차 배포 |

블루그린 대비 추가 서버 비용 없이 무중단 달성