# Gallery Backend (Java + MongoDB)

Simple Spring Boot backend for photo gallery metadata + file upload.

## Stack

- Java 17
- Spring Boot 3
- MongoDB

## Run

1. Start MongoDB locally (default: `mongodb://localhost:27017`).
2. Update DB connection URI in `src/main/resources/application.yml` if needed.
3. Start app:

```bash
mvn spring-boot:run
```

App starts on `http://localhost:8080` and auto-creates upload dirs at `./uploads/full` and `./uploads/thumb`.

## API

### Create photo

`POST /api/photos` with `multipart/form-data`

- `file`: image file (jpeg/png)
- `title`: text
- `tags`: optional, repeat field (`tags=nature`, `tags=sunset`)
- `likes`: optional integer (defaults to 0)

Example curl:

```bash
curl -X POST "http://localhost:8080/api/photos" \
  -F "file=@/path/to/photo.jpg" \
  -F "title=Sunset" \
  -F "tags=nature" \
  -F "tags=travel" \
  -F "likes=120"
```

### List photos

`GET /api/photos?page=0&size=20&tag=nature&search=sun`

Returns array in this shape:

```json
[
  {
    "id": "p1",
    "title": "Sunset",
    "url": "https://.../full.jpg",
    "thumbUrl": "https://.../thumb.jpg",
    "createdAt": "2026-03-18T12:00:00Z",
    "width": 1920,
    "height": 1280,
    "likes": 120
  }
]
```

### Get one photo

`GET /api/photos/{id}`

### Update photo metadata

`PUT /api/photos/{id}`

```json
{
  "title": "New title",
  "tags": ["nature", "evening"],
  "likes": 200
}
```

### Delete photo

`DELETE /api/photos/{id}`

### List tags

`GET /api/tags`

### Direct image URLs

- Full image: `GET /api/photos/{id}/file`
- Thumbnail: `GET /api/photos/{id}/thumb`

## Notes

- `url` and `thumbUrl` are generated using `app.base-url` in `application.yml`.
- `createdAt` is stored in UTC.
