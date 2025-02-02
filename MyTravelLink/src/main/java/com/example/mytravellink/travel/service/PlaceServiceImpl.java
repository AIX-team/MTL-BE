package com.example.mytravellink.travel.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.mytravellink.infrastructure.ai.Guide.AIGuideInfrastructure;
import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseRequest;
import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;
import com.example.mytravellink.travel.domain.Place;
import com.example.mytravellink.travel.repository.PlaceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {
  
  private final PlaceRepository placeRepository;
  private final AIGuideInfrastructure aiGuideInfrastructure;

  @Override
  public AIGuideCourseResponse getAIGuideCourse(List<String> placeIdList, int dayNum) {
    List<Place> placeList = placeRepository.findByIds(placeIdList);
    AIGuideCourseRequest aiGuideCourseRequest = AIGuideCourseRequest.builder()
      .placeList(placeList)
      .dayNum(dayNum)
      .build();
    return aiGuideInfrastructure.getGuideRecommendation(aiGuideCourseRequest);
  }

  @Override
  public Place findById(String id) {
    return placeRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Place not found"));
  }
}
