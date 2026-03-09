package com.project.baedalsodae.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.entity.Address;
import com.project.baedalsodae.user.dto.request.CreateUserAddressRequest;
import com.project.baedalsodae.user.dto.request.UpdateUserAddressRequest;
import com.project.baedalsodae.user.dto.response.UserAddressResponse;
import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.entity.UserAddress;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.repository.UserAddressRepository;
import com.project.baedalsodae.user.repository.UserRepository;
import com.project.baedalsodae.user.service.impl.UserAddressServiceImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock private UserAddressRepository userAddressRepository;

    @Mock private UserRepository userRepository;

    @InjectMocks private UserAddressServiceImpl userAddressService;

    @Test
    @DisplayName("성공 - 사용자의 새로운 주소 등록")
    void createAddress_Success() {
        // given
        UUID userId = UUID.randomUUID();
        CreateUserAddressRequest request = createCreateAddressRequest();
        User user = createTestUser(userId);

        given(
                        userAddressRepository
                                .existsByUserIdAndAddressRoadAddressAndAddressDetailAddress(
                                        eq(userId), any(), any()))
                .willReturn(false);
        given(userRepository.findByUserIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));
        given(userAddressRepository.save(any(UserAddress.class)))
                .willReturn(createTestAddress(user, UUID.randomUUID()));

        // when
        userAddressService.createAddress(userId, request);

        // then
        verify(userAddressRepository).save(any(UserAddress.class));
    }

    @Test
    @DisplayName("실패 - 동일한 사용자가 중복된 주소 등록 시도 시 예외 발생")
    void createAddress_Duplicated_Failed() {
        // given
        UUID userId = UUID.randomUUID();
        CreateUserAddressRequest request = createCreateAddressRequest();

        given(
                        userAddressRepository
                                .existsByUserIdAndAddressRoadAddressAndAddressDetailAddress(
                                        eq(userId), any(), any()))
                .willReturn(true);

        // when & then
        assertThatThrownBy(() -> userAddressService.createAddress(userId, request))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("성공 - 주소 상세 정보 수정")
    void updateAddress_Success() {
        // given
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        UpdateUserAddressRequest request = createUpdateUserAddressRequest(addressId);
        User user = createTestUser(userId);
        UserAddress address = createTestAddress(user, addressId);

        given(
                        userAddressRepository
                                .existsByUserIdAndAddressRoadAddressAndAddressDetailAddressAndIdNot(
                                        eq(userId), any(), any(), eq(addressId)))
                .willReturn(false);
        given(userAddressRepository.findByIdAndUserId(addressId, userId))
                .willReturn(Optional.of(address));

        // when
        UserAddressResponse response = userAddressService.updateAddress(userId, addressId, request);

        // then
        assertThat(response.getRoadAddress()).isEqualTo(request.getRoadAddress());
    }

    @Test
    @DisplayName("성공 - 사용자의 전체 주소 목록 조회")
    void getAddressList_Success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId);
        UserAddress address1 = createTestAddress(user, UUID.randomUUID());
        ReflectionTestUtils.setField(user, "userAddresses", new ArrayList<>(List.of(address1)));

        given(userRepository.findByUserIdAndIsDeletedFalse(userId)).willReturn(Optional.of(user));

        // when
        List<UserAddressResponse> results = userAddressService.getAddressList(userId);

        // then
        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("성공 - 특정 주소를 대표 주소로 설정")
    void setMainAddress_Success() {
        // given
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        User user = createTestUser(userId);
        UserAddress address = createTestAddress(user, addressId);
        user.addAddress(address);

        given(userAddressRepository.findByIdAndUserId(addressId, userId))
                .willReturn(Optional.of(address));

        // when
        userAddressService.setMainAddress(userId, addressId);

        // then
        assertThat(user.getUserMainAddressId()).isEqualTo(addressId);
    }

    @Test
    @DisplayName("실패 - 주소가 1개일 때 삭제 시도 시 예외 발생")
    void deleteAddress_Failed_OnlyOneAddress() {
        // given
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        User user = createTestUser(userId);
        UserAddress address = createTestAddress(user, addressId);
        ReflectionTestUtils.setField(user, "userAddresses", new ArrayList<>(List.of(address)));

        given(userAddressRepository.findByIdAndUserId(addressId, userId))
                .willReturn(Optional.of(address));

        // when & then
        assertThatThrownBy(() -> userAddressService.deleteAddress(userId, addressId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("성공 - 대표 주소 삭제 시 다른 주소를 대표 주소로 위임")
    void deleteAddress_DelegateMainAddress() {
        // given
        UUID userId = UUID.randomUUID();
        UUID mainAddressId = UUID.randomUUID();
        UUID nextAddressId = UUID.randomUUID();
        User user = createTestUser(userId);
        UserAddress mainAddress = createTestAddress(user, mainAddressId);
        UserAddress nextAddress = createTestAddress(user, nextAddressId);

        user.addAddress(mainAddress);
        user.addAddress(nextAddress);
        user.changeMainAddress(mainAddressId);

        given(userAddressRepository.findByIdAndUserId(mainAddressId, userId))
                .willReturn(Optional.of(mainAddress));

        // when
        userAddressService.deleteAddress(userId, mainAddressId);

        // then
        assertThat(user.getUserMainAddressId()).isEqualTo(nextAddressId);
        verify(userAddressRepository).delete(mainAddress);
    }

    @Test
    @DisplayName("성공 - 회원탈퇴 시 등록된 모든 주소 일괄 삭제")
    void deleteAllAddresses_Success() {
        // given
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId);
        UserAddress address = createTestAddress(user, UUID.randomUUID());
        ReflectionTestUtils.setField(user, "userAddresses", new ArrayList<>(List.of(address)));

        given(userRepository.findUserWithAddressesByIdAndIsDeletedFalse(userId))
                .willReturn(Optional.of(user));

        // when
        userAddressService.deleteAllAddressesByUserId(userId);

        // then
        verify(userAddressRepository).deleteAllByUserId(userId);
        assertThat(user.getUserAddresses()).isEmpty();
        assertThat(user.getUserMainAddressId()).isNull();
    }

    private User createTestUser(UUID userId) {
        User user =
                User.create(
                        "tester",
                        "010-1234-5678",
                        "test@test.com",
                        "pwd",
                        "테스터",
                        "닉네임",
                        UserRole.CUSTOMER);
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }

    private UserAddress createTestAddress(User user, UUID addressId) {
        Address address =
                Address.createAddress("11", "서울", "110", "강남구", "11010", "역삼동", "도로명", "상세");
        UserAddress userAddress = UserAddress.create(user, address, "설명");
        ReflectionTestUtils.setField(userAddress, "id", addressId);
        return userAddress;
    }

    private CreateUserAddressRequest createCreateAddressRequest() {
        return CreateUserAddressRequest.builder()
                .roadAddress("도로명")
                .detailAddress("상세주소")
                .sidoCode("11")
                .sidoName("서울")
                .sigunguCode("110")
                .sigunguName("강남구")
                .dongCode("11010")
                .dongName("역삼동")
                .build();
    }

    private UpdateUserAddressRequest createUpdateUserAddressRequest(UUID addressId) {
        return UpdateUserAddressRequest.builder()
                .userAddressId(addressId)
                .roadAddress("수정된 도로명")
                .detailAddress("수정된 상세주소")
                .sidoCode("11")
                .sidoName("서울")
                .sigunguCode("110")
                .sigunguName("강남구")
                .dongCode("11010")
                .dongName("역삼동")
                .build();
    }
}
