package com.project.baedalsodae.store.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.dto.AddressRequest;
import com.project.baedalsodae.global.common.dto.AddressResponse;
import com.project.baedalsodae.review.dto.query.ReviewSummary;
import com.project.baedalsodae.review.dto.response.ReviewDetailResponse;
import com.project.baedalsodae.store.dto.request.store.CreateStoreRequest;
import com.project.baedalsodae.store.dto.request.store.StoreCursorRequest;
import com.project.baedalsodae.store.dto.request.store.UpdateStoreRequest;
import com.project.baedalsodae.store.dto.request.store.UpdateStoreStatusRequest;
import com.project.baedalsodae.store.dto.response.store.*;
import com.project.baedalsodae.store.entity.enums.SortType;
import com.project.baedalsodae.store.entity.enums.StoreStatus;
import com.project.baedalsodae.store.service.StoreCommandService;
import com.project.baedalsodae.store.service.StoreQueryService;
import com.project.baedalsodae.user.entity.UserRole;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StoreController.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class StoreControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private StoreCommandService storeCommandService;

    @MockitoBean private StoreQueryService storeQueryService;

    private static final UUID STORE_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UserDetailsImpl ownerDetails = createOwnerUserDetails(UUID.randomUUID());

    private static UserDetailsImpl createOwnerUserDetails(UUID userId) {
        return UserDetailsImpl.from(
                userId,
                "username",
                "password",
                UserRole.OWNER, // userRole
                false // isDeleted
                );
    }

    private StoreSummaryResponse storeSummary() {
        return StoreSummaryResponse.builder()
                .storeId(STORE_ID)
                .storeName("테스트 가게")
                .reviewCount(10)
                .avgRating(4.5)
                .storeStatus(StoreStatus.OPEN)
                .build();
    }

    private AddressRequest addressRequest() {
        return new AddressRequest("11", "서울특별시", "110", "강남구", "11010", "역삼동", "테헤란로 427", "위워크");
    }

    private AddressResponse addressResponse() {
        return new AddressResponse("11", "서울특별시", "110", "강남구", "11010", "역삼동", "테헤란로 427", "위워크");
    }

    @Test
    @DisplayName("카테고리별 가게 목록 조회 - 성공")
    void getStorePageByStoreCategory_success() throws Exception {

        StorePageResponse pageResponse =
                StorePageResponse.builder()
                        .storeCategoryId(CATEGORY_ID)
                        .storeCategoryName("한식")
                        .hasNext(false)
                        .storeCount(1)
                        .lastCursorId(STORE_ID)
                        .stores(List.of(storeSummary()))
                        .build();

        given(storeQueryService.getStorePage(eq(CATEGORY_ID), any(StoreCursorRequest.class)))
                .willReturn(pageResponse);

        mockMvc.perform(
                        get("/stores/categories")
                                .with(user(ownerDetails))
                                .param("storeCategoryId", CATEGORY_ID.toString())
                                .param("sortType", "LATEST")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.storeCategoryId").value(CATEGORY_ID.toString()))
                .andExpect(jsonPath("$.data.storeCategoryName").value("한식"))
                .andExpect(jsonPath("$.data.stores[0].storeName").value("테스트 가게"))
                .andDo(
                        document(
                                "store/get-by-category",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                queryParameters(
                                        parameterWithName("storeCategoryId")
                                                .description("카테고리 UUID"),
                                        parameterWithName("sortType")
                                                .description("정렬 타입 (LATEST / RATING / REVIEW)")
                                                .optional(),
                                        parameterWithName("size")
                                                .description("페이지 크기 (기본값 10)")
                                                .optional(),
                                        parameterWithName("lastId")
                                                .description("마지막 커서 ID")
                                                .optional(),
                                        parameterWithName("lastCreatedAt")
                                                .description("마지막 생성일시 (LATEST 정렬 시)")
                                                .optional(),
                                        parameterWithName("lastRating")
                                                .description("마지막 평점 (RATING 정렬 시)")
                                                .optional(),
                                        parameterWithName("lastReviewCount")
                                                .description("마지막 리뷰 수 (REVIEW 정렬 시)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.storeCategoryId")
                                                .description("카테고리 UUID"),
                                        fieldWithPath("data.storeCategoryName")
                                                .description("카테고리 이름"),
                                        fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                                        fieldWithPath("data.storeCount").description("현재 페이지 가게 수"),
                                        fieldWithPath("data.lastCursorId").description("마지막 커서 ID"),
                                        fieldWithPath("data.stores[].storeId")
                                                .description("가게 UUID"),
                                        fieldWithPath("data.stores[].storeName")
                                                .description("가게 이름"),
                                        fieldWithPath("data.stores[].reviewCount")
                                                .description("리뷰 수"),
                                        fieldWithPath("data.stores[].avgRating")
                                                .description("평균 평점"),
                                        fieldWithPath("data.stores[].storeStatus")
                                                .description("가게 상태"))));
    }

    @Test
    @DisplayName("키워드로 가게 검색 - 성공")
    void getStorePageByKeyword_success() throws Exception {

        StoreSearchPageResponse searchResponse =
                StoreSearchPageResponse.builder()
                        .stores(List.of(storeSummary()))
                        .totalCount(1L)
                        .currentPage(1)
                        .hasPrevious(false)
                        .hasNext(false)
                        .build();

        given(
                        storeQueryService.getStoreByKeyword(
                                eq("테스트"), any(Pageable.class), eq(SortType.LATEST)))
                .willReturn(searchResponse);

        mockMvc.perform(
                        get("/stores/keywords")
                                .with(user(ownerDetails))
                                .param("keyword", "테스트")
                                .param("sortType", "LATEST")
                                .param("page", "0")
                                .param("size", "10")
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalCount").value(1))
                .andExpect(jsonPath("$.data.currentPage").value(1))
                .andExpect(jsonPath("$.data.stores[0].storeName").value("테스트 가게"))
                .andDo(
                        document(
                                "store/get-by-keyword",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                queryParameters(
                                        parameterWithName("keyword").description("검색 키워드"),
                                        parameterWithName("sortType")
                                                .description("정렬 타입 (LATEST / RATING / REVIEW)"),
                                        parameterWithName("page")
                                                .description("페이지 번호 (0-based)")
                                                .optional(),
                                        parameterWithName("size").description("페이지 크기").optional()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.stores[].storeId")
                                                .description("가게 UUID"),
                                        fieldWithPath("data.stores[].storeName")
                                                .description("가게 이름"),
                                        fieldWithPath("data.stores[].reviewCount")
                                                .description("리뷰 수"),
                                        fieldWithPath("data.stores[].avgRating")
                                                .description("평균 평점"),
                                        fieldWithPath("data.stores[].storeStatus")
                                                .description("가게 상태"),
                                        fieldWithPath("data.totalCount").description("전체 가게 수"),
                                        fieldWithPath("data.currentPage").description("현재 페이지 번호"),
                                        fieldWithPath("data.hasPrevious")
                                                .description("이전 페이지 존재 여부"),
                                        fieldWithPath("data.hasNext")
                                                .description("다음 페이지 존재 여부"))));
    }

    @Test
    @DisplayName("가게 상세 조회 - 성공")
    void getStoreDetail_success() throws Exception {

        StoreDetailResponse detailResponse =
                StoreDetailResponse.of(storeSummary(), List.of(), true, true);

        given(storeQueryService.getStoreDetail(STORE_ID, USER_ID)).willReturn(detailResponse);

        mockMvc.perform(
                        get("/stores/{storeId}", STORE_ID)
                                .with(user(ownerDetails))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.store.storeId").value(STORE_ID.toString()))
                .andExpect(jsonPath("$.data.store.storeName").value("테스트 가게"))
                .andDo(
                        document(
                                "store/get-detail",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("storeId").description("가게 UUID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.store.storeId").description("가게 UUID"),
                                        fieldWithPath("data.store.storeName").description("가게 이름"),
                                        fieldWithPath("data.store.reviewCount").description("리뷰 수"),
                                        fieldWithPath("data.store.avgRating").description("평균 평점"),
                                        fieldWithPath("data.store.storeStatus")
                                                .description("가게 상태"),
                                        fieldWithPath("data.storeMenuCategoryItems")
                                                .description("메뉴 카테고리 목록"))));
    }

    @Test
    @DisplayName("가게 리뷰 목록 및 요약 조회 - 성공")
    void getStoreReviews_success() throws Exception {
        // given
        ReviewDetailResponse review1 =
                new ReviewDetailResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        ownerDetails.getUserId(),
                        5,
                        "정말 맛있어요! 광화문 최고 맛집입니다.",
                        true);
        ReviewDetailResponse review2 =
                new ReviewDetailResponse(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        4,
                        "배달이 빨라요.",
                        false);

        ReviewSummary summary = new ReviewSummary(2L, 4.5);

        StoreReviewResponse response = StoreReviewResponse.of(List.of(review1, review2), summary);

        given(storeQueryService.getStoreReview(any(UUID.class), any(UUID.class)))
                .willReturn(response);

        // when & than
        mockMvc.perform(
                        get("/stores/{storeId}/reviews", STORE_ID)
                                .with(user(ownerDetails))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewSummary.totalReviews").value(2))
                .andExpect(jsonPath("$.data.reviewSummary.averageRating").value(4.5))
                .andExpect(jsonPath("$.data.reviews[0].isOwner").value(true))
                .andDo(
                        document(
                                "store/get-reviews",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(parameterWithName("storeId").description("가게 UUID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data.reviewSummary.totalReviews")
                                                .description("총 리뷰 개수"),
                                        fieldWithPath("data.reviewSummary.averageRating")
                                                .description("평균 별점"),
                                        fieldWithPath("data.reviews[]").description("리뷰 목록"),
                                        fieldWithPath("data.reviews[].reviewId")
                                                .description("리뷰 UUID"),
                                        fieldWithPath("data.reviews[].orderId")
                                                .description("주문 UUID"),
                                        fieldWithPath("data.reviews[].userId")
                                                .description("작성자 UUID"),
                                        fieldWithPath("data.reviews[].rating").description("별점"),
                                        fieldWithPath("data.reviews[].comment")
                                                .description("리뷰 내용"),
                                        fieldWithPath("data.reviews[].isOwner")
                                                .description("본인 작성 여부"))));
    }

    @Test
    @DisplayName("점주 가게 조회 - 성공")
    void getOwnerStore_success() throws Exception {

        OwnerStoreResponse ownerResponse =
                OwnerStoreResponse.builder()
                        .storeId(UUID.randomUUID())
                        .storeName("감자네 치킨")
                        .phoneNumber("02-1234-5678")
                        .businessNumber("123-45-67890")
                        .description("맛있는 치킨집입니다.")
                        .reviewCount(10)
                        .avgRating(4.5)
                        .storeStatus("OPEN")
                        .address(addressResponse())
                        .build();

        given(storeQueryService.getOwnerStore(any(), any(), any())).willReturn(ownerResponse);

        mockMvc.perform(
                        get("/stores/{storeId}/manage", STORE_ID)
                                .with(user(ownerDetails))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "store/get-owner-store",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("storeId").description("가게 UUID"))));
    }

    @Test
    @DisplayName("가게 생성 - 성공")
    void createStore_success() throws Exception {
        CreateStoreRequest request =
                new CreateStoreRequest(
                        CATEGORY_ID,
                        "교촌치킨",
                        "123-45-67890",
                        "02-123-4567",
                        addressRequest(),
                        "맛있는 치킨");

        willDoNothing()
                .given(storeCommandService)
                .createStore(any(CreateStoreRequest.class), any(UUID.class));

        mockMvc.perform(
                        post("/stores")
                                .with(user(ownerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "store/create",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("storeCategoryId").description("카테고리 UUID"),
                                        fieldWithPath("storeName").description("수정할 가게 이름"),
                                        fieldWithPath("businessNumber").description("사업자 등록 번호"),
                                        fieldWithPath("storePhone").description("가게 전화번호"),
                                        fieldWithPath("description").description("가게 설명"),
                                        fieldWithPath("address").description("가게 주소"),
                                        fieldWithPath("address.sidoCode").description("시도 코드"),
                                        fieldWithPath("address.sidoName").description("시도 이름"),
                                        fieldWithPath("address.sigunguCode").description("시군구 코드"),
                                        fieldWithPath("address.sigunguName").description("시군구 이름"),
                                        fieldWithPath("address.dongCode").description("동 코드"),
                                        fieldWithPath("address.dongName").description("동 이름"),
                                        fieldWithPath("address.roadAddress").description("도로명 주소"),
                                        fieldWithPath("address.detailAddress")
                                                .description("상세 주소")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data")
                                                .description("응답 데이터 (null)")
                                                .optional())));
    }

    @Test
    @DisplayName("가게 수정 - 성공")
    void updateStore_success() throws Exception {

        UpdateStoreRequest request =
                new UpdateStoreRequest(
                        CATEGORY_ID, "BHC치킨", "02-987-6543", addressRequest(), "수정된 설명");

        willDoNothing()
                .given(storeCommandService)
                .updateStore(any(UpdateStoreRequest.class), eq(STORE_ID), any(UUID.class));

        mockMvc.perform(
                        patch("/stores/{storeId}", STORE_ID)
                                .with(user(ownerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "store/update",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("storeId").description("수정할 가게 UUID")),
                                requestFields(
                                        fieldWithPath("storeCategoryId").description("카테고리 UUID"),
                                        fieldWithPath("storeName").description("수정할 가게 이름"),
                                        fieldWithPath("storePhone").description("가게 전화번호"),
                                        fieldWithPath("description").description("가게 설명"),
                                        fieldWithPath("address").description("가게 주소"),
                                        fieldWithPath("address.sidoCode").description("시도 코드"),
                                        fieldWithPath("address.sidoName").description("시도 이름"),
                                        fieldWithPath("address.sigunguCode").description("시군구 코드"),
                                        fieldWithPath("address.sigunguName").description("시군구 이름"),
                                        fieldWithPath("address.dongCode").description("동 코드"),
                                        fieldWithPath("address.dongName").description("동 이름"),
                                        fieldWithPath("address.roadAddress").description("도로명 주소"),
                                        fieldWithPath("address.detailAddress")
                                                .description("상세 주소")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data")
                                                .description("응답 데이터 (null)")
                                                .optional())));
    }

    @Test
    @DisplayName("가게 상태 변경 - 성공")
    void updateStoreStatus_success() throws Exception {

        UpdateStoreStatusRequest request =
                new UpdateStoreStatusRequest(StoreStatus.TEMPORARILY_CLOSED);

        willDoNothing()
                .given(storeCommandService)
                .updateStoreOpened(eq(STORE_ID), eq(StoreStatus.SUSPENDED), any(UUID.class));

        mockMvc.perform(
                        patch("/stores/{storeId}/status", STORE_ID)
                                .with(user(ownerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "store/update-status",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("storeId")
                                                .description("상태를 변경할 가게 UUID")),
                                requestFields(
                                        fieldWithPath("status")
                                                .description("변경할 가게 상태 (OPEN / CLOSED)")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data")
                                                .description("응답 데이터 (null)")
                                                .optional())));
    }

    @Test
    @DisplayName("가게 삭제 - 성공")
    void deleteStore_success() throws Exception {

        willDoNothing().given(storeCommandService).deleteStore(eq(STORE_ID), any(UUID.class));

        mockMvc.perform(
                        delete("/stores/{storeId}", STORE_ID)
                                .with(user(ownerDetails))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "store/delete",
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("storeId").description("삭제할 가게 UUID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("timestamp").description("응답 타임스탬프"),
                                        fieldWithPath("data")
                                                .description("응답 데이터 (null)")
                                                .optional())));
    }
}
