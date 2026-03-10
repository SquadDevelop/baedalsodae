-- Role이 없을 때만 생성 (DO 블록 안에서는 CREATE ROLE만)
DO
$$
BEGIN
   IF NOT EXISTS (
      SELECT FROM pg_catalog.pg_roles
      WHERE rolname = 'baedalsodae_admin'
   ) THEN
      CREATE ROLE baedalsodae_admin LOGIN PASSWORD 'p@ssw0rd';
   END IF;
END
$$;

-- GRANT/ALTER는 DO 블록 밖에서 실행 (멱등성 보장)
ALTER USER baedalsodae_admin CREATEDB;
GRANT ALL PRIVILEGES ON DATABASE baedalsodae TO baedalsodae_admin;

ALTER DATABASE baedalsodae SET search_path TO baedalsodae_admin, public;

CREATE SCHEMA IF NOT EXISTS baedalsodae AUTHORIZATION baedalsodae_admin;
GRANT ALL ON SCHEMA baedalsodae TO baedalsodae_admin;
GRANT ALL ON ALL TABLES IN SCHEMA baedalsodae TO baedalsodae_admin;
GRANT ALL ON ALL SEQUENCES IN SCHEMA baedalsodae TO baedalsodae_admin;

SET search_path TO baedalsodae;

-- ============================================================
-- MOCK DATA (중복 실행 안전: ON CONFLICT DO NOTHING 사용)
-- 삽입 순서: sido_area → sigg_area → end_area
--            → user → user_address
--            → store_category → store
--            → menu_category → menu_item
--            → cart → cart_item
--            → order → order_item → payment
-- ============================================================

-- ============================================================
-- 0-A. p_sido_area (시도 10건)
-- ============================================================
INSERT INTO baedalsodae.p_sido_area (id, adm_code, name, created_at, updated_at, version)
VALUES
    ('a0000001-0000-0000-0000-000000000001', '11', '서울특별시', NOW(), NOW(), 0),
    ('a0000001-0000-0000-0000-000000000002', '26', '부산광역시', NOW(), NOW(), 0),
    ('a0000001-0000-0000-0000-000000000003', '27', '대구광역시', NOW(), NOW(), 0),
    ('a0000001-0000-0000-0000-000000000004', '28', '인천광역시', NOW(), NOW(), 0),
    ('a0000001-0000-0000-0000-000000000005', '29', '광주광역시', NOW(), NOW(), 0),
    ('a0000001-0000-0000-0000-000000000006', '30', '대전광역시', NOW(), NOW(), 0),
    ('a0000001-0000-0000-0000-000000000007', '31', '울산광역시', NOW(), NOW(), 0),
    ('a0000001-0000-0000-0000-000000000008', '41', '경기도',     NOW(), NOW(), 0),
    ('a0000001-0000-0000-0000-000000000009', '43', '충청북도',   NOW(), NOW(), 0),
    ('a0000001-0000-0000-0000-000000000010', '44', '충청남도',   NOW(), NOW(), 0)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 0-B. p_sigg_area (시구군 10건)
-- ============================================================
INSERT INTO baedalsodae.p_sigg_area (id, adm_code, name, created_at, updated_at, version)
VALUES
    ('b0000002-0000-0000-0000-000000000001', '11110', '종로구',   NOW(), NOW(), 0),
    ('b0000002-0000-0000-0000-000000000002', '11140', '중구',     NOW(), NOW(), 0),
    ('b0000002-0000-0000-0000-000000000003', '11215', '광진구',   NOW(), NOW(), 0),
    ('b0000002-0000-0000-0000-000000000004', '11230', '동대문구', NOW(), NOW(), 0),
    ('b0000002-0000-0000-0000-000000000005', '11380', '은평구',   NOW(), NOW(), 0),
    ('b0000002-0000-0000-0000-000000000006', '11440', '마포구',   NOW(), NOW(), 0),
    ('b0000002-0000-0000-0000-000000000007', '11560', '영등포구', NOW(), NOW(), 0),
    ('b0000002-0000-0000-0000-000000000008', '11650', '서초구',   NOW(), NOW(), 0),
    ('b0000002-0000-0000-0000-000000000009', '11680', '강남구',   NOW(), NOW(), 0),
    ('b0000002-0000-0000-0000-000000000010', '11710', '송파구',   NOW(), NOW(), 0)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 0-C. p_end_area (읍면동 10건)
