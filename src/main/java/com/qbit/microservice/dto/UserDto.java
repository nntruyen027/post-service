package com.qbit.microservice.dto;


import lombok.*;

@Builder
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;

    private String fullName;

    private Boolean isMale;

    private String phone;

    private String email;

    private String avatar;

    private AccountDto account;
}
