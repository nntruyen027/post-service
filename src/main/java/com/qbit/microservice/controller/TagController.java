package com.qbit.microservice.controller;

import com.qbit.microservice.entity.Tag;
import com.qbit.microservice.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    @PreAuthorize("hasRole('admin')")
    @PostMapping
    public ResponseEntity<Tag> create(@Valid @RequestBody Tag tag) {
        return ResponseEntity.ok(tagService.create(tag));
    }

    @PreAuthorize("hasRole('admin')")
    @PutMapping("/{id}")
    public ResponseEntity<Tag> update(@PathVariable Long id, @Valid @RequestBody Tag tag) {
        return ResponseEntity.ok(tagService.update(id, tag));
    }

    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tag> findById(@PathVariable Long id) {
        return ResponseEntity.ok(tagService.findById(id));
    }

    @GetMapping
    public ResponseEntity<Page<Tag>> findAll(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        return ResponseEntity.ok(tagService.findAll(keyword, pageable));
    }

}
