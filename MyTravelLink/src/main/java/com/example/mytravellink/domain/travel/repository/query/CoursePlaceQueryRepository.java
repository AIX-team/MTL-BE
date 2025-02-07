package com.example.mytravellink.domain.travel.repository.query;

import java.util.List;
import java.util.UUID;
public interface CoursePlaceQueryRepository {
  
  void updateCoursePlace(String courseId, List<UUID> placeIds);
}
