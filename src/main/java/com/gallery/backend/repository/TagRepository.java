package com.gallery.backend.repository;

import com.gallery.backend.entity.TagEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<TagEntity, Long> {

    Optional<TagEntity> findByNameIgnoreCase(String name);
}
