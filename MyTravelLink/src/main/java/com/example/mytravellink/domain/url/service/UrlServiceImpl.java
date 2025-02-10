package com.example.mytravellink.domain.url.service;

import com.example.mytravellink.api.url.dto.*;
import com.example.mytravellink.domain.travel.entity.Place;
import com.example.mytravellink.domain.travel.entity.TravelInfo;
import com.example.mytravellink.domain.travel.repository.PlaceRepository;
import com.example.mytravellink.domain.url.entity.Url;
import com.example.mytravellink.domain.url.entity.UrlPlace;
import com.example.mytravellink.domain.url.repository.TravelInfoUrlRepository;
import com.example.mytravellink.domain.url.repository.UrlPlaceRepository;
import com.example.mytravellink.domain.url.repository.UrlRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
public class UrlServiceImpl implements UrlService {

    private final PlaceRepository placeRepository;
    private final RestTemplate restTemplate;
    private final UrlRepository urlRepository;
    private final UrlPlaceRepository urlPlaceRepository;
    private final TravelInfoUrlRepository travelInfoUrlRepository;

    @Value("${ai.server.url}")  // application.yml에서 설정
    private String fastAPiUrl;

    public UrlServiceImpl(RestTemplate restTemplate, UrlRepository urlRepository, PlaceRepository placeRepository, UrlPlaceRepository urlPlaceRepository, TravelInfoUrlRepository travelInfoUrlRepository) {
        this.restTemplate = restTemplate;
        this.urlRepository = urlRepository;
        this.placeRepository = placeRepository;
        this.urlPlaceRepository = urlPlaceRepository;
        this.travelInfoUrlRepository = travelInfoUrlRepository;
    }

    @Override
    public UrlResponse processUrl(UrlRequest urlRequest) {

        // 1. DB에서 기존 데이터 조회
        Optional<Url> existingData = urlRepository.findByUrl(urlRequest.getUrls());

        // 2. 기존 데이터가 있으면 해당 데이터로 반환
        if (existingData.isPresent()) {
            Url url = existingData.get();
            ObjectMapper objectMapper = new ObjectMapper();

            List<PlaceInfo> placeInfoList = url.getUrlPlaces().stream()
                    .map(urlPlace -> {
                        Place place = urlPlace.getPlace();

                        // 🔹 이미지 변환
                        List<PlacePhoto> images;
                        try {
                            images = place.getImage() != null
                                    ? objectMapper.readValue(place.getImage(), new TypeReference<List<PlacePhoto>>() {})
                                    : Collections.emptyList();
                        } catch (Exception e) {
                            images = Collections.emptyList();
                        }

                        // 🔹 영업시간 변환
                        List<String> openHours;
                        try {
                            openHours = place.getOpenHours() != null
                                    ? objectMapper.readValue(place.getOpenHours(), new TypeReference<List<String>>() {})
                                    : Collections.emptyList();
                        } catch (Exception e) {
                            openHours = Collections.emptyList();
                        }

                        return new PlaceInfo(
                                place.getTitle(),
                                place.getDescription(),
                                place.getAddress(),
                                images,  // ✅ JSON 변환된 이미지 리스트 적용
                                place.getPhone(),
                                place.getWebsite(),
                                place.getRating(),
                                openHours  // ✅ JSON 변환된 영업시간 리스트 적용
                        );
                    })
                    .toList();

            return UrlResponse.builder()
                    .contentInfos(Collections.emptyList())
                    .placeDetails(placeInfoList)
                    .processingTimeSeconds(0)
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

                            // ✅ opening_hours가 빈 리스트이거나 null이면 null로 변환
                            String openHours = Optional.ofNullable(placeInfo.getOpen_hours())
                                    .filter(list -> !list.isEmpty() && list.stream().anyMatch(str -> !str.isBlank()))
                                    .map(Object::toString)
                                    .orElse(null);


                            Place newPlace = Place.builder()
                                    .title(placeInfo.getName())
                                    .description(placeInfo.getDescription())
                                    .address(placeInfo.getFormattedAddress()) // 주소 필드
                                    .image(placeInfo.getPhotos() != null ? placeInfo.getPhotos().toString() : null) // 이미지 필드 (필요한 경우)
                                    .phone(placeInfo.getPhone()) // 전화번호
                                    .website(placeInfo.getWebsite()) // 웹사이트
                                    .rating(placeInfo.getRating()) // 평점
                                    .openHours(openHours)  // 시작 시간
                                    .build();
                            return placeRepository.save(newPlace);
                        });

                // 6. Url과 Place를 연결하는 UrlPlace 저장
                UrlPlace urlPlace = UrlPlace.builder()
                        .url(newUrl)
                        .place(place)
                        .build();
                urlPlaceRepository.save(urlPlace);
            }
        }
        return urlResponse;
    }

    public List<Url> findUrlByTravelInfoId(TravelInfo travelInfo) {

        // 1.TravelInfo 가 null 인지 확인
        if(travelInfo == null) {
            return Collections.emptyList();
        }

        // 2. TravelInfo에 해당하는 URL ID 리스트 조회
        List<String> urlIds = travelInfoUrlRepository.findUrlIdByTravelInfoId(travelInfo);

        // 3. ID 목록이 비어있으면 빈 리스트 반환
        if (urlIds == null || urlIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. ID를 기반으로 Url 엔티티 조회
        return urlRepository.findByIdIn(urlIds);
    }

    public List<Place> findPlaceByUrlId(String urlId) {

        // 1. URL에 연결된 UrlPlace 리스트 조회
        List<UrlPlace> urlPlaces = urlPlaceRepository.findByUrl_Id(urlId);

        // 2. UrlPlace 에서 place 리스트 추출 후 반환
        return urlPlaces.stream()
                .map(UrlPlace::getPlace) // UrlPlace 객체에서 Place 객체 추출
                .toList();
    }

}