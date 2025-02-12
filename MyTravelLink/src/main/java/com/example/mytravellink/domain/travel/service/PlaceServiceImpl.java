package com.example.mytravellink.domain.travel.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.mytravellink.infrastructure.ai.Guide.AIGuideInfrastructure;
import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseRequest;
import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;
import com.example.mytravellink.infrastructure.ai.Guide.dto.AISelectedPlaceRequest;
import com.example.mytravellink.infrastructure.ai.Guide.dto.AISelectedPlaceResponse;
import com.example.mytravellink.api.travelInfo.dto.travel.TravelInfoPlaceResponse;
import com.example.mytravellink.domain.travel.entity.Place;
import com.example.mytravellink.domain.travel.repository.PlaceRepository;
import com.example.mytravellink.domain.travel.repository.TravelInfoPlaceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceServiceImpl implements PlaceService {
  
  private final PlaceRepository placeRepository;
  private final TravelInfoPlaceRepository travelInfoPlaceRepository;
  private final AIGuideInfrastructure aiGuideInfrastructure;




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
  public AIGuideCourseResponse getAIGuideCourse(List<String> placeIds, int dayNum) {
    List<Place> placeList = placeRepository.findByIds(placeIds);
    AIGuideCourseRequest aiGuideCourseRequest = AIGuideCourseRequest.builder()
      .placeList(placeList)
      .dayNum(dayNum)
      .build();
    return aiGuideInfrastructure.getGuideRecommendation(aiGuideCourseRequest);
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
    String placeId = travelInfoPlaceRepository.findByTravelInfoId(travelInfoId).get(0);
    Place place = placeRepository.findById(placeId).orElseThrow(() -> new RuntimeException("Place not found"));
    String imageUrl = place.getImage();
    return imageUrl;
  }
}

