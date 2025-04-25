package com.qbit.microservice.service;

import com.qbit.microservice.client.AuthServiceClient;
import com.qbit.microservice.dto.AccountDto;
import com.qbit.microservice.dto.PostDto;
import com.qbit.microservice.dto.PostFavoriteDto;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private AuthServiceClient authServiceClient;


    public Page<PostDto> findByUserId(Long id, Pageable pageable) {
        Page<PostFavorite> posts = postFavoriteRepository.findByUserId(id, pageable);

        List<Long> userIds = posts.stream().map(PostFavorite::getUserId).distinct().toList();
        List<AccountDto> authors = authServiceClient.getUsersByIds(userIds).getBody();
        assert authors != null;
        Map<Long, AccountDto> authorMaps = authors.stream()
                .collect(Collectors.toMap(AccountDto::getId, a -> a));

        return posts.map(postFavorite -> PostDto.fromEntity(postFavorite.getPost(),
                authorMaps.get(postFavorite.getUserId()), true));
    }

    public Page<PostDto> findBySelf(Pageable pageable) {
        ResponseEntity<AccountDto> response = authServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful())
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        Long userId = Objects.requireNonNull(response.getBody()).getId();
        return findByUserId(userId, pageable);
    }

    public Page<PostFavoriteDto> findByPostId(Long id, Pageable pageable) {
        return postFavoriteRepository.findByPostId(id, pageable).map((e) -> {
            ResponseEntity<AccountDto> response = authServiceClient.getUserById(e.getUserId());
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null)
                throw new EntityNotFoundException("Failed to retrieve account information from auth service");

            return PostFavoriteDto.
                    fromEntity(postFavoriteRepository.findById(e.getId()).orElseThrow(EntityNotFoundException::new), response.getBody());
        });
    }

    public PostFavorite likePost(Long postId) {
        ResponseEntity<AccountDto> response = authServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
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
        ResponseEntity<AccountDto> response = authServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful())
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        Long userId = Objects.requireNonNull(response.getBody()).getId();

        PostFavorite postFavorite = postFavoriteRepository.findByUserIdAndPostId(userId, postId).orElseThrow();

        postService.removeFavouritePost(postId);
        postFavoriteRepository.delete(postFavorite);
    }
}
