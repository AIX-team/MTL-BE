package com.example.mytravellink.config;

import com.example.mytravellink.auth.filter.JwtAuthorizationFilter;
import com.example.mytravellink.auth.handler.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> {
                    // API 엔드포인트
                    auth.requestMatchers("/api/public/**").permitAll();
                    auth.requestMatchers("/api/v1/auth/**").permitAll();
                    
                    // 인증/인가 관련 경로
                    auth.requestMatchers("/login/**", "/auth/**", "/loginSuccess").permitAll();
                    
                    // Swagger UI
                    auth.requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**", 
                                      "/swagger-resources/**").permitAll();
                    
                    // 정적 리소스 및 SPA 라우팅
                    auth.requestMatchers("/", "/index.html", "/static/**", 
                                      "/*.js", "/*.css", "/*.ico", "/*.json", 
                                      "/images/**", "/assets/**").permitAll();
                    
                    auth.anyRequest().permitAll();
                })
                .addFilterBefore(new JwtAuthorizationFilter(jwtTokenProvider, userDetailsService), 
                               UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("https://mytravellink.site","http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Arrays.asList("Authorization","Content-Type"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",configuration);
        return source;
    }
} 