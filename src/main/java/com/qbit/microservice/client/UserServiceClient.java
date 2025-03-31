package com.qbit.microservice.client;

import com.qbit.microservice.config.FeignClientConfig;
import com.qbit.microservice.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${user.service.url}", configuration = FeignClientConfig.class)
public interface UserServiceClient {
    @GetMapping(value = "/self")
    ResponseEntity<UserDto> getUserByJwt(@RequestHeader("Authorization") String authorizationHeader);

    @GetMapping(value = "/{id}")
    ResponseEntity<UserDto> getUserById(@RequestHeader("Authorization") String authorizationHeader, @PathVariable Long id);
}