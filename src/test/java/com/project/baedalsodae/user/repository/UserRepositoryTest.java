package com.project.baedalsodae.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.baedalsodae.global.common.config.QueryDslConfig;
import com.project.baedalsodae.user.entity.User;
import com.project.baedalsodae.user.entity.UserRole;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@ActiveProfiles("test")
@DataJpaTest
@Import(QueryDslConfig.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("User 엔티티 저장 시 createdBy가 null이면 id 값으로 초기화되는지 확인")
    void user_Save_CheckCreatedByIsInitializedWithId() {
        // given
        User user = User.create(
                "testuser",
                "010-1234-5678",
                "test@test.com",
                "encodedPassword",
                "테스터",
                "테스트닉네임",
                UserRole.CUSTOMER
        );

        // when
        User savedUser = userRepository.saveAndFlush(user);

        // then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedBy()).isNotNull();
        assertThat(savedUser.getCreatedBy()).isEqualTo(savedUser.getId());
    }

    @Test
    @DisplayName("MASTER 관리자가 MANAGER 계정 생성 시 createdBy가 MASTER의 ID로 유지되는지 확인")
    void user_Save_ByMaster_CheckCreatedByIsMasterId() {
        // given
        UUID masterId = UUID.randomUUID();
        User manager = User.create(
                "manager123",
                "010-1111-2222",
                "manager@test.com",
                "encodedPassword",
                "매니저",
                "매니저닉네임",
                UserRole.MANAGER
        );
        ReflectionTestUtils.setField(manager, "createdBy", masterId);

        // when
        User savedManager = userRepository.saveAndFlush(manager);

        // then
        assertThat(savedManager.getId()).isNotNull();
        assertThat(savedManager.getCreatedBy()).isEqualTo(masterId); // 본인 ID가 아닌 MASTER ID여야 함
        assertThat(savedManager.getCreatedBy()).isNotEqualTo(savedManager.getId());
    }
}
