package com.project.baedalsodae.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.project.baedalsodae.user.dto.request.CreateUserAddressRequest;
import com.project.baedalsodae.user.dto.request.UpdateUserAddressRequest;
import com.project.baedalsodae.user.dto.response.UserAddressResponse;
import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.entity.UserAddress;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.repository.UserAddressRepository;
import com.project.baedalsodae.user.repository.UserRepository;
import com.project.baedalsodae.user.service.impl.UserAddressServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class UserAddressServiceTest {

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAddressServiceImpl userAddressService;

    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.create("tester", "010-1234-5678", "tester@test.com",
                "pwd1234", "testerName", "testerNickname", UserRole.CUSTOMER);
        ReflectionTestUtils.setField(user, "id", userId);
    }

    @Test
    @DisplayName("성공 - 주소 추가 시 첫번째 주소를 메인 주소지로 지정")
    void createAddress_Success() {
        CreateUserAddressRequest request = createCreateRequest("도로명1", "상세1");

        given(userRepository.findByUserIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));
        given(userAddressRepository.save(any())).willAnswer(invocationOnMock -> {
            UserAddress userAddress = invocationOnMock.getArgument(0);
            ReflectionTestUtils.setField(userAddress, "id", user.getId());
            return userAddress;
        });

        userAddressService.createAddress(userId, request);

        assertThat(user.getUserAddresses()).hasSize(1);
        assertThat(user.getUserMainAddressId()).isNotNull();
        verify(userAddressRepository).save(any());
    }

    @Test
    @DisplayName("성공 - 사용자의 주소 목록을 반환")
    void getAddressList_Success() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UserAddress a1 = UserAddress.builder().id(id1).user(user).build();
        UserAddress a2 = UserAddress.builder().id(id2).user(user).build();
        user.addAddresses(List.of(a1, a2));
        user.changeMainAddress(a2.getId());

        given(userRepository.findByUserIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));

        List<UserAddressResponse> result = userAddressService.getAddressList(userId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).isMainAddress()).isFalse();
        assertThat(result.get(1).getId()).isEqualTo(a2.getId());
    }

    @Test
    @DisplayName("성공 - 단건 주소 정보 수정")
    void updateAddress_Success() {
        UUID addrId = UUID.randomUUID();
        UserAddress addr = UserAddress.builder().id(addrId).user(user).roadAddress("기존").build();
        UpdateUserAddressRequest req = UpdateUserAddressRequest.builder().id(addrId).roadAddress("수정").build();
        
        given(userAddressRepository.findByIdAndUserId(addrId, userId)).willReturn(Optional.of(addr));

        userAddressService.updateAddress(userId, req);

        assertThat(addr.getRoadAddress()).isEqualTo("수정");
    }

    @Test
    @DisplayName("성공 - 주소 일괄 수정 기능")
    void updateAddressList_Success() {
        UUID myId1 = UUID.randomUUID();
        UUID myId2 = UUID.randomUUID();
        UserAddress myAddr1 = UserAddress.builder().id(myId1).user(user).roadAddress("기존도로1").build();
        UserAddress myAddr2 = UserAddress.builder().id(myId2).user(user).roadAddress("기존도로2(삭제)").build();
        user.addAddresses(List.of(myAddr1, myAddr2));
        
        UpdateUserAddressRequest req1 = createUpdateRequest(myId1, "수정된도로명", "101호", "설명수정");
        UpdateUserAddressRequest req3 = createUpdateRequest(null, "신규도로명", "303호", "신규주소");
        List<UpdateUserAddressRequest> updatedAddressReq = List.of(req1, req3);

        given(userRepository.findUserWithAddressesByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));

        userAddressService.updateAddressList(userId, updatedAddressReq);

        assertThat(user.getUserAddresses()).hasSize(2);
        assertThat(myAddr1.getRoadAddress()).isEqualTo("수정된도로명");
        assertThat(myAddr1.getDetailAddress()).isEqualTo("101호");
        assertThat(myAddr1.getDescription()).isEqualTo("설명수정");

        boolean hasMyAddr2 = user.getUserAddresses().stream()
                .anyMatch(addr -> addr.getId() != null && addr.getId().equals(myAddr2.getId()));
        assertThat(hasMyAddr2).isFalse();

        boolean hasMyAddr3 = user.getUserAddresses().stream()
                .anyMatch(addr -> addr.getRoadAddress().equals("신규도로명"));
        assertThat(hasMyAddr3).isTrue();
    }

    @Test
    @DisplayName("성공 - 주소 삭제 및 대표 주소 변경")
    void deleteAddress_Success() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UserAddress a1 = UserAddress.builder().id(id1).user(user).build();
        UserAddress a2 = UserAddress.builder().id(id2).user(user).build();
        user.addAddresses(List.of(a1, a2));
        user.changeMainAddress(id1);

        given(userAddressRepository.findByIdAndUserId(id1, userId)).willReturn(Optional.of(a1));

        userAddressService.deleteAddress(userId, id1);

        assertThat(user.getUserAddresses()).hasSize(1);
        assertThat(user.getUserMainAddressId()).isEqualTo(id2);
    }

    @Test
    @DisplayName("실패 - 주소가 1개일 때는 삭제 불가")
    void deleteAddress_Fail_OnlyOne() {
        UUID id1 = UUID.randomUUID();
        UserAddress a1 = UserAddress.builder().id(id1).user(user).build();
        user.addAddress(a1);

        given(userAddressRepository.findByIdAndUserId(id1, userId)).willReturn(Optional.of(a1));

        assertThatThrownBy(() -> userAddressService.deleteAddress(userId, id1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.USER_ADDRESS_CANNOT_DELETE.getMessage());
    }

    @Test
    @DisplayName("성공 - 대표 주소 변경")
    void setMainAddress_Success() {
        UUID addrId = UUID.randomUUID();
        UserAddress addr = UserAddress.builder().id(addrId).user(user).build();
        given(userAddressRepository.findByIdAndUserId(addrId, userId)).willReturn(Optional.of(addr));

        userAddressService.setMainAddress(userId, addrId);

        assertThat(user.getUserMainAddressId()).isEqualTo(addrId);
    }

    @Test
    @DisplayName("성공 - 회원탈퇴 시 모든 주소 일괄 삭제")
    void deleteAllAddresses_Success() {
        user.addAddress(UserAddress.builder().id(UUID.randomUUID()).user(user).build());
        given(userRepository.findUserWithAddressesByIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));

        userAddressService.deleteAllAddressesByUserId(userId);

        assertThat(user.getUserAddresses()).isEmpty();
        assertThat(user.getUserMainAddressId()).isNull();
        verify(userAddressRepository).deleteAllByUserId(userId);
    }

    private CreateUserAddressRequest createCreateRequest(String roadAddress, String detailAddress) {
        return CreateUserAddressRequest.from(roadAddress, detailAddress, "");
    }

    private UpdateUserAddressRequest createUpdateRequest(UUID userId, String roadAddress, String detailAddress, String description) {
        return UpdateUserAddressRequest.builder()
                .id(userId)
                .roadAddress(roadAddress)
                .detailAddress(detailAddress)
                .description(description)
                .build();
    }
}
