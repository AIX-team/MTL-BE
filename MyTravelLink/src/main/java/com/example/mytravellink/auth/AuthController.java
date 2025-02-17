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
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

        ResponseEntity<String> tokenResponse = restTemplate.exchange(tokenUrl, HttpMethod.POST, requestEntity, String.class);
        log.info("Token Response: {}", tokenResponse.getBody());

        // 2. 액세스 토큰 추출
        String accessToken = extractAccessToken(tokenResponse.getBody());
        if (accessToken == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseMessage(HttpStatus.BAD_REQUEST, "액세스 토큰 추출 실패", null));
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
        String backendAccessToken = jwtTokenProvider.generateToken(member); // 사용자 정보를 기반으로 JWT 생성

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("token", backendAccessToken);
        responseMap.put("user", member);

        log.info("Backend access token: {}", backendAccessToken);

        return ResponseEntity
                .ok()
                .body(new ResponseMessage(HttpStatus.CREATED, "로그인 성공", responseMap));
    }

    // /loginSuccess 엔드포인트에서 OAuth 인증 후 프론트엔드로 리디렉션하도록 수정
    @GetMapping("/loginSuccess")
    public RedirectView loginSuccess(@RequestParam("code") String code) {
        // googleCallback을 호출하여 OAuth 인증 처리
        ResponseEntity<ResponseMessage> responseEntity = googleCallback(code);
        ResponseMessage responseMessage = responseEntity.getBody();

        // results에 token과 user가 포함되어 있음
        // results가 Map 타입으로 저장되어 있다고 가정 (예: Map<String, Object>)
        String token = "";
        if (responseMessage != null && responseMessage.getResults() instanceof Map) {
            Map resultsMap = (Map) responseMessage.getResults();
            if (resultsMap.get("token") != null) {
                token = resultsMap.get("token").toString();
            }
        }

        // 프론트엔드 URL로 리디렉션 (토큰을 쿼리 파라미터로 전달)
        String redirectUrl = "https://mytravellink.site/?token=" + token;
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl(redirectUrl);
        return redirectView;
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
            String picture = jsonNode.get("picture").asText();

            Optional<Users> optionalUser = memberRepository.findByEmail(email);
            Users user;
            if (optionalUser.isPresent()) {
                user = optionalUser.get();
            } else {
                user = Users.builder()
                        .email(email)
                        .name(name)
                        .profileImg(picture)
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
