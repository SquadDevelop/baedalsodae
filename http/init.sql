-- 유저 추가1
INSERT INTO baedalsodae.p_user (is_deleted, created_at, deleted_at, updated_at, created_by, deleted_by, id, updated_by,
                                user_main_address_id, username, name, nickname, phone, email, password, role)
VALUES (DEFAULT, null, null, null, null, null, '00000000-0000-0000-0000-000000000010', null, null, 'jaebin1234', '정재빈',
        '억울해유', '01011112222', 'jaebin1234@naver.com', '1234', 'CUSTOMER');

-- 유저 추가2
INSERT INTO baedalsodae.p_user (is_deleted, created_at, deleted_at, updated_at, created_by, deleted_by, id, updated_by,
                                user_main_address_id, username, name, nickname, phone, email, password, role)
VALUES (DEFAULT, null, null, null, null, null, '00000000-0000-0000-0000-000000000011', null, null, 'jaebin12345', '정재빈2',
        '억울해유2', '01011112223', 'jaebin1235@naver.com', '1234', 'OWNER');


-- 가게 카테고리 추가
INSERT INTO baedalsodae.p_store_category (is_deleted, created_at, deleted_at, updated_at, created_by, deleted_by, id,
                                          updated_by, name, description)
VALUES (DEFAULT, null, null, null, null, null, '00000000-0000-0000-0000-000000000001', null, '중식', null);

-- 가게 추가
INSERT INTO baedalsodae.p_store (is_deleted, rating_sum, review_count, created_at, deleted_at, updated_at,
                                 business_number, created_by, deleted_by, id, store_category_id, updated_by, user_id,
                                 dong_code, phone, sido_code, sigungu_code, dong_name, name, sido_name, sigungu_name,
                                 description, detail_address, road_address, store_status)
VALUES (DEFAULT, 0, 0, null, null, null, '1111111111', null, null, '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001', null, '00000000-0000-0000-0000-000000000001', '1', '01011111111', '2',
        '3', '포일동', '가게1', '경기도', '안양시', null, '오금로', '16', 'OPEN');


-- 메뉴 카테고리 추가
INSERT INTO baedalsodae.p_menu_category (is_deleted, order_no, created_at, deleted_at, updated_at, created_by,
                                         deleted_by, id, store_id, updated_by, name)
VALUES (DEFAULT, 1, null, null, null, null, null, '00000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001', null, '인기 메뉴');

-- 메뉴 추가
INSERT INTO baedalsodae.p_menu_item (is_deleted, is_popular, order_no, price, created_at, deleted_at, updated_at,
                                     created_by, deleted_by, id, menu_category_id, updated_by, description, menu_status,
                                     name)
VALUES (DEFAULT, true, 1, 18000, null, null, null, null, null, '00000000-0000-0000-0000-000000000003',
        '00000000-0000-0000-0000-000000000001', null, null, null, '치킨');
--
-- -- 장바구니 추가
-- INSERT INTO baedalsodae.p_cart (created_at, updated_at, id, store_id, user_id)
-- VALUES (null, null, '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001',
--         '00000000-0000-0000-0000-000000000001');
--
-- -- 장바구니 아이템 추가
-- INSERT INTO baedalsodae.p_cart_item (quantity, created_at, updated_at, cart_id, id, menu_item_id)
-- VALUES (1, null, null, '00000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001',
--         '00000000-0000-0000-0000-000000000003');

