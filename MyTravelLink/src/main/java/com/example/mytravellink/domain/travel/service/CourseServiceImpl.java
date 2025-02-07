package com.example.mytravellink.domain.travel.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.mytravellink.api.travelInfo.dto.travel.GuideBookResponse;
import com.example.mytravellink.domain.travel.entity.Course;
import com.example.mytravellink.domain.travel.entity.CoursePlace;
import com.example.mytravellink.domain.travel.repository.CoursePlaceRepository;
import com.example.mytravellink.domain.travel.repository.CourseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

  private final CourseRepository courseRepository;
  private final CoursePlaceRepository coursePlaceRepository;

  @Override
  public List<GuideBookResponse.CourseList> getCoursePlace(String guideId) {

    try {
      List<GuideBookResponse.CourseList> courseListResp = new ArrayList<>();
      List<Course> courseList = courseRepository.findByGuideId(guideId);
    
    for (Course course : courseList) {
      List<GuideBookResponse.CoursePlaceResp> coursePlaceListResp = new ArrayList<>();
      List<CoursePlace> coursePlaceList = coursePlaceRepository.findByCourseId(course.getId());
      coursePlaceListResp.addAll(GuideBookResponse.toCoursePlace(coursePlaceList));

      GuideBookResponse.CourseList courseListResult = GuideBookResponse.CourseList.builder()
        .courseId(course.getId())
        .courseNum(course.getCourseNumber())
        .coursePlaces(coursePlaceListResp)
        .build();

        courseListResp.add(courseListResult);
      }
      return courseListResp;
    } catch (Exception e) {
      return new ArrayList<>();
    }
  }

  @Override
  public void updateCoursePlace(String courseId, List<String> placeIds) {
    try {
      List<UUID> uuidList = placeIds.stream()
        .map(id -> UUID.fromString(id))
        .collect(Collectors.toList());
      coursePlaceRepository.updateCoursePlace(courseId, uuidList);
    } catch (Exception e) {
      throw new RuntimeException("CoursePlace 업데이트 실패", e);
    }
  }
}