-- ============================================================
INSERT INTO baedalsodae.p_end_area (id, adm_code, name, created_at, updated_at, version)
VALUES
    ('c0000003-0000-0000-0000-000000000001', '1111010100', '청운동',   NOW(), NOW(), 0),
    ('c0000003-0000-0000-0000-000000000002', '1111010200', '신교동',   NOW(), NOW(), 0),
    ('c0000003-0000-0000-0000-000000000003', '1114010100', '무교동',   NOW(), NOW(), 0),
    ('c0000003-0000-0000-0000-000000000004', '1121510100', '중곡1동',  NOW(), NOW(), 0),
    ('c0000003-0000-0000-0000-000000000005', '1123010100', '회기동',   NOW(), NOW(), 0),
    ('c0000003-0000-0000-0000-000000000006', '1138010100', '증산동',   NOW(), NOW(), 0),
    ('c0000003-0000-0000-0000-000000000007', '1144010100', '아현동',   NOW(), NOW(), 0),
    ('c0000003-0000-0000-0000-000000000008', '1156010100', '여의도동', NOW(), NOW(), 0),
    ('c0000003-0000-0000-0000-000000000009', '1168010100', '잠원동',   NOW(), NOW(), 0),
    ('c0000003-0000-0000-0000-000000000010', '1168010400', '역삼1동',  NOW(), NOW(), 0)
ON CONFLICT DO NOTHING;

-- ============================================================
-- 1. p_user (100건) — ON CONFLICT(username) DO NOTHING
-- ============================================================
INSERT INTO baedalsodae.p_user (
    id, username, phone, email, password, name, nickname, role,
    user_main_address_id,
    created_at, updated_at, created_by, updated_by, deleted_at, deleted_by, is_deleted
)
SELECT
    gen_random_uuid(),
    'mockuser' || i,
    '010-' || LPAD((1000 + i)::text, 4, '0') || '-' || LPAD((i * 7 % 10000)::text, 4, '0'),
    'mockuser' || i || '@mock.com',
    '$2a$10$mockhashpassword' || LPAD(i::text, 6, '0'),
    CASE (i % 5)
        WHEN 0 THEN '김민준' WHEN 1 THEN '이서연' WHEN 2 THEN '박도윤'
        WHEN 3 THEN '최지아' ELSE '정준혁'
    END,
    'mocknick' || i,
    CASE WHEN i <= 10 THEN 'MANAGER'
         WHEN i <= 60 THEN 'OWNER'
         ELSE 'CUSTOMER'
    END,
    NULL,
    NOW() - (random() * INTERVAL '180 days'),
    NOW() - (random() * INTERVAL '30 days'),
    NULL, NULL, NULL, NULL, false
FROM generate_series(1, 100) AS i
ON CONFLICT DO NOTHING;

-- ============================================================
-- 2. p_user_address (100건)
-- ============================================================
INSERT INTO baedalsodae.p_user_address (
    id, user_id, road_address, detail_address, description,
    sido_id, sigungu_id, dong_id,
    created_at, updated_at
)
SELECT
    gen_random_uuid(),
    u.id,
    '서울특별시 강남구 테헤란로 ' || (u.rn * 3)::text,
    '101동 ' || (u.rn % 300 + 101)::text || '호',
    CASE (u.rn % 3) WHEN 0 THEN '집' WHEN 1 THEN '회사' ELSE '기타' END,
    'a0000001-0000-0000-0000-000000000001'::uuid,
    'b0000002-0000-0000-0000-000000000009'::uuid,
    'c0000003-0000-0000-0000-000000000010'::uuid,
    NOW() - (random() * INTERVAL '90 days'),
    NOW() - (random() * INTERVAL '30 days')
FROM (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_user
    WHERE username LIKE 'mockuser%'
    ORDER BY created_at
    LIMIT 100
) u
ON CONFLICT DO NOTHING;

