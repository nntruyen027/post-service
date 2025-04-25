package com.qbit.microservice.client;

import com.qbit.microservice.config.FeignClientConfig;
import com.qbit.microservice.dto.AccountDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "auth-service", url = "${auth.service.url}", configuration = FeignClientConfig.class)
public interface AuthServiceClient {
    @GetMapping(value = "/self")
    ResponseEntity<AccountDto> getUserByJwt(@RequestHeader("Authorization") String authorizationHeader);

    @PostMapping(value = "/public/users")
    ResponseEntity<List<AccountDto>> getUsersByIds(@RequestBody List<Long> ids);

    @GetMapping(value = "/public/{id}")
    ResponseEntity<AccountDto> getUserById(@PathVariable Long id);
}