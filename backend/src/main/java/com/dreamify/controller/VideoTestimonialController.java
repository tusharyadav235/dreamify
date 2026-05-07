package com.dreamify.controller;

import com.dreamify.model.VideoTestimonial;
import com.dreamify.repository.VideoTestimonialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/video-testimonials")
@RequiredArgsConstructor
public class VideoTestimonialController {

    private final VideoTestimonialRepository repo;

    @Value("${app.upload.dir:/app/uploads}")
    private String uploadDir;

    @Value("${app.upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    // ── YouTube ID extraction regex ──────────────────────────────────────────
    private static final Pattern YT_PATTERN = Pattern.compile(
        "(?:youtu\\.be/|youtube\\.com/(?:embed/|v/|watch\\?v=|shorts/))([A-Za-z0-9_-]{11})"
    );

    private String extractYoutubeId(String url) {
        if (url == null) return null;
        Matcher m = YT_PATTERN.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    // ── PUBLIC: active videos only (for main website) ────────────────────────
    @GetMapping
    public List<VideoTestimonial> getActive() {
        return repo.findAllByActiveTrueOrderByCreatedAtDesc();
    }

    // ── ADMIN: all videos including hidden ───────────────────────────────────
    @GetMapping("/all")
    public List<VideoTestimonial> getAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    // ── ADD YOUTUBE VIDEO ────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<VideoTestimonial> createYoutube(
            @RequestBody VideoTestimonial v) {

        v.setVideoType("YOUTUBE");

        // Extract YouTube ID and build embed + thumbnail URLs
        String ytId = extractYoutubeId(v.getVideoUrl());
        if (ytId != null) {
            v.setYoutubeVideoId(ytId);
            // Store the embed URL (restricts playback to embedded context)
            v.setVideoUrl("https://www.youtube.com/embed/" + ytId +
                    "?rel=0&modestbranding=1&showinfo=0&controls=1");
            // Auto thumbnail if none provided
            if (v.getThumbnailUrl() == null || v.getThumbnailUrl().isBlank()) {
                v.setThumbnailUrl("https://img.youtube.com/vi/" + ytId + "/hqdefault.jpg");
            }
        }

        return ResponseEntity.ok(repo.save(v));
    }

    // ── UPLOAD VIDEO FROM LAPTOP ─────────────────────────────────────────────
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<VideoTestimonial> uploadVideo(
            @RequestParam("file")        MultipartFile file,
            @RequestParam("clientName")  String clientName,
            @RequestParam(value = "clientRole",   required = false) String clientRole,
            @RequestParam(value = "description",  required = false) String description,
            @RequestParam(value = "rating",       defaultValue = "5") int rating,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile
    ) throws IOException {

        // Validate file type — only allow video formats
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("video/")) {
            return ResponseEntity.badRequest().build();
        }

        // Ensure upload directory exists
        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath);

        // Save video file with a unique name to avoid collisions
        String originalName = StringUtils.cleanPath(
                Objects.requireNonNull(file.getOriginalFilename()));
        String extension    = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".mp4";
        String uniqueName   = UUID.randomUUID() + extension;
        Path   targetPath   = uploadPath.resolve(uniqueName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // Handle optional custom thumbnail upload
        String thumbUrl = null;
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            String thumbCT = thumbnailFile.getContentType();
            if (thumbCT != null && thumbCT.startsWith("image/")) {
                String thumbExt  = Objects.requireNonNull(
                        thumbnailFile.getOriginalFilename()).contains(".")
                        ? thumbnailFile.getOriginalFilename()
                                       .substring(thumbnailFile.getOriginalFilename().lastIndexOf("."))
                        : ".jpg";
                String thumbName = "thumb_" + UUID.randomUUID() + thumbExt;
                Files.copy(thumbnailFile.getInputStream(),
                           uploadPath.resolve(thumbName),
                           StandardCopyOption.REPLACE_EXISTING);
                thumbUrl = baseUrl + "/" + thumbName;
            }
        }

        // Build the entity
        VideoTestimonial v = new VideoTestimonial();
        v.setClientName(clientName);
        v.setClientRole(clientRole);
        v.setDescription(description);
        v.setRating(rating);
        v.setVideoType("UPLOAD");
        v.setVideoUrl(baseUrl + "/" + uniqueName);
        v.setOriginalFilename(originalName);
        v.setThumbnailUrl(thumbUrl);
        v.setActive(true);

        return ResponseEntity.ok(repo.save(v));
    }

    // ── TOGGLE ACTIVE/HIDDEN ─────────────────────────────────────────────────
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<VideoTestimonial> toggle(@PathVariable Long id) {
        return repo.findById(id).map(v -> {
            v.setActive(!v.isActive());
            return ResponseEntity.ok(repo.save(v));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── DELETE (removes DB record + physical file for uploads) ───────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<VideoTestimonial> delete(@PathVariable Long id) {
        return repo.findById(id).map(v -> {
            // If it's an uploaded file, delete from disk too
            if ("UPLOAD".equals(v.getVideoType()) && v.getVideoUrl() != null) {
                try {
                    String filename = v.getVideoUrl().substring(v.getVideoUrl().lastIndexOf("/") + 1);
                    Path filePath = Paths.get(uploadDir).resolve(filename);
                    Files.deleteIfExists(filePath);

                    // Also delete thumbnail if it was an uploaded image
                    if (v.getThumbnailUrl() != null && v.getThumbnailUrl().contains("/uploads/")) {
                        String thumbName = v.getThumbnailUrl().substring(v.getThumbnailUrl().lastIndexOf("/") + 1);
                        Files.deleteIfExists(Paths.get(uploadDir).resolve(thumbName));
                    }
                } catch (IOException e) {
                    // Log but don't block deletion
                    System.err.println("Could not delete file: " + e.getMessage());
                }
            }
            repo.deleteById(id);
            return ResponseEntity.ok(v);
        }).orElse(ResponseEntity.notFound().build());
    }
}