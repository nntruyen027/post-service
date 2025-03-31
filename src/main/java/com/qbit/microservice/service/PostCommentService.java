package com.qbit.microservice.service;

import com.qbit.microservice.client.UserServiceClient;
import com.qbit.microservice.dto.PostCommentDto;
import com.qbit.microservice.dto.UserDto;
import com.qbit.microservice.entity.PostComment;
import com.qbit.microservice.repository.PostCommentRepository;
import com.qbit.microservice.repository.PostRepository;
import com.qbit.microservice.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PostCommentService {
    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserServiceClient userServiceClient;

    @Autowired
    private JwtUtil jwtUtil;

    public Page<PostCommentDto> findAllByPost(Long id, Pageable pageable) {
        return postCommentRepository.findByPostId(id, pageable).map((e) -> {
            ResponseEntity<UserDto> response = userServiceClient.getUserById("Bearer " + jwtUtil.getJwtFromContext(), e.getUserId());
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null)
                throw new EntityNotFoundException("Failed to retrieve account information from auth service");
            return PostCommentDto.
                    fromEntity(e, response.getBody());
        });
    }

    public PostCommentDto findOne(Long id) {
        PostComment postComment = postCommentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Khong co comment"));

        ResponseEntity<UserDto> response = userServiceClient.getUserById("Bearer " + jwtUtil.getJwtFromContext(), postComment.getUserId());
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null)
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");

        return PostCommentDto.
                fromEntity(postComment, response.getBody());
    }

    public PostComment createOne(Long postId, PostComment postComment) {
        ResponseEntity<UserDto> response = userServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null)
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");

        postComment.setPost(postRepository.findById(postId).orElseThrow());
        postComment.setUserId(response.getBody().getId());
        return postCommentRepository.save(postComment);
    }

    public PostComment updateOne(Long id, PostComment postComment) {
        ResponseEntity<UserDto> response = userServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null)
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");

        PostComment updatedComment = postCommentRepository.findByIdAndUserId(id, response.getBody().getId())
                .orElseThrow(() -> new EntityNotFoundException("Khong co comment"));
        updatedComment.setContent(postComment.getContent());
        return postCommentRepository.save(updatedComment);
    }

    public void deleteOne(Long id) {
        ResponseEntity<UserDto> response = userServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null)
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        PostComment deletedComment = postCommentRepository.findByIdAndUserId(id, response.getBody().getId())
                .orElseThrow(() -> new EntityNotFoundException("Khong co comment"));
        postCommentRepository.delete(deletedComment);
    }

    public void deleteOneByAdmin(Long id) {
        postCommentRepository.deleteById(id);
    }
}
