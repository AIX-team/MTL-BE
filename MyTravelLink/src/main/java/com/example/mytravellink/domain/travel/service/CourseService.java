package com.example.mytravellink.domain.travel.service;

import java.util.List;
import java.util.Map;

import com.example.mytravellink.api.travelInfo.dto.travel.GuideBookResponse;

public interface CourseService {
  List<GuideBookResponse.CourseList> getCoursePlace(String guideId);

  void updateCoursePlace(String courseId, Map<String, Integer> placeIds);
}
