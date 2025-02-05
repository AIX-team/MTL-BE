package com.example.mytravellink.domain.url.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.mytravellink.domain.travel.entity.Place;
import com.example.mytravellink.domain.travel.entity.TravelInfo;
import com.example.mytravellink.domain.travel.repository.PlaceRepository;
import com.example.mytravellink.domain.url.entity.Url;
import com.example.mytravellink.domain.url.repository.TravelInfoUrlRepository;
import com.example.mytravellink.domain.url.repository.UrlPlaceRepository;
import com.example.mytravellink.domain.url.repository.UrlRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

  private final UrlRepository urlRepository;
  private final UrlPlaceRepository urlPlaceRepository;
  private final PlaceRepository placeRepository;
  private final TravelInfoUrlRepository travelInfoUrlRepository;


  /**
   * URL ID 기준 장소 조회
   * @param urlId
   * @return List<Place>
   */
  @Override
  public List<Place> findPlaceByUrlId (String urlId) {
    List<String> placeIdList = urlPlaceRepository.findByUrlId(urlId);
    List<Place> placeList = new ArrayList<>();
    for (String placeId : placeIdList) {
      placeList.add(placeRepository.findById(placeId).get());
    }
    return placeList;
  }

  /**
   * 여행정보 ID 기준 URL 조회
   * @param travelId
   * @return List<Url>
   */
  @Override
  public List<Url> findUrlByTravelInfoId(TravelInfo travelInfo) {

    List<String> travelInfoUrlList = travelInfoUrlRepository.findUrlIdByTravelInfoId(travelInfo);
    List<Url> urlList = new ArrayList<>();
    for (String urlId : travelInfoUrlList) {
      urlList.add(urlRepository.findById(urlId).orElse(null));
    }
    return urlList;
  }
}
