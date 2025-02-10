package com.example.mytravellink.domain.travel.service;

import org.springframework.stereotype.Service;

import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;
import com.example.mytravellink.domain.travel.entity.Course;
import com.example.mytravellink.domain.travel.entity.CoursePlace;
import com.example.mytravellink.domain.travel.entity.Guide;
import com.example.mytravellink.domain.travel.entity.TravelInfo;
import com.example.mytravellink.domain.travel.repository.CoursePlaceRepository;
import com.example.mytravellink.domain.travel.repository.CourseRepository;
import com.example.mytravellink.domain.travel.repository.GuideRepository;
import com.example.mytravellink.domain.travel.repository.PlaceRepository;
import com.example.mytravellink.domain.travel.repository.TravelInfoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuideServiceImpl implements GuideService {
  
  private final GuideRepository guideRepository;
  private final CourseRepository courseRepository;
  private final CoursePlaceRepository coursePlaceRepository;
  private final PlaceRepository placeRepository;
  private final TravelInfoRepository travelInfoRepository;

  /**
   * Guide 조회
   * @param guideId
   * @return Guide
   */
  @Override
  public Guide getGuide(String guideId) {
    return guideRepository.findById(guideId).orElseThrow(() -> new RuntimeException("Guide not found"));
  }

  /**
   * Guide, Course, CoursePlace 생성
   * @param guide
   * @param courseList
   * @param coursePlaceList
   */
  @Override
  @Transactional
  public void createGuideAndCourses(Guide guide, AIGuideCourseResponse aiGuideCourseResponse) {

    Guide savedGuide = saveGuide(guide);

                for (AIGuideCourseResponse.CourseDTO courseResp : aiGuideCourseResponse.getCourses()) {
                    Course course = Course.builder()
                        .courseNumber(courseResp.getCourseNumber())
                        .guide(savedGuide)
                        .build();
                        Course savedCourse = saveCourse(course);
                    for (AIGuideCourseResponse.CourseDTO.PlaceDTO placeResp : courseResp.getPlaces()) {
                        CoursePlace coursePlace = CoursePlace.builder()
                            .course(savedCourse)
                            .place(placeRepository.findById(placeResp.getPlaceId()).orElseThrow(() -> new RuntimeException("Place not found")))
                            .placeNum(placeResp.getPlaceNum())
                            .build();
                        saveCoursePlace(coursePlace);
                    }
                }
  }

  /**
   * Guide 생성
   * @param guide
   * @return GuideId
   */
  private Guide saveGuide(Guide guide) {
    try {
      return guideRepository.save(guide);
    } catch (Exception e) {
      throw new RuntimeException("Guide 생성 실패", e);
    }
  }

  /**
   * Course 생성
   * @param courseList
   */
  private Course saveCourse(Course course) {
    try {
        return courseRepository.save(course);
    } catch (Exception e) {
      throw new RuntimeException("Course 생성 실패", e);
    }
  }

  /**
   * CoursePlace 생성
   * @param coursePlaceList
   */
  private void saveCoursePlace(CoursePlace coursePlace) {
    try {
      coursePlaceRepository.save(coursePlace);
    } catch (Exception e) {
      throw new RuntimeException("CoursePlace 생성 실패", e);
    }
  }

  /**
   * TravelInfo 조회
   * @param guideId
   * @return TravelInfo
   */
  @Override
  public TravelInfo getTravelInfo(String travelInfoId) {
    return travelInfoRepository.findById(travelInfoId).orElseThrow(() -> new RuntimeException("TravelInfo not found"));
  } 

  /**
   * 가이드 북 제목 수정
   * @param guideId
   * @param title
   */
  @Override
  public void updateGuideBookTitle(String guideId, String title) {
    Guide guide = guideRepository.findById(guideId).orElseThrow(() -> new RuntimeException("Guide not found"));
    guide.setTitle(title);
    guideRepository.save(guide);
  }
}
