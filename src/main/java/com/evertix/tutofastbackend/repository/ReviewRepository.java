package com.evertix.tutofastbackend.repository;

import com.evertix.tutofastbackend.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review,Long> {
    Page<Review> getAllByTeacherId(Long teacherId, Pageable pageable);

    List<Review> findAllByTeacherId(Long teacherId);
}
