package com.gallery.backend.controller;

import com.gallery.backend.service.PhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final PhotoService photoService;

    public TagController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @GetMapping
    public ResponseEntity<List<String>> listTags() {
        List<String> tags = photoService.listTags();
        return ResponseEntity.ok(tags);
    }
}
