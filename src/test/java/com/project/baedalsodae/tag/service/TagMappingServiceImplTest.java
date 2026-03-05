package com.project.baedalsodae.tag.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.tag.entity.Tag;
import com.project.baedalsodae.tag.repository.TagMappingRepository;
import com.project.baedalsodae.tag.service.Impl.TagMappingServiceImpl;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagMappingServiceImplTest {

    @Mock private TagService tagService;

    @Mock private TagMappingRepository tagMappingRepository;

    @InjectMocks private TagMappingServiceImpl tagMappingService;

    @Test
    @DisplayName("태그를 upsert 후 전체 조회하여 TagMapping을 저장한다")
    void createTagMappings_upsertAndSaveMappings() {
        // given
        MenuItem menuItem = mock(MenuItem.class);
        Tag tag1 = mock(Tag.class);
        Tag tag2 = mock(Tag.class);
        given(tag1.getName()).willReturn("치킨");
        given(tag2.getName()).willReturn("피자");
        given(tagService.findAllByNames(List.of("치킨", "피자"))).willReturn(List.of(tag1, tag2));

        // when
        tagMappingService.createTagMappings(menuItem, List.of("치킨", "피자"));

        // then
        then(tagService).should().createNewTagsIfNotExists(List.of("치킨", "피자"));
        then(tagService).should().findAllByNames(List.of("치킨", "피자"));
        then(tagMappingRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("중복 태그명이 포함된 경우 distinct 처리 후 upsert하고 TagMapping은 원본 순서대로 저장한다")
    void createTagMappings_withDuplicateTagNames_distinctBeforeUpsert() {
        // given
        MenuItem menuItem = mock(MenuItem.class);
        Tag tag = mock(Tag.class);
        given(tag.getName()).willReturn("치킨");
        given(tagService.findAllByNames(List.of("치킨"))).willReturn(List.of(tag));

        // when
        tagMappingService.createTagMappings(menuItem, List.of("치킨", "치킨"));

        // then
        then(tagService).should().createNewTagsIfNotExists(List.of("치킨"));
        then(tagService).should().findAllByNames(List.of("치킨"));
        then(tagMappingRepository).should().saveAll(anyList());
    }

    @Test
    @DisplayName("menuItemId로 해당 메뉴 아이템의 TagMapping을 전체 삭제한다")
    void deleteAllTagMappingByMenuItemId_deletesAllMappingsForMenuItem() {
        // given
        UUID menuItemId = UUID.randomUUID();

        // when
        tagMappingService.deleteAllTagMappingByMenuItemId(menuItemId);

        // then
        then(tagMappingRepository).should().deleteByMenuItemId(menuItemId);
    }
}
