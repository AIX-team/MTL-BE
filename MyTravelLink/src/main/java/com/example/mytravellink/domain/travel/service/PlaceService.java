package com.example.mytravellink.domain.travel.service;

import java.util.List;

import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;
import com.example.mytravellink.domain.travel.entity.Place;

public interface PlaceService {
  AIGuideCourseResponse getAIGuideCourse(List<String> placeIdList, int dayNum);
  Place findById(String id);
  List<Place> getPlacesByIds(List<String> placeIds);
}