-- user_main_address_id 업데이트
UPDATE baedalsodae.p_user u
SET user_main_address_id = (
    SELECT a.id FROM baedalsodae.p_user_address a WHERE a.user_id = u.id LIMIT 1
)
WHERE u.user_main_address_id IS NULL
  AND EXISTS (SELECT 1 FROM baedalsodae.p_user_address a WHERE a.user_id = u.id);

-- ============================================================
-- 3. p_store_category (10건)
-- ============================================================
INSERT INTO baedalsodae.p_store_category (
    id, name, description,
    created_at, updated_at, created_by, updated_by, deleted_at, deleted_by, is_deleted
)
SELECT
    gen_random_uuid(),
    cat_name, cat_desc,
    NOW(), NOW(), NULL, NULL, NULL, NULL, false
FROM (VALUES
    ('한식',        '한국 전통 음식'),
    ('중식',        '중국 요리'),
    ('일식',        '일본 요리 및 초밥'),
    ('양식',        '서양 음식'),
    ('치킨',        '후라이드·양념·구이 치킨'),
    ('피자',        '피자 및 파스타'),
    ('분식',        '떡볶이·순대·라면'),
    ('카페·디저트', '커피·케이크·빙수'),
    ('패스트푸드',  '버거·샌드위치·핫도그'),
    ('아시안',      '태국·베트남·인도 요리')
) AS v(cat_name, cat_desc)
WHERE NOT EXISTS (
    SELECT 1 FROM baedalsodae.p_store_category WHERE name = v.cat_name
);

-- ============================================================
-- 4. p_store (100건)
--    business_number: max 10자 → '123-45-001' 형식
-- ============================================================
INSERT INTO baedalsodae.p_store (
    id, user_id, store_category_id, name, business_number, phone,
    sido_id, sigungu_id, dong_id,
    road_address, detail_address,
    description, rating_sum, review_count, is_closed,
    created_at, updated_at, created_by, updated_by, deleted_at, deleted_by, is_deleted
)
SELECT
    gen_random_uuid(),
    owners.id,
    cats.id,
    CASE (i % 10)
        WHEN 0 THEN '맛있는한식당'   WHEN 1 THEN '중화루'
        WHEN 2 THEN '스시마루'        WHEN 3 THEN '팔레스레스토랑'
        WHEN 4 THEN '황금치킨'        WHEN 5 THEN '나폴리피자'
        WHEN 6 THEN '분식천국'        WHEN 7 THEN '달콤카페'
        WHEN 8 THEN '버거킹덤'        ELSE '메콩델타'
    END || i || '호점',
    -- business_number: max 10자 '123-45-NNN'
    '123-45-' || LPAD(i::text, 3, '0'),
    '02-' || LPAD((1000 + i)::text, 4, '0') || '-' || LPAD((i * 3 % 10000)::text, 4, '0'),
    'a0000001-0000-0000-0000-000000000001'::uuid,
    'b0000002-0000-0000-0000-000000000009'::uuid,
    'c0000003-0000-0000-0000-000000000010'::uuid,
    '서울특별시 강남구 테헤란로 ' || (i * 3)::text,
    i::text || '층',
    '정성껏 만든 건강한 한 끼 (' || i || '번 가게)',
    (i % 5) * 20,
    i % 10,
    (i % 10 = 0),
    NOW() - (random() * INTERVAL '120 days'),
    NOW() - (random() * INTERVAL '30 days'),
    owners.id, owners.id, NULL, NULL, false
FROM generate_series(1, 100) AS i
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_user WHERE role = 'OWNER'
) owners ON owners.rn = ((i - 1) % 50) + 1
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_store_category
) cats ON cats.rn = ((i - 1) % 10) + 1
ON CONFLICT DO NOTHING;

