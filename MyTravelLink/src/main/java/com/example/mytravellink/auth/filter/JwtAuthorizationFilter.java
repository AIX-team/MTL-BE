package com.example.mytravellink.auth.filter;

import com.example.mytravellink.auth.handler.JwtTokenProvider;
import com.example.mytravellink.auth.service.CustomUserDetails;
import com.example.mytravellink.domain.users.entity.Users;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰을 생성하고 검증하는 클래스
    private final UserDetailsService userDetailsService; // 사용자 세부 정보를 로드하는 서비스

    public JwtAuthorizationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        // Swagger와 관련된 요청은 JWT 인증 필터 로직을 건너뛰도록 합니다.
        if (requestURI.startsWith("/swagger") ||
            requestURI.startsWith("/v3/api-docs") ||
            requestURI.startsWith("/swagger-ui") ||
            requestURI.startsWith("/webjars")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 헤더에서 토큰 추출
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            log.info("추출한 토큰: {}", token);
        } else {
            log.info("Authorization 헤더가 없거나 'Bearer ' 접두어가 없습니다.");
            log.info("추출한 토큰: null");
        }

        // 토큰 유효성 검사 및 인증 객체 생성
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // JWT 토큰에서 클레임(Claims) 추출
            Claims claims = jwtTokenProvider.getClaimsFromToken(token);

            // 토큰에 담긴 정보로 사용자 객체 생성 (예: 이메일, 이름)
            Users member = Users.builder()
                    .email(claims.getSubject())
                    .name(claims.get("name").toString())
                    .build();

            // CustomUserDetails에 사용자 정보 설정
            CustomUserDetails userDetails = new CustomUserDetails();
            userDetails.setMember(member);

            // Authentication 객체 생성 및 SecurityContext에 설정
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 이후 필터 체인 진행
        filterChain.doFilter(request, response);
    }

}
