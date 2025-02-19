package com.example.mytravellink.auth.filter;

import com.example.mytravellink.auth.handler.JwtTokenProvider;
import com.example.mytravellink.auth.service.CustomUserDetails;
import com.example.mytravellink.domain.users.entity.Users;
import com.example.mytravellink.domain.users.repository.UsersRepository;

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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰을 생성하고 검증하는 클래스
    private final UserDetailsService userDetailsService; // 사용자 세부 정보를 로드하는 서비스
    private final UsersRepository userRepository; // 사용자 정보를 조회하는 리포지토리

    public JwtAuthorizationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService, UsersRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        //TODO: 02_03 프로젝트 테스트를 위해 JWT 검증 로직 전체를 주석 처리하고 바로 다음 필터로 진행하도록 수정

        List<String> roleLeessList = Arrays.asList(
                // 토큰 사용하지 않아도 기능 수행할 수 있게 설정 ( 로그인해서 사용하는 기능은 안 써도 됨)

                "/swagger-ui/(.*)",        //swagger 설정
                "/swagger-ui/index.html",  //swagger 설정
                "/v3/api-docs",              //swagger 설정
                "/v3/api-docs/(.*)",         //swagger 설정
                "/swagger-resources",        //swagger 설정
                "/swagger-resources/(.*)",    //swagger 설정
                "/auth/google/callback",
                "/api/v1/travels/guide"
        );

        if(roleLeessList.stream().anyMatch(uri -> roleLeessList.stream().anyMatch(pattern -> Pattern.matches(pattern, request.getRequestURI())))){
            filterChain.doFilter(request,response);
            return;
        }

        String token = request.getHeader("Authorization");
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.replace("Bearer ", "");
        }
        
        log.info("추출한 토큰: {}", token);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmailFromToken(token);
            Users member = userRepository.findByEmail(email).orElse(null);
            
            if (member != null) {
                CustomUserDetails userDetails = new CustomUserDetails();
                userDetails.setMember(member);
                
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}
