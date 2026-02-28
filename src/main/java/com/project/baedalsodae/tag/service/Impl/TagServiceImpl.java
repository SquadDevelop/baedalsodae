package com.project.baedalsodae.tag.service.Impl;

import com.project.baedalsodae.tag.entity.Tag;
import com.project.baedalsodae.tag.repository.TagRepository;
import com.project.baedalsodae.tag.service.TagService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

  private final TagRepository tagRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Tag> findAllByNames(List<String> names) {
    return tagRepository.findAllByNameIn(names);
  }

  @Override
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public Map<String, Tag> createNewTags(List<String> names) {
    List<Tag> tags = names.stream().map(Tag::create).toList();
    tagRepository.saveAllAndFlush(tags);
    return tags.stream().collect(Collectors.toMap(Tag::getName, t -> t));
  }

  @Override
  public Tag findOrCreateTag(String name) {
    return tagRepository.findByName(name).orElseGet(() -> {
      Tag newTag = Tag.create(name);
      return tagRepository.saveAndFlush(newTag);
    });
  }

}
