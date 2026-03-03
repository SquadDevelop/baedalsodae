package com.project.baedalsodae.tag.service.Impl;

import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.tag.entity.Tag;
import com.project.baedalsodae.tag.entity.TagMapping;
import com.project.baedalsodae.tag.repository.TagMappingRepository;
import com.project.baedalsodae.tag.service.TagMappingService;
import com.project.baedalsodae.tag.service.TagService;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagMappingServiceImpl implements TagMappingService {

  private final TagService tagService;
  private final TagMappingRepository tagMappingRepository;

  @Override
  @Transactional
  public void createTagMappings(MenuItem menuItem, List<String> tagNames) {

    List<String> distinctNames = tagNames.stream().distinct().toList();

    tagService.createNewTagsIfNotExists(distinctNames);

    Map<String, Tag> tagMap =
        tagService.findAllByNames(distinctNames).stream()
            .collect(Collectors.toMap(Tag::getName, t -> t));

    List<TagMapping> tagMappings = new ArrayList<>();

    for (int i = 0; i < tagNames.size(); i++) {
      String tagName = tagNames.get(i);
      Tag tag = tagMap.get(tagName);
      TagMapping mapping = TagMapping.create(tag, menuItem, i);
      tagMappings.add(mapping);
    }

    tagMappingRepository.saveAll(tagMappings);
  }

  @Transactional
  @Override
  public void deleteAllTagMappingByMenuItemId(UUID menuItemId) {
    tagMappingRepository.deleteByMenuItemId(menuItemId);
  }
}
