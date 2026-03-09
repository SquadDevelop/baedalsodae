package com.project.baedalsodae.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.user.dto.request.CreateUserAddressRequest;
import com.project.baedalsodae.user.dto.request.CreateUserRequest;
import com.project.baedalsodae.user.dto.request.UpdateUserRequest;
import com.project.baedalsodae.user.dto.request.UserSearchRequest;
import com.project.baedalsodae.user.dto.response.UserDetailResponse;
import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.entity.UserRole;
import com.project.baedalsodae.user.repository.UserRepository;
import com.project.baedalsodae.user.service.impl.UserServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private UserAddressService userAddressService;

    @InjectMocks private UserServiceImpl userService;

    @Test
    @DisplayName("성공 - 회원가입 시 비밀번호 암호화 및 주소 등록")
    void createUser_Success() {
        CreateUserRequest request = createCreateRequest();
        String encodedPassword = "encodedPassword2";
        User savedUser = createTestUser(UUID.randomUUID(), request.getUsername(), encodedPassword);

        given(passwordEncoder.encode(request.getPassword())).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        UserDetailResponse result = userService.createUser(request);

        assertThat(result.getUserId()).isNotNull();
        assertThat(result.getUsername()).isEqualTo(request.getUsername());

        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
        verify(userAddressService)
                .createAddress(eq(savedUser.getId()), any(CreateUserAddressRequest.class));
    }

    @Test
    @DisplayName("실패 - 이미 존재하는 아이디로 가입 시도 시 예외 발생")
    void createUser_Failed() {
        CreateUserRequest request = createCreateRequest();
        given(userRepository.save(any(User.class)))
                .willThrow(
                        new DataIntegrityViolationException(
                                ErrorCode.DATA_INTEGRITY_VIOLATION.getMessage()));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("성공 - 존재하는 회원 ID로 조회 시 상세 정보 반환")
    void getUser_Success() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, "tester", "encodedPwd");
        given(userRepository.findUserWithAddressesByIdAndIsDeletedFalse(userId))
                .willReturn(Optional.of(user));

        UserDetailResponse result = userService.getUser(userId);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("tester");
        verify(userRepository).findUserWithAddressesByIdAndIsDeletedFalse(userId);
    }

    @Test
    @DisplayName("성공 - 비밀번호 포함 회원정보 수정")
    void updateUser_Success() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, "tester", "oldPassword123");
        UpdateUserRequest updateRequest = createUpdateRequest("newPassword123");
        String newEncodedPassword = "newEncodedPassword";

        given(userRepository.findUserWithAddressesByIdAndIsDeletedFalse(userId))
                .willReturn(Optional.of(user));
        given(passwordEncoder.encode(updateRequest.getPassword())).willReturn(newEncodedPassword);

        userService.updateUser(userId, updateRequest);

        assertThat(user.getPassword()).isEqualTo(newEncodedPassword);
        verify(passwordEncoder).encode(updateRequest.getPassword());
        verify(userAddressService).updateAddressList(eq(userId), anyList());
    }

    @Test
    @DisplayName("성공 - 회원 삭제 시 주소 삭제 후 soft delete 수행")
    void deleteUser_Success() {
        UUID userId = UUID.randomUUID();
        User user = createTestUser(userId, "tester", "password3");
        given(userRepository.findUserWithAddressesByIdAndIsDeletedFalse(userId))
                .willReturn(Optional.of(user));

        userService.deleteUser(userId);

        verify(userAddressService).deleteAllAddressesByUserId(userId);
        assertThat(user.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("성공 - 관리자(MANAGER) 목록 페이징 조회")
    void getUsers_Success() {
        // given
        User manager1 = createTestUser(UUID.randomUUID(), "manager1", "pwd1", UserRole.MANAGER);
        UserSearchRequest request = new UserSearchRequest();
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(manager1), pageable, 1);

        given(
                        userRepository.searchUsers(
                                eq(UserRole.MANAGER),
                                any(UserSearchRequest.class),
                                any(Pageable.class)))
                .willReturn(userPage);

        // when
        Page<UserDetailResponse> results =
                userService.getUsers(UserRole.MANAGER, request, pageable);

        // then
        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().get(0).getRole()).isEqualTo(UserRole.MANAGER);
        verify(userRepository)
                .searchUsers(
                        eq(UserRole.MANAGER), any(UserSearchRequest.class), any(Pageable.class));
    }

    @Test
    @DisplayName("성공 - 잘못된 페이지 사이즈(20) 요청 시 기본값(10)으로 보정")
    void getUsers_InvalidSize_ShouldDefaultTo10() {
        // given
        UserSearchRequest request = new UserSearchRequest();
        Pageable requestedPageable = PageRequest.of(0, 20); // 10, 30, 50이 아님
        Pageable expectedPageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), expectedPageable, 0);

        given(
                        userRepository.searchUsers(
                                eq(UserRole.MANAGER),
                                any(UserSearchRequest.class),
                                eq(expectedPageable)))
                .willReturn(emptyPage);

        // when
        userService.getUsers(UserRole.MANAGER, request, requestedPageable);

        // then
        verify(userRepository)
                .searchUsers(
                        eq(UserRole.MANAGER), any(UserSearchRequest.class), eq(expectedPageable));
    }

    private CreateUserRequest createCreateRequest() {
        return CreateUserRequest.builder()
                .username("tester")
                .phone("010-1234-5678")
                .email("tester@example.com")
                .password("password1234")
                .name("테스터")
                .nickname("테스터A")
                .role(UserRole.CUSTOMER)
                .address(
                        CreateUserAddressRequest.builder()
                                .roadAddress("서울시 강남구 역삼동")
                                .detailAddress("101호")
                                .sidoCode("11")
                                .sidoName("서울")
                                .sigunguCode("110")
                                .sigunguName("강남구")
                                .dongCode("11010")
                                .dongName("역삼동")
                                .description("직장")
                                .build())
                .build();
    }

    private User createTestUser(UUID userId, String username, String password) {
        return createTestUser(userId, username, password, UserRole.CUSTOMER);
    }

    private User createTestUser(UUID userId, String username, String password, UserRole role) {
        User user =
                User.create(
                        username,
                        "010-1234-5678",
                        "tester@example.com",
                        password,
                        "테스터",
                        "테스터A",
                        role);
        ReflectionTestUtils.setField(user, "id", userId);

        return user;
    }

    private UpdateUserRequest createUpdateRequest(String newPassword) {
        return UpdateUserRequest.builder()
                .password(newPassword)
                .phone("010-1111-1111")
                .email("tester01@example.com")
                .nickname("newNickname")
                .addresses(List.of())
                .build();
    }
}
