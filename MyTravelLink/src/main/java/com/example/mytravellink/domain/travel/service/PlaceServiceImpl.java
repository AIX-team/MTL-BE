package com.example.mytravellink.domain.travel.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.mytravellink.infrastructure.ai.Guide.dto.*;
import org.springframework.stereotype.Service;

import com.example.mytravellink.infrastructure.ai.Guide.AIGuideInfrastructure;
import com.example.mytravellink.api.travelInfo.dto.travel.AIPlace;
import com.example.mytravellink.api.travelInfo.dto.travel.TravelInfoPlaceResponse;
import com.example.mytravellink.domain.travel.entity.Place;
import com.example.mytravellink.domain.travel.repository.PlaceRepository;
import com.example.mytravellink.domain.travel.repository.TravelInfoPlaceRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {
  
  private final PlaceRepository placeRepository;
  private final TravelInfoPlaceRepository travelInfoPlaceRepository;
  private final AIGuideInfrastructure aiGuideInfrastructure;
  private static final Logger log = LoggerFactory.getLogger(PlaceServiceImpl.class);


  /**
   * 장소 조회
   * @param id 장소 ID
   * @return 장소
   */
  @Override
  public Place findById(String id) {
    return placeRepository.findById(id).orElseThrow(() -> new RuntimeException("Place not found"));
  }


  /**
   * AI 코스 추천
   * @param placeIds 장소 ID 리스트
   * @param dayNum 여행 일수
   * @return AI 코스 추천 응답
   */

  @Override
  public List<AIGuideCourseResponse> getAIGuideCourse(AIGuideCourseRequest aiGuideCourseRequest, int travelDays) {
    try {
        log.info("AI 가이드 코스 요청 시작: days={}, taste={}", travelDays, aiGuideCourseRequest.getTravelTaste());
        log.debug("요청 데이터: {}", aiGuideCourseRequest);

        // 1. 장소 데이터 변환 및 유효성 검사
        List<AIPlace> places = aiGuideCourseRequest.getPlaces().stream()
            .map(place -> AIPlace.builder()
                .id(place.getId())
                .address(place.getAddress())
                .title(place.getTitle())
                .description(place.getDescription() != null ? place.getDescription() : "")
                .intro(place.getIntro() != null ? place.getIntro() : "")
                .type(place.getType() != null ? place.getType() : "")
                .image(place.getImage() != null ? place.getImage() : "")
                .latitude(place.getLatitude())
                .longitude(place.getLongitude())
                .openHours(place.getOpenHours() != null ? place.getOpenHours() : "")
                .phone(place.getPhone() != null ? place.getPhone() : "")
                .rating(place.getRating() != null ? place.getRating() : BigDecimal.ZERO)
                .build())
            .collect(Collectors.toList());

        if (places.isEmpty()) {
            throw new RuntimeException("장소 목록이 비어있습니다.");
        }

        // 2. AI 가이드 요청 객체 생성
        AIGuideCourseRequest request = AIGuideCourseRequest.builder()
            .places(places)
            .travelDays(travelDays)
            .travelTaste(aiGuideCourseRequest.getTravelTaste())
            .build();

        // 3. AI 가이드 추천 요청 및 응답 검증
        List<AIGuideCourseResponse> aiGuideCourseResponses = aiGuideInfrastructure.getGuideRecommendation(request);
        
        if (aiGuideCourseResponses == null || aiGuideCourseResponses.isEmpty()) {
            log.error("AI 가이드 추천 응답이 비어있습니다");
            throw new RuntimeException("AI 가이드 추천 응답이 비어있습니다.");
        }

        // 4. 응답 데이터 구조 검증
        for (AIGuideCourseResponse response : aiGuideCourseResponses) {
            if (response.getDailyPlans() == null || response.getDailyPlans().isEmpty()) {
                throw new RuntimeException("일일 계획이 비어있습니다.");
            }
            
            // 각 일일 계획의 장소 목록 검증
            response.getDailyPlans().forEach(dailyPlan -> {
                if (dailyPlan.getPlaces() == null || dailyPlan.getPlaces().isEmpty()) {
                    throw new RuntimeException("일일 계획의 장소 목록이 비어있습니다.");
                }
            });
        }

        log.info("AI 응답 받음: {} 개의 일정", aiGuideCourseResponses.size());
        return aiGuideCourseResponses;

    } catch (Exception e) {
        log.error("AI 가이드 추천 처리 중 오류 발생: {}", e.getMessage(), e);
        throw new RuntimeException("AI 가이드 추천 처리 중 오류 발생: " + e.getMessage());
    }
  }

  /**
   * AI 장소 선택
   * @param travelInfoId 여행 정보 ID
   * @param travelDays 여행 일수
   * @return AI 장소 선택 응답
   */
  @Override
  public TravelInfoPlaceResponse getAISelectPlace(String travelInfoId, int travelDays) {
    List<String> placeIdList = travelInfoPlaceRepository.findByTravelInfoId(travelInfoId);
    List<Place> placeList = placeRepository.findByIds(placeIdList);

    // AI 장소 선택
    try {
      AISelectedPlaceRequest aiSelectedPlaceRequest = AISelectedPlaceRequest.builder()
        .placeList(placeList)
        .travelDays(travelDays)
        .build();

      AISelectedPlaceResponse aiSelectedPlaceResponse = aiGuideInfrastructure.getAISelectPlace(aiSelectedPlaceRequest);
      return TravelInfoPlaceResponse.builder()
        .success("success")
        .message("AI 장소 선택 성공")
        .content(aiSelectedPlaceResponse.dtoConvert())
        .build();
    } catch (Exception e) {
      throw new IllegalArgumentException("AI 장소 선택 실패");
    }
  }

  /**
   * 장소 조회
   * @param placeIds 장소 ID 리스트
   * @return 장소 리스트
   */
  @Override
  public List<Place> getPlacesByIds(List<String> placeIds) {
    return placeRepository.findByIds(placeIds);
  }

  /**
   * 여행 정보 첫 장소 이미지 조회
   * @param travelInfoId 여행 정보 ID
   * @return 장소 이미지
   */
  @Override
  public String getPlaceImage(String travelInfoId) {
    List<String> placeIds = travelInfoPlaceRepository.findByTravelInfoId(travelInfoId);
    Place place = placeRepository.findById(placeIds.get(0)).orElseThrow(() -> new RuntimeException("Place not found"));
    String imageUrl = place.getImage();
    return imageUrl;
  }
}

