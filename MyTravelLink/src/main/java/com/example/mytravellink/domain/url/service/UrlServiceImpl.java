package com.example.mytravellink.domain.url.service;

import com.example.mytravellink.api.url.dto.PlaceInfo;
import com.example.mytravellink.api.url.dto.UrlRequest;
import com.example.mytravellink.api.url.dto.UrlResponse;
import com.example.mytravellink.domain.travel.entity.Place;
import com.example.mytravellink.domain.travel.repository.PlaceRepository;
import com.example.mytravellink.domain.url.entity.Url;
import com.example.mytravellink.domain.url.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UrlServiceImpl implements UrlService {

  private final PlaceRepository placeRepository;
  private final RestTemplate restTemplate;
  private final UrlRepository urlRepository;

  @Value("${ai.server.url}")  // application.yml에서 설정
  private String fastAPiUrl;

  public UrlServiceImpl(RestTemplate restTemplate, UrlRepository urlRepository, PlaceRepository placeRepository) {
    this.restTemplate = restTemplate;
    this.urlRepository = urlRepository;
    this.placeRepository = placeRepository;
  }

  @Override
  public UrlResponse processUrl(UrlRequest urlRequest) {

    // 1. DB에서 기존 데이터 조회
    Optional<Url> existingData = urlRepository.findByUrl(urlRequest.getUrls());

    // 2. 기존 데이터가 있으면 해당 데이터로 반환
    if (existingData.isPresent()) {
      return UrlResponse.builder()
              .contentInfos(Collections.emptyList()) // 필요에 따라 빈 리스트로 초기화
              .placeDetails(Collections.emptyList()) // 필요에 따라 빈 리스트로 초기화
              .processingTimeSeconds(0) // 초기값 설정
              .build();
    }

    // 3. FASTAPI로 요청 해서 처리된 데이터 가져오기
    String requestUrl = fastAPiUrl + "/api/v1/contentanalysis";

    // 요청 본문 설정
    Map<String, Object> requestBody = new HashMap<>();
    requestBody.put("urls", Collections.singletonList(urlRequest.getUrls()));

    ResponseEntity<UrlResponse> response = restTemplate.postForEntity(
            requestUrl, requestBody, UrlResponse.class
    );

    UrlResponse urlResponse = response.getBody();
    if (urlResponse != null) {

      // 4. 새로운 URL 엔티티 저장
      Url newUrl = Url.builder()
              .url(urlRequest.getUrls())
              .urlTitle(urlRequest.getUrls())
              .urlAuthor(urlRequest.getUrls())
              .build();
      urlRepository.save(newUrl);

      // 5. FASTAPI 에서 추출된 장소 관련 데이터 Place에 저장
      for (PlaceInfo placeInfo : urlResponse.getPlaceDetails()) {
        Place place = placeRepository.findByTitle(placeInfo.getName())
                .orElseGet(() -> {
                  Place newPlace = Place.builder()
                          .title(placeInfo.getName())
                          .description(placeInfo.getDescription())
                          .address(placeInfo.getFormattedAddress()) // 주소 필드
                          .image(placeInfo.getPhotos().toString()) // 이미지 필드 (필요한 경우)
                          .phone(placeInfo.getPhone()) // 전화번호
                          .website(placeInfo.getWebsite()) // 웹사이트
                          .rating(placeInfo.getRating()) // 평점
                          .openHours(placeInfo.getOpeningHours().toString())  // 시작 시간
                          .build();
                  return placeRepository.save(newPlace);
                });
      }
    }
    return urlResponse;
  }
}