package com.project.baedalsodae.tag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.project.baedalsodae.tag.dto.responseDto.TagResponseDto;
import com.project.baedalsodae.tag.entity.Tag;
import com.project.baedalsodae.tag.repository.TagBulkRepository;
import com.project.baedalsodae.tag.repository.TagRepository;
import com.project.baedalsodae.tag.service.Impl.TagServiceImpl;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Slf4j
@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

  @Mock private TagRepository tagRepository;
  @Mock private TagBulkRepository tagBulkRepository;

  @Mock private Tag tag1;
  @Mock private Tag tag2;

  @InjectMocks private TagServiceImpl tagService;

  @Test
  @DisplayName("이름 목록이 주어지면 일치하는 태그 목록을 반환한다")
  void findAllByNames_getExistingTagsByNames() {
    // given
    given(tagRepository.findAllByNameIn(List.of("치킨", "피자"))).willReturn(List.of(tag1, tag2));

    // when
    List<Tag> result = tagService.findAllByNames(List.of("치킨", "피자"));
    log.info("result = {}", result);

    // then
    assertThat(result).containsExactly(tag1, tag2);
    verify(tagRepository).findAllByNameIn(List.of("치킨", "피자"));
  }

  @Test
  @DisplayName("태그 이름 목록에 중복이 있으면 중복을 제거한 후 벌크 insert한다")
  void createNewTagsIfNotExists_deduplicatesBeforeBulkInsert() {
    // given
    List<String> namesWithDuplicate = List.of("치킨", "피자", "치킨");

    // when
    tagService.createNewTagsIfNotExists(namesWithDuplicate);

    // then
    verify(tagBulkRepository).bulkInsertIgnore(List.of("치킨", "피자"));
  }

  @Test
  @DisplayName("keyword가 null이면 태그 목록을 TagResponseDto로 변환하여 반환한다")
  void getTagListByParams_whenKeywordIsNull_returnsTagResponseDtos() {
    // given
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    given(tag1.getId()).willReturn(id1);
    given(tag1.getName()).willReturn("매운맛");
    given(tag2.getId()).willReturn(id2);
    given(tag2.getName()).willReturn("인기");
    given(tagRepository.findTopByMappingCount(10)).willReturn(List.of(tag1, tag2));

    // when
    List<TagResponseDto> result = tagService.getTagListByParams(null, 10);
    log.info("result = {}", result);

    // then
    assertThat(result)
        .extracting(TagResponseDto::id, TagResponseDto::name)
        .containsExactly(tuple(id1, "매운맛"), tuple(id2, "인기"));
    verify(tagRepository).findTopByMappingCount(10);
    then(tagRepository).should(never()).findByKeywordOrderByRelevance(any(), anyInt());
  }

  @Test
  @DisplayName("keyword가 공백 문자열이면 null과 동일하게 처리한다")
  void getTagListByParams_whenKeywordIsBlank_treatedSameAsNull() {
    // given
    UUID id1 = UUID.randomUUID();
    given(tag1.getId()).willReturn(id1);
    given(tag1.getName()).willReturn("매운맛");
    given(tagRepository.findTopByMappingCount(10)).willReturn(List.of(tag1));

    // when
    List<TagResponseDto> result = tagService.getTagListByParams("   ", 10);
    log.info("result = {}", result);

    // then
    assertThat(result)
        .extracting(TagResponseDto::id, TagResponseDto::name)
        .containsExactly(tuple(id1, "매운맛"));
    verify(tagRepository).findTopByMappingCount(10);
    then(tagRepository).should(never()).findByKeywordOrderByRelevance(any(), anyInt());
  }

  @Test
  @DisplayName("keyword가 있으면 해당 keyword로 태그를 조회하여 TagResponseDto로 반환한다")
  void getTagListByParams_whenKeywordIsPresent_returnsTagResponseDtos() {
    // given
    String keyword = "매운";
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    given(tag1.getId()).willReturn(id1);
    given(tag1.getName()).willReturn("매운맛");
    given(tag2.getId()).willReturn(id2);
    given(tag2.getName()).willReturn("아주매운닭갈비");
    given(tagRepository.findByKeywordOrderByRelevance(keyword, 10)).willReturn(List.of(tag1, tag2));

    // when
    List<TagResponseDto> result = tagService.getTagListByParams(keyword, 10);
    log.info("result = {}", result);

    // then
    assertThat(result)
        .extracting(TagResponseDto::id, TagResponseDto::name)
        .containsExactly(tuple(id1, "매운맛"), tuple(id2, "아주매운닭갈비"));
    verify(tagRepository).findByKeywordOrderByRelevance(keyword, 10);
    then(tagRepository).should(never()).findTopByMappingCount(anyInt());
  }

  @Test
  @DisplayName("keyword와 일치하는 태그가 없으면 빈 리스트를 반환한다")
  void getTagListByParams_whenNoResult_returnsEmptyList() {
    // given
    given(tagRepository.findByKeywordOrderByRelevance("없는태그", 10)).willReturn(List.of());

    // when
    List<TagResponseDto> result = tagService.getTagListByParams("없는태그", 10);
    log.info("result = {}", result);

    // then
    assertThat(result).isEmpty();
  }
}