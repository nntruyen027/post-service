package com.qbit.microservice.service;

import com.qbit.microservice.client.AuthServiceClient;
import com.qbit.microservice.client.FileServerClient;
import com.qbit.microservice.dto.AccountDto;
import com.qbit.microservice.dto.PostCommentDto;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    public Page<PostDto> findAllByPublic(String keyword, Pageable pageable) {
        AccountDto currentUser = getCurrentUser();
        Set<Long> likedPostIds;

        if (currentUser != null) {
            Long userId = currentUser.getId();
            likedPostIds = getLikedPostIds(userId);
        } else {
            likedPostIds = Collections.emptySet();
        }

        Page<Post> posts = getPostsByKeyword(keyword, pageable, true);
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();
        List<AccountDto> authors = authServiceClient.getUsersByIds(userIds).getBody();
        assert authors != null;
        Map<Long, AccountDto> authorMaps = authors.stream()
                .collect(Collectors.toMap(AccountDto::getId, a -> a));

        return posts.map(post ->
                PostDto.fromEntity(post, authorMaps.get(post.getUserId()), likedPostIds.contains(post.getId()))
        );
    }


    public Page<PostDto> findAll(String keyword, Pageable pageable) {
        AccountDto currentUser = getCurrentUser();
        Set<Long> likedPostIds;

        if (currentUser != null) {
            Long userId = currentUser.getId();
            likedPostIds = getLikedPostIds(userId);
        } else {
            likedPostIds = Collections.emptySet();
        }
        Page<Post> posts = getPostsByKeyword(keyword, pageable, false);
        List<Long> userIds = posts.stream().map(Post::getUserId).distinct().toList();
        List<AccountDto> authors = authServiceClient.getUsersByIds(userIds).getBody();
        assert authors != null;
        Map<Long, AccountDto> authorMaps = authors.stream().collect(Collectors.toMap(AccountDto::getId, a -> a));
        return posts.map(post -> {
            assert likedPostIds != null;
            return PostDto.fromEntity(post, authorMaps.get(post.getUserId()), likedPostIds.contains(post.getId()));
        });
    }

    private Page<Post> getPostsByKeyword(String keyword, Pageable pageable, boolean onlyPublic) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return onlyPublic ? postRepository.findByIsPublicTrue(pageable) : postRepository.findAll(pageable);
        } else {
            return onlyPublic ? postRepository.searchPublicPostsByKeyword(keyword, pageable)
                    : postRepository.findAllWithOptionalKeyword(keyword, pageable);
        }
    }

    public PostDto findOne(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không có bài viết"));
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        AccountDto author = authServiceClient.getUserById(post.getUserId()).getBody();
        List<PostComment> comments = postCommentRepository.findByPostId(post.getId());

        ResponseEntity<List<AccountDto>> response = authServiceClient.getUsersByIds(comments.stream().map(PostComment::getUserId).toList());
        Map<Long, AccountDto> userMap = Objects.requireNonNull(response.getBody()).stream()
                .collect(Collectors.toMap(AccountDto::getId, Function.identity()));

        List<PostCommentDto> commentDtos = comments.stream()
                .map(comment -> PostCommentDto.fromEntity(comment, userMap.get(comment.getUserId())))
                .toList();

        return PostDto.fromEntity(post, author, checkLiked(post), commentDtos);
    }

    public PostDto createOne(Post post) {
        AccountDto user = getCurrentUser();
        assert user != null;
        post.setUserId(user.getId());
        post.setViewCount(0);
        post.setLikeCount(0);
        Set<String> hashtags = extractHashtags(post.getContent());
        post.setHashtags(hashtags);
        return PostDto.fromEntity(postRepository.save(post), user, false);
    }

    public void addFavouritePost(Long id) {
        updateLikeCount(id, 1);
    }

    public void removeFavouritePost(Long id) {
        updateLikeCount(id, -1);
    }

    private void updateLikeCount(Long id, int delta) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không có bài viết"));
        post.setLikeCount(Math.max(0, post.getLikeCount() + delta));
        postRepository.save(post);
    }

    public PostDto updateOne(Long id, Post post) {
        Post updatedPost = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không có bài viết"));
        updatedPost.setImage(post.getImage());
        updatedPost.setTitle(post.getTitle());
        updatedPost.setContent(post.getContent());
        updatedPost.setIsPublic(post.getIsPublic());
        updatedPost.setDescription(post.getDescription());
        Set<String> hashtags = extractHashtags(post.getContent());
        updatedPost.setHashtags(hashtags);
        updatedPost.setUpdatedAt(LocalDateTime.now());
        AccountDto author = authServiceClient.getUserById(post.getUserId()).getBody();
        return PostDto.fromEntity(postRepository.save(updatedPost), author, checkLiked(post));
    }

    public void deleteOne(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không có bài viết"));
        if (post.getImage() != null) {
            String[] parts = post.getImage().split("/");
            fileServerClient.deleteFile(parts[parts.length - 1]);
        }
        postRepository.deleteById(id);
    }

    private AccountDto getCurrentUser() {
        if (jwtUtil.getJwtFromContext() == null)
            return null;
        ResponseEntity<AccountDto> response = authServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        }
        return response.getBody();
    }

    private Set<Long> getLikedPostIds(Long userId) {
        if (userId == null)
            return null;
        return postFavoriteRepository.findByUserId(userId)
                .stream()
                .map(fav -> fav.getPost().getId())
                .collect(Collectors.toSet());
    }

    private boolean checkLiked(Post post) {
        AccountDto currentUser = getCurrentUser();
        if (currentUser == null) return false;
        return postFavoriteRepository.existsByUserIdAndPostId(currentUser.getId(), post.getId());
    }


    public Set<String> extractHashtags(String content) {
        Set<String> hashtags = new HashSet<>();
        if (content != null) {
            Pattern pattern = Pattern.compile("#\\w+");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                hashtags.add(matcher.group().toLowerCase());
            }
        }
        return hashtags;
    }
}
