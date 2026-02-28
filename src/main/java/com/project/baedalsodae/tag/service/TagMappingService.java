package com.project.baedalsodae.tag.service;

import com.project.baedalsodae.menu.entity.MenuItem;
import java.util.List;

public interface TagMappingService {

  void createTagMappings(MenuItem menuItem, List<String> tagNames);
}
