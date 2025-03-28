package com.qbit.microservice.dto;

import com.qbit.microservice.entity.Post;
import com.qbit.microservice.entity.PostComment;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    private LocalDateTime createAt = LocalDateTime.now();

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
                .build();
    }


}
