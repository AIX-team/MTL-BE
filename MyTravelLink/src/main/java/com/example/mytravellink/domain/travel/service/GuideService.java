package com.example.mytravellink.domain.travel.service;

import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;
import com.example.mytravellink.domain.travel.entity.Guide;

public interface GuideService {
  void createGuideAndCourses(Guide guide, AIGuideCourseResponse aiGuideCourseResponse);
}
