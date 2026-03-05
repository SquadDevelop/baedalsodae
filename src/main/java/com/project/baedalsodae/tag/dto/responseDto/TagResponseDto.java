package com.project.baedalsodae.tag.dto.responseDto;

import com.project.baedalsodae.tag.entity.Tag;
import java.util.UUID;

public record TagResponseDto(UUID id, String name) {
    public static TagResponseDto fromEntity(Tag tag) {
        return new TagResponseDto(tag.getId(), tag.getName());
    }
}
