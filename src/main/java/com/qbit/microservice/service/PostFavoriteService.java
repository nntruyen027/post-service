package com.qbit.microservice.service;

import com.qbit.microservice.client.UserServiceClient;
import com.qbit.microservice.dto.PostFavoriteDto;
import com.qbit.microservice.dto.UserDto;
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
import java.util.Optional;

@Service
public class PostFavoriteService {
    @Autowired
    private PostFavoriteRepository postFavoriteRepository;

    @Autowired
    private JwtUtil jwtUtil;


    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserServiceClient userServiceClient;


    public Page<PostFavorite> findByUserId(Long id, Pageable pageable) {
        return postFavoriteRepository.findByUserId(id, pageable);
    }

    public Page<PostFavorite> findBySelf(Pageable pageable) {
        ResponseEntity<UserDto> response = userServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful())
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        Long userId = Objects.requireNonNull(response.getBody()).getId();
        return findByUserId(userId, pageable);
    }

    public Page<PostFavoriteDto> findByPostId(Long id, Pageable pageable) {
        return postFavoriteRepository.findByPostId(id, pageable).map((e) -> {
            ResponseEntity<UserDto> response = userServiceClient.getUserById("Bearer " + jwtUtil.getJwtFromContext(), e.getUserId());
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null)
                throw new EntityNotFoundException("Failed to retrieve account information from auth service");

            return PostFavoriteDto.
                    fromEntity(postFavoriteRepository.findById(e.getId()).orElseThrow(EntityNotFoundException::new), response.getBody());
        });
    }

    public PostFavorite likePost(Long postId) {
        ResponseEntity<UserDto> response = userServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful())
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        Long userId = Objects.requireNonNull(response.getBody()).getId();

        Optional<PostFavorite> present = postFavoriteRepository.findByUserIdAndPostId(userId, postId);
        if (present.isPresent())
            throw new EntityNotFoundException("Đã thích");

        PostFavorite postFavorite = new PostFavorite();
        postFavorite.setPost(postRepository.findById(postId).orElseThrow(EntityNotFoundException::new));
        postFavorite.setUserId(userId);

        postService.addFavouritePost(postId);
        return postFavoriteRepository.save(postFavorite);
    }

    public void unLikePost(Long postId) {
        ResponseEntity<UserDto> response = userServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful())
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        Long userId = Objects.requireNonNull(response.getBody()).getId();

        PostFavorite postFavorite = postFavoriteRepository.findByUserIdAndPostId(userId, postId).orElseThrow();

        postService.removeFavouritePost(postId);
        postFavoriteRepository.delete(postFavorite);
    }
}
