package com.qbit.microservice.repository;

import com.qbit.microservice.entity.PostFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostFavoriteRepository extends JpaRepository<PostFavorite, Long> {
    int countByPostId(Long id);

    List<PostFavorite> findByUserId(Long userId);

    Page<PostFavorite> findByUserId(Long userId, Pageable pageable);

    Page<PostFavorite> findByPostId(Long postId, Pageable pageable);

    Optional<PostFavorite> findByUserIdAndPostId(Long userId, Long postId);

    boolean existsByUserIdAndPostId(Long userId, Long postId);

}