-- ============================================================
-- 5. p_menu_category (100건)
-- ============================================================
INSERT INTO baedalsodae.p_menu_category (
    id, name, order_no,
    created_at, updated_at, created_by, updated_by, deleted_at, deleted_by, is_deleted
)
SELECT
    gen_random_uuid(),
    CASE (i % 6)
        WHEN 0 THEN '대표메뉴' WHEN 1 THEN '인기메뉴' WHEN 2 THEN '세트메뉴'
        WHEN 3 THEN '사이드'   WHEN 4 THEN '음료'      ELSE '계절특선'
    END,
    i,
    NOW() - (random() * INTERVAL '90 days'),
    NOW() - (random() * INTERVAL '30 days'),
    NULL, NULL, NULL, NULL, false
FROM generate_series(1, 100) AS i;

-- ============================================================
-- 6. p_menu_item (100건)
--    menu_status 허용값: AVAILABLE, UNAVAILABLE, SOLD_OUT, PREPARING, HIDDEN
-- ============================================================
INSERT INTO baedalsodae.p_menu_item (
    id, name, order_no, price, description, menu_category_id,
    is_popular, menu_status,
    created_at, updated_at, created_by, updated_by, deleted_at, deleted_by, is_deleted
)
SELECT
    gen_random_uuid(),
    CASE (i % 20)
        WHEN 0  THEN '김치찌개'     WHEN 1  THEN '된장찌개'   WHEN 2  THEN '삼겹살'
        WHEN 3  THEN '짜장면'       WHEN 4  THEN '짬뽕'       WHEN 5  THEN '탕수육'
        WHEN 6  THEN '연어초밥'     WHEN 7  THEN '참치마키'   WHEN 8  THEN '우동'
        WHEN 9  THEN '스테이크'     WHEN 10 THEN '파스타'     WHEN 11 THEN '피자마르게리타'
        WHEN 12 THEN '후라이드치킨' WHEN 13 THEN '양념치킨'   WHEN 14 THEN '떡볶이'
        WHEN 15 THEN '순대국밥'     WHEN 16 THEN '아메리카노' WHEN 17 THEN '치즈버거'
        WHEN 18 THEN '팟타이'       ELSE '쌀국수'
    END,
    i,
    (i * 500 + 5000),
    '최고의 재료로 정성껏 만든 메뉴 (' || i || '번)',
    mc.id,
    (i % 4 = 0),
    CASE (i % 5)
        WHEN 0 THEN 'SOLD_OUT'
        WHEN 1 THEN 'UNAVAILABLE'
        ELSE 'AVAILABLE'
    END,
    NOW() - (random() * INTERVAL '90 days'),
    NOW() - (random() * INTERVAL '30 days'),
    NULL, NULL, NULL, NULL, false
FROM generate_series(1, 100) AS i
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_menu_category
) mc ON mc.rn = i;

-- ============================================================
-- 7. p_cart (100건)
-- ============================================================
INSERT INTO baedalsodae.p_cart (id, user_id, store_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    cust.id,
    st.id,
    NOW() - (random() * INTERVAL '7 days'),
    NOW() - (random() * INTERVAL '1 days')
FROM (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_user WHERE role = 'CUSTOMER'
    LIMIT 100
) cust
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_store WHERE is_closed = false
    LIMIT 100
) st ON st.rn = cust.rn
ON CONFLICT DO NOTHING;

-- ============================================================
-- 8. p_cart_item (100건)
-- ============================================================
INSERT INTO baedalsodae.p_cart_item (id, cart_id, menu_item_id, quantity, created_at, updated_at)
SELECT
    gen_random_uuid(),
    c.id,
    mi.id,
    (ROW_NUMBER() OVER () % 5) + 1,
    NOW() - (random() * INTERVAL '3 days'),
    NOW() - (random() * INTERVAL '1 days')
FROM (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn FROM baedalsodae.p_cart LIMIT 100
) c
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn FROM baedalsodae.p_menu_item LIMIT 100
) mi ON mi.rn = c.rn
ON CONFLICT DO NOTHING;

