package com.example.mytravellink.domain.travel.service;

import com.example.mytravellink.api.travelInfo.dto.travel.AIPlace;
import com.example.mytravellink.api.travelInfo.dto.travel.PlaceSelectRequest;
import com.example.mytravellink.domain.travel.entity.*;
import com.example.mytravellink.infrastructure.ai.Guide.dto.*;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.mytravellink.domain.travel.repository.CoursePlaceRepository;
import com.example.mytravellink.domain.travel.repository.CourseRepository;
import com.example.mytravellink.domain.travel.repository.GuideRepository;
import com.example.mytravellink.domain.travel.repository.PlaceRepository;
import com.example.mytravellink.domain.travel.repository.TravelInfoRepository;
import com.example.mytravellink.domain.job.service.JobStatusService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.core.task.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.beans.factory.annotation.Value;
import java.util.UUID;

@Service
@EnableAsync  // 비동기 처리 활성화
@RequiredArgsConstructor
public class GuideServiceImpl implements GuideService {

  private final GuideRepository guideRepository;
  private final CourseRepository courseRepository;
  private final CoursePlaceRepository coursePlaceRepository;
  private final PlaceRepository placeRepository;
  private final TravelInfoRepository travelInfoRepository;
  private final JobStatusService jobStatusService;
  
  @Autowired
  private TaskExecutor taskExecutor;  // TaskExecutor 주입

  @Value("${server.tomcat.connection-timeout:180000}")
  private int connectionTimeout; // 3분으로 설정

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
    @Async
    @Override
    @Transactional
    public CompletableFuture<String> createGuideAndCourses(Guide guide, List<AIGuideCourseResponse> aiGuideCourseResponses) {
        return CompletableFuture.supplyAsync(() -> {
            String jobId = UUID.randomUUID().toString();
            jobStatusService.setStatus(jobId, "Processing");
            
            try {
                var savedGuide = saveGuide(guide);
                
                aiGuideCourseResponses.forEach(aiGuideCourseResponse -> 
                    aiGuideCourseResponse.getDailyPlans().forEach(dailyPlan -> {
                        var courseNumber = courseRepository.findMaxCourseNumberByGuideId(savedGuide.getId());
                        var newCourseNumber = courseNumber == null ? 1 : courseNumber + 1;
                        
                        var course = Course.builder()
                                .courseNumber(newCourseNumber)
                                .guide(savedGuide)
                                .build();
                                
                        var savedCourse = saveCourse(course);
                        
                        dailyPlan.getPlaces().forEach(placeResp -> {
                            var placeNum = dailyPlan.getPlaces().indexOf(placeResp) + 1;
                            var place = placeRepository.findById(placeResp.getId())
                                    .orElseThrow(() -> new RuntimeException("Place not found: " + placeResp.getName()));
                                    
                            var coursePlace = CoursePlace.builder()
                                    .course(savedCourse)
                                    .place(place)
                                    .placeNum(placeNum)
                                    .build();
                                    
                            saveCoursePlace(coursePlace);
                        });
                    })
                );
                
                jobStatusService.setStatus(jobId, "Completed");
                jobStatusService.setResult(jobId, savedGuide.getId());
                return jobId;
            } catch (Exception e) {
                jobStatusService.setStatus(jobId, "Failed");
                throw new RuntimeException("가이드 생성 실패: " + e.getMessage());
            }
        }, taskExecutor);
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
