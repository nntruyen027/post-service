package com.qbit.microservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
public class FeignClientConfig {
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.getCredentials() instanceof String) {
                String jwt = (String) authentication.getCredentials();
                requestTemplate.header("Authorization", "Bearer " + jwt); // Thêm header Authorization
                System.out.println("JWT sent to Auth Service: " + jwt);
            }
        };
    }
}