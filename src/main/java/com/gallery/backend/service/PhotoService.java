package com.gallery.backend.service;

import com.gallery.backend.config.AppProperties;
import com.gallery.backend.dto.PhotoResponse;
import com.gallery.backend.dto.PhotoUpdateRequest;
import com.gallery.backend.entity.PhotoEntity;
import com.gallery.backend.entity.TagEntity;
import com.gallery.backend.repository.PhotoRepository;
import com.gallery.backend.repository.TagRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class PhotoService {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png");

    private final PhotoRepository photoRepository;
    private final TagRepository tagRepository;
    private final AppProperties appProperties;

    public PhotoService(PhotoRepository photoRepository, TagRepository tagRepository, AppProperties appProperties) {
        this.photoRepository = photoRepository;
        this.tagRepository = tagRepository;
        this.appProperties = appProperties;
    }

    @Transactional
    public PhotoResponse create(MultipartFile file, String title, List<String> tags, Integer likes) {
        validateFile(file);
        if (!StringUtils.hasText(title)) {
            throw new ResponseStatusException(BAD_REQUEST, "title is required");
        }

        try {
            byte[] inputBytes = file.getBytes();
            BufferedImage original = ImageIO.read(new ByteArrayInputStream(inputBytes));
            if (original == null) {
                throw new ResponseStatusException(BAD_REQUEST, "Invalid image file");
            }

            String id = UUID.randomUUID().toString();
            String extension = resolveExtension(file.getContentType(), file.getOriginalFilename());
            Path root = Paths.get(appProperties.getStorage().getUploadDir()).toAbsolutePath().normalize();
            Path fullPath = root.resolve("full").resolve(id + "." + extension);
            Path thumbPath = root.resolve("thumb").resolve(id + ".jpg");

            Files.copy(new ByteArrayInputStream(inputBytes), fullPath, StandardCopyOption.REPLACE_EXISTING);

            BufferedImage thumb = createThumbnail(original, 480);
            try (ByteArrayOutputStream thumbOut = new ByteArrayOutputStream()) {
                ImageIO.write(thumb, "jpg", thumbOut);
                Files.copy(new ByteArrayInputStream(thumbOut.toByteArray()), thumbPath, StandardCopyOption.REPLACE_EXISTING);
            }

            PhotoEntity entity = new PhotoEntity();
            entity.setId(id);
            entity.setTitle(title.trim());
            entity.setMimeType(file.getContentType());
            entity.setSizeBytes(file.getSize());
            entity.setWidth(original.getWidth());
            entity.setHeight(original.getHeight());
            entity.setLikes(likes == null ? 0 : Math.max(0, likes));
            entity.setCreatedAt(Instant.now());
            entity.setStoragePath(fullPath.toString());
            entity.setThumbStoragePath(thumbPath.toString());
            entity.setUrl(buildPublicUrl("/api/photos/" + id + "/file"));
            entity.setThumbUrl(buildPublicUrl("/api/photos/" + id + "/thumb"));
            entity.setTags(resolveTags(tags));

            PhotoEntity saved = photoRepository.save(entity);
            return toDto(saved);
        } catch (IOException e) {
            throw new ResponseStatusException(BAD_REQUEST, "Failed to process image", e);
        }
    }

    public List<PhotoResponse> list(int page, int size, String tag, String search) {
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), 100));
        return photoRepository.findAllFiltered(normalizeNullable(tag), normalizeNullable(search), pageable)
                .stream()
                .map(this::toDto)
                .toList();
    }

    public PhotoResponse getById(String id) {
        PhotoEntity photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Photo not found"));
        return toDto(photo);
    }

    @Transactional
    public PhotoResponse update(String id, PhotoUpdateRequest request) {
        PhotoEntity photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Photo not found"));
        photo.setTitle(request.title().trim());
        photo.setLikes(request.likes() == null ? photo.getLikes() : Math.max(0, request.likes()));
        photo.setTags(resolveTags(request.tags()));
        return toDto(photoRepository.save(photo));
    }

    @Transactional
    public void delete(String id) {
        PhotoEntity photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Photo not found"));
        try {
            Files.deleteIfExists(Path.of(photo.getStoragePath()));
            Files.deleteIfExists(Path.of(photo.getThumbStoragePath()));
        } catch (IOException ignored) {
        }
        photoRepository.delete(photo);
    }

    public byte[] readFile(String id, boolean thumb) {
        PhotoEntity photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Photo not found"));
        Path path = Path.of(thumb ? photo.getThumbStoragePath() : photo.getStoragePath());
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new ResponseStatusException(NOT_FOUND, "Image file not found");
        }
    }

    public MediaType resolveMediaType(String id, boolean thumb) {
        if (thumb) {
            return MediaType.IMAGE_JPEG;
        }
        PhotoEntity photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Photo not found"));
        try {
            return MediaType.parseMediaType(photo.getMimeType());
        } catch (IllegalArgumentException ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "file is required");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new ResponseStatusException(BAD_REQUEST, "Only jpeg/png are allowed");
        }
    }

    private Set<TagEntity> resolveTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new HashSet<>();
        }
        return tags.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .map(this::findOrCreateTag)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private TagEntity findOrCreateTag(String tagName) {
        return tagRepository.findByNameIgnoreCase(tagName)
                .orElseGet(() -> {
                    TagEntity tag = new TagEntity();
                    tag.setName(tagName);
                    return tagRepository.save(tag);
                });
    }

    private PhotoResponse toDto(PhotoEntity photo) {
        return new PhotoResponse(
                photo.getId(),
                photo.getTitle(),
                photo.getUrl(),
                photo.getThumbUrl(),
                photo.getCreatedAt(),
                photo.getWidth(),
                photo.getHeight(),
                photo.getLikes()
        );
    }

    private String resolveExtension(String contentType, String originalFilename) {
        if (MediaType.IMAGE_JPEG_VALUE.equals(contentType)) {
            return "jpg";
        }
        if (MediaType.IMAGE_PNG_VALUE.equals(contentType)) {
            return "png";
        }
        if (StringUtils.hasText(originalFilename) && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf('.') + 1);
        }
        return "jpg";
    }

    private BufferedImage createThumbnail(BufferedImage original, int maxWidth) {
        int sourceWidth = original.getWidth();
        int sourceHeight = original.getHeight();
        int targetWidth = Math.min(maxWidth, sourceWidth);
        int targetHeight = (int) (((double) targetWidth / sourceWidth) * sourceHeight);

        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resized.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();
        return resized;
    }

    private String buildPublicUrl(String path) {
        return appProperties.getBaseUrl() + path;
    }

    private String normalizeNullable(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
