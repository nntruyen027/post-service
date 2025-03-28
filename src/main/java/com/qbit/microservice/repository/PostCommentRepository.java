package com.qbit.microservice.repository;

import com.qbit.microservice.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostId(Long id);

    Page<PostComment> findByPostId(Long id, Pageable pageable);
}
