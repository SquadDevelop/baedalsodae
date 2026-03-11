

CREATE TABLE IF NOT EXISTS baedalsodae.event (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    aggregate_id uuid,
    aggregate_type character varying(255) NOT NULL,
    event_type character varying(255) NOT NULL,
    payload jsonb,
    published_at timestamp(6) with time zone,
    retry_count integer NOT NULL,
    status character varying(255) NOT NULL,
    trace_id uuid
);

-- ALTER TABLE baedalsodae.event OWNER TO baedalsodae_admin;

--
-- Name: p_cart; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_cart (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    user_id uuid NOT NULL,
    store_id uuid NOT NULL
);


-- ALTER TABLE baedalsodae.p_cart OWNER TO baedalsodae_admin;

--
-- Name: p_cart_item; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_cart_item (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    quantity integer NOT NULL,
    cart_id uuid NOT NULL,
    menu_item_id uuid NOT NULL
);


-- ALTER TABLE baedalsodae.p_cart_item OWNER TO baedalsodae_admin;

--
-- Name: p_end_area; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_end_area (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    adm_code character varying(50) NOT NULL,
    location character varying(255),
    name character varying(100) NOT NULL,
    version bigint
);


-- ALTER TABLE baedalsodae.p_end_area OWNER TO baedalsodae_admin;

--
-- Name: p_menu_category; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_menu_category (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    created_by uuid,
    deleted_at timestamp(6) with time zone,
    deleted_by uuid,
    is_deleted boolean DEFAULT false,
    updated_by uuid,
    name character varying(255) NOT NULL,
    order_no integer,
    store_id uuid NOT NULL
);


-- ALTER TABLE baedalsodae.p_menu_category OWNER TO baedalsodae_admin;

--
-- Name: p_menu_item; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_menu_item (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    created_by uuid,
    deleted_at timestamp(6) with time zone,
    deleted_by uuid,
    is_deleted boolean DEFAULT false,
    updated_by uuid,
    description text,
    is_popular boolean NOT NULL,
    menu_status character varying(255),
    name character varying(255) NOT NULL,
    order_no integer,
    price integer NOT NULL,
    menu_category_id uuid NOT NULL,
    CONSTRAINT p_menu_item_menu_status_check CHECK (((menu_status)::text = ANY ((ARRAY['AVAILABLE'::character varying, 'UNAVAILABLE'::character varying, 'SOLD_OUT'::character varying, 'PREPARING'::character varying, 'HIDDEN'::character varying])::text[])))
);


-- ALTER TABLE baedalsodae.p_menu_item OWNER TO baedalsodae_admin;

