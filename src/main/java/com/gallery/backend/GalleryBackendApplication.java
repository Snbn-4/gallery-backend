package com.gallery.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.gallery.backend.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class GalleryBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GalleryBackendApplication.class, args);
    }
}
