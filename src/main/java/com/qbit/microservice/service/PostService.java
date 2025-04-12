package com.qbit.microservice.service;

import com.qbit.microservice.client.AuthServiceClient;
import com.qbit.microservice.client.FileServerClient;
import com.qbit.microservice.dto.AccountDto;
import com.qbit.microservice.dto.PostDto;
import com.qbit.microservice.entity.Post;
import com.qbit.microservice.entity.PostComment;
import com.qbit.microservice.entity.Tag;
import com.qbit.microservice.repository.PostCommentRepository;
import com.qbit.microservice.repository.PostFavoriteRepository;
import com.qbit.microservice.repository.PostRepository;
import com.qbit.microservice.repository.TagRepository;
import com.qbit.microservice.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    @Autowired
    private TagRepository tagRepository;

    public Page<PostDto> findAllByPublic(String keyword, Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<Post> posts = getPostsByKeyword(keyword, pageable, true);
        Set<Long> likedPostIds = getLikedPostIds(userId);
        return posts.map(post -> PostDto.fromEntity(post, likedPostIds.contains(post.getId())));
    }

    public Page<PostDto> findAll(String keyword, Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<Post> posts = getPostsByKeyword(keyword, pageable, false);
        Set<Long> likedPostIds = getLikedPostIds(userId);
        return posts.map(post -> PostDto.fromEntity(post, likedPostIds.contains(post.getId())));
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
        List<PostComment> comments = postCommentRepository.findByPostId(post.getId());
        return PostDto.fromEntity(post, checkLiked(post), comments);
    }

    public PostDto createOne(Post post) {
        Long userId = getCurrentUserId();
        post.setUserId(userId);
        post.setViewCount(0);
        post.setLikeCount(0);
        Set<String> hashtags = extractHashtags(post.getContent());
        post.setHashtags(hashtags);
        return PostDto.fromEntity(postRepository.save(post), false);
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
        return PostDto.fromEntity(postRepository.save(updatedPost), checkLiked(post));
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

    private Long getCurrentUserId() {
        ResponseEntity<AccountDto> response = authServiceClient.getUserByJwt("Bearer " + jwtUtil.getJwtFromContext());
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new EntityNotFoundException("Failed to retrieve account information from auth service");
        }
        return response.getBody().getId();
    }

    private Set<Long> getLikedPostIds(Long userId) {
        return postFavoriteRepository.findByUserId(userId)
                .stream()
                .map(fav -> fav.getPost().getId())
                .collect(Collectors.toSet());
    }

    private boolean checkLiked(Post post) {
        Long userId = getCurrentUserId();
        return postFavoriteRepository.existsByUserIdAndPostId(userId, post.getId());
    }

    @Transactional
    public PostDto assignTagsToPost(Long postId, List<Long> tagIds) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy bài viết"));

        List<Tag> tags = tagRepository.findAllById(tagIds);

        if (tags.isEmpty()) {
            throw new EntityNotFoundException("Không tìm thấy thẻ nào từ danh sách ID đã cho");
        }

        post.setTags(tags);
        post.setUpdatedAt(LocalDateTime.now());

        postRepository.save(post);

        return PostDto.fromEntity(post, checkLiked(post));
    }

    public Set<String> extractHashtags(String content) {
        Set<String> hashtags = new HashSet<>();
        if (content != null) {
            // Biểu thức chính quy để tìm hashtag (chuỗi bắt đầu bằng #)
            Pattern pattern = Pattern.compile("#\\w+");
            Matcher matcher = pattern.matcher(content);

            while (matcher.find()) {
                hashtags.add(matcher.group().toLowerCase());  // Chuyển thành chữ thường để đồng nhất
            }
        }
        return hashtags;
    }
}
