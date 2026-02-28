package com.project.baedalsodae.tag.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.tag.entity.Tag;
import com.project.baedalsodae.tag.repository.TagMappingRepository;
import com.project.baedalsodae.tag.service.Impl.TagMappingServiceImpl;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class TagMappingServiceImplTest {

  @Mock
  private TagService tagService;

  @Mock
  private TagMappingRepository tagMappingRepository;

  @InjectMocks
  private TagMappingServiceImpl tagMappingService;

  @Test
  @DisplayName("모든 태그가 이미 존재하면 배치 생성 없이 TagMapping을 저장한다")
  void createTagMappings_whenAllTagsExist_savesWithoutBatchCreation() {
    // given
    MenuItem menuItem = mock(MenuItem.class);
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);
    given(tag1.getName()).willReturn("치킨");
    given(tag2.getName()).willReturn("피자");
    given(tagService.findAllByNames(anyList())).willReturn(List.of(tag1, tag2));

    // when
    tagMappingService.createTagMappings(menuItem, List.of("치킨", "피자"));

    // then
    then(tagService).should(never()).createNewTags(anyList());
    then(tagMappingRepository).should().saveAll(anyList());
  }

  @Test
  @DisplayName("신규 태그가 있으면 배치 생성 후 TagMapping을 저장한다")
  void createTagMappings_addsNewTagsAndSavesMappings() {
    // given
    MenuItem menuItem = mock(MenuItem.class);
    Tag existingTag = mock(Tag.class);
    Tag newTag = mock(Tag.class);
    given(existingTag.getName()).willReturn("치킨");
    given(tagService.findAllByNames(anyList())).willReturn(List.of(existingTag));
    given(tagService.createNewTags(List.of("피자"))).willReturn(Map.of("피자", newTag));

    // when
    tagMappingService.createTagMappings(menuItem, List.of("치킨", "피자"));

    // then
    then(tagService).should().createNewTags(List.of("피자"));
    then(tagMappingRepository).should().saveAll(anyList());
  }

  @Test
  @DisplayName("배치 생성 중 동시 충돌 발생 시 개별 findOrCreateTag로 fallback 처리한다")
  void createTagMappings_batchCreationConflict_fallbackToFindOrCreate() {
    // given
    MenuItem menuItem = mock(MenuItem.class);
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);
    given(tagService.findAllByNames(anyList())).willReturn(List.of());
    given(tagService.createNewTags(anyList())).willThrow(DataIntegrityViolationException.class);
    given(tagService.findOrCreateTag("신규1")).willReturn(tag1);
    given(tagService.findOrCreateTag("신규2")).willReturn(tag2);

    // when
    tagMappingService.createTagMappings(menuItem, List.of("신규1", "신규2"));

    // then
    then(tagService).should().findOrCreateTag("신규1");
    then(tagService).should().findOrCreateTag("신규2");
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