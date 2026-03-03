package com.project.baedalsodae.tag.service;

import com.project.baedalsodae.tag.entity.Tag;
import java.util.List;
import java.util.Map;

public interface TagService {

  List<Tag> findAllByNames(List<String> names);

  void createNewTagsIfNotExists(List<String> distinctNames);
}
