package com.project.baedalsodae.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.project.baedalsodae.global.common.BusinessException;
import com.project.baedalsodae.global.common.ErrorCode;
import com.project.baedalsodae.store.dto.request.CreateStoreCategoryRequest;
import com.project.baedalsodae.store.dto.request.PatchStoreCategoryRequest;
import com.project.baedalsodae.store.dto.response.StoreCategoryDetailResponse;
import com.project.baedalsodae.store.dto.response.StoreCategoryListResponse;
import com.project.baedalsodae.store.entity.StoreCategory;
import com.project.baedalsodae.store.repository.StoreCategoryRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.project.baedalsodae.store.service.impl.StoreCategoryServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StoreCategoryServiceTest {

    @Mock private StoreCategoryRepository storeCategoryRepository;

    @InjectMocks private StoreCategoryServiceImpl storeCategoryService;

    @Test
    @DisplayName("Soft 삭제된 카테고리는 목록 조회에서 제외된다")
    void storeCategoryList_FilterDeletedSuccess() {
        StoreCategory category1 = StoreCategory.createStoreCategory("한식", "맛있는 한식");
        StoreCategory category2 = StoreCategory.createStoreCategory("일식", "맛있는 일식");
        StoreCategory category3 = StoreCategory.createStoreCategory("양식", "진짜 양식");
        List<StoreCategory> repositoryData =
                new ArrayList<>(List.of(category1, category2, category3));

        given(storeCategoryRepository.findAllByIsDeletedFalse())
                .willAnswer(
                        invocation -> repositoryData.stream().filter(c -> !c.isDeleted()).toList());

        UUID deleteId = UUID.randomUUID();
        given(storeCategoryRepository.findById(deleteId)).willReturn(Optional.of(category2));

        storeCategoryService.deleteStoreCategory(deleteId);
        StoreCategoryListResponse response = storeCategoryService.getActiveStoreCategories();

        assertThat(response.getStoreCategoryList()).hasSize(2);
        assertThat(response.getStoreCategoryList().get(0).getName()).isEqualTo("한식");
    }

    @Test
    @DisplayName("카테고리 상세 조회 성공")
    void getStoreCategoryDetail_Success() {
        UUID id = UUID.randomUUID();
        StoreCategory category = StoreCategory.createStoreCategory("중식", "짜장면과 짬뽕 등");
        ReflectionTestUtils.setField(category, "id", id);

        given(storeCategoryRepository.findById(id)).willReturn(Optional.of(category));

        StoreCategoryDetailResponse response = storeCategoryService.getStoreCategoryDetail(id);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("중식");
        assertThat(response.getAuditInfo()).isNotNull();

        verify(storeCategoryRepository).findById(id);
    }

    @Test
    @DisplayName("카테고리 생성 성공")
    void createStoreCategory_Success() {
        CreateStoreCategoryRequest request = new CreateStoreCategoryRequest("중식", "짜장면 짬뽕");

        storeCategoryService.createStoreCategory(request);

        verify(storeCategoryRepository).save(any(StoreCategory.class));
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void patchStoreCategory_Success() {
        UUID id = UUID.randomUUID();
        StoreCategory existingCategory = StoreCategory.createStoreCategory("일식", "초밥");
        PatchStoreCategoryRequest request = new PatchStoreCategoryRequest("새로운 일식", "라멘 포함");

        given(storeCategoryRepository.findById(id)).willReturn(Optional.of(existingCategory));

        storeCategoryService.patchStoreCategory(request, id);

        assertThat(existingCategory.getName()).isEqualTo("새로운 일식");
        assertThat(existingCategory.getDescription()).isEqualTo("라멘 포함");
    }

    @Test
    @DisplayName("카테고리 삭제(Soft Delete) 성공")
    void deleteStoreCategory_Success() {
        UUID id = UUID.randomUUID();
        StoreCategory existingCategory = StoreCategory.createStoreCategory("치킨", "양념치킨");
        given(storeCategoryRepository.findById(id)).willReturn(Optional.of(existingCategory));

        storeCategoryService.deleteStoreCategory(id);

        verify(storeCategoryRepository).findById(id);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 수정 시 예외 발생")
    void patchStoreCategory_NotFound() {
        UUID id = UUID.randomUUID();
        PatchStoreCategoryRequest request = new PatchStoreCategoryRequest("이름", "설명");
        given(storeCategoryRepository.findById(id)).willReturn(Optional.empty());

        assertThatThrownBy(() -> storeCategoryService.patchStoreCategory(request, id))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.STORE_CATEGORY_NOT_FOUND.getMessage());
    }
}
