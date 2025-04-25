package com.qbit.microservice.dto;

import com.qbit.microservice.entity.Post;
import com.qbit.microservice.entity.PostFavorite;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostFavoriteDto {
    private Long id;

    private Post post;

    private AccountDto user;

    private String content;

    private LocalDateTime createdAt;

    public static PostFavoriteDto fromEntity(PostFavorite entity, AccountDto account) {
        return PostFavoriteDto.builder()
                .id(entity.getId())
                .post(entity.getPost())
                .user(account)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
