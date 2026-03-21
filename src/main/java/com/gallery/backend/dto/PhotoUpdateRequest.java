package com.gallery.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PhotoUpdateRequest(
        @NotBlank @Size(max = 200) String title,
        List<@Size(min = 1, max = 100) String> tags,
        @Min(0) Integer likes
) {
}
