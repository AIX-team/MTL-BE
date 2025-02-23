package com.example.mytravellink.domain.travel.service;

import com.example.mytravellink.api.travelInfo.dto.travel.AIPlace;
import com.example.mytravellink.api.travelInfo.dto.travel.PlaceSelectRequest;
import com.example.mytravellink.domain.job.service.JobStatusService;
import com.example.mytravellink.domain.travel.entity.*;
import com.example.mytravellink.infrastructure.ai.Guide.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.mytravellink.domain.travel.repository.CoursePlaceRepository;
import com.example.mytravellink.domain.travel.repository.CourseRepository;
import com.example.mytravellink.domain.travel.repository.GuideRepository;
import com.example.mytravellink.domain.travel.repository.PlaceRepository;
import com.example.mytravellink.domain.travel.repository.TravelInfoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuideServiceImpl implements GuideService {

  private final GuideRepository guideRepository;
  private final CourseRepository courseRepository;
  private final CoursePlaceRepository coursePlaceRepository;
  private final PlaceRepository placeRepository;
  private final TravelInfoRepository travelInfoRepository;
  private final JobStatusService jobStatusService;
  private final ObjectMapper objectMapper;
  private final PlaceService placeService;
  private final TravelInfoService travelInfoService;
  private final TransactionTemplate transactionTemplate;


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
  public String createGuideAndCourses(Guide guide, List<AIGuideCourseResponse> aiGuideCourseResponses) {
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
      return savedGuide.getId();
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
        log.debug("Converting PlaceSelectRequest to AIGuideCourseRequest");
        log.debug("PlaceSelectRequest: {}", placeSelectRequest);  // 입력 데이터 로깅

        List<Place> places = placeRepository.findAllById(placeSelectRequest.getPlaceIds());
        List<AIPlace> guidePlaces = places.stream()
                .map(place -> {
                    AIPlace aiPlace = AIPlace.builder()
                            .id(place.getId())
                            .address(place.getAddress())
                            .title(place.getTitle())
                            .description(place.getDescription())
                            .intro(place.getIntro())
                            .type(place.getType())
                            .image(place.getImage())
                            .latitude(place.getLatitude().floatValue())
                            .longitude(place.getLongitude().floatValue())
                            .phone(place.getPhone())
                            .rating(place.getRating())
                            .openHours(place.getOpenHours())
                            .build();
                    return aiPlace;
                })
                .collect(Collectors.toList());

        AIGuideCourseRequest request = AIGuideCourseRequest.builder()
                .places(guidePlaces)
                .travelDays(placeSelectRequest.getTravelDays())
                .travelTaste(placeSelectRequest.getTravelTaste())
                .build();

        log.debug("Converted AIGuideCourseRequest: {}", request);  // 변환된 데이터 로깅
        return request;
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
  
  /**
   * 가이드 북 비동기 생성
   * @param placeSelectRequest
   * @param jobId
   */
  @Async
  @Override
  public void createGuideAsync(PlaceSelectRequest placeSelectRequest, String jobId, String email) {
    try {
      log.info("[작업 시작] jobId: {}, email: {}", jobId, email);
      jobStatusService.setJobStatus(jobId, "PROCESSING", "가이드 생성 시작");

      // 1. AI 요청 데이터 준비
      AIGuideCourseRequest aiGuideCourseRequest = transactionTemplate.execute(status -> {
        try {
          return convertToAIGuideCourseRequest(placeSelectRequest);
        } catch (Exception e) {
          log.error("[AI 요청 준비] jobId: {}, 실패: {}", jobId, e.getMessage());
          throw new RuntimeException("AI 요청 데이터 준비 실패", e);
        }
      });
      
      log.info("[AI 요청] jobId: {}, 데이터 준비 완료", jobId);
      jobStatusService.setResult(jobId, "AI 서버 요청 중...");

      // 2. AI 코스 추천 요청
      List<AIGuideCourseResponse> aiGuideCourseResponses = 
          placeService.getAIGuideCourse(aiGuideCourseRequest, placeSelectRequest.getTravelDays());

      if (aiGuideCourseResponses == null || aiGuideCourseResponses.isEmpty()) {
        throw new RuntimeException("AI 응답 데이터가 없습니다");
      }

      log.info("[AI 응답] jobId: {}, 코스 수: {}", jobId, aiGuideCourseResponses.size());
      jobStatusService.setResult(jobId, "가이드북 생성 중...");

      // 3. 가이드북 생성 및 저장
      String guideId = transactionTemplate.execute(status -> {
        try {
          String title = "가이드북" + (travelInfoService.getGuideCount(email) +1);
          Guide guide = createGuideEntity(placeSelectRequest, title, email);
          return createGuideAndCourses(guide, aiGuideCourseResponses);
        } catch (Exception e) {
          log.error("[가이드 생성] jobId: {}, 실패: {}", jobId, e.getMessage());
          throw new RuntimeException("가이드북 생성 실패", e);
        }
      });

      log.info("[완료] jobId: {}, guideId: {}", jobId, guideId);
      jobStatusService.setJobStatus(jobId, "COMPLETED", guideId);

    } catch (Exception e) {
      String errorDetail = String.format(
        "가이드 생성 실패: %s\n위치: %s:%d",
        e.getMessage(),
        e.getStackTrace()[0].getFileName(),
        e.getStackTrace()[0].getLineNumber()
      );
      log.error("[실패] jobId: {}, {}", jobId, errorDetail);
      jobStatusService.setJobStatus(jobId, "FAILED", errorDetail);
    }
  }

  /**
   * 비동기적 가이드 북 생성을 위한 컨트롤러 서비스
   * @param placeSelectRequest
   * @return String
   */
  @Override
  @Transactional
  public String createGuide(PlaceSelectRequest placeSelectRequest, String email) {
    try {
      AIGuideCourseRequest aiGuideCourseRequest = convertToAIGuideCourseRequest(placeSelectRequest);
      log.debug("AI 요청 데이터: {}", aiGuideCourseRequest);

      List<AIGuideCourseResponse> aiGuideCourseResponses = 
          placeService.getAIGuideCourse(aiGuideCourseRequest, placeSelectRequest.getTravelDays());

      if (aiGuideCourseResponses == null) {
          log.error("AI 응답 데이터가 null입니다");
          throw new RuntimeException("AI 응답 데이터 없음");
      }

      String title = "가이드북" + travelInfoService.getGuideCount(email);
      Guide guide = createGuideEntity(placeSelectRequest, title, email);
      
      return createGuideAndCourses(guide, aiGuideCourseResponses);
    } catch (Exception e) {
      log.error("가이드 생성 중 오류 발생", e);
      throw new RuntimeException("가이드 생성 실패", e);
    }
  }

  /**
   * 가이드 엔티티 생성
   * @param placeSelectRequest
   * @param title
   * @param email
   * @return Guide
   */
  private Guide createGuideEntity(PlaceSelectRequest placeSelectRequest, String title, String email) {
    return Guide.builder()
            .travelInfo(travelInfoService.getTravelInfo(placeSelectRequest.getTravelInfoId()))
            .title(title)
            .travelDays(placeSelectRequest.getTravelDays())
            .courseCount(placeSelectRequest.getTravelDays())
            .planTypes(placeSelectRequest.getTravelTaste()) // 타입별 수정해야됨
            .isFavorite(false)
            .fixed(false)
            .isDelete(false)
            .build();
  }
}
