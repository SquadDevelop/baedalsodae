# 🛵 BaedalSodae — 주문 · 배달 · 리뷰 플랫폼

> 배달의 민족을 모티브로 한 음식 주문, 배달, 리뷰 통합 플랫폼입니다.  
> Spring Boot 백엔드와 React 프론트엔드로 구성된 풀스택 프로젝트입니다.

---

## 📑 목차

- [기술 스택](#-기술-스택)
- [아키텍처](#-아키텍처)
- [주요 기능](#-주요-기능)
- [데이터 모델](#-데이터-모델)
- [API 엔드포인트](#-api-엔드포인트)
- [실행 방법](#-실행-방법)
- [테스트](#-테스트)
- [기술 스택 선정 배경](#-기술-스택-선정-배경)

---

## 🛠 기술 스택

| 구분 | 기술 |
|---|---|
| **Backend** | Spring Boot 3.5 · Java 17 · Spring Data JPA · Spring Security |
| **Frontend** | React 19 · TypeScript · Vite |
| **Database** | PostgreSQL 16 |
| **Cache / Session** | Redis 7 |
| **Infrastructure** | Docker · Docker Compose |
| **Testing** | JUnit 5 · Mockito |
| **빌드 도구** | Gradle |

---

## 🏗 아키텍처

```
┌─────────────────────────────────────────────────────────┐
│                     Docker Compose                      │
│                                                         │
│  ┌───────────┐    ┌───────────┐    ┌──────────────────┐ │
│  │  React    │───▶│  Spring   │───▶│   PostgreSQL 16  │ │
│  │ Frontend  │    │   Boot    │    │  (주문/리뷰 데이터) │ │
│  │ :5173     │    │ Backend   │    └──────────────────┘ │
│  └───────────┘    │  :8080    │                         │
│                   │           │───▶┌──────────────────┐ │
│                   └───────────┘    │    Redis 7       │ │
│                                    │  (세션 / 캐시)    │ │
│                                    └──────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 디렉터리 구조

```
baedalsodae/
├── src/
│   ├── main/
│   │   ├── java/com/project/baedalsodae/
│   │   │   ├── domain/          # 도메인 엔티티 (User, Store, Order, Review …)
│   │   │   ├── repository/      # Spring Data JPA 리포지토리
│   │   │   ├── service/         # 비즈니스 로직
│   │   │   ├── controller/      # REST 컨트롤러
│   │   │   ├── dto/             # 요청/응답 DTO
│   │   │   ├── security/        # JWT 인증/인가
│   │   │   └── config/          # Redis, JPA 등 설정
│   │   └── resources/
│   │       └── application.yml
│   └── test/                    # JUnit 5 + Mockito 테스트
├── frontend/                    # React 프론트엔드
├── docker-compose.yml
├── Dockerfile
└── build.gradle
```

---

## ✨ 주요 기능

### 👤 사용자 (User)
- 회원가입 · 로그인 · 로그아웃 (JWT + Redis 세션)
- 프로필 조회 및 수정

### 🏪 가게 (Store)
- 가게 목록 조회 (카테고리 · 지역 필터)
- 가게 상세 정보 조회
- 메뉴 목록 조회

### 🛒 주문 (Order)
- 장바구니 담기 → 주문 생성
- 주문 상태 조회 (`PENDING` → `ACCEPTED` → `DELIVERING` → `DELIVERED`)
- 주문 내역 조회

### 🚴 배달 (Delivery)
- 배달 기사 배정 및 상태 업데이트
- 배달 현황 실시간 조회

### ⭐ 리뷰 (Review)
- 배달 완료 주문에 대한 리뷰 작성 (별점 + 텍스트)
- 리뷰 조회 · 수정 · 삭제
- 가게별 평균 별점 집계

---

## 🗄 데이터 모델

```
User ──< Order ──< OrderItem >── Menu >── Store
                                           │
                              Category ───┘
Order ──< Review
Order ──< Delivery
```

| 엔티티 | 주요 필드 |
|---|---|
| `User` | id, email, password, name, phone, address, role |
| `Store` | id, name, category, address, minOrderAmount, deliveryFee |
| `Menu` | id, store, name, price, description, imageUrl |
| `Order` | id, user, store, status, totalPrice, deliveredAt |
| `OrderItem` | id, order, menu, quantity, price |
| `Delivery` | id, order, rider, status, estimatedTime |
| `Review` | id, order, user, store, rating, content, createdAt |

---

## 📡 API 엔드포인트

### Auth
| Method | URL | 설명 |
|---|---|---|
| `POST` | `/api/v1/auth/signup` | 회원가입 |
| `POST` | `/api/v1/auth/login` | 로그인 (JWT 발급) |
| `POST` | `/api/v1/auth/logout` | 로그아웃 (Redis 토큰 무효화) |
| `POST` | `/api/v1/auth/refresh` | 토큰 갱신 |

### Users
| Method | URL | 설명 |
|---|---|---|
| `GET` | `/api/v1/users/me` | 내 프로필 조회 |
| `PUT` | `/api/v1/users/me` | 내 프로필 수정 |

### Stores
| Method | URL | 설명 |
|---|---|---|
| `GET` | `/api/v1/stores` | 가게 목록 (카테고리/지역 필터) |
| `GET` | `/api/v1/stores/{id}` | 가게 상세 조회 |
| `GET` | `/api/v1/stores/{id}/menus` | 가게 메뉴 조회 |

### Orders
| Method | URL | 설명 |
|---|---|---|
| `POST` | `/api/v1/orders` | 주문 생성 |
| `GET` | `/api/v1/orders` | 내 주문 목록 |
| `GET` | `/api/v1/orders/{id}` | 주문 상세 조회 |
| `PATCH` | `/api/v1/orders/{id}/status` | 주문 상태 변경 (가게/관리자) |

### Reviews
| Method | URL | 설명 |
|---|---|---|
| `POST` | `/api/v1/orders/{orderId}/reviews` | 리뷰 작성 |
| `GET` | `/api/v1/stores/{storeId}/reviews` | 가게 리뷰 목록 |
| `PUT` | `/api/v1/reviews/{id}` | 리뷰 수정 |
| `DELETE` | `/api/v1/reviews/{id}` | 리뷰 삭제 |

---

## 🚀 실행 방법

### 방법 1: Docker Compose (권장)

#### 1. 사전 요구사항

```bash
docker --version          # Docker 24+ 권장
docker compose version    # Docker Compose v2+
```

#### 2. 환경 변수 설정

```bash
cp .env.example .env
# .env 파일을 열어 필요한 값을 설정하세요
```

**.env 주요 항목:**

```env
# PostgreSQL
POSTGRES_DB=baedalsodae
POSTGRES_USER=baedal_user
POSTGRES_PASSWORD=your_password

# Redis
REDIS_PASSWORD=your_redis_password

# JWT
JWT_SECRET=your_jwt_secret_key_at_least_32_chars
JWT_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
```

#### 3. 빌드 및 실행

```bash
# 전체 서비스 빌드 후 실행
docker compose up --build

# 백그라운드 실행
docker compose up -d

# 로그 확인
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f db
docker compose logs -f redis
```

#### 4. 접속 URL

| 서비스 | URL |
|---|---|
| 프론트엔드 | http://localhost:5173 |
| 백엔드 API | http://localhost:8080/api/v1 |
| PostgreSQL | localhost:5432 |
| Redis | localhost:6379 |

#### 5. 서비스 종료

```bash
# 중지
docker compose down

# 중지 + 볼륨 삭제 (DB 초기화)
docker compose down -v
```

---

### 방법 2: 로컬 직접 실행

#### 사전 요구사항
- Java 17+
- Node.js 20+
- PostgreSQL 16
- Redis 7

#### 백엔드

```bash
# 환경 변수 설정 후 실행(인텔레제이 환경 변수 사용도 가능)
export POSTGRES_DB=baedalsodae
export POSTGRES_DB_URL=jdbc:postgresql://localhost:5432/baedalsodae
export POSTGRES_USER=baedal_user
export POSTGRES_PASSWORD=your_password
export REDIS_HOST=localhost
export JWT_SECRET=your_jwt_secret

./gradlew bootRun
```

#### 프론트엔드

```bash
cd frontend
npm install
npm run dev
```

---

## 로컬 환경 세팅

> 백엔드 단독 로컬 실행 시 사용하는 간단한 세팅 가이드입니다.

1. `.env.example`을 복사해서 `.env` 파일 생성 후 값 입력
   ```bash
   cp .env.example .env
   ```

2. Docker로 PostgreSQL 실행
   ```bash
   docker-compose up -d
   ```

3. 애플리케이션 실행
  - 인텔레제이 환경 변수 설정 후 실행

---

## 🧪 테스트

### 백엔드 테스트 (JUnit 5 + Mockito)

```bash
# 전체 테스트 실행
./gradlew test

# 특정 클래스만 실행
./gradlew test --tests "com.project.baedalsodae.service.ReviewServiceTest"

# Docker 환경에서 실행
docker compose exec backend ./gradlew test

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

### 테스트 구조

```
src/test/
└── java/com/project/baedalsodae/
    ├── service/              # Service 단위 테스트 (Mockito)
    │   ├── UserServiceTest.java
    │   ├── OrderServiceTest.java
    │   └── ReviewServiceTest.java
    ├── controller/           # Controller 슬라이스 테스트 (@WebMvcTest)
    │   ├── AuthControllerTest.java
    │   ├── OrderControllerTest.java
    │   └── ReviewControllerTest.java
    └── repository/           # Repository 통합 테스트 (@DataJpaTest)
        ├── OrderRepositoryTest.java
        └── ReviewRepositoryTest.java
```

### 테스트 커버리지

| 레이어 | 전략 | 도구 |
|---|---|---|
| Service | Mockito로 Repository 목킹, 비즈니스 로직 검증 | JUnit 5 + Mockito |
| Controller | `@WebMvcTest` + `MockMvc`로 HTTP 슬라이스 테스트 | JUnit 5 + MockMvc |
| Repository | `@DataJpaTest`로 실제 DB 쿼리 검증 | JUnit 5 + H2 |

### Service 테스트 예시

```java
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("배달 완료된 주문에 리뷰를 작성할 수 있다")
    void createReview_Success() {
        // given
        Order order = Order.builder()
            .status(OrderStatus.DELIVERED)
            .build();
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        ReviewCreateRequest request = new ReviewCreateRequest(5, "맛있어요!");

        // when
        reviewService.createReview(1L, request, mockUser());

        // then
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("배달 완료 전 주문에는 리뷰를 작성할 수 없다")
    void createReview_Fail_NotDelivered() {
        // given
        Order order = Order.builder()
            .status(OrderStatus.DELIVERING)
            .build();
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(1L, new ReviewCreateRequest(5, "맛있어요!"), mockUser()))
            .isInstanceOf(ReviewNotAllowedException.class);
    }
}
```

### Controller 테스트 예시

```java
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Test
    @DisplayName("POST /api/v1/orders/{orderId}/reviews - 리뷰 작성 성공")
    void createReview_Returns201() throws Exception {
        mockMvc.perform(post("/api/v1/orders/1/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"rating": 5, "content": "맛있어요!"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.rating").value(5));
    }
}
```

---

## 💡 기술 스택 선정 배경

### Spring Boot 3.5 + Java 17
- **생산성**: Spring Boot Auto-configuration으로 빠른 개발
- **안정성**: 대규모 서비스에서 검증된 프레임워크
- **생태계**: Spring Security, Spring Data JPA 등 풍부한 라이브러리
- **Java 17**: Record, Sealed Class 등 최신 문법 활용, LTS 버전으로 안정성 확보

### PostgreSQL 16
- **신뢰성**: ACID 완전 지원, 복잡한 트랜잭션 (주문 처리) 안전하게 처리
- **JSON 지원**: 영수증 정보 등 반정형 데이터를 JSONB로 저장 가능
- **성능**: 고급 인덱싱 (부분 인덱스, 복합 인덱스)으로 대용량 리뷰 쿼리 최적화
- MySQL 대비 **표준 SQL 준수도** 우수

### Redis 7
- **세션 관리**: JWT Refresh Token 중앙 저장 및 블랙리스트 관리
- **캐싱**: 자주 조회되는 가게 목록, 메뉴 정보 캐싱으로 DB 부하 감소
- **성능**: 인메모리 저장소로 밀리초 단위 응답
- **TTL 지원**: 토큰 만료를 자동으로 처리

### Docker + Docker Compose
- **환경 일관성**: 개발/스테이징/프로덕션 환경 동일하게 유지
- **격리성**: 각 서비스(백엔드, DB, Redis)가 독립된 컨테이너에서 실행
- **간편한 실행**: `docker compose up` 한 줄로 전체 스택 기동

### JUnit 5 + Mockito
- **JUnit 5**: `@ExtendWith`, `@DisplayName`, 파라미터화 테스트 등 강력한 테스트 표현력
- **Mockito**: 외부 의존성을 목킹하여 순수 비즈니스 로직만 단위 테스트
- **레이어별 전략**: `@DataJpaTest`, `@WebMvcTest`, `@SpringBootTest` 구분으로 빠른 피드백

### React 19 + TypeScript + Vite
- **React**: 컴포넌트 기반 UI, 빠른 생태계와 풍부한 라이브러리
- **TypeScript**: 컴파일 타임 타입 체크로 런타임 오류 사전 방지
- **Vite**: 빠른 HMR(Hot Module Replacement)로 개발 생산성 향상

---

## ⚠️ 주의사항

- `.env` 파일은 절대 Git에 커밋하지 마세요 (`.gitignore`에 포함됨)
- JWT Secret Key는 최소 32자 이상으로 설정하세요
- 프로덕션 배포 시 `SPRING_PROFILES_ACTIVE=prod` 설정 필요
- Node.js 20+ 권장

---

## 📄 라이선스

This project is for educational purposes.