--
-- Name: p_order; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_order (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    created_by uuid,
    deleted_at timestamp(6) with time zone,
    deleted_by uuid,
    is_deleted boolean DEFAULT false,
    updated_by uuid,
    address_id uuid NOT NULL,
    delivery_address_snapshot text,
    delivery_fee integer,
    delivery_request_note text,
    discount_amount integer,
    final_amount integer,
    order_no character varying(100),
    status character varying(255) NOT NULL,
    store_id uuid NOT NULL,
    store_name_snapshot character varying(50) NOT NULL,
    store_request_note text,
    total_amount integer,
    user_id uuid NOT NULL,
    user_nickname_snapshot character varying(255) NOT NULL,
    user_phone_snapshot character varying(255) NOT NULL,
    CONSTRAINT p_order_status_check CHECK (((status)::text = ANY ((ARRAY['CREATED'::character varying, 'REQUESTED'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'COOKING'::character varying, 'COOKED'::character varying, 'DELIVERING'::character varying, 'DELIVERED'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])))
);


-- ALTER TABLE  baedalsodae.p_order OWNER TO baedalsodae_admin;

--
-- Name: p_order_item; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_order_item (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    menu_item_id uuid NOT NULL,
    name_snapshot character varying(255),
    price_snapshot integer,
    quantity integer,
    order_id uuid NOT NULL
);


-- ALTER TABLE baedalsodae.p_order_item OWNER TO baedalsodae_admin;

--
-- Name: p_order_status_history; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_order_status_history (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    actor_id uuid,
    actor_type character varying(255) NOT NULL,
    from_status character varying(255),
    order_id uuid NOT NULL,
    reason text,
    to_status character varying(255),
    CONSTRAINT p_order_status_history_actor_type_check CHECK (((actor_type)::text = ANY ((ARRAY['SYSTEM'::character varying, 'CUSTOMER'::character varying, 'OWNER'::character varying, 'MANAGER'::character varying, 'MASTER'::character varying])::text[]))),
    CONSTRAINT p_order_status_history_from_status_check CHECK (((from_status)::text = ANY ((ARRAY['CREATED'::character varying, 'REQUESTED'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'COOKING'::character varying, 'COOKED'::character varying, 'DELIVERING'::character varying, 'DELIVERED'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[]))),
    CONSTRAINT p_order_status_history_to_status_check CHECK (((to_status)::text = ANY ((ARRAY['CREATED'::character varying, 'REQUESTED'::character varying, 'ACCEPTED'::character varying, 'REJECTED'::character varying, 'COOKING'::character varying, 'COOKED'::character varying, 'DELIVERING'::character varying, 'DELIVERED'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying])::text[])))
);


-- ALTER TABLE baedalsodae.p_order_status_history OWNER TO baedalsodae_admin;

--
-- Name: p_payment; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_payment (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    amount numeric(38,2) NOT NULL,
    created_by uuid NOT NULL,
    order_id uuid NOT NULL,
    paid_at timestamp(6) without time zone,
    payment_method smallint NOT NULL,
    pg_transaction_id character varying(255),
    payment_status character varying(255) NOT NULL,
    updated_by uuid NOT NULL,
    user_id uuid NOT NULL,
    CONSTRAINT p_payment_payment_method_check CHECK (((payment_method >= 0) AND (payment_method <= 4))),
    CONSTRAINT p_payment_payment_status_check CHECK (((payment_status)::text = ANY ((ARRAY['PENDING'::character varying, 'SUCCESS'::character varying, 'FAILED'::character varying, 'CANCELED'::character varying, 'REFUNDING'::character varying, 'REFUNDED'::character varying])::text[])))
);


-- ALTER TABLE baedalsodae.p_payment OWNER TO baedalsodae_admin;

--
-- Name: p_sido_area; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_sido_area (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    adm_code character varying(50) NOT NULL,
    name character varying(100) NOT NULL,
    version bigint
);


-- ALTER TABLE baedalsodae.p_sido_area OWNER TO baedalsodae_admin;

--
-- Name: p_sigg_area; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_sigg_area (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    adm_code character varying(50) NOT NULL,
    name character varying(100) NOT NULL,
    version bigint
);


-- ALTER TABLE baedalsodae.p_sigg_area OWNER TO baedalsodae_admin;

--
-- Name: p_store; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_store (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    created_by uuid,
    deleted_at timestamp(6) with time zone,
    deleted_by uuid,
    is_deleted boolean DEFAULT false,
    updated_by uuid,
    detail_address character varying(255) NOT NULL,
    dong_code character varying(20) NOT NULL,
    dong_name character varying(50) NOT NULL,
    road_address character varying(255) NOT NULL,
    sido_code character varying(20) NOT NULL,
    sido_name character varying(50) NOT NULL,
    sigungu_code character varying(20) NOT NULL,
    sigungu_name character varying(50) NOT NULL,
    avg_rating double precision NOT NULL,
    business_number character varying(15) NOT NULL,
    description text,
    name character varying(50) NOT NULL,
    phone character varying(20) NOT NULL,
    review_count integer NOT NULL,
    store_status character varying(255) NOT NULL,
    user_id uuid NOT NULL,
    store_category_id uuid NOT NULL,
    CONSTRAINT p_store_store_status_check CHECK (((store_status)::text = ANY ((ARRAY['OPEN'::character varying, 'TEMPORARILY_CLOSED'::character varying, 'SUSPENDED'::character varying, 'PENDING_APPROVAL'::character varying])::text[])))
);


-- ALTER TABLE baedalsodae.p_store OWNER TO baedalsodae_admin;

--
-- Name: p_store_category; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_store_category (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    created_by uuid,
    deleted_at timestamp(6) with time zone,
    deleted_by uuid,
    is_deleted boolean DEFAULT false,
    updated_by uuid,
    description character varying(200),
    name character varying(50)
);


-- ALTER TABLE baedalsodae.p_store_category OWNER TO baedalsodae_admin;

--
-- Name: p_store_hours; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_store_hours (
    id uuid NOT NULL,
    break_end timestamp(6) with time zone,
    break_start timestamp(6) with time zone,
    close_time timestamp(6) with time zone,
    day_of_week character varying(3),
    is_open boolean DEFAULT true NOT NULL,
    open_time timestamp(6) with time zone,
    store_id uuid NOT NULL,
    CONSTRAINT p_store_hours_day_of_week_check CHECK (((day_of_week)::text = ANY ((ARRAY['MONDAY'::character varying, 'TUESDAY'::character varying, 'WEDNESDAY'::character varying, 'THURSDAY'::character varying, 'FRIDAY'::character varying, 'SATURDAY'::character varying, 'SUNDAY'::character varying])::text[])))
);


-- ALTER TABLE baedalsodae.p_store_hours OWNER TO baedalsodae_admin;

--
-- Name: p_tag; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_tag (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    name character varying(255) NOT NULL
);


-- ALTER TABLE baedalsodae.p_tag OWNER TO baedalsodae_admin;

--
-- Name: p_tag_mapping; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_tag_mapping (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone NOT NULL,
    order_no integer NOT NULL,
    menu_item uuid NOT NULL,
    tag_id uuid NOT NULL
);


-- ALTER TABLE baedalsodae.p_tag_mapping OWNER TO baedalsodae_admin;

--
-- Name: p_user; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_user (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    created_by uuid,
    deleted_at timestamp(6) with time zone,
    deleted_by uuid,
    is_deleted boolean DEFAULT false,
    updated_by uuid,
    email character varying(255) NOT NULL,
    name character varying(100) NOT NULL,
    nickname character varying(100) NOT NULL,
    password character varying(255) NOT NULL,
    phone character varying(100) NOT NULL,
    role character varying(255) NOT NULL,
    user_main_address_id uuid,
    username character varying(20) NOT NULL,
    CONSTRAINT p_user_role_check CHECK (((role)::text = ANY ((ARRAY['CUSTOMER'::character varying, 'OWNER'::character varying, 'MANAGER'::character varying, 'MASTER'::character varying])::text[])))
);


-- ALTER TABLE baedalsodae.p_user OWNER TO baedalsodae_admin;

--
-- Name: p_user_address; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.p_user_address (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    description character varying(255),
    detail_address character varying(255) NOT NULL,
    road_address character varying(255) NOT NULL,
    user_id uuid NOT NULL
);


-- ALTER TABLE baedalsodae.p_user_address OWNER TO baedalsodae_admin;

--
-- Name: review; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.review (
    id uuid NOT NULL,
    created_at timestamp(6) with time zone,
    updated_at timestamp(6) with time zone,
    created_by uuid,
    deleted_at timestamp(6) with time zone,
    deleted_by uuid,
    is_deleted boolean DEFAULT false,
    updated_by uuid,
    content character varying(255),
    is_hidden boolean NOT NULL,
    order_id uuid,
    rating double precision NOT NULL,
    user_id uuid
);


-- ALTER TABLE baedalsodae.review OWNER TO baedalsodae_admin;

--
-- Name: vector_store; Type: TABLE; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE TABLE IF NOT EXISTS baedalsodae.vector_store (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    content text,
    metadata json,
    embedding public.vector(1536)
);


-- ALTER TABLE baedalsodae.vector_store OWNER TO baedalsodae_admin;

--
-- Name: event event_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.event
    ADD CONSTRAINT event_pkey PRIMARY KEY (id);


--
-- Name: p_cart_item p_cart_item_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_cart_item
    ADD CONSTRAINT p_cart_item_pkey PRIMARY KEY (id);


--
-- Name: p_cart p_cart_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_cart
    ADD CONSTRAINT p_cart_pkey PRIMARY KEY (id);


--
-- Name: p_end_area p_end_area_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_end_area
    ADD CONSTRAINT p_end_area_pkey PRIMARY KEY (id);


--
-- Name: p_menu_category p_menu_category_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_menu_category
    ADD CONSTRAINT p_menu_category_pkey PRIMARY KEY (id);


--
-- Name: p_menu_item p_menu_item_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_menu_item
    ADD CONSTRAINT p_menu_item_pkey PRIMARY KEY (id);


--
-- Name: p_order_item p_order_item_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_order_item
    ADD CONSTRAINT p_order_item_pkey PRIMARY KEY (id);


--
-- Name: p_order p_order_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_order
    ADD CONSTRAINT p_order_pkey PRIMARY KEY (id);


--
-- Name: p_order_status_history p_order_status_history_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_order_status_history
    ADD CONSTRAINT p_order_status_history_pkey PRIMARY KEY (id);


--
-- Name: p_payment p_payment_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_payment
    ADD CONSTRAINT p_payment_pkey PRIMARY KEY (id);


--
-- Name: p_sido_area p_sido_area_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_sido_area
    ADD CONSTRAINT p_sido_area_pkey PRIMARY KEY (id);


--
-- Name: p_sigg_area p_sigg_area_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_sigg_area
    ADD CONSTRAINT p_sigg_area_pkey PRIMARY KEY (id);


--
-- Name: p_store_category p_store_category_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_store_category
    ADD CONSTRAINT p_store_category_pkey PRIMARY KEY (id);


--
-- Name: p_store_hours p_store_hours_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_store_hours
    ADD CONSTRAINT p_store_hours_pkey PRIMARY KEY (id);


--
-- Name: p_store p_store_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_store
    ADD CONSTRAINT p_store_pkey PRIMARY KEY (id);


--
-- Name: p_tag_mapping p_tag_mapping_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_tag_mapping
    ADD CONSTRAINT p_tag_mapping_pkey PRIMARY KEY (id);


--
-- Name: p_tag p_tag_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_tag
    ADD CONSTRAINT p_tag_pkey PRIMARY KEY (id);


--
-- Name: p_user_address p_user_address_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_user_address
    ADD CONSTRAINT p_user_address_pkey PRIMARY KEY (id);


--
-- Name: p_user p_user_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_user
    ADD CONSTRAINT p_user_pkey PRIMARY KEY (id);


--
-- Name: review review_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.review
    ADD CONSTRAINT review_pkey PRIMARY KEY (id);


--
-- Name: vector_store vector_store_pkey; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.vector_store
    ADD CONSTRAINT vector_store_pkey PRIMARY KEY (id);


--
-- Name: p_end_area uk48liqt663gvogebd75xwexsa3; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_end_area
    ADD CONSTRAINT uk48liqt663gvogebd75xwexsa3 UNIQUE (adm_code);


--
-- Name: p_user uk9739vq99qad264nukqskib9l5; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_user
    ADD CONSTRAINT uk9739vq99qad264nukqskib9l5 UNIQUE (nickname);


--
-- Name: p_sigg_area ukc6wffef85ir8q7isat0o44ebb; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_sigg_area
    ADD CONSTRAINT ukc6wffef85ir8q7isat0o44ebb UNIQUE (adm_code);


--
-- Name: p_order ukdmica10vdoubxovddj7cf66rj; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_order
    ADD CONSTRAINT ukdmica10vdoubxovddj7cf66rj UNIQUE (order_no);


--
-- Name: p_tag ukk97bmi9ssbd7u65hmv1dx4udc; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_tag
    ADD CONSTRAINT ukk97bmi9ssbd7u65hmv1dx4udc UNIQUE (name);


--
-- Name: p_user ukogywo3ggsigo9oljx32xua8hg; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_user
    ADD CONSTRAINT ukogywo3ggsigo9oljx32xua8hg UNIQUE (email);


--
-- Name: p_user ukpk8x5a850e4nwxtqdk3yj2e40; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_user
    ADD CONSTRAINT ukpk8x5a850e4nwxtqdk3yj2e40 UNIQUE (username);


--
-- Name: p_sido_area uktfnfpk2sjgauwc9u1wfhtcfoe; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_sido_area
    ADD CONSTRAINT uktfnfpk2sjgauwc9u1wfhtcfoe UNIQUE (adm_code);


--
-- Name: p_cart_item uq_cart_item_menu_item; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_cart_item
    ADD CONSTRAINT uq_cart_item_menu_item UNIQUE (cart_id, menu_item_id);


--
-- Name: p_cart uq_cart_user_store; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_cart
    ADD CONSTRAINT uq_cart_user_store UNIQUE (user_id);


--
-- Name: p_menu_category uq_menu_category_order_no; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_menu_category
    ADD CONSTRAINT uq_menu_category_order_no UNIQUE (store_id, order_no);


--
-- Name: p_menu_item uq_menu_item_order_no; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_menu_item
    ADD CONSTRAINT uq_menu_item_order_no UNIQUE (menu_category_id, order_no);


--
-- Name: p_store uq_store_business_number; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_store
    ADD CONSTRAINT uq_store_business_number UNIQUE (business_number);


--
-- Name: p_store_hours uq_store_hours_day; Type: CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_store_hours
    ADD CONSTRAINT uq_store_hours_day UNIQUE (store_id, day_of_week);


--
-- Name: uq_menu_category_name_active; Type: INDEX; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE UNIQUE INDEX uq_menu_category_name_active ON baedalsodae.p_menu_category USING btree (store_id, name) WHERE (is_deleted = false);


--
-- Name: vector_store_embedding_idx; Type: INDEX; Schema: baedalsodae; Owner: baedalsodae_admin
--

CREATE INDEX vector_store_embedding_idx ON baedalsodae.vector_store USING hnsw (embedding public.vector_cosine_ops);


--
-- Name: p_menu_category fk1eujvnaldty37vg6tpkac2qby; Type: FK CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_menu_category
    ADD CONSTRAINT fk1eujvnaldty37vg6tpkac2qby FOREIGN KEY (store_id) REFERENCES baedalsodae.p_store(id);


--
-- Name: p_cart fk1j68lwv22q6o3emn797nwjyqu; Type: FK CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_cart
    ADD CONSTRAINT fk1j68lwv22q6o3emn797nwjyqu FOREIGN KEY (store_id) REFERENCES baedalsodae.p_store(id);


--
-- Name: p_tag_mapping fk5c2mn0ca2wow2thgcwnktm26m; Type: FK CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_tag_mapping
    ADD CONSTRAINT fk5c2mn0ca2wow2thgcwnktm26m FOREIGN KEY (menu_item) REFERENCES baedalsodae.p_menu_item(id);


--
-- Name: p_cart_item fk_cart_item_cart_id; Type: FK CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_cart_item
    ADD CONSTRAINT fk_cart_item_cart_id FOREIGN KEY (cart_id) REFERENCES baedalsodae.p_cart(id);


--
-- Name: p_cart_item fk_cart_item_menu_item_id; Type: FK CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_cart_item
    ADD CONSTRAINT fk_cart_item_menu_item_id FOREIGN KEY (menu_item_id) REFERENCES baedalsodae.p_menu_item(id);


--
-- Name: p_order_item fk_order_item_order_id; Type: FK CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_order_item
    ADD CONSTRAINT fk_order_item_order_id FOREIGN KEY (order_id) REFERENCES baedalsodae.p_order(id);


--
-- Name: p_store fk_store_category; Type: FK CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_store
    ADD CONSTRAINT fk_store_category FOREIGN KEY (store_category_id) REFERENCES baedalsodae.p_store_category(id);


--
-- Name: p_store_hours fk_store_hours_store; Type: FK CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_store_hours
    ADD CONSTRAINT fk_store_hours_store FOREIGN KEY (store_id) REFERENCES baedalsodae.p_store(id);


--
-- Name: p_menu_item fkahud6ajfc8tdjoa2hslwatpe5; Type: FK CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_menu_item
    ADD CONSTRAINT fkahud6ajfc8tdjoa2hslwatpe5 FOREIGN KEY (menu_category_id) REFERENCES baedalsodae.p_menu_category(id);


--
-- Name: p_user_address fkkrjfeprj94j7tv40rym506du8; Type: FK CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_user_address
    ADD CONSTRAINT fkkrjfeprj94j7tv40rym506du8 FOREIGN KEY (user_id) REFERENCES baedalsodae.p_user(id);


--
-- Name: p_tag_mapping fkrlbjt95f440hlg1f1fg9121kp; Type: FK CONSTRAINT; Schema: baedalsodae; Owner: baedalsodae_admin
--

ALTER TABLE ONLY baedalsodae.p_tag_mapping
    ADD CONSTRAINT fkrlbjt95f440hlg1f1fg9121kp FOREIGN KEY (tag_id) REFERENCES baedalsodae.p_tag(id);




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
ON CONFLICT DO NOTHING;

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
ON CONFLICT DO NOTHING;

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
ON CONFLICT DO NOTHING;

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
ON CONFLICT DO NOTHING;

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
ON CONFLICT DO NOTHING;

-- user_main_address_id 업데이트
UPDATE baedalsodae.p_user u
SET user_main_address_id = (
    SELECT a.id FROM baedalsodae.p_user_address a WHERE a.user_id = u.id LIMIT 1
)
WHERE u.user_main_address_id IS NULL
  AND EXISTS (SELECT 1 FROM baedalsodae.p_user_address a WHERE a.user_id = u.id);

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
);

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
ON CONFLICT DO NOTHING;

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
) st ON st.rn = i;

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
) mc ON mc.rn = i;

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
ON CONFLICT DO NOTHING;

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
ON CONFLICT DO NOTHING;

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
) st ON st.rn = ((i - 1) % 90) + 1;

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
) mi ON mi.rn = o.rn;

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
FROM baedalsodae.p_order o;

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
END $$;
