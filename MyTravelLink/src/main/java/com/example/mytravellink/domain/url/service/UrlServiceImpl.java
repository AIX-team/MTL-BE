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

    // 2. 기존 데이터가 있으면 해당 데이터로 반환
    if (existingData.isPresent()) {
      return new UrlResponse(existingData.get().getUrl());
    }

    // 3. FASTAPI로 요청 해서 처리된 데이터 가져오기
    String requestUrl = fastAPiUrl + "/processyoutube";
    ResponseEntity<UrlResponse> response = restTemplate.postForEntity(
            requestUrl, urlRequest, UrlResponse.class
    );

    UrlResponse urlResponse = response.getBody();
    if (urlResponse != null) {

      // 4. 새로운 URL 엔티티 저장
      Url newUrl = Url.builder()
              .url(urlRequest.getUrl())
              .urlTitle(urlRequest.getUrl())
              .urlAuthor(urlRequest.getUrl())
              .build();
      urlRepository.save(newUrl);

      // 5. FASTAPI 에서 추출된 장소 관련 데이터 Place에 저장
      String processedData = urlResponse.getProcessedData(); // 단일 문자열
      if (processedData != null && !processedData.isEmpty()) {
        String[] placesArray = processedData.split(",\\s*"); // 쉼표 + 공백 기준으로 분할

        for (String placeTitle : placesArray) {
          Place place = placeRepository.findByTitle(placeTitle)
                  .orElseGet(() -> { // 없으면 새로 저장
                    Place newPlace = Place.builder()
                            .title(placeTitle)
                            .build();
                    return placeRepository.save(newPlace);
                  });
        }
      }

    }
    return urlResponse;
  }
}