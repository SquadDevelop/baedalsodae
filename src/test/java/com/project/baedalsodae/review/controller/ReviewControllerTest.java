package com.project.baedalsodae.review.controller;

import static com.project.baedalsodae.review.fixture.ReviewTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.global.common.TimeCursorPage;
import com.project.baedalsodae.review.dto.request.ReviewRequest;
import com.project.baedalsodae.review.dto.response.ReviewResponse;
import com.project.baedalsodae.review.service.ReviewService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class ReviewControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private ReviewService reviewService;

    private UUID userId;
    private UUID reviewId;
    private UUID orderId;
    private UserDetailsImpl customerDetails;
    private ReviewResponse reviewResponse;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        reviewId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        customerDetails = createCustomerUserDetails(userId);
        reviewResponse =
                new ReviewResponse(reviewId, orderId, userId, DEFAULT_RATING, DEFAULT_COMMENT);
    }

    @Test
    @DisplayName("성공 - 리뷰 목록 조회")
    void getReviews_success() throws Exception {
        TimeCursorPage<List<ReviewResponse>> page =
                TimeCursorPage.of(List.of(reviewResponse), false, null);
        given(reviewService.getReviewsByUser(any(), any(), eq(10))).willReturn(page);

        mockMvc.perform(
                        get("/reviews")
                                .with(user(customerDetails))
                                .param("userId", userId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].rating").value(DEFAULT_RATING))
                .andExpect(jsonPath("$.data.content[0].comment").value(DEFAULT_COMMENT))
                .andExpect(jsonPath("$.data.hasNext").value(false))
                .andDo(
                        document(
                                "review/get-list",
                                queryParameters(
                                        parameterWithName("userId").description("사용자 ID"),
                                        parameterWithName("cursor")
                                                .description("커서 (이전 페이지 마지막 시각, ISO-8601)")
                                                .optional(),
                                        parameterWithName("size")
                                                .description("페이지 크기 (기본값: 10)")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.content[].reviewId")
                                                .description("리뷰 ID"),
                                        fieldWithPath("data.content[].orderId")
                                                .description("주문 ID"),
                                        fieldWithPath("data.content[].userId")
                                                .description("작성자 ID"),
                                        fieldWithPath("data.content[].rating")
                                                .description("평점 (1~5)"),
                                        fieldWithPath("data.content[].comment")
                                                .description("리뷰 내용"),
                                        fieldWithPath("data.hasNext").description("다음 페이지 존재 여부"),
                                        fieldWithPath("data.nextCursor")
                                                .description("다음 커서 값")
                                                .optional())));
    }

    @Test
    @DisplayName("성공 - 리뷰 상세 조회")
    void getReviewDetail_success() throws Exception {
        given(reviewService.getReviewDetail(any(), eq(reviewId))).willReturn(reviewResponse);

        mockMvc.perform(
                        get("/reviews/{reviewId}", reviewId)
                                .with(user(customerDetails))
                                .param("userId", userId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewId").value(reviewId.toString()))
                .andExpect(jsonPath("$.data.rating").value(DEFAULT_RATING))
                .andExpect(jsonPath("$.data.comment").value(DEFAULT_COMMENT))
                .andDo(
                        document(
                                "review/get-detail",
                                pathParameters(parameterWithName("reviewId").description("리뷰 ID")),
                                queryParameters(parameterWithName("userId").description("사용자 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.reviewId").description("리뷰 ID"),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.userId").description("작성자 ID"),
                                        fieldWithPath("data.rating").description("평점 (1~5)"),
                                        fieldWithPath("data.comment").description("리뷰 내용"))));
    }

    @Test
    @DisplayName("성공 - 리뷰 수정")
    void updateReview_success() throws Exception {
        ReviewRequest request = new ReviewRequest(UPDATED_RATING, UPDATED_COMMENT);
        ReviewResponse updatedResponse =
                new ReviewResponse(reviewId, orderId, userId, UPDATED_RATING, UPDATED_COMMENT);
        given(reviewService.updateReview(any(), eq(reviewId), any())).willReturn(updatedResponse);

        mockMvc.perform(
                        put("/reviews/{reviewId}", reviewId)
                                .with(user(customerDetails))
                                .param("userId", userId.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.rating").value(UPDATED_RATING))
                .andExpect(jsonPath("$.data.comment").value(UPDATED_COMMENT))
                .andDo(
                        document(
                                "review/update",
                                pathParameters(parameterWithName("reviewId").description("리뷰 ID")),
                                queryParameters(parameterWithName("userId").description("사용자 ID")),
                                requestFields(
                                        fieldWithPath("rating").description("변경할 평점 (1~5)"),
                                        fieldWithPath("comment").description("변경할 리뷰 내용")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.reviewId").description("리뷰 ID"),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.userId").description("작성자 ID"),
                                        fieldWithPath("data.rating").description("평점 (1~5)"),
                                        fieldWithPath("data.comment").description("리뷰 내용"))));
    }

    @Test
    @DisplayName("성공 - 리뷰 삭제")
    void deleteReview_success() throws Exception {
        given(reviewService.deleteReview(any(), eq(reviewId))).willReturn(reviewResponse);

        mockMvc.perform(
                        delete("/reviews/{reviewId}", reviewId)
                                .with(user(customerDetails))
                                .param("userId", userId.toString()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewId").value(reviewId.toString()))
                .andDo(
                        document(
                                "review/delete",
                                pathParameters(parameterWithName("reviewId").description("리뷰 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.reviewId").description("리뷰 ID"),
                                        fieldWithPath("data.orderId").description("주문 ID"),
                                        fieldWithPath("data.userId").description("작성자 ID"),
                                        fieldWithPath("data.rating").description("평점 (1~5)"),
                                        fieldWithPath("data.comment").description("리뷰 내용"))));
    }
}
