# 📡 API 엔드포인트

> 기본 경로: `/api/v1`
> 권한 계층: `CUSTOMER` < `OWNER` < `MANAGER` < `MASTER` . `MANAGER` 이상 권한이 필요한 API는 `MASTER`도 접근 가능합니다.

## Auth

| Method | URL             | 설명                   |
|--------|-----------------|----------------------|
| `POST` | `/auth/signup`  | 회원가입                 |
| `POST` | `/auth/login`   | 로그인 (JWT 발급)         |
| `POST` | `/auth/logout`  | 로그아웃 (Redis 토큰 무효화)  |
| `POST` | `/auth/reissue` | 토큰 갱신                |

## Users

| Method   | URL         | 설명       |
|----------|-------------|----------|
| `GET`    | `/users/me` | 내 프로필 조회 |
| `PUT`    | `/users/me` | 내 프로필 수정 |
| `DELETE` | `/users/me` | 회원 탈퇴    |

## User Address

| Method   | URL                                | 설명       |
|----------|------------------------------------|----------|
| `POST`   | `/user-addresses`                  | 주소 등록    |
| `GET`    | `/user-addresses`                  | 주소 목록 조회 |
| `GET`    | `/user-addresses/main`             | 기본 주소 조회 |
| `PUT`    | `/user-addresses/{addressId}`      | 주소 수정    |
| `PUT`    | `/user-addresses`                  | 주소 전체 수정 |
| `DELETE` | `/user-addresses/{addressId}`      | 주소 삭제    |
| `PATCH`  | `/user-addresses/{addressId}/main` | 기본 주소 설정 |

## Store Categories

| Method   | URL                                   | 설명         |
|----------|---------------------------------------|------------|
| `GET`    | `/store-categories`                   | 카테고리 목록 조회 |
| `GET`    | `/store-categories/{storeCategoryId}` | 카테고리 상세 조회 |
| `POST`   | `/store-categories`                   | 카테고리 생성    |
| `PATCH`  | `/store-categories/{storeCategoryId}` | 카테고리 수정    |
| `DELETE` | `/store-categories/{storeCategoryId}` | 카테고리 삭제    |

## Stores

| Method   | URL                         | 설명          |
|----------|-----------------------------|-------------|
| `GET`    | `/stores/categories`        | 카테고리별 가게 목록 |
| `GET`    | `/stores/keywords`          | 키워드로 가게 검색  |
| `GET`    | `/stores/{storeId}`         | 가게 상세 조회    |
| `GET`    | `/stores/{storeId}/reviews` | 가게 리뷰 목록    |
| `GET`    | `/stores/{storeId}/manage`  | 점주 가게 관리 조회 |
| `POST`   | `/stores`                   | 가게 생성       |
| `PATCH`  | `/stores/{storeId}`         | 가게 수정       |
| `PATCH`  | `/stores/{storeId}/status`  | 가게 상태 변경    |
| `DELETE` | `/stores/{storeId}`         | 가게 삭제       |

## Store Hours

| Method   | URL                       | 설명      |
|----------|---------------------------|---------|
| `POST`   | `/stores/{storeId}/hours` | 운영시간 등록 |
| `GET`    | `/stores/{storeId}/hours` | 운영시간 조회 |
| `PATCH`  | `/stores/{storeId}/hours` | 운영시간 수정 |
| `DELETE` | `/stores/{storeId}/hours` | 운영시간 삭제 |

## Store Menus

| Method | URL                                                 | 설명            |
|--------|-----------------------------------------------------|---------------|
| `POST` | `/stores/{storeId}/menu-categories`                 | 메뉴 카테고리 생성    |
| `GET`  | `/stores/{storeId}/menu-categories`                 | 메뉴 카테고리 목록 조회 |
| `GET`  | `/stores/{storeId}/menu-categories/duplicate-check` | 메뉴 카테고리 중복 확인 |
| `GET`  | `/stores/{storeId}/menu-items/duplicate-check`      | 메뉴 아이템 중복 확인  |

## Menu Categories

| Method   | URL                                            | 설명            |
|----------|------------------------------------------------|---------------|
| `PUT`    | `/menu-categories/{menuCategoryId}`            | 메뉴 카테고리 수정    |
| `PATCH`  | `/menu-categories/{menuCategoryId}/orders`     | 메뉴 카테고리 순서 변경 |
| `DELETE` | `/menu-categories/{menuCategoryId}`            | 메뉴 카테고리 삭제    |
| `GET`    | `/menu-categories/{menuCategoryId}/menu-items` | 메뉴 아이템 목록 조회  |
| `POST`   | `/menu-categories/{menuCategoryId}/menu-items` | 메뉴 아이템 생성     |

## Menu Items

