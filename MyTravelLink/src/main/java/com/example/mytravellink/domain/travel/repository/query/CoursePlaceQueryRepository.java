package com.example.mytravellink.domain.travel.repository.query;

import java.util.List;

public interface CoursePlaceQueryRepository {
  
  void updateCoursePlace(String courseId, List<String> placeId);
}
