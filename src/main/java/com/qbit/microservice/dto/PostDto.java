package com.qbit.microservice.dto;

import com.qbit.microservice.entity.Post;
import com.qbit.microservice.entity.PostComment;
import com.qbit.microservice.entity.Tag;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PostDto {
    private Long id;

    private Long userId;

    private String title;

    private String image;

    private String content;

    private int likeCount = 0;

    private List<PostComment> comments;

    private boolean isLiked = false;
    private int viewCount;

    private LocalDateTime createAt = LocalDateTime.now();
    private String description;

    private Boolean isPublic;

    private Set<String> hashtags;

    private List<Tag> tags;

    public static PostDto fromEntity(Post entity, boolean isLiked, List<PostComment> comments) {
        return PostDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .image(entity.getImage())
                .content(entity.getContent())
                .isLiked(isLiked)
                .likeCount(entity.getLikeCount())
                .comments(comments)
                .viewCount(entity.getViewCount())
                .description(entity.getDescription())
                .isPublic(entity.getIsPublic())
                .hashtags(entity.getHashtags())
                .tags(entity.getTags())
                .build();
    }

    public static PostDto fromEntity(Post entity, boolean isLiked) {
        return PostDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .image(entity.getImage())
                .content(entity.getContent())
                .likeCount(entity.getLikeCount())
                .isLiked(isLiked)
                .viewCount(entity.getViewCount())
                .isPublic(entity.getIsPublic())
                .description(entity.getDescription())
                .hashtags(entity.getHashtags())
                .tags(entity.getTags())
                .build();
    }


}
