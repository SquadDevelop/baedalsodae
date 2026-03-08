package com.project.baedalsodae.store.controller;

import static com.project.baedalsodae.store.fixture.StoreHoursTestConstants.*;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.baedalsodae.auth.security.UserDetailsImpl;
import com.project.baedalsodae.store.dto.request.StoreHoursRequest;
import com.project.baedalsodae.store.dto.response.StoreHoursResponse;
import com.project.baedalsodae.store.entity.enums.DayOfWeek;
import com.project.baedalsodae.store.fixture.StoreHoursRequestFixture;
import com.project.baedalsodae.store.service.StoreHoursService;
import java.util.Arrays;
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

@WebMvcTest(StoreHourController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
class StoreHourControllerTest {

    @Autowired private MockMvc mockMvc;

    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private StoreHoursService storeHoursService;

    private UUID storeId;
    private UserDetailsImpl ownerDetails;
    private List<StoreHoursRequest> requests;
    private StoreHoursResponse.StoreHoursInfo storeHoursInfo;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        ownerDetails = createOwnerUserDetails(UUID.randomUUID());
        requests = StoreHoursRequestFixture.createDefaultRequests();

        List<StoreHoursResponse.StoreHourDto> hourDtos =
                Arrays.stream(DayOfWeek.values())
                        .map(
                                day ->
                                        StoreHoursResponse.StoreHourDto.builder()
                                                .storeHoursId(UUID.randomUUID())
                                                .dayOfWeek(day)
                                                .openTime(DEFAULT_OPEN_TIME)
                                                .closeTime(DEFAULT_CLOSE_TIME)
                                                .breakStart(DEFAULT_BREAK_START)
                                                .breakEnd(DEFAULT_BREAK_END)
                                                .isOpen(true)
                                                .build())
                        .toList();

        storeHoursInfo = new StoreHoursResponse.StoreHoursInfo(storeId, hourDtos);
    }

    @Test
    @DisplayName("성공 - 영업시간 등록")
    void createStoreHours_success() throws Exception {
        willDoNothing().given(storeHoursService).createStoreHours(eq(storeId), any(), any());

        mockMvc.perform(
                        post("/stores/{storeId}/hours", storeId)
                                .with(user(ownerDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requests)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "store-hours/create",
                                pathParameters(parameterWithName("storeId").description("가게 ID")),
                                requestFields(
                                        fieldWithPath("[].dayOfWeek")
                                                .description(
                                                        "요일 (`MON`, `TUE`, `WED`, `THU`, `FRI`, `SAT`, `SUN`)"),
                                        fieldWithPath("[].openTime")
                                                .description("영업 시작 시간 (HH:mm:ss)"),
                                        fieldWithPath("[].closeTime")
                                                .description("영업 종료 시간 (HH:mm:ss)"),
                                        fieldWithPath("[].breakStart")
                                                .description("브레이크 시작 시간 (선택)")
                                                .optional(),
                                        fieldWithPath("[].breakEnd")
                                                .description("브레이크 종료 시간 (선택)")
                                                .optional(),
                                        fieldWithPath("[].open").description("영업 여부"),
                                        fieldWithPath("[].validOpenCloseTime").ignored(),
                                        fieldWithPath("[].validBreakTime").ignored()),
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
    @DisplayName("성공 - 영업시간 조회")
    void getStoreHours_success() throws Exception {
        given(storeHoursService.getStoreHours(eq(storeId))).willReturn(storeHoursInfo);

        mockMvc.perform(get("/stores/{storeId}/hours", storeId))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "store-hours/get",
                                pathParameters(parameterWithName("storeId").description("가게 ID")),
                                responseFields(
                                        fieldWithPath("code").description("응답 코드"),
                                        fieldWithPath("status").description("HTTP 상태"),
                                        fieldWithPath("message").description("응답 메시지"),
                                        fieldWithPath("timestamp").ignored(),
                                        fieldWithPath("data.storeId").description("가게 ID"),
                                        fieldWithPath("data.storeHours[].storeHoursId")
                                                .description("영업시간 ID"),
                                        fieldWithPath("data.storeHours[].dayOfWeek")
                                                .description(
                                                        "요일 (`MON`, `TUE`, `WED`, `THU`, `FRI`, `SAT`, `SUN`)"),
                                        fieldWithPath("data.storeHours[].openTime")
                                                .description("영업 시작 시간"),
                                        fieldWithPath("data.storeHours[].closeTime")
                                                .description("영업 종료 시간"),
                                        fieldWithPath("data.storeHours[].breakStart")
                                                .description("브레이크 시작 시간"),
                                        fieldWithPath("data.storeHours[].breakEnd")
                                                .description("브레이크 종료 시간"),
                                        fieldWithPath("data.storeHours[].open")
                                                .description("영업 여부"),
                                        fieldWithPath("data.empty").ignored())));
    }

    @Test
    @DisplayName("성공 - 영업시간 수정")
    void updateStoreHours_success() throws Exception {
        willDoNothing().given(storeHoursService).updateStoreHours(eq(storeId), any(), any());

        mockMvc.perform(
                        patch("/stores/{storeId}/hours", storeId)
                                .with(user(ownerDetails))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requests)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "store-hours/update",
                                pathParameters(parameterWithName("storeId").description("가게 ID")),
                                requestFields(
                                        fieldWithPath("[].dayOfWeek")
                                                .description(
                                                        "요일 (`MON`, `TUE`, `WED`, `THU`, `FRI`, `SAT`, `SUN`)"),
                                        fieldWithPath("[].openTime")
                                                .description("영업 시작 시간 (HH:mm:ss)"),
                                        fieldWithPath("[].closeTime")
                                                .description("영업 종료 시간 (HH:mm:ss)"),
                                        fieldWithPath("[].breakStart")
                                                .description("브레이크 시작 시간 (선택)")
                                                .optional(),
                                        fieldWithPath("[].breakEnd")
                                                .description("브레이크 종료 시간 (선택)")
                                                .optional(),
                                        fieldWithPath("[].open").description("영업 여부"),
                                        fieldWithPath("[].validOpenCloseTime").ignored(),
                                        fieldWithPath("[].validBreakTime").ignored()),
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
    @DisplayName("성공 - 영업시간 삭제")
    void deleteStoreHours_success() throws Exception {
        willDoNothing().given(storeHoursService).deleteStoreHours(eq(storeId), any());

        mockMvc.perform(delete("/stores/{storeId}/hours", storeId).with(user(ownerDetails)))
                .andDo(print())
                .andExpect(status().isOk())
                .andDo(
                        document(
                                "store-hours/delete",
                                pathParameters(parameterWithName("storeId").description("가게 ID")),
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
