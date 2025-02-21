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

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import com.example.mytravellink.domain.job.service.JobStatusService;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.dao.DataAccessException;
import org.springframework.web.client.RestClientException;
import java.util.stream.Collectors;
import java.util.concurrent.CompletionException;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionDefinition;

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
    private final TransactionTemplate transactionTemplate;

    @Value("${ai.server.url}")  // application.yml에서 설정
    private String fastAPiUrl;

    @Override
    @Transactional
    public UrlResponse processUrl(UrlRequest urlRequest, String jobId, String email) {
        try {
            AtomicReference<UrlResponse> urlResponse = new AtomicReference<>();
            List<String> newUrlStr = new ArrayList<>();
            
            // 1. 캐시된 데이터 처리
            for(String urlStr : urlRequest.getUrls()) {
                Optional<Url> existingData = urlRepository.findByUrl(urlStr);
                if (!existingData.isPresent()) {
                    newUrlStr.add(urlStr);
                    continue;
                }
                
                UrlResponse cachedResponse = convertToUrlResponse(existingData.get());
                if (cachedResponse != null && !cachedResponse.getPlaceDetails().isEmpty()) {
                    if (urlResponse.get() == null) {
                        urlResponse.set(cachedResponse);
                    } else {
                        urlResponse.get().getPlaceDetails().addAll(cachedResponse.getPlaceDetails());
                    }
                }
            }

            // 2. FastAPI 호출이 필요한 경우
            if (!newUrlStr.isEmpty()) {
                jobStatusService.setJobStatus(jobId, "Processing", "FastAPI 분석 중...");
                
                String requestUrl = fastAPiUrl + "/api/v1/contentanalysis";
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("urls", newUrlStr);
                
                ResponseEntity<UrlResponse> response = restTemplate.postForEntity(
                    requestUrl, requestBody, UrlResponse.class
                );
                
                if (response.getBody() == null || response.getBody().getPlaceDetails() == null) {
                    throw new RuntimeException("FastAPI 응답이 유효하지 않습니다");
                }
                
                // 응답 처리
                processPlaceInfo(response.getBody(), newUrlStr, urlResponse);
            }
            
            // 3. 최종 검증
            if (urlResponse.get() == null || urlResponse.get().getPlaceDetails().isEmpty()) {
                throw new RuntimeException("처리된 장소 데이터가 없습니다");
            }
            
            return urlResponse.get();
        } catch (Exception e) {
            log.error("URL 처리 실패", e);
            
            StringBuilder errorDetail = new StringBuilder();
            
            if (e instanceof TimeoutException) {
                errorDetail.append("FastAPI 처리 시간 초과: ").append(e.getMessage());
            } else if (e instanceof RestClientException) {
                errorDetail.append("FastAPI 서버 통신 오류: ").append(e.getMessage());
            } else if (e instanceof DataAccessException) {
                errorDetail.append("데이터베이스 처리 오류: ").append(e.getMessage());
            } else {
                errorDetail.append("처리 중 오류 발생: ")
                          .append(e.getMessage())
                          .append("\n상세 위치: ")
                          .append(e.getStackTrace()[0].getFileName())
                          .append(":")
                          .append(e.getStackTrace()[0].getLineNumber());
            }
            
            jobStatusService.setJobStatus(jobId, "Failed", errorDetail.toString());
            throw new RuntimeException(errorDetail.toString(), e);
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
        List<PlaceInfo> placeInfoList = new ArrayList<>();  // 수정 가능한 ArrayList 생성
        
        for (UrlPlace urlPlace : url.getUrlPlaces()) {
            placeInfoList.add(convertToPlaceInfo(urlPlace));
        }

        return UrlResponse.builder()
            .contentInfos(new ArrayList<>())  // 수정 가능한 ArrayList 사용
            .placeDetails(placeInfoList)
            .processingTimeSeconds(0)
            .build();
    }

    private PlaceInfo convertToPlaceInfo(UrlPlace urlPlace) {
        Place place = urlPlace.getPlace();
        List<PlacePhoto> images = parseJson(place.getImage(), new TypeReference<List<PlacePhoto>>() {});
        List<String> openHours = parseJson(place.getOpenHours(), new TypeReference<List<String>>() {});

        return PlaceInfo.builder()
            .name(place.getTitle())
            .description(place.getDescription())
            .formattedAddress(place.getAddress())
            .photos(images != null ? new ArrayList<>(images) : new ArrayList<>())
            .phone(place.getPhone())
            .website(place.getWebsite())
            .rating(place.getRating())
            .open_hours(openHours != null ? new ArrayList<>(openHours) : new ArrayList<>())
            .officialDescription(place.getIntro())
            .build();
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
    public void processUrlAsync(UrlRequest urlRequest, String jobId, String email) {
        try {
            jobStatusService.setJobStatus(jobId, "PROCESSING", "URL 분석 중...");
            
            // TransactionTemplate의 propagation을 REQUIRES_NEW로 설정
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            
            // 트랜잭션 내에서 모든 데이터를 즉시 로딩
            UrlResponse response = transactionTemplate.execute(status -> {
                try {
                    UrlResponse result = processUrl(urlRequest, jobId, email);
                    // 결과를 JSON으로 직렬화하여 모든 데이터를 즉시 로딩
                    String jsonResult = objectMapper.writeValueAsString(result);
                    return objectMapper.readValue(jsonResult, UrlResponse.class);
                } catch (Exception e) {
                    log.error("URL 처리 중 오류 발생", e);
                    throw new RuntimeException(e);
                }
            });

            if (response != null && response.getPlaceDetails() != null && 
                !response.getPlaceDetails().isEmpty()) {
                String result = objectMapper.writeValueAsString(response);
                jobStatusService.setJobStatus(jobId, "COMPLETED", result);
            } else {
                throw new RuntimeException("처리된 장소 데이터가 없습니다");
            }
            
        } catch (Exception e) {
            log.error("URL 분석 실패", e);
            StringBuilder errorDetail = new StringBuilder();
            errorDetail.append("비동기 처리 실패: ")
                      .append(e.getMessage());
            
            jobStatusService.setJobStatus(jobId, "FAILED", errorDetail.toString());
        }
    }
    public boolean isUser(String urlId, String userEmail) {
        return usersUrlRepository.existsByIdAndUserEmail(urlId, userEmail);
    }

    // Place 정보 처리를 위한 별도 메서드
    private void processPlaceInfo(UrlResponse apiResponse, List<String> newUrlStr, 
                                AtomicReference<UrlResponse> urlResponse) {
        // URL 조회를 한 번만 수행
        List<Url> savedUrls = newUrlStr.stream()
            .map(urlStr -> urlRepository.findByUrl(urlStr)
                .orElseThrow(() -> new RuntimeException("URL not found")))
            .collect(Collectors.toList());

        int index = 0;
        for (PlaceInfo placeInfo : apiResponse.getPlaceDetails()) {
            List<PlacePhoto> photos = placeInfo.getPhotos();
            String imageUrl = "https://via.placeholder.com/300x200?text=No+Image";
            
            if (photos != null && !photos.isEmpty() && photos.get(0) != null) {
                imageUrl = imageService.redirectImageUrl(photos.get(0).getUrl());
            }
            
            Place place = saveOrUpdatePlace(placeInfo, imageUrl);
            
            // 이미 조회한 URL 사용
            saveUrlPlaceMapping(savedUrls.get(index), place);
            index++;
            
        }
        
        // 응답 데이터 병합
        if (urlResponse.get() == null) {
            urlResponse.set(apiResponse);
        } else {
            urlResponse.get().getPlaceDetails().addAll(apiResponse.getPlaceDetails());
        }
    }
}