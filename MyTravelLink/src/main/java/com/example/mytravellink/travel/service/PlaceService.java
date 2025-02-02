package com.example.mytravellink.travel.service;

import java.util.List;

import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;
import com.example.mytravellink.travel.domain.Place;

public interface PlaceService {
  AIGuideCourseResponse getAIGuideCourse(List<String> placeIdList, int dayNum);
  Place findById(String id);
}
