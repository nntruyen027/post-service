package com.qbit.microservice.repository;

import com.qbit.microservice.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostId(Long id);

    Page<PostComment> findByPostId(Long id, Pageable pageable);

    Optional<PostComment> findByIdAndUserId(Long id, Long userId);
}
