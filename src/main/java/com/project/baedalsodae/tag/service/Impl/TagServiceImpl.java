package com.project.baedalsodae.tag.service.Impl;

import com.project.baedalsodae.tag.dto.responseDto.TagResponseDto;
import com.project.baedalsodae.tag.entity.Tag;
import com.project.baedalsodae.tag.repository.TagBulkRepository;
import com.project.baedalsodae.tag.repository.TagRepository;
import com.project.baedalsodae.tag.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagBulkRepository tagBulkRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findAllByNames(List<String> names) {
        return tagRepository.findAllByNameIn(names);
    }

    @Override
    public void createNewTagsIfNotExists(List<String> tagNames) {
        List<String> distinctNames = tagNames.stream().distinct().toList();
        log.debug(tagNames.toString());
        tagBulkRepository.bulkInsertIgnore(distinctNames);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TagResponseDto> getTagListByParams(String keyword, int count) {
        List<Tag> tags;
        if (keyword == null || keyword.isBlank()) {
            tags = tagRepository.findTopByMappingCount(count);
        } else tags = tagRepository.findByKeywordOrderByRelevance(keyword, count);

        return tags.stream().map(TagResponseDto::fromEntity).toList();
    }
}
