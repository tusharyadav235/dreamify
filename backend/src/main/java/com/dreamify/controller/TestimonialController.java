package com.dreamify.controller;

import com.dreamify.model.Testimonial;
import com.dreamify.repository.TestimonialRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/testimonials")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TestimonialController {

    private final TestimonialRepository repo;

    @GetMapping
    public List<Testimonial> getAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    @PostMapping
    public ResponseEntity<Testimonial> create(@Valid @RequestBody Testimonial testimonial) {
        return ResponseEntity.ok(repo.save(testimonial));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
