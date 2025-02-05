package com.example.mytravellink.auth;

import com.example.mytravellink.auth.handler.JwtTokenProvider;
import com.example.mytravellink.common.ResponseMessage;
import com.example.mytravellink.domain.user.entity.User;
import com.example.mytravellink.domain.user.repository.UserRepository;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final UserRepository memberRepository;

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

        // 요청 본문을 MultiValueMap으로 생성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);

        log.info(response.getBody());

        // 2. 액세스 토큰 반환
        String accessToken = extractAccessToken(response.getBody());

        // 3. 사용자 정보 요청
        String userInfoUrl = profileUrl; // 사용자 정보 URL
        log.info(userInfoUrl);
        HttpHeaders userInfoHeaders = new HttpHeaders();
        userInfoHeaders.setBearerAuth(accessToken); // 액세스 토큰을 Authorization 헤더에 추가

        HttpEntity<String> userInfoRequestEntity = new HttpEntity<>(userInfoHeaders);
        ResponseEntity<String> userInfoResponse = restTemplate.exchange(userInfoUrl, HttpMethod.GET, userInfoRequestEntity, String.class);

        // 4. 사용자 정보 처리 및 회원가입 로직
        String userInfo = userInfoResponse.getBody();
        User member = processUserInfo(userInfo);
        log.info(member.toString());

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

    // 사용자가 없으면 데이터 추가
    private User processUserInfo(String userInfo) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(userInfo);

            String name = jsonNode.get("name").asText(); // 사용자 이름
            String email = jsonNode.get("email").asText(); // 이메일

            // Optional<Member>로 변경
            Optional<User> optionalUser = memberRepository.findByEmail(email);
            User user;

            if (optionalUser.isPresent()) {
                user = optionalUser.get(); // 존재하는 사용자
            } else {
                // 사용자 정보가 없으면 새로운 사용자 생성
                user = new User();
                user.setEmail(email);
                user.setName(name);
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
