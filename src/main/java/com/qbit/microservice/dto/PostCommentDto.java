package com.qbit.microservice.dto;

import com.qbit.microservice.entity.Post;
import com.qbit.microservice.entity.PostComment;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCommentDto {
    private Long id;

    private Post post;

    private UserDto user;

    private String content;

    private LocalDateTime createdAt;

    public static PostCommentDto fromEntity(PostComment entity, UserDto account) {
        return PostCommentDto.builder()
                .id(entity.getId())
                .post(entity.getPost())
                .user(account)
                .content(entity.getContent())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
