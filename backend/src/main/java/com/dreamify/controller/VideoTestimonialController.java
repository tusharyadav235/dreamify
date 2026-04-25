package com.dreamify.controller;

import com.dreamify.model.VideoTestimonial;
import com.dreamify.repository.VideoTestimonialRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/video-testimonials")
@RequiredArgsConstructor
public class VideoTestimonialController {

    private final VideoTestimonialRepository repo;

    /** Public endpoint — only returns active entries */
    @GetMapping
    public List<VideoTestimonial> getActive() {
        return repo.findAllByActiveTrueOrderByCreatedAtDesc();
    }

    /** Admin endpoint — returns all including hidden */
    @GetMapping("/all")
    public List<VideoTestimonial> getAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public ResponseEntity<VideoTestimonial> create(
            @Valid @RequestBody VideoTestimonial v) {
        return ResponseEntity.ok(repo.save(v));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<VideoTestimonial> toggle(@PathVariable Long id) {
        return repo.findById(id).map(v -> {
            v.setActive(!v.isActive());
            return ResponseEntity.ok(repo.save(v));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