| Method   | URL                               | 설명           |
|----------|-----------------------------------|--------------|
| `PUT`    | `/menu-items/{menuItemId}`        | 메뉴 아이템 수정    |
| `PATCH`  | `/menu-items/{menuItemId}`        | 메뉴 아이템 부분 수정 |
| `PATCH`  | `/menu-items/{menuItemId}/orders` | 메뉴 아이템 순서 변경 |
| `DELETE` | `/menu-items/{menuItemId}`        | 메뉴 아이템 삭제    |

## Cart

| Method   | URL                         | 설명          |
|----------|-----------------------------|-------------|
| `GET`    | `/carts`                    | 장바구니 조회     |
| `POST`   | `/carts/items`              | 메뉴 담기       |
| `PATCH`  | `/carts/items/{cartItemId}` | 수량 변경       |
| `DELETE` | `/carts/items/{cartItemId}` | 개별 삭제       |
| `DELETE` | `/carts`                    | 장바구니 전체 비우기 |

## Orders

| Method | URL                                | 설명       |
|--------|------------------------------------|----------|
| `POST` | `/orders`                          | 주문 생성    |
| `GET`  | `/orders`                          | 내 주문 목록  |
| `GET`  | `/orders/{orderId}`                | 주문 상세 조회 |
| `GET`  | `/orders/{orderId}/status`         | 주문 상태 조회 |
| `POST` | `/orders/{orderId}/request`        | 주문 요청    |
| `POST` | `/orders/{orderId}/accept`         | 주문 수락    |
| `POST` | `/orders/{orderId}/reject`         | 주문 거절    |
| `POST` | `/orders/{orderId}/cooked`         | 조리 완료    |
| `POST` | `/orders/{orderId}/delivering`     | 배달 시작    |
| `POST` | `/orders/{orderId}/delivered`      | 배달 완료    |
| `POST` | `/orders/{orderId}/cancel-request` | 취소 요청    |
| `POST` | `/orders/{orderId}/cancel`         | 취소 완료    |
| `POST` | `/orders/{orderId}/reviews`        | 리뷰 작성    |

## Reviews

| Method   | URL                   | 설명       |
|----------|-----------------------|----------|
| `GET`    | `/reviews`            | 리뷰 목록 조회 |
| `GET`    | `/reviews/{reviewId}` | 리뷰 상세 조회 |
| `PUT`    | `/reviews/{reviewId}` | 리뷰 수정    |
| `DELETE` | `/reviews/{reviewId}` | 리뷰 삭제    |

## Payments

| Method | URL                     | 설명       |
|--------|-------------------------|----------|
| `GET`  | `/payments`             | 결제 목록 조회 |
| `GET`  | `/payments/{paymentId}` | 결제 상세 조회 |

## Allowed Regions

| Method  | URL                                             | 설명          | 권한               |
|---------|-------------------------------------------------|-------------|------------------|
| `GET`   | `/allowed-regions`                              | 허용 지역 목록 조회 | `MANAGER` 이상     |
| `POST`  | `/allowed-regions`                              | 허용 지역 등록    | `MANAGER` 이상     |
| `PATCH` | `/allowed-regions/{allowedRegionId}/activation` | 활성화/비활성화 토글 | `MANAGER` 이상     |

## Tags

| Method | URL     | 설명       |
|--------|---------|----------|
| `GET`  | `/tags` | 태그 목록 조회 |

## Recommendations

| Method | URL                                | 설명             |
|--------|------------------------------------|----------------|
| `POST` | `/recommendations/voice`           | 음성 기반 AI 메뉴 추천 |
| `POST` | `/recommendations/sync-embeddings` | 메뉴 임베딩 동기화     |

## Admin — Users

| Method | URL                            | 설명          | 권한           |
|--------|--------------------------------|-------------|--------------|
| `POST` | `/admins/managers`             | 관리자 계정 생성   | `MASTER` 전용  |
| `GET`  | `/admins/managers`             | 관리자 목록 조회   | `MASTER` 전용  |
| `GET`  | `/admins/managers/{managerId}` | 관리자 상세 조회   | `MASTER` 전용  |
| `GET`  | `/admins/me`                   | 내 관리자 정보 조회 | `MANAGER` 이상 |

## Admin — Stores

| Method   | URL                               | 설명       | 권한           |
|----------|-----------------------------------|----------|--------------|
| `PATCH`  | `/admins/stores/{storeId}`        | 가게 정보 수정 | `MANAGER` 이상 |
| `PATCH`  | `/admins/stores/{storeId}/status` | 가게 상태 변경 | `MANAGER` 이상 |
| `DELETE` | `/admins/stores/{storeId}`        | 가게 삭제    | `MANAGER` 이상 |

## Admin — Orders

| Method | URL                                       | 설명          | 권한           |
|--------|-------------------------------------------|-------------|--------------|
| `GET`  | `/admins/orders`                          | 전체 주문 목록 조회 | `MANAGER` 이상 |
| `GET`  | `/admins/orders/{orderId}`                | 주문 상세 조회    | `MANAGER` 이상 |
| `GET`  | `/admins/orders/{orderId}/status-history` | 주문 상태 이력 조회 | `MANAGER` 이상 |
