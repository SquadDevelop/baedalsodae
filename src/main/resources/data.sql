
-- ============================================================
-- MOCK DATA (ON CONFLICT DO NOTHING)
-- ============================================================

-- 0-A. p_sido_area (10건)
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
ON CONFLICT DO NOTHING
^^^ ---

-- 0-B. p_sigg_area (10건)
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
ON CONFLICT DO NOTHING
^^^ ---

-- 0-C. p_end_area (10건)
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
ON CONFLICT DO NOTHING
^^^ ---

-- 1. p_user (100건)
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
ON CONFLICT DO NOTHING
^^^ ---

-- 2. p_user_address (100건)
INSERT INTO baedalsodae.p_user_address (
    id, user_id, road_address, detail_address, description,
    created_at, updated_at
)
SELECT
    gen_random_uuid(),
    u.id,
    '서울특별시 강남구 테헤란로 ' || (u.rn * 3)::text,
    '101동 ' || (u.rn % 300 + 101)::text || '호',
    CASE (u.rn % 3) WHEN 0 THEN '집' WHEN 1 THEN '회사' ELSE '기타' END,
    NOW() - (random() * INTERVAL '90 days'),
    NOW() - (random() * INTERVAL '30 days')
FROM (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_user
    WHERE username LIKE 'mockuser%'
    ORDER BY created_at
    LIMIT 100
) u
ON CONFLICT DO NOTHING
^^^ ---

-- user_main_address_id 업데이트
UPDATE baedalsodae.p_user u
SET user_main_address_id = (
    SELECT a.id FROM baedalsodae.p_user_address a WHERE a.user_id = u.id LIMIT 1
)
WHERE u.user_main_address_id IS NULL
  AND EXISTS (SELECT 1 FROM baedalsodae.p_user_address a WHERE a.user_id = u.id)
^^^ ---

-- 3. p_store_category (10건)
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
)
^^^ ---

-- 4. p_store (100건)
INSERT INTO baedalsodae.p_store (
    id, user_id, store_category_id, name, business_number, phone,
    sido_code, sido_name, sigungu_code, sigungu_name, dong_code, dong_name,
    road_address, detail_address,
    description, avg_rating, review_count, store_status,
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
    '123-45-' || LPAD(i::text, 3, '0'),
    '02-' || LPAD((1000 + i)::text, 4, '0') || '-' || LPAD((i * 3 % 10000)::text, 4, '0'),
    '11', '서울특별시', '11680', '강남구', '1168010400', '역삼동',
    '서울특별시 강남구 테헤란로 ' || (i * 3)::text,
    i::text || '층',
    '정성껏 만든 건강한 한 끼 (' || i || '번 가게)',
    (i % 5)::float,
    i % 10,
    CASE WHEN (i % 10 = 0) THEN 'TEMPORARILY_CLOSED' ELSE 'OPEN' END,
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
ON CONFLICT DO NOTHING
^^^ ---

-- 5. p_menu_category (100건)
INSERT INTO baedalsodae.p_menu_category (
    id, name, order_no, store_id,
    created_at, updated_at, created_by, updated_by, deleted_at, deleted_by, is_deleted
)
SELECT
    gen_random_uuid(),
    CASE (i % 6)
        WHEN 0 THEN '대표메뉴' WHEN 1 THEN '인기메뉴' WHEN 2 THEN '세트메뉴'
        WHEN 3 THEN '사이드'   WHEN 4 THEN '음료'      ELSE '계절특선'
    END,
    i,
    st.id,
    NOW() - (random() * INTERVAL '90 days'),
    NOW() - (random() * INTERVAL '30 days'),
    NULL, NULL, NULL, NULL, false
FROM generate_series(1, 100) AS i
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_store
) st ON st.rn = i
^^^ ---

-- 6. p_menu_item (100건)
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
) mc ON mc.rn = i
^^^ ---

-- 7. p_cart (100건)
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
    FROM baedalsodae.p_store WHERE store_status = 'OPEN'
    LIMIT 100
) st ON st.rn = cust.rn
ON CONFLICT DO NOTHING
^^^ ---

-- 8. p_cart_item (100건)
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
ON CONFLICT DO NOTHING
^^^ ---

