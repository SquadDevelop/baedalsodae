package com.project.baedalsodae.tag.service.Impl;

import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.tag.entity.Tag;
import com.project.baedalsodae.tag.entity.TagMapping;
import com.project.baedalsodae.tag.repository.TagMappingRepository;
import com.project.baedalsodae.tag.service.TagMappingService;
import com.project.baedalsodae.tag.service.TagService;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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

    Map<String, Tag> resolvedTagMap = new HashMap<>();

    List<Tag> tagList = tagService.findAllByNames(tagNames);
    for (Tag tag : tagList) {
      resolvedTagMap.put(tag.getName(), tag);
    }

    List<String> tagNotFoundNames =
        tagNames.stream().filter(name -> !resolvedTagMap.containsKey(name)).toList();

    if (!tagNotFoundNames.isEmpty()) {
      try {
        Map<String, Tag> newTagMap = tagService.createNewTags(tagNotFoundNames);
        resolvedTagMap.putAll(newTagMap);
      } catch (DataIntegrityViolationException e) {
        for (String name : tagNotFoundNames) {
          resolvedTagMap.put(name, tagService.findOrCreateTag(name));
        }
      }
    }

    List<TagMapping> tagMappings = new ArrayList<>();

    for (int i = 0; i < tagNames.size(); i++) {
      String tagName = tagNames.get(i);
      Tag tag = resolvedTagMap.get(tagName);
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
