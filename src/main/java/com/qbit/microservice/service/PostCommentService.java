package com.qbit.microservice.service;

import com.qbit.microservice.entity.PostComment;
import com.qbit.microservice.repository.PostCommentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PostCommentService {
    @Autowired
    private PostCommentRepository postCommentRepository;

    public Page<PostComment> findAllByPost(Long id, Pageable pageable) {
        return postCommentRepository.findByPostId(id, pageable);
    }

    public PostComment findOne(Long id) {
        return postCommentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Khong co comment"));
    }

    public PostComment createOne(PostComment postComment) {
        return postCommentRepository.save(postComment);
    }

    public PostComment updateOne(Long id, PostComment postComment) {
        PostComment updatedComment = postCommentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Khong co comment"));
        updatedComment.setContent(postComment.getContent());
        return postCommentRepository.save(updatedComment);
    }

    public void deleteOne(Long id) {
        postCommentRepository.deleteById(id);
    }
}
