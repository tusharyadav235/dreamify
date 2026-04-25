package com.dreamify.repository;

import com.dreamify.model.VideoTestimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VideoTestimonialRepository extends JpaRepository<VideoTestimonial, Long> {
    List<VideoTestimonial> findAllByActiveTrueOrderByCreatedAtDesc();
    List<VideoTestimonial> findAllByOrderByCreatedAtDesc();
}
