package com.example.mytravellink.domain.travel.service;

import java.util.List;

import com.example.mytravellink.api.travelInfo.dto.GuideBookResponse;

public interface CourseService {
  List<GuideBookResponse.CourseList> getCoursePlace(String guideId);
}