-- ============================================================
-- 9. p_order (100건)
--    컬럼: order_no, user_id, store_id, status, request_note,
--           delivery_address_snapshot, total_amount/delivery_fee/discount_amount/final_amount(bigint)
-- ============================================================
INSERT INTO baedalsodae.p_order (
    id, order_no, user_id, store_id, status,
    request_note, delivery_address_snapshot,
    total_amount, delivery_fee, discount_amount, final_amount,
    created_at, updated_at, created_by, updated_by, deleted_at, deleted_by, is_deleted
)
SELECT
    gen_random_uuid(),
    'ORD-' || TO_CHAR(NOW() - (i || ' hours')::interval, 'YYYYMMDD') || '-' || LPAD(i::text, 6, '0'),
    cust.id,
    st.id,
    CASE (i % 9)
        WHEN 0 THEN 'CREATED'    WHEN 1 THEN 'ACCEPTED'   WHEN 2 THEN 'COOKING'
        WHEN 3 THEN 'COOKED'     WHEN 4 THEN 'DELIVERING' WHEN 5 THEN 'DELIVERED'
        WHEN 6 THEN 'CANCELED'   WHEN 7 THEN 'REJECTED'   ELSE 'FAILED'
    END,
    CASE (i % 3)
        WHEN 0 THEN '빨리 만들어주세요. 문 앞에 놔주세요.'
        WHEN 1 THEN '덜 맵게 해주세요. 경비실에 맡겨주세요.'
        ELSE NULL
    END,
    '서울특별시 강남구 테헤란로 ' || (i * 3)::text || ' ' || i::text || '층',
    (i * 500 + 10000)::bigint,
    3000::bigint,
    CASE WHEN i % 5 = 0 THEN 1000::bigint ELSE 0::bigint END,
    ((i * 500 + 10000) + 3000 - CASE WHEN i % 5 = 0 THEN 1000 ELSE 0 END)::bigint,
    NOW() - ((101 - i) || ' hours')::interval,
    NOW() - ((101 - i) || ' hours')::interval,
    cust.id, cust.id, NULL, NULL, false
FROM generate_series(1, 100) AS i
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_user WHERE role = 'CUSTOMER'
) cust ON cust.rn = ((i - 1) % 40) + 1
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_store WHERE is_closed = false
) st ON st.rn = ((i - 1) % 90) + 1;

-- ============================================================
-- 10. p_order_item (100건)
-- ============================================================
INSERT INTO baedalsodae.p_order_item (
    id, order_id, menu_item_id, name_snapshot, price_snapshot, quantity,
    created_at, updated_at
)
SELECT
    gen_random_uuid(),
    o.id,
    mi.id,
    mi.name,
    mi.price::bigint,
    ((ROW_NUMBER() OVER () % 3) + 1)::bigint,
    o.created_at,
    o.created_at
FROM (
    SELECT id, created_at, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_order LIMIT 100
) o
JOIN (
    SELECT id, name, price, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_menu_item
) mi ON mi.rn = o.rn;

-- ============================================================
-- 11. p_payment (100건)
--    payment_method smallint(enum ordinal):
--      0=CREDIT_CARD, 1=DEBIT_CARD, 2=MOBILE_PAYMENT, 3=BANK_TRANSFER, 4=CASH
--    payment_status smallint(enum ordinal):
--      0=PENDING, 1=SUCCESS, 2=FAILED, 3=CANCELED, 4=REFUNDING, 5=REFUNDED
-- ============================================================
INSERT INTO baedalsodae.p_payment (
    id, order_id, user_id, amount, payment_method, payment_status,
    paid_at, created_by, updated_by, created_at, updated_at
)
SELECT
    gen_random_uuid(),
    o.id,
    o.user_id,
    o.final_amount,
    ((ROW_NUMBER() OVER (ORDER BY o.created_at) % 4))::smallint,
    CASE o.status
        WHEN 'DELIVERED'  THEN 1::smallint
        WHEN 'CANCELED'   THEN 3::smallint
        WHEN 'FAILED'     THEN 2::smallint
        ELSE 0::smallint
    END,
    CASE WHEN o.status = 'DELIVERED'
         THEN (o.created_at + INTERVAL '5 minutes')
         ELSE NULL
    END,
    o.user_id, o.user_id,
    o.created_at,
    o.created_at + INTERVAL '1 minute'
FROM baedalsodae.p_order o;