-- 9. p_order (100건)
INSERT INTO baedalsodae.p_order (
    id, order_no, user_id, user_nickname_snapshot, user_phone_snapshot, store_id, store_name_snapshot, status,
    store_request_note, delivery_request_note, address_id, delivery_address_snapshot,
    total_amount, delivery_fee, discount_amount, final_amount,
    created_at, updated_at, created_by, updated_by, deleted_at, deleted_by, is_deleted
)
SELECT
    gen_random_uuid(),
    'ORD-' || TO_CHAR(NOW() - (i || ' hours')::interval, 'YYYYMMDD') || '-' || LPAD(i::text, 6, '0'),
    cust.id,
    cust.nickname,
    cust.phone,
    st.id,
    st.name,
    CASE (i % 9)
        WHEN 0 THEN 'CREATED'    WHEN 1 THEN 'ACCEPTED'   WHEN 2 THEN 'COOKING'
        WHEN 3 THEN 'COOKED'     WHEN 4 THEN 'DELIVERING' WHEN 5 THEN 'DELIVERED'
        WHEN 6 THEN 'CANCELED'   WHEN 7 THEN 'REJECTED'   ELSE 'FAILED'
    END,
    CASE (i % 3)
        WHEN 0 THEN '빨리 만들어주세요. 문 앞에 놔주세요.' ELSE NULL END,
    CASE (i % 3)
        WHEN 1 THEN '문 앞에 놔주세요.' ELSE NULL END,
    ua.id,
    '서울특별시 강남구 테헤란로 ' || (i * 3)::text || ' ' || i::text || '층',
    (i * 500 + 10000),
    3000,
    CASE WHEN i % 5 = 0 THEN 1000 ELSE 0 END,
    ((i * 500 + 10000) + 3000 - CASE WHEN i % 5 = 0 THEN 1000 ELSE 0 END),
    NOW() - ((101 - i) || ' hours')::interval,
    NOW() - ((101 - i) || ' hours')::interval,
    cust.id, cust.id, NULL, NULL, false
FROM generate_series(1, 100) AS i
JOIN (
    SELECT id, nickname, phone, user_main_address_id, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_user WHERE role = 'CUSTOMER'
) cust ON cust.rn = ((i - 1) % 40) + 1
JOIN baedalsodae.p_user_address ua ON ua.id = cust.user_main_address_id
JOIN (
    SELECT id, name, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_store WHERE store_status = 'OPEN'
) st ON st.rn = ((i - 1) % 90) + 1
^^^ ---

-- 10. p_order_item (100건)
INSERT INTO baedalsodae.p_order_item (
    id, order_id, menu_item_id, name_snapshot, price_snapshot, quantity,
    created_at, updated_at
)
SELECT
    gen_random_uuid(),
    o.id,
    mi.id,
    mi.name,
    mi.price,
    ((ROW_NUMBER() OVER () % 3) + 1),
    o.created_at,
    o.created_at
FROM (
    SELECT id, created_at, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_order LIMIT 100
) o
JOIN (
    SELECT id, name, price, ROW_NUMBER() OVER (ORDER BY created_at) AS rn
    FROM baedalsodae.p_menu_item
) mi ON mi.rn = o.rn
^^^ ---

-- 11. p_payment (100건)
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
        WHEN 'DELIVERED'  THEN 'SUCCESS'
        WHEN 'CANCELED'   THEN 'CANCELED'
        WHEN 'FAILED'     THEN 'FAILED'
        ELSE 'PENDING'
    END,
    CASE WHEN o.status = 'DELIVERED'
         THEN (o.created_at + INTERVAL '5 minutes')
         ELSE NULL
    END,
    o.user_id, o.user_id,
    o.created_at,
    o.created_at + INTERVAL '1 minute'
FROM baedalsodae.p_order o
^^^ ---

-- 12. 태그 및 태그 매핑 삽입 (메뉴당 3~5개)
DO $$
DECLARE
    tag_names text[] := ARRAY['매콤한', '달콤한', '짭짤한', '바삭한', '부드러운', '치즈듬뿍', '가성비', '프리미엄', '야식', '간식', '안주', '아이들간식', '혼밥', '비건', '글루텐프리', '다이어트', '단백질', '매니아', '베스트', '신메뉴', '따뜻한', '시원한', '1인분', '술안주'];
    t_name text;
    t_id uuid;
    m_record record;
    random_tag_count int;
    i int;
BEGIN
    -- 1. 태그 생성
    FOREACH t_name IN ARRAY tag_names
    LOOP
        INSERT INTO baedalsodae.p_tag (id, name, created_at)
        VALUES (public.uuid_generate_v4(), t_name, now())
        ON CONFLICT (name) DO NOTHING;
    END LOOP;

    -- 기존 매핑 제거 (재실행 위한 멱등성 보장)
    DELETE FROM baedalsodae.p_tag_mapping;

    -- 2. 메뉴별 3~5개 랜덤 태그 매핑
    FOR m_record IN SELECT id FROM baedalsodae.p_menu_item
    LOOP
        random_tag_count := floor(random() * 3 + 3)::int; -- 3, 4, or 5
        
        i := 1;
        FOR t_id IN (
            SELECT id FROM baedalsodae.p_tag ORDER BY random() LIMIT random_tag_count
        )
        LOOP
            INSERT INTO baedalsodae.p_tag_mapping (id, created_at, order_no, menu_item, tag_id)
            VALUES (public.uuid_generate_v4(), now(), i, m_record.id, t_id);
            i := i + 1;
        END LOOP;
    END LOOP;
END $$
^^^ ---