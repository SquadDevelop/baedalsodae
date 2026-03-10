package com.project.baedalsodae.tag.service.Impl;

import com.project.baedalsodae.menu.entity.MenuItem;
import com.project.baedalsodae.tag.entity.Tag;
import com.project.baedalsodae.tag.entity.TagMapping;
import com.project.baedalsodae.tag.repository.TagMappingRepository;
import com.project.baedalsodae.tag.repository.TagMappingRepository.TagNameProjection;
import com.project.baedalsodae.tag.service.TagMappingService;
import com.project.baedalsodae.tag.service.TagService;
import java.util.*;
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
        if (tagNames == null || tagNames.isEmpty()) return;

        List<String> distinctNames = tagNames.stream().distinct().toList();

        tagService.createNewTagsIfNotExists(distinctNames);

        Map<String, Tag> tagMap = new HashMap<>();
        for (Tag tag : tagService.findAllByNames(distinctNames)) {
            tagMap.put(tag.getName(), tag);
        }

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

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, List<String>> getTagNamesByMenuItemIds(List<UUID> menuItemIds) {
        if (menuItemIds.isEmpty()) return Map.of();
        Map<UUID, List<String>> result = new HashMap<>();
        for (TagNameProjection row : tagMappingRepository.findTagDataByMenuItemIds(menuItemIds)) {
            result.computeIfAbsent(row.getMenuItemId(), k -> new ArrayList<>())
                    .add(row.getTagName());
        }
        return result;
    }
}
