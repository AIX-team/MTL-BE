package com.example.mytravellink.infrastructure.ai.Guide;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseRequest;
import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;
import com.example.mytravellink.infrastructure.ai.Guide.dto.AISelectedPlaceRequest;
import com.example.mytravellink.infrastructure.ai.Guide.dto.AISelectedPlaceResponse;
import com.example.mytravellink.infrastructure.ai.common.AIServerClient;
import com.example.mytravellink.infrastructure.ai.common.exception.AIServerException;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AIGuideInfrastructure {
  private final AIServerClient aiServerClient;
  private final RestTemplate restTemplate;
  private static final String url = "/api/v1";

  @Value("${ai.server.url}")  // application.yml에서 설정
  private String fastAPiUrl;

  // AI 코스 추천
  public List<AIGuideCourseResponse> getGuideRecommendation(AIGuideCourseRequest request) {
    try {
      ResponseEntity<AIGuideCourseResponse> response = restTemplate.postForEntity(
              fastAPiUrl+ url + "/generate-plans",
              request,
              AIGuideCourseResponse.class
      );

      // 응답 본문 로그
      System.out.println("AI 서버 응답: " + response.getBody());
      return (List<AIGuideCourseResponse>) response.getBody();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("AI 서버와의 통신 실패", e);
    }
  }

  // AI 장소 추천
  public AISelectedPlaceResponse getAISelectPlace(AISelectedPlaceRequest aiSelectedPlaceRequest) {
    try {
      return aiServerClient.post(
        url + "/select",
        aiSelectedPlaceRequest,
        AISelectedPlaceResponse.class);
    } catch (Exception e) {
      throw new AIServerException("AI 서버 호출 실패", e);
    }
  }
}

