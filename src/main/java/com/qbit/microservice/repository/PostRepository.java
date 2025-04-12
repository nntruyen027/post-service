package com.qbit.microservice.repository;

import com.qbit.microservice.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByUserIdAndId(Long userId, Long id);

    Page<Post> findByIsPublicTrue(Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "WHERE p.isPublic = true AND (" +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) ")
    Page<Post> searchPublicPostsByKeyword(String keyword, Pageable pageable);


    @Query("""
                SELECT p FROM Post p
                WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    Page<Post> findAllWithOptionalKeyword(String keyword, Pageable pageable);


}

