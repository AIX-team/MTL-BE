package com.example.mytravellink.domain.travel.service;

import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;
import com.example.mytravellink.domain.travel.entity.Guide;
import com.example.mytravellink.domain.travel.entity.TravelInfo;

public interface GuideService {
  Guide getGuide(String guideId);
  TravelInfo getTravelInfo(String guideId);
  void createGuideAndCourses(Guide guide, AIGuideCourseResponse aiGuideCourseResponse);
}

