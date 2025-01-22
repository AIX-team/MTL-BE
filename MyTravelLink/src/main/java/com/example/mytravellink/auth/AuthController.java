package com.example.mytravellink.auth;

import com.example.mytravellink.auth.handler.JwtTokenProvider;
import com.example.mytravellink.common.ResponseMessage;
import com.example.mytravellink.member.domain.entity.Member;
import com.example.mytravellink.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final MemberRepository memberRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;
    @Value("${url.google.access-token}")
    private String accessTokenUrl;
    @Value("${url.google.profile}")
    private String profileUrl;

    @GetMapping("/auth/google/callback")
    public ResponseEntity<?> googleCallback(@RequestParam("code") String code) {
        // 1. 구글에 access token 요청
        String tokenUrl = accessTokenUrl;
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = String.format("code=%s&client_id=%s&client_secret=%s&redirect_uri=%s&grant_type=authorization_code",
                code, clientId, clientSecret, redirectUri);

        HttpEntity<String> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);

        // 2. 액세스 토큰 반환
        String accessToken = extractAccessToken(response.getBody());

        // 3. 사용자 정보 요청
        String userInfoUrl = profileUrl + accessToken;
        ResponseEntity<String> userInfoResponse = restTemplate.getForEntity(userInfoUrl, String.class);

        // 4. 사용자 정보 처리 및 회원가입 로직
        String userInfo = userInfoResponse.getBody();
        Member member = processUserInfo(userInfo);

        // 5. 백엔드 서버 access token 생성하여 프론트 서버로 전달
        String backendAccessToken = jwtTokenProvider.generateToken(member); // 사용자 정보를 기반으로 JWT 생성

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("token", backendAccessToken);
        responseMap.put("user", member);

        log.info("backendAccessToken : {}", backendAccessToken);

        return ResponseEntity
                .ok()
                .body(new ResponseMessage(HttpStatus.CREATED, "로그인 성공", responseMap)); // 백엔드 액세스 토큰 반환
    }

    private String extractAccessToken(String responseBody) {
        // JSON 파싱을 통해 access token 추출
        try {
            // Jackson ObjectMapper를 사용하여 JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            // access_token을 추출
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 오류 발생 시 null 반환
        }
    }

    private Member processUserInfo(String userInfo) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(userInfo);

            String googleId = jsonNode.get("sub").asText(); // 구글 ID
            String name = jsonNode.get("name").asText(); // 사용자 이름
            String email = jsonNode.get("email").asText(); // 이메일

            Member user = memberRepository.findByGoogleId(googleId);
            if (user == null) {
                // 사용자 정보가 없으면 새로운 사용자 생성
                user = new Member();
                user.setGoogleId(googleId);
                user.setName(name);
                user.setEmail(email);
                memberRepository.save(user); // 데이터베이스에 저장
            }
            log.info("user 정보 : {}", user);
            return user; // 사용자 반환
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 오류 발생 시 null 반환
        }
    }

}
