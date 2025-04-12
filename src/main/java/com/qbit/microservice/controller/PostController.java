package com.qbit.microservice.controller;

import com.qbit.microservice.dto.PostDto;
import com.qbit.microservice.entity.Post;
import com.qbit.microservice.entity.PostComment;
import com.qbit.microservice.service.PostCommentService;
import com.qbit.microservice.service.PostFavoriteService;
import com.qbit.microservice.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/posts")
public class PostController {
    @Autowired
    private PostService postService;

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private PostFavoriteService postFavoriteService;

    @GetMapping()
    public ResponseEntity<?> findAll(Pageable pageable, @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(postService.findAllByPublic(keyword, pageable));
    }

    @PreAuthorize("hasRole('admin')")
    @GetMapping("/admin")
    public ResponseEntity<?> findAllByAdmin(Pageable pageable, @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(postService.findAll(keyword, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findOne(@PathVariable Long id) {
        return ResponseEntity.ok(postService.findOne(id));
    }

    @PostMapping()
    public ResponseEntity<?> createOne(@RequestBody Post post) {
        return ResponseEntity.ok(postService.createOne(post));
    }

    @PreAuthorize("hasRole('admin')")
    @PostMapping("/admin/{postId}/tags")
    public PostDto assignTags(@PathVariable Long postId, @RequestBody List<Long> tagIds) {
        return postService.assignTagsToPost(postId, tagIds);
    }

    @PreAuthorize("hasRole('admin')")
    @PutMapping("/admin/{id}")
    public ResponseEntity<?> updateOne(@PathVariable Long id, @RequestBody Post post) {
        return ResponseEntity.ok(postService.updateOne(id, post));
    }

    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteOne(@PathVariable Long id) {
        postService.deleteOne(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getCommentsByPost(@PathVariable Long postId, Pageable pageable) {
        return ResponseEntity.ok(postCommentService.findAllByPost(postId, pageable));
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<?> createComment(@PathVariable Long postId, @RequestBody PostComment postComment) {
        return ResponseEntity.ok(postCommentService.createOne(postId, postComment));
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long commentId, @RequestBody PostComment postComment) {
        return ResponseEntity.ok(postCommentService.updateOne(commentId, postComment));
    }

    @DeleteMapping("/comments/admin/{commentId}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<?> deleteCommentByAdmin(@PathVariable Long commentId) {
        postCommentService.deleteOneByAdmin(commentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        postCommentService.deleteOne(commentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{postId}/favorites")
    public ResponseEntity<?> getFavoritesByPost(@PathVariable Long postId, Pageable pageable) {
        return ResponseEntity.ok(postFavoriteService.findByPostId(postId, pageable));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<?> likePost(@PathVariable Long postId) {
        return ResponseEntity.ok(postFavoriteService.likePost(postId));
    }

    @DeleteMapping("/{postId}/unlike")
    public ResponseEntity<?> unlikePost(@PathVariable Long postId) {
        postFavoriteService.unLikePost(postId);
        return ResponseEntity.noContent().build();
    }
}
