package com.project.baedalsodae.tag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

import com.project.baedalsodae.tag.entity.Tag;
import com.project.baedalsodae.tag.repository.TagBulkRepository;
import com.project.baedalsodae.tag.repository.TagRepository;
import com.project.baedalsodae.tag.service.Impl.TagServiceImpl;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

  @Mock private TagRepository tagRepository;
  @Mock private TagBulkRepository tagBulkRepository;

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
  @DisplayName("태그 이름 목록을 ON CONFLICT DO NOTHING으로 벌크 insert한다")
  void createNewTagsIfNotExists_bulkInsertWithConflictIgnore() {
    // given
    List<String> names = List.of("치킨", "피자");

    // when
    tagService.createNewTagsIfNotExists(names);

    // then
    then(tagBulkRepository).should().bulkInsertIgnore(names);
  }
}