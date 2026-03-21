package com.gallery.backend.controller;

import com.gallery.backend.repository.TagRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagRepository tagRepository;

    public TagController(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @GetMapping
    public ResponseEntity<List<String>> listTags() {
        List<String> tags = tagRepository.findAll()
                .stream()
                .map(tag -> tag.getName())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
        return ResponseEntity.ok(tags);
    }
}
