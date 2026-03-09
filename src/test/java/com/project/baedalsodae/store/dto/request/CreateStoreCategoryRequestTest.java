package com.project.baedalsodae.store.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.baedalsodae.store.dto.request.storeCategory.CreateStoreCategoryRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CreateStoreCategoryRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("DTO 생성 및 Getter 작동 확인 (기본 테스트)")
    void createDto_Success() {
        // given
        String name = "한식";
        String desc = "맛있는 한국 음식";

        // when
        CreateStoreCategoryRequest request = new CreateStoreCategoryRequest(name, desc);

        // then
        assertThat(request.getName()).isEqualTo(name);
        assertThat(request.getDescription()).isEqualTo(desc);
    }

    @Test
    @DisplayName("카테고리 이름이 공백일 경우 검증 실패")
    void validate_NameNotBlank() {
        // given (이름이 공백인 상황)
        CreateStoreCategoryRequest request = new CreateStoreCategoryRequest("", "설명");

        // when
        Set<ConstraintViolation<CreateStoreCategoryRequest>> violations =
                validator.validate(request);

        // then (에러가 존재해야 함)
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("카테고리 이름은 필수입니다.");
    }

    @Test
    @DisplayName("설명이 200자를 초과할 경우 검증 실패")
    void validate_DescriptionSize() {
        // given (200자가 넘는 문자열 생성)
        String longDesc = "a".repeat(201);
        CreateStoreCategoryRequest request = new CreateStoreCategoryRequest("한식", longDesc);

        // when
        Set<ConstraintViolation<CreateStoreCategoryRequest>> violations =
                validator.validate(request);

        // then
        assertThat(violations).isNotEmpty();
        assertThat(violations.iterator().next().getMessage()).isEqualTo("설명은 200자 이내로 작성해주세요.");
    }
}
