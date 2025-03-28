package com.qbit.microservice.service;

import com.qbit.microservice.client.AuthServiceClient;
import com.qbit.microservice.dto.AccountDto;
import com.qbit.microservice.entity.PostFavorite;
import com.qbit.microservice.repository.PostFavoriteRepository;
import com.qbit.microservice.repository.PostRepository;
import com.qbit.microservice.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PostFavoriteService {
    @Autowired
    private PostFavoriteRepository postFavoriteRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    public Page<PostFavorite> findByUserId(Long id, Pageable pageable) {
        return postFavoriteRepository.findByUserId(id, pageable);
    }

    public Page<PostFavorite> findBySelf(Pageable pageable) {
        ResponseEntity<AccountDto> response = authServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful())
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        Long userId = Objects.requireNonNull(response.getBody()).getId();
        return findByUserId(userId, pageable);
    }

    public Page<PostFavorite> findByPostId(Long id, Pageable pageable) {
        return postFavoriteRepository.findByPostId(id, pageable);
    }

    public PostFavorite likePost(Long postId) {
        ResponseEntity<AccountDto> response = authServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful())
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        Long userId = Objects.requireNonNull(response.getBody()).getId();

        postFavoriteRepository.findByUserIdAndPostId(userId, postId).orElseThrow();

        PostFavorite postFavorite = PostFavorite.builder()
                .post(postRepository.findById(postId).orElseThrow(EntityNotFoundException::new))
                .id(userId)
                .build();

        postService.addFavouritePost(postId);
        return postFavoriteRepository.save(postFavorite);
    }

    public void unLikePost(Long postId) {
        ResponseEntity<AccountDto> response = authServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful())
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        Long userId = Objects.requireNonNull(response.getBody()).getId();

        PostFavorite postFavorite = postFavoriteRepository.findByUserIdAndPostId(userId, postId).orElseThrow();

        postService.removeFavouritePost(postId);
        postFavoriteRepository.delete(postFavorite);
    }
}
