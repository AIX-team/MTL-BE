package com.example.mytravellink.domain.travel.service;

import java.util.List;

import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;
import com.example.mytravellink.api.travelInfo.dto.travel.TravelInfoPlaceResponse;
import com.example.mytravellink.domain.travel.entity.Place;

public interface PlaceService {

  // AI 코스 추천
  AIGuideCourseResponse getAIGuideCourse(List<String> placeIdList, int dayNum);

  // AI 장소 선택
  TravelInfoPlaceResponse getAISelectPlace(String travelInfoId, int travelDays);

  // 장소 조회
  Place findById(String id);
  List<Place> getPlacesByIds(List<String> placeIds);
}

