package com.qbit.microservice.repository;

import com.qbit.microservice.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByUserIdAndId(Long userId, Long id);

    Page<Post> findByIsPublicTrue(Pageable pageable);
}
