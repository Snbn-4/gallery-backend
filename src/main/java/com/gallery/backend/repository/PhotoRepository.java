package com.gallery.backend.repository;

import com.gallery.backend.entity.PhotoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PhotoRepository extends JpaRepository<PhotoEntity, String> {

    @Query("""
            SELECT DISTINCT p FROM PhotoEntity p
            LEFT JOIN p.tags t
            WHERE (:tag IS NULL OR LOWER(t.name) = LOWER(:tag))
              AND (:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY p.createdAt DESC
            """)
    Page<PhotoEntity> findAllFiltered(
            @Param("tag") String tag,
            @Param("search") String search,
            Pageable pageable
    );
}
