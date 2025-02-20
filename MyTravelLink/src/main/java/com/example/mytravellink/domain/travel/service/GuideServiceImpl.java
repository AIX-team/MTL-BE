package com.example.mytravellink.domain.travel.service;

import com.example.mytravellink.api.travelInfo.dto.travel.AIPlace;
import com.example.mytravellink.api.travelInfo.dto.travel.PlaceSelectRequest;
import com.example.mytravellink.domain.travel.entity.*;
import com.example.mytravellink.infrastructure.ai.Guide.dto.*;
import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.mytravellink.domain.travel.repository.CoursePlaceRepository;
import com.example.mytravellink.domain.travel.repository.CourseRepository;
import com.example.mytravellink.domain.travel.repository.GuideRepository;
import com.example.mytravellink.domain.travel.repository.PlaceRepository;
import com.example.mytravellink.domain.travel.repository.TravelInfoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
  @Async
  public CompletableFuture<String> createGuideAndCourses(Guide guide, List<AIGuideCourseResponse> aiGuideCourseResponses) {

    try {
      Guide savedGuide = saveGuide(guide); // 1. 가이드 저장
      System.out.println("가이드 저장 완료: " + savedGuide);
      System.out.println("저장된 가이드 번호: " +savedGuide.getId());

      // AI 응답 리스트를 반복하며 각 항목에 대해 처리
      for (AIGuideCourseResponse aiGuideCourseResponse : aiGuideCourseResponses) {
        // 각 일일 계획에 대해 반복
        for (DailyPlans dailyPlan : aiGuideCourseResponse.getDailyPlans()) {

          System.out.println("일일 계획: Day " + dailyPlan.getDayNumber());

          // guide_id에 대해 가장 큰 course_number를 찾고, 그 값보다 1을 더하여 courseNumber 생성
          Integer maxCourseNumber = courseRepository.findMaxCourseNumberByGuideId(savedGuide.getId());
          int courseNumber = maxCourseNumber == null ? 1 : maxCourseNumber + 1;

          // 각 일일 계획에 대한 코스 생성
          Course course = Course.builder()
                  .courseNumber(courseNumber)
                  .guide(savedGuide)
                  .build();
          Course savedCourse = saveCourse(course);
          System.out.println("코스 저장 완료: " + savedCourse);

          // 각 장소에 대해 반복하여 CoursePlace 생성
          for (PlaceDTO placeResp : dailyPlan.getPlaces()) {

            int placeNum = dailyPlan.getPlaces().indexOf(placeResp)+1;

            System.out.println("현재 장소 번호: " + placeNum + "- 장소:  " + placeResp);

            // Place 조회
            Place place = placeRepository.findById(placeResp.getId())
                    .orElseThrow(() -> new RuntimeException("Place not found: " + placeResp.getName()));

            // 각 장소에 대해 CoursePlace 생성
            CoursePlace coursePlace = CoursePlace.builder()
                    .course(savedCourse)
                    .place(place) // 장소 찾기
                    .placeNum(placeNum) // 장소 번호
                    .build();
            saveCoursePlace(coursePlace); // CoursePlace 저장
            System.out.println("CoursePlace 저장 완료: " + coursePlace);
          }
        }
      }
      return CompletableFuture.completedFuture(savedGuide.getId());
    } catch(Exception e){
      e.printStackTrace(); // 예외 출력
      throw new RuntimeException("가이드 생성 중 오류 발생" + e.getMessage(), e);
    }
  }


    /**
     * PlaceSelectRequest를 AIGuideCourseRequest로 변환
     * @param placeSelectRequest
     * @return AIGuideCourseRequest
     */

    public AIGuideCourseRequest convertToAIGuideCourseRequest(PlaceSelectRequest placeSelectRequest) {

      List<Place> places = placeRepository.findAllById(placeSelectRequest.getPlaceIds());
      // PlaceSelectRequest에서 입력된 장소 리스트를 기반으로 AIPlace 리스트 생성
      List<AIPlace> guidePlaces = places.stream() // places가 List<PlaceInfo>라고 가정
              .map(place -> {

                // AIPlace로 변환
                AIPlace aiPlace = AIPlace.builder()
                        .id(place.getId()) // ID
                        .address(place.getAddress()) // 주소
                        .title(place.getTitle()) // 제목
                        .description(place.getDescription()) // 설명
                        .intro(place.getIntro()) // 소개
                        .type(place.getType()) // 타입
                        .image(place.getImage()) // 이미지
                        .latitude(place.getLatitude().floatValue()) // 위도
                        .longitude(place.getLongitude().floatValue()) // 경도
                        .phone(place.getPhone()) // 전화번호
                        .rating(place.getRating()) // 평점
                        .openHours(place.getOpenHours()) // 영업시간
                        .build();

                return aiPlace;
              })
              .collect(Collectors.toList());

      // AIGuideCourseRequest 객체 생성
      AIGuideCourseRequest aiGuideCourseRequest = AIGuideCourseRequest.builder()
              .places(guidePlaces) // 변환된 AIPlace 리스트 추가
              .travelDays(placeSelectRequest.getTravelDays()) // 여행 일수
              .build();

      return aiGuideCourseRequest;
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

  /**
   * 가이드 북 목록 조회
   * @param userEmail
   * @return List<Guide>
   */
  @Override
  public List<Guide> getGuideList(String userEmail) {
    List<String> travelInfoIdList = travelInfoRepository.findTravelInfoIdByUserEmail(userEmail);
    return guideRepository.findByTravelInfoIdList(travelInfoIdList);
  }

  /**
   * 가이드 북 즐겨찾기 여부 수정
   * @param guideId
   * @param isFavorite
   */
  @Override
  public void updateGuideBookFavorite(String guideId, boolean isFavorite) {
    guideRepository.updateGuideBookFavorite(guideId, isFavorite);
  }

  /**
   * 가이드 북 고정 여부 수정
   * @param guideId
   * @param isFixed
   */
  @Override
  public void updateGuideBookFixed(String guideId, boolean isFixed) {
    guideRepository.updateGuideBookFixed(guideId, isFixed);
  }

  /**
   * 가이드 북 삭제
   * @param guideId
   */
  @Override
  public void deleteGuideBook(String guideId) {
    guideRepository.updateGuideBookDelete(guideId);
  }

  /**
   * 가이드 북 사용자 여부 조회
   * @param guideId
   * @param userEmail
   * @return boolean
   */
  @Override
  public boolean isUser(String guideId, String userEmail) {
    return guideRepository.isUser(guideId, userEmail);
  }
}
