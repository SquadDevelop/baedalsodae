package com.project.baedalsodae.menu.controller;

import static com.project.baedalsodae.menu.fixture.MenuTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
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
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPutRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import com.project.baedalsodae.menu.dto.responseDto.item.MenuItemResponseDto;
import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import com.project.baedalsodae.menu.fixture.MenuItemRequestFixture;
import com.project.baedalsodae.menu.service.MenuItemService;
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

@WebMvcTest(MenuItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class MenuItemControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private MenuItemService menuItemService;

    private UUID menuItemId;
    private UUID menuCategoryId;
    private UserDetailsImpl ownerDetails;
    private MenuItemResponseDto itemResponse;

    @BeforeEach
    void setUp() {
        menuItemId = UUID.randomUUID();
        menuCategoryId = UUID.randomUUID();
        ownerDetails = createOwnerUserDetails(UUID.randomUUID());

        MenuCategoryResponseDto categoryResponse =
                new MenuCategoryResponseDto(menuCategoryId, DEFAULT_CATEGORY_NAME, 1);
        itemResponse =
                new MenuItemResponseDto(
                        menuItemId,
                        DEFAULT_MENU_ITEM_NAME,
                        DEFAULT_ITEM_DESCRIPTION,
                        DEFAULT_ITEM_PRICE,
                        1,
                        false,
                        MenuStatus.AVAILABLE,
                        categoryResponse,
                        List.of(TAG_1, TAG_2));
    }

    @Test
    @DisplayName("성공 - 메뉴 아이템 전체 수정")
    void updateMenuItem_success() throws Exception {
        MenuItemPutRequestDto request =
                MenuItemRequestFixture.aPutRequest().withCategoryId(menuCategoryId).build();
        given(menuItemService.updateMenuItem(eq(menuItemId), any(), any()))
                .willReturn(itemResponse);

        mockMvc.perform(
                        put("/menu-items/{menuItemId}", menuItemId)
                                .with(user(ownerDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(DEFAULT_MENU_ITEM_NAME))
                .andDo(
                        document(
                                "menu-item/update",
                                pathParameters(
                                        parameterWithName("menuItemId").description("메뉴 아이템 ID")),
                                requestFields(
                                        fieldWithPath("name").description("메뉴 이름"),
                                        fieldWithPath("description")
                                                .description("메뉴 설명")
                                                .optional(),
                                        fieldWithPath("price").description("가격"),
                                        fieldWithPath("isPopular").description("인기 메뉴 여부"),
                                        fieldWithPath("categoryId").description("이동할 카테고리 ID"),
                                        fieldWithPath("menuStatus")
                                                .description(
                                                        "메뉴 상태 (`AVAILABLE`, `SOLD_OUT`, `HIDDEN`)"),
                                        fieldWithPath("tagNames").description("태그 목록").optional()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.id").description("메뉴 아이템 ID"),
                                        fieldWithPath("data.name").description("메뉴 이름"),
                                        fieldWithPath("data.description").description("메뉴 설명"),
                                        fieldWithPath("data.price").description("가격"),
                                        fieldWithPath("data.orderNo").description("정렬 순서"),
                                        fieldWithPath("data.isPopular").description("인기 메뉴 여부"),
                                        fieldWithPath("data.menuStatus")
                                                .description(
                                                        "메뉴 상태 (`AVAILABLE`, `SOLD_OUT`, `HIDDEN`)"),
                                        fieldWithPath("data.category.id").description("카테고리 ID"),
                                        fieldWithPath("data.category.name").description("카테고리 이름"),
                                        fieldWithPath("data.category.orderNo")
                                                .description("카테고리 정렬 순서"),
                                        fieldWithPath("data.tagNames").description("태그 목록"))));
    }

    @Test
    @DisplayName("성공 - 메뉴 아이템 부분 수정")
    void patchMenuItem_success() throws Exception {
        MenuItemPatchRequestDto request =
                MenuItemRequestFixture.createPatchRequestWithName(ALTERNATIVE_MENU_ITEM_NAME);
        given(menuItemService.patchMenuItem(eq(menuItemId), any(), any())).willReturn(itemResponse);

        mockMvc.perform(
                        patch("/menu-items/{menuItemId}", menuItemId)
                                .with(user(ownerDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "menu-item/patch",
                                pathParameters(
                                        parameterWithName("menuItemId").description("메뉴 아이템 ID")),
                                requestFields(
                                        fieldWithPath("name").description("변경할 메뉴 이름").optional(),
                                        fieldWithPath("description")
                                                .description("변경할 메뉴 설명")
                                                .optional(),
                                        fieldWithPath("price").description("변경할 가격").optional(),
                                        fieldWithPath("isPopular")
                                                .description("변경할 인기 메뉴 여부")
                                                .optional(),
                                        fieldWithPath("categoryId")
                                                .description("이동할 카테고리 ID")
                                                .optional(),
                                        fieldWithPath("menuStatus")
                                                .description(
                                                        "변경할 메뉴 상태 (`AVAILABLE`, `SOLD_OUT`, `HIDDEN`)")
                                                .optional(),
                                        fieldWithPath("tagNames")
                                                .description("변경할 태그 목록")
                                                .optional()),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.id").description("메뉴 아이템 ID"),
                                        fieldWithPath("data.name").description("메뉴 이름"),
                                        fieldWithPath("data.description").description("메뉴 설명"),
                                        fieldWithPath("data.price").description("가격"),
                                        fieldWithPath("data.orderNo").description("정렬 순서"),
                                        fieldWithPath("data.isPopular").description("인기 메뉴 여부"),
                                        fieldWithPath("data.menuStatus")
                                                .description(
                                                        "메뉴 상태 (`AVAILABLE`, `SOLD_OUT`, `HIDDEN`)"),
                                        fieldWithPath("data.category.id").description("카테고리 ID"),
                                        fieldWithPath("data.category.name").description("카테고리 이름"),
                                        fieldWithPath("data.category.orderNo")
                                                .description("카테고리 정렬 순서"),
                                        fieldWithPath("data.tagNames").description("태그 목록"))));
    }

    @Test
    @DisplayName("성공 - 메뉴 아이템 순서 변경")
    void updateMenuItemOrder_success() throws Exception {
        given(menuItemService.updateMenuItemOrder(eq(menuItemId), eq(2), any()))
                .willReturn(itemResponse);

        mockMvc.perform(
                        patch("/menu-items/{menuItemId}/orders?order=2", menuItemId)
                                .with(user(ownerDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "menu-item/update-order",
                                pathParameters(
                                        parameterWithName("menuItemId").description("메뉴 아이템 ID")),
                                queryParameters(
                                        parameterWithName("order").description("변경할 정렬 순서 (1 이상)")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.id").description("메뉴 아이템 ID"),
                                        fieldWithPath("data.name").description("메뉴 이름"),
                                        fieldWithPath("data.description").description("메뉴 설명"),
                                        fieldWithPath("data.price").description("가격"),
                                        fieldWithPath("data.orderNo").description("변경된 정렬 순서"),
                                        fieldWithPath("data.isPopular").description("인기 메뉴 여부"),
                                        fieldWithPath("data.menuStatus")
                                                .description(
                                                        "메뉴 상태 (`AVAILABLE`, `SOLD_OUT`, `HIDDEN`)"),
                                        fieldWithPath("data.category.id").description("카테고리 ID"),
                                        fieldWithPath("data.category.name").description("카테고리 이름"),
                                        fieldWithPath("data.category.orderNo")
                                                .description("카테고리 정렬 순서"),
                                        fieldWithPath("data.tagNames").description("태그 목록"))));
    }

    @Test
    @DisplayName("성공 - 메뉴 아이템 삭제")
    void deleteMenuItem_success() throws Exception {
        willDoNothing().given(menuItemService).deleteMenuItem(eq(menuItemId), any());

        mockMvc.perform(delete("/menu-items/{menuItemId}", menuItemId).with(user(ownerDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "menu-item/delete",
                                pathParameters(
                                        parameterWithName("menuItemId").description("메뉴 아이템 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data")
                                                .description("응답 데이터 (없음)")
                                                .optional())));
    }
}
