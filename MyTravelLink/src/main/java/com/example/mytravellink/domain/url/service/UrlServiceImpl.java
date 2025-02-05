package com.example.mytravellink.domain.url.service;

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
    Optional<Url> existingData = urlRepository.findByUrl(urlRequest.getUrl());

    if (existingData.isPresent()) {
      return UrlResponse.builder()
              .sourceUrl(existingData.get().getUrl())
              .build();
    }

    // 2. FASTAPI 요청 (URL 분석)
    String requestUrl = fastAPiUrl + "/api/v1/contentanalysis";
    ResponseEntity<UrlResponse> response = restTemplate.postForEntity(
            requestUrl, urlRequest, UrlResponse.class
    );

    UrlResponse urlResponse = response.getBody();
    if (urlResponse != null) {
      // 3. 새로운 URL 엔티티 저장
      Url newUrl = Url.builder()
              .url(urlResponse.getSourceUrl())  // FastAPI에서 받은 URL 사용
              .urlTitle(urlResponse.getName())
              .urlAuthor("Unknown")  // 필요하면 수정
              .build();
      urlRepository.save(newUrl);

      // 4. 장소 정보 저장
      Place place = placeRepository.findByTitle(urlResponse.getName())
              .orElseGet(() -> {
                Place newPlace = Place.builder()
                        .title(urlResponse.getName())
                        .description(urlResponse.getDescription())
                        .address(urlResponse.getFormattedAddress())
                        .rating(urlResponse.getRating())
                        .phone(urlResponse.getPhone())
                        .website(urlResponse.getWebsite())
                        .build();
                return placeRepository.save(newPlace);
              });
    }
    return urlResponse;
  }

}