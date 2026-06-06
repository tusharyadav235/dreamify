package com.dreamify.controller;

import com.dreamify.model.Enquiry;
import com.dreamify.repository.EnquiryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enquiries")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EnquiryController {

    private final EnquiryRepository repo;

    // SECURE: Admin Dashboard only
    @GetMapping
    public List<Enquiry> getAll() {
        return repo.findAllByOrderByCreatedAtDesc();
    }

    // SECURE: Admin Dashboard only
    @GetMapping("/{id}")
    public ResponseEntity<Enquiry> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // PUBLIC: Accessible by public website visitors
    @PostMapping("/submit")
    public ResponseEntity<Enquiry> create(@Valid @RequestBody Enquiry enquiry) {
        enquiry.setStatus(Enquiry.Status.NEW);
        return ResponseEntity.ok(repo.save(enquiry));
    }

    // SECURE: Admin Dashboard only
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        String status = body.get("status");
        if (status == null) {
            return ResponseEntity.badRequest().body("status is required");
        }

        return repo.findById(id).map(e -> {
            try {
                e.setStatus(Enquiry.Status.valueOf(status));
                return ResponseEntity.ok(repo.save(e));
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body("Invalid status value");
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    // SECURE: Admin Dashboard only
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // SECURE: Admin Dashboard only
    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return Map.of(
            "total", repo.count(),
            "new", repo.countByStatus(Enquiry.Status.NEW),
            "contacted", repo.countByStatus(Enquiry.Status.CONTACTED),
            "completed", repo.countByStatus(Enquiry.Status.COMPLETED)
        );
    }
}
