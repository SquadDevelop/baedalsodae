package com.project.baedalsodae.tag.service;

import com.project.baedalsodae.tag.dto.responseDto.TagResponseDto;
import com.project.baedalsodae.tag.entity.Tag;
import java.util.List;

public interface TagService {

  List<Tag> findAllByNames(List<String> names);

  void createNewTagsIfNotExists(List<String> distinctNames);

  List<TagResponseDto> getTagListByParams(String keyword, int count);
}
