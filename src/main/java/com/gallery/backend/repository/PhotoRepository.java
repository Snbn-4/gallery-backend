package com.gallery.backend.repository;

import com.gallery.backend.entity.PhotoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PhotoRepository extends MongoRepository<PhotoEntity, String> {
}
