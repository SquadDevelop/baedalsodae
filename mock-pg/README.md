# Mock PG 서버 (WireMock)

WireMock 기반의 결제 대행사(PG) 목(Mock) 서버입니다.  
요청값(금액, 결제수단 등)에 따라 **동적으로 응답**을 반환합니다.

## 디렉토리 구조

```
mock-pg/
├── docker-compose.yml         # WireMock 단독 실행용
└── mappings/
    ├── pg-pay.json            # POST /pg/pay 매핑
    └── pg-cancel.json         # POST /pg/cancel 매핑
```

## 실행 방법

### 단독 실행 (WireMock만)
```bash
cd mock-pg
docker compose up -d
```

### 전체 서비스와 함께 실행 (루트에서)
```bash
docker compose up -d
```

WireMock 서버는 **포트 8999**로 실행됩니다.

---

## API 엔드포인트

### POST /pg/pay — 결제 요청

**Request Body**
```json
{
  "orderId": "UUID",
  "userId": "UUID",
  "paymentMethod": "CREDIT_CARD | DEBIT_CARD | MOBILE_PAYMENT | BANK_TRANSFER | CASH",
  "amount": 15000
}
```

**응답 분기 로직**

| 조건 | status | message |
|------|--------|---------|
| `amount <= 0` | `FAILED` | 결제 금액이 유효하지 않습니다 |
| `amount > 1,000,000` | `FAILED` | 결제 한도를 초과했습니다 |
| `paymentMethod == CASH` | `FAILED` | 지원하지 않는 결제수단입니다 |
| 정상 요청 | `SUCCESS` | 결제가 성공적으로 처리되었습니다 |

**성공 응답 예시**
```json
{
  "success": true,
  "paymentId": "550e8400-e29b-41d4-a716-446655440000",
  "pgTransactionId": "mock-3f8a2b1c-...",
  "amount": 15000,
  "status": "SUCCESS",
  "message": "결제가 성공적으로 처리되었습니다",
  "createdAt": "2026-03-04T12:30:00",
  "modifiedAt": "2026-03-04T12:30:00"
}
```

---

### POST /pg/cancel — 결제 취소

**Request Body**
```json
{
  "orderId": "UUID",
  "userId": "UUID",
  "paymentMethod": "CREDIT_CARD",
  "amount": 15000,
  "pgTransactionId": "mock-3f8a2b1c-..."
}
```

| 조건 | status | message |
|------|--------|---------|
| `pgTransactionId` 없음 | `FAILED` | 취소할 거래 ID가 없습니다 |
| 정상 요청 | `CANCELED` | 결제가 성공적으로 취소되었습니다 |

---

## 테스트 curl 예시

```bash
# ✅ 정상 결제
curl -X POST http://localhost:8999/pg/pay \
  -H "Content-Type: application/json" \
  -d '{"orderId":"550e8400-e29b-41d4-a716-446655440000","userId":"550e8400-e29b-41d4-a716-446655440001","paymentMethod":"CREDIT_CARD","amount":15000}'

# ❌ 금액 0 이하
curl -X POST http://localhost:8999/pg/pay \
  -H "Content-Type: application/json" \
  -d '{"orderId":"550e8400-e29b-41d4-a716-446655440000","userId":"550e8400-e29b-41d4-a716-446655440001","paymentMethod":"CREDIT_CARD","amount":-1}'

# ❌ 한도 초과 (100만원 초과)
curl -X POST http://localhost:8999/pg/pay \
  -H "Content-Type: application/json" \
  -d '{"orderId":"550e8400-e29b-41d4-a716-446655440000","userId":"550e8400-e29b-41d4-a716-446655440001","paymentMethod":"CREDIT_CARD","amount":2000000}'

# ❌ 현금 결제 (미지원)
curl -X POST http://localhost:8999/pg/pay \
  -H "Content-Type: application/json" \
  -d '{"orderId":"550e8400-e29b-41d4-a716-446655440000","userId":"550e8400-e29b-41d4-a716-446655440001","paymentMethod":"CASH","amount":15000}'

# ✅ 결제 취소
curl -X POST http://localhost:8999/pg/cancel \
  -H "Content-Type: application/json" \
  -d '{"orderId":"550e8400-e29b-41d4-a716-446655440000","userId":"550e8400-e29b-41d4-a716-446655440001","paymentMethod":"CREDIT_CARD","amount":15000,"pgTransactionId":"mock-3f8a2b1c-0000-0000-0000-000000000000"}'

# 📋 등록된 매핑 확인
curl http://localhost:8999/__admin/mappings
```

---

## WireMock Admin API

WireMock은 런타임에 매핑을 추가/수정할 수 있는 Admin API를 제공합니다.

- `GET /__admin/mappings` — 전체 매핑 조회
- `POST /__admin/mappings` — 매핑 동적 추가
- `DELETE /__admin/mappings/{id}` — 매핑 삭제
- `POST /__admin/reset` — 전체 초기화
