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
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPatchRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.category.MenuCategoryPutRequestDto;
import com.project.baedalsodae.menu.dto.requestDto.item.MenuItemPostRequestDto;
import com.project.baedalsodae.menu.dto.responseDto.category.MenuCategoryResponseDto;
import com.project.baedalsodae.menu.dto.responseDto.item.MenuItemResponseDto;
import com.project.baedalsodae.menu.entity.enums.MenuStatus;
import com.project.baedalsodae.menu.fixture.MenuCategoryRequestFixture;
import com.project.baedalsodae.menu.fixture.MenuItemRequestFixture;
import com.project.baedalsodae.menu.service.MenuCategoryService;
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

@WebMvcTest(MenuCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class MenuCategoryControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private MenuCategoryService menuCategoryService;

    @MockitoBean private MenuItemService menuItemService;

    private UUID menuCategoryId;
    private UserDetailsImpl ownerDetails;
    private MenuCategoryResponseDto categoryResponse;
    private MenuItemResponseDto itemResponse;

    @BeforeEach
    void setUp() {
        menuCategoryId = UUID.randomUUID();
        ownerDetails = createOwnerUserDetails(UUID.randomUUID());

        categoryResponse = new MenuCategoryResponseDto(menuCategoryId, DEFAULT_CATEGORY_NAME, 1);

        MenuCategoryResponseDto categoryInItem =
                new MenuCategoryResponseDto(menuCategoryId, DEFAULT_CATEGORY_NAME, 1);
        itemResponse =
                new MenuItemResponseDto(
                        UUID.randomUUID(),
                        DEFAULT_MENU_ITEM_NAME,
                        DEFAULT_ITEM_DESCRIPTION,
                        DEFAULT_ITEM_PRICE,
                        1,
                        false,
                        MenuStatus.AVAILABLE,
                        categoryInItem,
                        List.of(TAG_1, TAG_2));
    }

    @Test
    @DisplayName("성공 - 메뉴 카테고리 이름 수정")
    void updateMenuCategory_success() throws Exception {
        MenuCategoryPutRequestDto request = MenuCategoryRequestFixture.createDefaultPutRequest();
        given(menuCategoryService.updateMenuCategory(eq(menuCategoryId), any(), any()))
                .willReturn(categoryResponse);

        mockMvc.perform(
                        put("/menu-categories/{menuCategoryId}", menuCategoryId)
                                .with(user(ownerDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(DEFAULT_CATEGORY_NAME))
                .andDo(
                        document(
                                "menu-category/update",
                                pathParameters(
                                        parameterWithName("menuCategoryId")
                                                .description("메뉴 카테고리 ID")),
                                requestFields(fieldWithPath("name").description("변경할 카테고리 이름")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.id").description("카테고리 ID"),
                                        fieldWithPath("data.name").description("카테고리 이름"),
                                        fieldWithPath("data.orderNo").description("카테고리 정렬 순서"))));
    }

    @Test
    @DisplayName("성공 - 메뉴 카테고리 순서 변경")
    void updateMenuCategoryOrder_success() throws Exception {
        MenuCategoryPatchRequestDto request = MenuCategoryRequestFixture.createPatchRequest(2);
        given(menuCategoryService.updateMenuCategoryOrder(eq(menuCategoryId), any(), any()))
                .willReturn(categoryResponse);

        mockMvc.perform(
                        patch("/menu-categories/{menuCategoryId}/orders", menuCategoryId)
                                .with(user(ownerDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "menu-category/update-order",
                                pathParameters(
                                        parameterWithName("menuCategoryId")
                                                .description("메뉴 카테고리 ID")),
                                requestFields(
                                        fieldWithPath("orderNo").description("변경할 정렬 순서 (1 이상)")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.id").description("카테고리 ID"),
                                        fieldWithPath("data.name").description("카테고리 이름"),
                                        fieldWithPath("data.orderNo").description("카테고리 정렬 순서"))));
    }

    @Test
    @DisplayName("성공 - 메뉴 카테고리 삭제")
    void deleteMenuCategory_success() throws Exception {
        willDoNothing().given(menuCategoryService).deleteMenuCategory(eq(menuCategoryId), any());

        mockMvc.perform(
                        delete("/menu-categories/{menuCategoryId}", menuCategoryId)
                                .with(user(ownerDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "menu-category/delete",
                                pathParameters(
                                        parameterWithName("menuCategoryId")
                                                .description("메뉴 카테고리 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data")
                                                .description("응답 데이터 (없음)")
                                                .optional())));
    }

    @Test
    @DisplayName("성공 - 메뉴 카테고리별 메뉴 아이템 목록 조회")
    void getMenuItems_success() throws Exception {
        given(menuItemService.getMenuItem(eq(menuCategoryId), any()))
                .willReturn(List.of(itemResponse));

        mockMvc.perform(
                        get("/menu-categories/{menuCategoryId}/menu-items", menuCategoryId)
                                .with(user(ownerDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "menu-category/get-menu-items",
                                pathParameters(
                                        parameterWithName("menuCategoryId")
                                                .description("메뉴 카테고리 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data[].id").description("메뉴 아이템 ID"),
                                        fieldWithPath("data[].name").description("메뉴 이름"),
                                        fieldWithPath("data[].description").description("메뉴 설명"),
                                        fieldWithPath("data[].price").description("가격"),
                                        fieldWithPath("data[].orderNo").description("정렬 순서"),
                                        fieldWithPath("data[].isPopular").description("인기 메뉴 여부"),
                                        fieldWithPath("data[].menuStatus")
                                                .description(
                                                        "메뉴 상태 (`AVAILABLE`, `SOLD_OUT`, `HIDDEN`)"),
                                        fieldWithPath("data[].category.id").description("카테고리 ID"),
                                        fieldWithPath("data[].category.name")
                                                .description("카테고리 이름"),
                                        fieldWithPath("data[].category.orderNo")
                                                .description("카테고리 정렬 순서"),
                                        fieldWithPath("data[].tagNames").description("태그 목록"))));
    }

    @Test
    @DisplayName("성공 - 메뉴 아이템 생성")
    void createMenuItem_success() throws Exception {
        MenuItemPostRequestDto request = MenuItemRequestFixture.createDefaultPostRequest();
        given(menuItemService.createMenuItem(eq(menuCategoryId), any(), any()))
                .willReturn(itemResponse);

        mockMvc.perform(
                        post("/menu-categories/{menuCategoryId}/menu-items", menuCategoryId)
                                .with(user(ownerDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "menu-category/create-menu-item",
                                pathParameters(
                                        parameterWithName("menuCategoryId")
                                                .description("메뉴 카테고리 ID")),
                                requestFields(
                                        fieldWithPath("name").description("메뉴 이름"),
                                        fieldWithPath("description")
                                                .description("메뉴 설명")
                                                .optional(),
                                        fieldWithPath("price").description("가격"),
                                        fieldWithPath("isPopular").description("인기 메뉴 여부"),
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
}
