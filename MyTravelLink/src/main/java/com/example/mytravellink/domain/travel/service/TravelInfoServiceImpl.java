package com.example.mytravellink.domain.travel.service;

import java.util.List;
import java.util.ArrayList;

import org.springframework.stereotype.Service;

import com.example.mytravellink.domain.travel.entity.Place;
import com.example.mytravellink.domain.travel.entity.TravelInfo;
import com.example.mytravellink.domain.travel.repository.PlaceRepository;
import com.example.mytravellink.domain.travel.repository.TravelInfoPlaceRepository;
import com.example.mytravellink.domain.travel.repository.TravelInfoRepository;
import com.example.mytravellink.domain.url.repository.TravelInfoUrlRepository;
import com.example.mytravellink.domain.url.repository.UrlRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TravelInfoServiceImpl implements TravelInfoService {
  

  private final TravelInfoRepository travelInfoRepository;
  private final TravelInfoPlaceRepository travelInfoPlaceRepository;
  private final PlaceRepository placeRepository;
  private final TravelInfoUrlRepository travelInfoUrlRepository;
  private final UrlRepository urlRepository;

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

  /**
   * UserID 기준 여행 정보 조회
   * @param userId
   * 
   */
  public List<TravelInfo> getTravelInfoList(String userEmail) {
    return travelInfoRepository.findByUserEmail(userEmail);
    }

  /**
   * 여행 정보 ID 기준 즐겨찾기 여부 수정
   * @param travelInfoId
   * @param isFavorite
   */
  public void updateFavorite(String travelInfoId, Boolean isFavorite) {
    travelInfoRepository.updateFavorite(travelInfoId, isFavorite);
  }

  /**
   * 여행 정보 ID 기준 고정 여부 수정
   * @param travelInfoId
   * @param fixed
   */
  public void updateFixed(String travelInfoId, Boolean fixed) {
    travelInfoRepository.updateFixed(travelInfoId, fixed);
  }

  /**
   * 여행 정보 ID 기준 여행 정보 삭제
   * @param travelInfoId
   */
  public void deleteTravelInfo(String travelInfoId) {
    travelInfoRepository.updateDeleted(travelInfoId, true);
  } 

  /**
   * 여행 정보 ID 기준 URL 작성자 조회
   * @param travelInfoId
   * 
   * @return List<String>
   */
  public List<String> getUrlAuthors(String travelInfoId) {
    List<String> urlIdList = travelInfoUrlRepository.findUrlIdByTravelInfoId(travelInfoId);

    List<String> urlAuthorList = new ArrayList<>();

    for (String urlId : urlIdList) {
      urlAuthorList.add(urlRepository.findById(urlId).orElseThrow(() -> new RuntimeException("Url not found")).getUrlAuthor());
    }

    return urlAuthorList;
  }
}
