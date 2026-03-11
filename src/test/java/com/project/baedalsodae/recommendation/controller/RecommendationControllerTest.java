package com.project.baedalsodae.recommendation.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.recommendation.dto.VoiceRecommendationRequest;
import com.project.baedalsodae.recommendation.dto.VoiceRecommendationResponse;
import com.project.baedalsodae.recommendation.service.MenuEmbeddingService;
import com.project.baedalsodae.recommendation.service.RecommendationService;
import com.project.baedalsodae.user.entity.UserRole;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RecommendationController.class)
@AutoConfigureMockMvc
@AutoConfigureRestDocs
class RecommendationControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private RecommendationService recommendationService;

    @MockitoBean private MenuEmbeddingService menuEmbeddingService;

    private UUID userId;
    private UserDetailsImpl managerDetails;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("00000000-0000-0000-0000-000000000010");
        managerDetails =
                UserDetailsImpl.from(userId, "admin_user", "password", UserRole.MANAGER, false);
    }

    @Nested
    @DisplayName("POST /recommendations/voice")
    class RecommendByVoice {

        @Test
        @DisplayName("정상적으로 메뉴를 추천한다")
        void recommendByVoice_success() throws Exception {
            //            UUID userId = UUID.randomUUID();

            // given
            VoiceRecommendationRequest request = new VoiceRecommendationRequest("치킨 추천해줘");

            List<VoiceRecommendationResponse.RecommendedMenu> menus =
                    List.of(
                            new VoiceRecommendationResponse.RecommendedMenu(
                                    UUID.randomUUID(),
                                    "고소한 깐풍기",
                                    4.5,
                                    UUID.randomUUID(),
                                    "후라이드 치킨",
                                    18000,
                                    "바삭한 후라이드 치킨"));

            VoiceRecommendationResponse response =
                    new VoiceRecommendationResponse("고소한 깐풍기의 후라이드 치킨을 추천드려요!", menus);

            given(
                            recommendationService.recommendMenuItems(
                                    any(VoiceRecommendationRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(
                            post("/recommendations/voice")
                                    .with(user(managerDetails))
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andDo(
                            document(
                                    "recommendations/voice",
                                    requestFields(
                                            fieldWithPath("transcribedText")
                                                    .description("음성 인식 텍스트")),
                                    responseFields(
                                            fieldWithPath("aiMessage").description("AI 추천 메시지"),
                                            fieldWithPath("recommendedMenus")
                                                    .description("추천 메뉴 목록"),
                                            fieldWithPath("recommendedMenus[].storeId")
                                                    .description("가게 ID"),
                                            fieldWithPath("recommendedMenus[].storeName")
                                                    .description("가게 이름"),
                                            fieldWithPath("recommendedMenus[].rating")
                                                    .description("가게 평점"),
                                            fieldWithPath("recommendedMenus[].menuId")
                                                    .description("메뉴 ID"),
                                            fieldWithPath("recommendedMenus[].menuName")
                                                    .description("메뉴 이름"),
                                            fieldWithPath("recommendedMenus[].price")
                                                    .description("메뉴 가격"),
                                            fieldWithPath("recommendedMenus[].description")
                                                    .description("메뉴 설명"))));
        }

        @Test
        @DisplayName("추천 결과가 없으면 안내 메시지를 반환한다")
        void recommendByVoice_emptyResult() throws Exception {
            // given
            VoiceRecommendationRequest request = new VoiceRecommendationRequest("치킨 추천해줘");

            VoiceRecommendationResponse response =
                    new VoiceRecommendationResponse("죄송해요, 원하시는 조건에 맞는 메뉴를 찾지 못했어요.", List.of());

            given(
                            recommendationService.recommendMenuItems(
                                    any(VoiceRecommendationRequest.class)))
                    .willReturn(response);

            // when & then
            mockMvc.perform(
                            post("/recommendations/voice")
                                    .with(user(managerDetails))
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.aiMessage").value("죄송해요, 원하시는 조건에 맞는 메뉴를 찾지 못했어요."))
                    .andExpect(jsonPath("$.recommendedMenus").isEmpty())
                    .andDo(print());
        }

        @Test
        @DisplayName("transcribedText가 없으면 400을 반환한다")
        void recommendByVoice_missingText() throws Exception {
            // given
            String invalidRequest =
                    """
                    {"userId": "00000000-0000-0000-0000-000000000010"}
                    """;

            // when & then
            mockMvc.perform(
                            post("/recommendations/voice")
                                    .with(user(managerDetails))
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(invalidRequest))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("POST /recommendations/sync-embeddings")
    class SyncEmbeddings {

        @Test
        @DisplayName("임베딩 동기화에 성공한다")
        void syncEmbeddings_success() throws Exception {
            // given
            doNothing().when(menuEmbeddingService).syncAllMenuItemsToVectorStore();

            // when & then
            mockMvc.perform(
                            post("/recommendations/sync-embeddings")
                                    .with(user(managerDetails))
                                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andDo(document("recommendations/sync-embeddings"));
        }

        @Test
        @DisplayName("임베딩 동기화 중 예외가 발생하면 500을 반환한다")
        void syncEmbeddings_failure() throws Exception {
            // given
            doThrow(new RuntimeException("VectorStore 연결 실패"))
                    .when(menuEmbeddingService)
                    .syncAllMenuItemsToVectorStore();

            // when & then
            mockMvc.perform(
                            post("/recommendations/sync-embeddings")
                                    .with(user(managerDetails))
                                    .with(csrf()))
                    .andExpect(status().isInternalServerError())
                    .andDo(print());
        }
    }
}
