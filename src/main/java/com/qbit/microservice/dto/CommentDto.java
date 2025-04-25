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
public class CommentDto {
    private Long id;

    private Post post;

    private AccountDto user;

    private String content;

    private LocalDateTime createdAt;

    public static CommentDto fromEntity(PostComment comment, AccountDto user) {
        return CommentDto.builder()
                .id(comment.getId())
                .post(comment.getPost())
                .user(user)
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
