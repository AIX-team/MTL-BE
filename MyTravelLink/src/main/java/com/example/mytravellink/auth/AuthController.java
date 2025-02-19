package com.example.mytravellink.auth;

import com.example.mytravellink.auth.handler.JwtTokenProvider;
import com.example.mytravellink.common.ResponseMessage;
import com.example.mytravellink.domain.users.entity.Users;
import com.example.mytravellink.domain.users.repository.UsersRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final UsersRepository memberRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;
    
    @Value("${url.google.access-token}")
    private String accessTokenUrl;
    
    @Value("${url.google.profile}")
    private String profileUrl; // profileUrl은 "https://www.googleapis.com/oauth2/v3/userinfo" 이어야 합니다.

    @GetMapping("/auth/google/callback")
    public void googleCallback(@RequestParam("code") String code, HttpServletResponse response) throws IOException {
        // 1. 구글에 access token 요청
        String tokenUrl = accessTokenUrl;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // 요청 본문을 MultiValueMap으로 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<String> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);
        log.info("Token Response: {}", tokenResponse.getBody());

        // 2. 액세스 토큰 추출
        String accessToken = extractAccessToken(tokenResponse.getBody());
        if (accessToken == null) {
            response.sendRedirect("https://mytravellink.site/loginError");
            return;
        }

        // 3. 사용자 정보 요청 (access token을 HTTP Header에 담아 전송)
        String userInfoUrl = profileUrl; // 예: "https://www.googleapis.com/oauth2/v3/userinfo"
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.set("Authorization", "Bearer " + accessToken);
        HttpEntity<?> userInfoEntity = new HttpEntity<>(userInfoHeaders);

        ResponseEntity<String> userInfoResponse = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                userInfoEntity,
                String.class
        );
        log.info("UserInfo Response: {}", userInfoResponse.getBody());

        // 4. 사용자 정보 처리 및 회원가입 로직
        String userInfo = userInfoResponse.getBody();
        Users member = processUserInfo(userInfo);
        log.info("Processed user: {}", member);

        // 5. 백엔드 서버 access token 생성 후 프론트에 전달
        String backendAccessToken = jwtTokenProvider.generateToken(member);
        
        // 6. FE로 redirect (예: /loginSuccess?token=xxx)
        String encodedToken = URLEncoder.encode(backendAccessToken, "UTF-8");
        String redirectUrl = "https://back.mytravellink.site/loginSuccess?token=" + encodedToken;
        response.sendRedirect(redirectUrl);
    } 

    // JSON 파싱을 통해 access token 추출
    private String extractAccessToken(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            log.error("Access token extraction error", e);
            return null;
        }
    }

    // 사용자 정보를 처리하여 기존 사용자가 없으면 저장 후 반환
    private Users processUserInfo(String userInfo) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(userInfo);

            String name = jsonNode.get("name").asText();
            String email = jsonNode.get("email").asText();

            Optional<Users> optionalUser = memberRepository.findByEmail(email);
            Users user;
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            } else {
                user = Users.builder()
                        .email(email)
                        .name(name)
                        .build();
                memberRepository.save(user);
            }
            log.info("User info: {}", user);
            return user;
        } catch (Exception e) {
            log.error("Error processing user info", e);
            return null;
        }
    }
}