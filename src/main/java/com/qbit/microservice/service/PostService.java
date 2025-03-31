package com.qbit.microservice.service;

import com.qbit.microservice.client.AuthServiceClient;
import com.qbit.microservice.client.FileServerClient;
import com.qbit.microservice.dto.AccountDto;
import com.qbit.microservice.dto.PostDto;
import com.qbit.microservice.entity.Post;
import com.qbit.microservice.entity.PostComment;
import com.qbit.microservice.repository.PostCommentRepository;
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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostFavoriteRepository postFavoriteRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthServiceClient authServiceClient;

    @Autowired
    private PostCommentRepository postCommentRepository;

    @Autowired
    private FileServerClient fileServerClient;

    public Page<PostDto> findAll(Pageable pageable) {
        ResponseEntity<AccountDto> response = authServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful())
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        Long userId = Objects.requireNonNull(response.getBody()).getId();

        Page<Post> posts = postRepository.findAll(pageable);


        Set<Long> likedPostIds = postFavoriteRepository.findByUserId(userId)
                .stream()
                .map(fav -> fav.getPost().getId())
                .collect(Collectors.toSet());

        return posts.map(post -> {
            boolean isLiked = likedPostIds.contains(post.getId());
            return PostDto.fromEntity(post, isLiked);
        });
    }

    boolean checkLiked(Post post) {
        ResponseEntity<AccountDto> response = authServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful())
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        Long userId = Objects.requireNonNull(response.getBody()).getId();
        return postFavoriteRepository.existsByUserIdAndPostId(userId, post.getId());
    }

    public PostDto findOne(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không có bài viết"));
        List<PostComment> comments = postCommentRepository.findByPostId(post.getId());

        return PostDto.fromEntity(post, checkLiked(post), comments);
    }

    public PostDto createOne(Post post) {
        ResponseEntity<AccountDto> response = authServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful())
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        Long userId = Objects.requireNonNull(response.getBody()).getId();

        post.setUserId(userId);

        return PostDto.fromEntity(postRepository.save(post), false);
    }


    public void addFavouritePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không có bài viết"));
        post.setLikeCount(post.getLikeCount() + 1);
        postRepository.save(post);
    }

    public void removeFavouritePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không có bài viết"));
        post.setLikeCount(post.getLikeCount() - 1);
        postRepository.save(post);
    }

    public PostDto updateOne(Long id, Post post) {
        ResponseEntity<AccountDto> response = authServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful())
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        Long userId = Objects.requireNonNull(response.getBody()).getId();
        Post updatedPost = postRepository.findByUserIdAndId(userId, id).orElseThrow(() -> new EntityNotFoundException("Không có bài viết"));
        updatedPost.setImage(post.getImage());
        updatedPost.setTitle(post.getTitle());
        updatedPost.setContent(post.getContent());
        return PostDto.fromEntity(postRepository.save(updatedPost), checkLiked(post));
    }

    public void deleteOne(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Không có bài viết"));
        if (post.getImage() != null) {
            String[] parts = post.getImage().split("/");
            fileServerClient.deleteFile(parts[parts.length - 1]);
        }

        postRepository.deleteById(id);
    }
}
