package com.example.mytravellink.travel.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.example.mytravellink.travel.domain.Place;
import com.example.mytravellink.travel.domain.TravelInfo;
import com.example.mytravellink.travel.repository.PlaceRepository;
import com.example.mytravellink.travel.repository.TravelInfoPlaceRepository;
import com.example.mytravellink.travel.repository.TravelInfoRepository;

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
    return travelInfoRepository.findById(travelInfoId).orElse(null);
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
      placeList.add(placeRepository.findById(placeId).orElse(null));
    }

    return placeList;
  }
  
}
