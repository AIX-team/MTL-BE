package com.example.mytravellink.auth.filter;

import com.example.mytravellink.auth.handler.JwtTokenProvider;
import com.example.mytravellink.auth.service.CustomUserDetails;
import com.example.mytravellink.user.domain.User;
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        List<String> roleLeessList = Arrays.asList(
                // 토큰 사용하지 않아도 기능 수행할 수 있게 설정 ( 로그인해서 사용하는 기능은 안 써도 됨)

                "/swagger-ui/(.*)",        //swagger 설정
                "/swagger-ui/index.html",  //swagger 설정
                "/v3/api-docs",              //swagger 설정
                "/v3/api-docs/(.*)",         //swagger 설정
                "/swagger-resources",        //swagger 설정
                "/swagger-resources/(.*)",    //swagger 설정
                "/auth/google/callback"
        );

        if(roleLeessList.stream().anyMatch(uri -> roleLeessList.stream().anyMatch(pattern -> Pattern.matches(pattern, request.getRequestURI())))){
            filterChain.doFilter(request,response);
            return;
        }

        // 헤더에서 토큰 꺼내기
        String token = jwtTokenProvider.resolveToken(request); // 요청에서 JWT 토큰 추출
        log.info("추출한 토큰: {}", token);

        // 토큰 유효성 검사
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Claims claims = jwtTokenProvider.getClaimsFromToken(token); // JWT에서 사용자 고유 넘버 추출

            User member = User.builder()
                    .email(claims.getSubject())
                    .name(claims.get("name").toString())
                    .build();

            // 토큰에 담겨있던 정보로 인증 객체를 만든다.
            CustomUserDetails userDetails = new CustomUserDetails();
            userDetails.setMember(member);

            // Authentication 객체 생성 및 SecurityContext에 설정
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication); // SecurityContext에 인증 정보 설정
        }

        filterChain.doFilter(request, response); // 다음 필터로 요청 전달
    }

}
