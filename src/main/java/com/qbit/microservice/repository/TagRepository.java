package com.qbit.microservice.repository;

import com.qbit.microservice.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Page<Tag> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Tag> findAllByIdIn(List<Long> ids);
}