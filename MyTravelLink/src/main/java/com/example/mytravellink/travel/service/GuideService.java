package com.example.mytravellink.travel.service;

import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;
import com.example.mytravellink.travel.domain.Guide;

public interface GuideService {
  void createGuideAndCourses(Guide guide, AIGuideCourseResponse aiGuideCourseResponse);
}
