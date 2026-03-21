package com.gallery.backend.dto;

import java.time.Instant;

public record PhotoResponse(
        String id,
        String title,
        String url,
        String thumbUrl,
        Instant createdAt,
        int width,
        int height,
        int likes
) {
}
