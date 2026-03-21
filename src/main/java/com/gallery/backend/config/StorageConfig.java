package com.gallery.backend.config;

import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class StorageConfig {

    private final AppProperties appProperties;

    public StorageConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    public void ensureUploadDirs() throws IOException {
        Path root = Paths.get(appProperties.getStorage().getUploadDir()).toAbsolutePath().normalize();
        Files.createDirectories(root.resolve("full"));
        Files.createDirectories(root.resolve("thumb"));
    }
}
