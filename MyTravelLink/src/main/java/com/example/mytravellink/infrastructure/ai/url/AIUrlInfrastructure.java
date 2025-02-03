package com.example.mytravellink.infrastructure.ai.url;

import org.springframework.stereotype.Component;

import com.example.mytravellink.infrastructure.ai.common.AIServerClient;
import com.example.mytravellink.infrastructure.ai.common.exception.AIServerException;
import com.example.mytravellink.infrastructure.ai.url.dto.AIUrlExtPlaceRequest;
import com.example.mytravellink.infrastructure.ai.url.dto.AIUrlExtPlaceResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AIUrlInfrastructure {
  private final AIServerClient aiServerClient;
  private static final String url = "api/v1/ai/url";

  public AIUrlExtPlaceResponse getUrlPlaces(AIUrlExtPlaceRequest urlExtPlaceRequest) {
    try {
      return aiServerClient.post(
        url + "/places",
        urlExtPlaceRequest,
        AIUrlExtPlaceResponse.class
        );
    } catch (Exception e) {
      throw new AIServerException("AI 서버 호출 실패", e);
    }
  }
}

