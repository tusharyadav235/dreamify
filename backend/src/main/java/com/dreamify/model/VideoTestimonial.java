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

    private String clientRole;      // e.g. "CEO - Bloom Journal"

    private String clientCompany;

    // YouTube / Vimeo embed URL  OR  direct MP4 link
    @NotBlank
    @Column(nullable = false)
    private String videoUrl;

    // Optional thumbnail override (if empty, we use YouTube auto-thumb)
    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String description;     // short caption shown on hover / mobile

    private int rating = 5;         // 1-5 stars

    private boolean active = true;  // admin can hide without deleting

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
