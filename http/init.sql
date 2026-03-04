-- 유저 추가
INSERT INTO baedalsodae.p_user (is_deleted, created_at, deleted_at, updated_at, created_by, deleted_by, id, updated_by,
                                user_main_address_id, username, name, nickname, phone, email, password, role)
VALUES (DEFAULT, null, null, null, null, null, '00000000-0000-0000-0000-000000000010', null, null, 'jaebin1234', '정재빈',
        '억울해유', '01011112222', 'jaebin1234@naver.com', '1234', 'CUSTOMER');

-- 가게 카테고리 추가
INSERT INTO baedalsodae.p_store_category (is_deleted, created_at, deleted_at, updated_at, created_by, deleted_by, id,
                                          updated_by, name, description)
VALUES (DEFAULT, null, null, null, null, null, '00000000-0000-0000-0000-000000000001', null, '중식', null);

-- 가게 추가
INSERT INTO baedalsodae.p_store (is_closed, is_deleted, rating_sum, review_count, created_at, deleted_at, updated_at,
                                 business_number, created_by, deleted_by, dong_id, id, sido_id, sigungu_id,
                                 store_category_id, updated_by, user_id, phone, name, description, detail_address,
                                 road_address)
VALUES (DEFAULT, DEFAULT, DEFAULT, DEFAULT, null, null, null,
        '1111111111', null, null,
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000002', -- 가게 id 끝자리 2로 변경
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001',
        null,
        '00000000-0000-0000-0000-000000000010', -- 유저 id 끝자리 10 유지
        '01011111111', '정재빈가게', null, '111-111', '오금로 16');

-- 메뉴 카테고리 추가
INSERT INTO baedalsodae.p_menu_category (is_deleted, order_no, created_at, deleted_at, updated_at, created_by,
                                         deleted_by, id, store_id, updated_by, name)
VALUES (DEFAULT, 1, null, null, null, null, null, '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000002', null, '인기 메뉴');

-- 메뉴 추가
INSERT INTO baedalsodae.p_menu_item (is_deleted, is_popular, order_no, price, created_at, deleted_at, updated_at,
                                     created_by, deleted_by, id, menu_category_id, updated_by, description, menu_status,
                                     name)
VALUES (DEFAULT, true, 1, 18000, null, null, null, null, null, '00000000-0000-0000-0000-000000000003',
        '00000000-0000-0000-0000-000000000001', null, null, null, '치킨');

-- 장바구니 추가
INSERT INTO baedalsodae.p_cart (created_at, updated_at, id, store_id, user_id)
VALUES (null, null, '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000002',
        '00000000-0000-0000-0000-000000000001');

-- 장바구니 아이템 추가
INSERT INTO baedalsodae.p_cart_item (quantity, created_at, updated_at, cart_id, id, menu_item_id) VALUES (1, null, null, '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000003');

