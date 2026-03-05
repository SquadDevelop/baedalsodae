package com.project.baedalsodae.tag.controller;

import com.project.baedalsodae.global.common.ApiResponse;
import com.project.baedalsodae.tag.dto.responseDto.TagResponseDto;
import com.project.baedalsodae.tag.service.TagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tags")
public class TagController {

    private final TagService tagService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponseDto>>> getTagListByParams(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "10") Integer count) {
        List<TagResponseDto> response = tagService.getTagListByParams(keyword, count);
        return ResponseEntity.ok(ApiResponse.success("", response));
    }
}
