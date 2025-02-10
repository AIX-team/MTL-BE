package com.example.mytravellink.domain.travel.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.example.mytravellink.domain.travel.entity.Place;
import com.example.mytravellink.domain.travel.entity.TravelInfo;
import com.example.mytravellink.domain.travel.repository.PlaceRepository;
import com.example.mytravellink.domain.travel.repository.TravelInfoPlaceRepository;
import com.example.mytravellink.domain.travel.repository.TravelInfoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelInfoServiceImpl implements TravelInfoService {
  

  private final TravelInfoRepository travelInfoRepository;
  private final TravelInfoPlaceRepository travelInfoPlaceRepository;
  private final PlaceRepository placeRepository;


  /**
   * 여행정보 ID 기준 여행정보 조회
   * @param travelId
   * 
   * @return TravelInfo
   * 
   */
  public TravelInfo getTravelInfo (String travelInfoId) {
    return travelInfoRepository.findById(travelInfoId).orElseThrow(() -> new RuntimeException("TravelInfo not found"));
  }
  
  /**
   * 여행정보 ID 기준 장소 조회
   * @param travelInfoId
   * 
   * @return List<Place>
   */
  public List<Place> getTravelInfoPlace (String travelInfoId) {
    List<String> placeIdList = travelInfoPlaceRepository.findByTravelInfoId(travelInfoId);

    List<Place> placeList = new ArrayList<>();

    if (placeIdList.isEmpty()) {
      return placeList;
    }

    for (String placeId : placeIdList) {
      placeList.add(placeRepository.findById(placeId).orElseThrow(() -> new RuntimeException("Place not found")));
    }

    return placeList;
  }

  /**
   * 여행정보 ID 기준 여행정보 수정
   * 제목, 여행일 수정
   * @param travelInfoId
   * @param travelInfoUpdateTitleAndTravelDaysRequest
   */
  public void updateTravelInfo(String travelInfoId, String travelInfoTitle, Integer travelDays) {
    travelInfoRepository.updateTravelInfo(travelInfoId, travelInfoTitle, travelDays);
  }

  /**
   * 여행정보 ID 기준 장소 수 조회
   * @param travelInfoId
   * 
   * @return int 
   */
  public int getPlaceCnt(String travelInfoId) {return travelInfoPlaceRepository.getPlaceCnt(travelInfoId);}
}
