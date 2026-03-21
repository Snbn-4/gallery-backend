CREATE TABLE photos (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    title NVARCHAR(200) NOT NULL,
    url NVARCHAR(1000) NOT NULL,
    thumb_url NVARCHAR(1000) NOT NULL,
    storage_path NVARCHAR(1000) NOT NULL,
    thumb_storage_path NVARCHAR(1000) NOT NULL,
    mime_type NVARCHAR(100) NOT NULL,
    size_bytes BIGINT NOT NULL,
    created_at DATETIME2 NOT NULL CONSTRAINT df_photos_created_at DEFAULT SYSUTCDATETIME(),
    width INT NOT NULL,
    height INT NOT NULL,
    likes INT NOT NULL CONSTRAINT df_photos_likes DEFAULT 0
);

CREATE TABLE tags (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE photo_tags (
    photo_id VARCHAR(36) NOT NULL,
    tag_id BIGINT NOT NULL,
    CONSTRAINT pk_photo_tags PRIMARY KEY (photo_id, tag_id),
    CONSTRAINT fk_photo_tags_photo FOREIGN KEY (photo_id) REFERENCES photos(id) ON DELETE CASCADE,
    CONSTRAINT fk_photo_tags_tag FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

CREATE INDEX ix_photos_created_at ON photos(created_at DESC);
CREATE INDEX ix_tags_name ON tags(name);
CREATE INDEX ix_photo_tags_tag_photo ON photo_tags(tag_id, photo_id);
