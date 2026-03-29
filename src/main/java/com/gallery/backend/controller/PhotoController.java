package com.gallery.backend.controller;

import com.gallery.backend.dto.PhotoResponse;
import com.gallery.backend.dto.PhotoUpdateRequest;
import com.gallery.backend.service.PhotoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoResponse> createPhoto(
            @RequestPart("file") MultipartFile file,
            @RequestParam("title") @NotBlank String title,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "likes", required = false) Integer likes
    ) {
        return ResponseEntity.ok(photoService.create(file, title, tags, likes));
    }

    @GetMapping
    public ResponseEntity<List<PhotoResponse>> listPhotos(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(photoService.list(page, size, tag, search));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhotoResponse> getPhoto(@PathVariable String id) {
        return ResponseEntity.ok(photoService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhotoResponse> updatePhoto(
            @PathVariable String id,
            @Valid @RequestBody PhotoUpdateRequest request
    ) {
        return ResponseEntity.ok(photoService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(@PathVariable String id) {
        photoService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> getPhotoFile(@PathVariable String id) {
        return ResponseEntity.ok()
                .contentType(photoService.resolveMediaType(id, false))
                .body(photoService.readFile(id, false));
    }

    @GetMapping("/{id}/thumb")
    public ResponseEntity<byte[]> getPhotoThumb(@PathVariable String id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(photoService.readFile(id, true));
    }
}
