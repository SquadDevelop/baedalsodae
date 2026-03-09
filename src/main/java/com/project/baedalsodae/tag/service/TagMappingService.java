package com.project.baedalsodae.tag.service;

import com.project.baedalsodae.menu.entity.MenuItem;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TagMappingService {

    void createTagMappings(MenuItem menuItem, List<String> tagNames);

    void deleteAllTagMappingByMenuItemId(UUID menuItemId);

    Map<UUID, List<String>> getTagNamesByMenuItemIds(List<UUID> menuItemIds);
}
