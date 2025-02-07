package com.example.mytravellink.infrastructure.ai.Guide;

import org.springframework.stereotype.Component;

import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseRequest;
import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;
import com.example.mytravellink.infrastructure.ai.Guide.dto.AISelectedPlaceRequest;
import com.example.mytravellink.infrastructure.ai.Guide.dto.AISelectedPlaceResponse;
import com.example.mytravellink.infrastructure.ai.common.AIServerClient;
import com.example.mytravellink.infrastructure.ai.common.exception.AIServerException;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AIGuideInfrastructure {
  private final AIServerClient aiServerClient;
  private static final String url = "api/v1/ai/route";

  public AIGuideCourseResponse getGuideRecommendation(AIGuideCourseRequest guideCourseRequest) {
    try {
      return aiServerClient.post(
        url + "/recommend",
        guideCourseRequest,
        AIGuideCourseResponse.class
      );
    } catch (Exception e) {
      throw new AIServerException("AI 서버 호출 실패", e);
    }
  }

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

