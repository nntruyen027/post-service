package com.qbit.microservice.config;

import com.qbit.microservice.client.AuthServiceClient;
import com.qbit.microservice.dto.AccountDto;
import com.qbit.microservice.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthServiceClient authServiceClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                chain.doFilter(request, response); // Cho qua, xử lý tiếp trong SecurityConfig
                return;
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                ResponseEntity<AccountDto> responseEntity = authServiceClient.getUserByJwt("Bearer " + jwt);
                AccountDto accountDto = responseEntity.getBody();

                if (accountDto != null && jwtUtil.validateToken(jwt, accountDto.getUsername())) {
                    JwtAuthentication authenticationToken =
                            new JwtAuthentication(accountDto, null,
                                    accountDto.getRoles().stream()
                                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                                            .toList(),
                                    jwt);
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (Exception ex) {
            }
        }

        chain.doFilter(request, response);
    }

}
