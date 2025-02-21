package com.example.mytravellink.domain.url.service;

import com.example.mytravellink.api.url.dto.*;
import com.example.mytravellink.domain.travel.entity.Place;
import com.example.mytravellink.domain.travel.entity.TravelInfo;
import com.example.mytravellink.domain.travel.entity.TravelInfoPlace;
import com.example.mytravellink.domain.travel.repository.PlaceRepository;
import com.example.mytravellink.domain.travel.repository.TravelInfoRepository;
import com.example.mytravellink.domain.travel.service.ImageService;
import com.example.mytravellink.domain.travel.repository.TravelInfoPlaceRepository;
import com.example.mytravellink.domain.travel.entity.TravelInfoUrl;
import com.example.mytravellink.domain.travel.entity.TravelInfoUrlId;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import com.example.mytravellink.domain.users.entity.Users;
import com.example.mytravellink.domain.users.entity.UsersUrl;
import com.example.mytravellink.domain.users.entity.UsersUrlId;
import com.example.mytravellink.domain.users.repository.UsersRepository;
import com.example.mytravellink.domain.users.repository.UsersUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import com.example.mytravellink.domain.job.service.JobStatusService;
import java.util.concurrent.atomic.AtomicReference;

@Service
@EnableAsync  // 비동기 처리 활성화
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private final PlaceRepository placeRepository;
    private final RestTemplate restTemplate;
    private final UrlRepository urlRepository;
    private final UrlPlaceRepository urlPlaceRepository;
    private final TravelInfoUrlRepository travelInfoUrlRepository;
    private final TravelInfoRepository travelInfoRepository;
    private final UsersRepository usersRepository;
    private final UsersUrlRepository usersUrlRepository;
    private final TravelInfoPlaceRepository travelInfoPlaceRepository;
    private final ImageService imageService;
    private final JobStatusService jobStatusService;
    private final ObjectMapper objectMapper;  // ObjectMapper 추가


    @Value("${ai.server.url}")  // application.yml에서 설정
    private String fastAPiUrl;

    @Override
    @Transactional
    public UrlResponse processUrl(UrlRequest urlRequest, String jobId, String email) {
        try {
            // 1. 입력값 검증
            if (urlRequest.getUrls() == null || urlRequest.getUrls().isEmpty()) {
                jobStatusService.setJobStatus(jobId, "FAILED", "URL 리스트가 비어있습니다.");
                throw new IllegalArgumentException("URL 리스트가 비어있습니다.");
            }
            
            AtomicReference<UrlResponse> urlResponse = new AtomicReference<>();
            List<String> newUrlStr = new ArrayList<>();
            
            // 2. 각 URL에 대한 캐시 데이터 확인
            for(String urlStr : urlRequest.getUrls()) {
                Optional<Url> existingData = urlRepository.findByUrl(urlStr);
                
                existingData.ifPresent(cachedUrl -> 
                {//캐시 데이터가 있는 경우
                    UrlResponse cachedResponse = convertToUrlResponse(cachedUrl);
                    if (urlResponse.get() == null) { // 만약 가져온 데이터가 없다면
                        urlResponse.set(cachedResponse);
                    } else {// 만약 가져온 데이터가 있다면
                        urlResponse.get().getPlaceDetails().addAll(cachedResponse.getPlaceDetails());
                    }
                });
                // 캐시 데이터가 없는 경우
                if (!existingData.isPresent()) {
                    newUrlStr.add(urlStr);
                }
            }
            
            // 3. 새로운 URL 분석이 필요한 경우 FastAPI 호출
            if (!newUrlStr.isEmpty()) {
                String requestUrl = fastAPiUrl + "/api/v1/contentanalysis";
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("urls", newUrlStr);
                
                ResponseEntity<UrlResponse> response = restTemplate.postForEntity(
                    requestUrl, requestBody, UrlResponse.class
                );
                
                if (response.getBody() != null) { // 만약 가져온 데이터가 있다면
                    
                    // Place 정보 저장
                    for (PlaceInfo placeInfo : response.getBody().getPlaceDetails()) {
                        List<PlacePhoto> photos = placeInfo.getPhotos();
                        String imageUrl = "https://via.placeholder.com/300x200?text=No+Image";
                        
                        // 만약 이미지가 있다면
                        if (photos != null && !photos.isEmpty() && photos.get(0) != null) {
                            // 이미지 리다이렉트
                            imageUrl = imageService.redirectImageUrl(photos.get(0).getUrl());
                        }
                        
                        // Place 정보 저장
                        Place place = saveOrUpdatePlace(placeInfo, imageUrl);
                        
                        // URL과 Place 연관 매핑 저장
                        for(String urlStr : newUrlStr) {
                            Url url = urlRepository.findByUrl(urlStr)
                                .orElseThrow(() -> new RuntimeException("URL not found"));
                            saveUrlPlaceMapping(url, place);
                        }
                    }
                    
                    for(String urlStr : urlRequest.getUrls()) {
                        UsersUrl usersUrl = usersUrlRepository.findByEmailAndUrl_Url(email, urlStr)
                            .orElseThrow(() -> new RuntimeException("URL not found"));
                        usersUrl.setUse(false);
                        usersUrlRepository.save(usersUrl);
                    }

                    // 응답 데이터 병합
                    if (urlResponse.get() == null) {
                        urlResponse.set(response.getBody());
                    } else {
                        urlResponse.get().getPlaceDetails().addAll(response.getBody().getPlaceDetails());
                    }
                }
            }
            
            return urlResponse.get() != null ? urlResponse.get() : 
                UrlResponse.builder()
                    .placeDetails(new ArrayList<>())
                    .processingTimeSeconds(0.0f)
                    .build();
                
        } catch (Exception e) {
            log.error("URL 처리 실패", e);
            jobStatusService.setJobStatus(jobId, "FAILED", e.getMessage());
            throw new RuntimeException("URL 처리 실패: " + e.getMessage());
        }
    }

    private Place saveOrUpdatePlace(PlaceInfo placeInfo, String imageUrl) {
        return placeRepository.findByTitle(placeInfo.getName())
            .orElseGet(() -> {
                String openHours = Optional.ofNullable(placeInfo.getOpen_hours())
                    .filter(list -> !list.isEmpty())
                    .map(Object::toString)
                    .orElse(null);

                Place newPlace = Place.builder()
                    .title(placeInfo.getName())
                    .description(placeInfo.getDescription())
                    .address(placeInfo.getFormattedAddress())
                    .image(imageUrl)
                    .phone(placeInfo.getPhone())
                    .intro(placeInfo.getOfficialDescription())
                    .website(placeInfo.getWebsite())
                    .rating(placeInfo.getRating())
                    .openHours(openHours)
                    .type(placeInfo.getType())
                    .latitude(placeInfo.getGeometry() != null ? placeInfo.getGeometry().getLatitude() : null)
                    .longitude(placeInfo.getGeometry() != null ? placeInfo.getGeometry().getLongitude() : null)
                    .build();
                return placeRepository.save(newPlace);
            });
    }

    private void saveUrlPlaceMapping(Url url, Place place) {
        UrlPlace urlPlace = UrlPlace.builder()
            .url(url)
            .place(place)
            .build();
        urlPlaceRepository.save(urlPlace);
    }

    @Transactional(readOnly = true)
    private UrlResponse convertToUrlResponse(Url url) {
        List<PlaceInfo> placeInfoList = url.getUrlPlaces().stream()
            .map(this::convertToPlaceInfo)
            .toList();

        return UrlResponse.builder()
            .contentInfos(Collections.emptyList())
            .placeDetails(placeInfoList)
            .processingTimeSeconds(0)
            .build();
    }

    private PlaceInfo convertToPlaceInfo(UrlPlace urlPlace) {
        Place place = urlPlace.getPlace();
        List<PlacePhoto> images = parseJson(place.getImage(), new TypeReference<List<PlacePhoto>>() {});
        List<String> openHours = parseJson(place.getOpenHours(), new TypeReference<List<String>>() {});

        return new PlaceInfo(
            place.getTitle(),
            place.getDescription(),
            place.getAddress(),
            images,
            place.getPhone(),
            place.getWebsite(),
            place.getRating(),
            openHours,
            place.getIntro()
        );
    }

    private <T> T parseJson(String json, TypeReference<T> typeReference) {
        try {
            return json != null ? objectMapper.readValue(json, typeReference) : null;
        } catch (Exception e) {
            log.warn("JSON 파싱 실패: {}", e.getMessage());
            return null;
        }
    }

    // 나머지 메서드들은 기존 로직 유지...

    @Override
    public List<Url> findUrlByTravelInfoId(TravelInfo travelInfo) {
        if(travelInfo == null) {
            return Collections.emptyList();
        }

        List<String> urlIds = travelInfoUrlRepository.findUrlIdByTravelInfoId(travelInfo);

        if (urlIds == null || urlIds.isEmpty()) {
            return Collections.emptyList();
        }

        return urlRepository.findByIdIn(urlIds);
    }

    @Override
    public List<Place> findPlaceByUrlId(String urlId) {
        List<UrlPlace> urlPlaces = urlPlaceRepository.findByUrl_Id(urlId);
        return urlPlaces.stream()
                .map(UrlPlace::getPlace)
                .toList();
    }

    @Override
    public void saveUrl(String travelInfoId, String url, String title, String author) {
        Url newUrl = Url.builder()
            .urlTitle(title)
            .urlAuthor(author)
            .url(url)
            .build();
        urlRepository.save(newUrl);
        TravelInfo travelInfo = travelInfoRepository.findById(travelInfoId)
            .orElseThrow(() -> new RuntimeException("TravelInfo not found"));
        TravelInfoUrl travelInfoUrl = TravelInfoUrl.builder()
            .travelInfo(travelInfo)
            .url(newUrl)
            .build();
        travelInfoUrl.setId(TravelInfoUrlId.builder()
                .travelInfoId(travelInfo.getId())
                .urlId(newUrl.getId())
                .build());
        travelInfoUrlRepository.save(travelInfoUrl);
    }

    @Override
    @Transactional
    public void saveUserUrl(String email, UserUrlRequest request) {
        String urlStr = request.getUrl();
        Url urlEntity = urlRepository.findById(generateUrlId(urlStr)).orElseGet(() -> {
            Url newUrl = Url.builder()
                    .url(urlStr)
                    .urlTitle(request.getTitle())
                    .urlAuthor(request.getAuthor())
                    .build();
            return urlRepository.save(newUrl);
        });

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        UsersUrlId mappingId = new UsersUrlId(user.getEmail(), urlEntity.getId());

        if (!usersUrlRepository.existsById(mappingId)) {
            UsersUrl usersUrlMapping = UsersUrl.builder()
                    .id(mappingId)
                    .user(user)
                    .url(urlEntity)
                    .isUse(true)
                    .build();
            usersUrlRepository.save(usersUrlMapping);
        }
    }

    @Override
    @Transactional
    public void deleteUserUrl(String email, String urlId) {
        if (urlRepository.existsById(urlId)) {
            urlRepository.deleteById(urlId);
        }
    }

    private String generateUrlId(String url) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(url.getBytes(StandardCharsets.UTF_8));
            BigInteger number = new BigInteger(1, hash);
            StringBuilder hexString = new StringBuilder(number.toString(16));
            while (hexString.length() < 128) {
                hexString.insert(0, '0');
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-512 해시 생성 중 오류 발생", e);
        }
    }

    @Override
    @Transactional
    public void deleteUserUrlByUrl(String email, String url) {
        String id = generateUrlId(url);
        UsersUrlId mappingId = UsersUrlId.builder()
                            .email(email)
                            .urlId(id)
                            .build();
        if (usersUrlRepository.existsById(mappingId)) {
            usersUrlRepository.deleteById(mappingId);
        }
        if (urlRepository.existsById(id)) {
            urlRepository.deleteById(id);
        }
    }

    // private String extractYoutubeVideoId(String url) {
    //     try {
    //         URI uri = new URI(url);
    //         String query = uri.getQuery();
    //         if(query != null) {
    //             String[] params = query.split("&");
    //             for (String param : params) {
    //                 String[] keyValue = param.split("=");
    //                 if (keyValue[0].equals("v") && keyValue.length > 1) {
    //                     return keyValue[1];
    //                 }
    //             }
    //         }
    //     } catch (Exception e) {
    //         // 추출 실패 시 빈 문자열 반환
    //     }
    //     return "";
    // }

    @Override
    @Transactional
    public String mappingUrl(UrlRequest request, String email) {
        if (request.getUrls() == null || request.getUrls().isEmpty()) {
            throw new IllegalArgumentException("URL 리스트가 비어있습니다.");
        }

        // 사용자 조회
        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // TravelInfo 생성
        TravelInfo travelInfo = TravelInfo.builder()
                .user(user)
                .travelDays(request.getDays())
                .placeCount(0)  // 초기값 0으로 설정
                .title("여행지")  // 첫 번째 URL을 title로 설정
                .isFavorite(false)
                .fixed(false)
                .isDelete(false)
                .extPlaceListId("")  // 빈 문자열로 초기화
                .travelTasteId("")  // 빈 문자열로 초기화
                .build();

        travelInfo = travelInfoRepository.save(travelInfo);

        // URL들의 모든 Place를 중복 없이 저장하기 위한 Set
        Set<Place> uniquePlaces = new HashSet<>();

        // URL 처리 및 매핑
        for (String urlStr : request.getUrls()) {
            Url url = urlRepository.findByUrl(urlStr)
                    .orElseGet(() -> {
                        Url newUrl = Url.builder()
                                .url(urlStr)
                                .urlTitle(urlStr)
                                .urlAuthor(email)
                                .build();
                        return urlRepository.save(newUrl);
                    });

            // TravelInfo와 URL 연결
            TravelInfoUrl travelInfoUrl = TravelInfoUrl.builder()
                    .travelInfo(travelInfo)
                    .url(url)
                    .build();
            travelInfoUrlRepository.save(travelInfoUrl);

            // URL에 연결된 모든 Place 수집
            List<UrlPlace> urlPlaces = urlPlaceRepository.findByUrl_Id(url.getId());
            urlPlaces.forEach(urlPlace -> uniquePlaces.add(urlPlace.getPlace()));
        }

        // 수집된 모든 Place를 TravelInfo와 매핑
        for (Place place : uniquePlaces) {
            TravelInfoPlace travelInfoPlace = TravelInfoPlace.builder()
                    .travelInfo(travelInfo)
                    .place(place)
                    .build();
            travelInfoPlaceRepository.save(travelInfoPlace);
        }

        // 최종 placeCount 업데이트
        travelInfo.setPlaceCount(uniquePlaces.size());
        travelInfoRepository.save(travelInfo);

        return travelInfo.getId();
    }

    @Override
    public boolean checkYoutubeSubtitles(String videoUrl) {
        try {
            // FastAPI의 유튜브 자막 체크 엔드포인트 호출 (예: http://.../api/v1/youtube/check_subtitles)
            String subtitleUrl = fastAPiUrl + "/api/v1/youtube/check_subtitles";
            Map<String, String> payload = new HashMap<>();
            payload.put("video_url", videoUrl);
            ResponseEntity<Map> response = restTemplate.postForEntity(subtitleUrl, payload, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object hasSubtitles = response.getBody().get("has_subtitles");
                if (hasSubtitles instanceof Boolean) {
                    return (Boolean) hasSubtitles;
                }
            }
        } catch (Exception e) {
            // 에러 로그 기록 (필요 시)
            e.printStackTrace();
        }
        return false;
    }

    @Override
    @Async
    @Transactional
    public void processUrlAsync(UrlRequest urlRequest, String jobId, String email) {
        try {
            UrlResponse response = processUrl(urlRequest, jobId, email);
            String result = objectMapper.writeValueAsString(response);
            jobStatusService.setJobStatus(jobId, "Completed", result);
        } catch (Exception e) {
            log.error("URL 분석 실패", e);
            String errorMessage = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            jobStatusService.setJobStatus(jobId, "FAILED", errorMessage);
        }
    }
    public boolean isUser(String urlId, String userEmail) {
        return usersUrlRepository.existsByIdAndUserEmail(urlId, userEmail);
    }

    
}