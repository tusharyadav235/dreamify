package com.dreamify.repository;

import com.dreamify.model.Enquiry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnquiryRepository extends JpaRepository<Enquiry, Long> {
    List<Enquiry> findAllByOrderByCreatedAtDesc();
    long countByStatus(Enquiry.Status status);
}
