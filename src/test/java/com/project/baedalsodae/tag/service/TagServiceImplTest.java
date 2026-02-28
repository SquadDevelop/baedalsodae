package com.project.baedalsodae.tag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.project.baedalsodae.tag.entity.Tag;
import com.project.baedalsodae.tag.repository.TagRepository;
import com.project.baedalsodae.tag.service.Impl.TagServiceImpl;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

  @Mock private TagRepository tagRepository;

  @InjectMocks private TagServiceImpl tagService;

  @Test
  @DisplayName("이름 목록으로 기존 태그를 일괄 조회한다")
  void findAllByNames_getExistingTagsByNames() {
    // given
    Tag tag1 = mock(Tag.class);
    Tag tag2 = mock(Tag.class);
    given(tagRepository.findAllByNameIn(List.of("치킨", "피자"))).willReturn(List.of(tag1, tag2));

    // when
    List<Tag> result = tagService.findAllByNames(List.of("치킨", "피자"));

    // then
    assertThat(result).containsExactly(tag1, tag2);
    then(tagRepository).should().findAllByNameIn(List.of("치킨", "피자"));
  }

  @Test
  @DisplayName("신규 태그 목록을 단일 배치 INSERT로 저장하고 Map으로 반환한다")
  void createNewTags_saveNewTagsInBatchAndReturnMap() {
    // given
    given(tagRepository.saveAllAndFlush(anyList())).willReturn(List.of());

    // when
    Map<String, Tag> result = tagService.createNewTags(List.of("새태그1", "새태그2"));

    // then
    assertThat(result).containsKeys("새태그1", "새태그2");
    then(tagRepository).should().saveAllAndFlush(anyList());
  }

  @Test
  @DisplayName("배치 저장 중 동시 충돌 발생 시 DataIntegrityViolationException을 그대로 전파한다")
  void createNewTags_whenBatchSaveConflict_thenThrowDataIntegrityViolationException() {
    // given
    given(tagRepository.saveAllAndFlush(anyList()))
        .willThrow(DataIntegrityViolationException.class);

    // when & then
    assertThatThrownBy(() -> tagService.createNewTags(List.of("중복태그")))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("태그가 이미 존재하면 저장 없이 기존 태그를 반환한다")
  void findOrCreateTag_whenTagExists_thenReturnExistingTagWithoutSaving() {
    // given
    Tag existingTag = mock(Tag.class);
    given(tagRepository.findByName("치킨")).willReturn(Optional.of(existingTag));

    // when
    Tag result = tagService.findOrCreateTag("치킨");

    // then
    assertThat(result).isEqualTo(existingTag);
    then(tagRepository).should(never()).saveAndFlush(any());
  }

  @Test
  @DisplayName("태그가 없으면 새로 생성하여 반환한다")
  void findOrCreateTag_whenTagDoesNotExist_thenCreateAndReturnNewTag() {
    // given
    Tag savedTag = mock(Tag.class);
    given(tagRepository.findByName("신규태그")).willReturn(Optional.empty());
    given(tagRepository.saveAndFlush(any(Tag.class))).willReturn(savedTag);

    // when
    Tag result = tagService.findOrCreateTag("신규태그");

    // then
    assertThat(result).isEqualTo(savedTag);
    then(tagRepository).should().saveAndFlush(any(Tag.class));
  }
}
