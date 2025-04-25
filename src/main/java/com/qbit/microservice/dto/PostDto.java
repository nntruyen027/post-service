package com.qbit.microservice.dto;

import com.qbit.microservice.entity.Post;
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

    private AccountDto author;

    private String title;

    private String image;

    private String content;

    private int likeCount = 0;

    private List<PostCommentDto> comments;

    private boolean isLiked = false;
    private int viewCount;

    private String description;

    private Boolean isPublic;

    private Set<String> hashtags;


    private LocalDateTime createAt;

    public static PostDto fromEntity(Post entity, AccountDto author, boolean isLiked, List<PostCommentDto> comments) {
        return PostDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .image(entity.getImage())
                .content(entity.getContent())
                .isLiked(isLiked)
                .author(author)
                .likeCount(entity.getLikeCount())
                .comments(comments)
                .viewCount(entity.getViewCount())
                .description(entity.getDescription())
                .isPublic(entity.getIsPublic())
                .hashtags(entity.getHashtags())
                .createAt(entity.getCreateAt())
                .build();
    }


    public static PostDto fromEntity(Post entity, AccountDto author, boolean isLiked) {
        return PostDto.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .title(entity.getTitle())
                .image(entity.getImage())
                .author(author)
                .content(entity.getContent())
                .likeCount(entity.getLikeCount())
                .isLiked(isLiked)
                .viewCount(entity.getViewCount())
                .isPublic(entity.getIsPublic())
                .description(entity.getDescription())
                .hashtags(entity.getHashtags())
                .createAt(entity.getCreateAt())
                .build();
    }


}
