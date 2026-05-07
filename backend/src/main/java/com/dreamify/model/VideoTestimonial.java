package com.dreamify.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "video_testimonials")
@Data
public class VideoTestimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String clientName;

    private String clientRole;

    // "YOUTUBE" or "UPLOAD"
    @Column(nullable = false)
    private String videoType = "YOUTUBE";

    // YouTube: raw URL. Upload: full public URL to the file
    @NotBlank
    @Column(nullable = false)
    private String videoUrl;

    // YouTube video ID extracted from URL (null for uploaded videos)
    private String youtubeVideoId;

    // Thumbnail URL (auto from YouTube or custom uploaded)
    private String thumbnailUrl;

    // Original filename — stored for UPLOAD type only
    private String originalFilename;

    @Column(columnDefinition = "TEXT")
    private String description;

    private int rating = 5;

    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}