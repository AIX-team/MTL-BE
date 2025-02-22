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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

import java.util.concurrent.atomic.AtomicReference;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.Duration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.util.stream.Collectors;

@Service
@EnableAsync  // 비동기 처리 활성화
@RequiredArgsConstructor
@Slf4j
public class UrlServiceImpl implements UrlService {

    private final PlaceRepository placeRepository;
    private final UrlRepository urlRepository;
    private final UrlPlaceRepository urlPlaceRepository;
    private final TravelInfoUrlRepository travelInfoUrlRepository;
    private final TravelInfoRepository travelInfoRepository;
    private final UsersRepository usersRepository;
    private final UsersUrlRepository usersUrlRepository;
    private final TravelInfoPlaceRepository travelInfoPlaceRepository;
    private final ImageService imageService;
    private final JobStatusService jobStatusService;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;
    private final WebClient webClient;

    @Value("${ai.server.url}")
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
                
                UrlResponse cachedResponse = convertToUrlResponse(existingData.get(), jobId);
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
                
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("urls", newUrlStr);
                
                // WebClient를 사용한 FastAPI 호출
                UrlResponse response = webClient
                    .post()
                    .uri("/api/v1/contentanalysis")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(
                        status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                            .flatMap(error -> Mono.error(new RuntimeException("FastAPI 오류: " + error)))
                    )
                    .bodyToMono(UrlResponse.class)
                    .block(Duration.ofMinutes(5));  // 최대 5분 대기
                
                if (response == null || 
                    response.getPlaceDetails() == null || 
                    response.getPlaceDetails().isEmpty()) {
                    throw new RuntimeException("FastAPI 응답이 유효하지 않습니다");
                }
                
                processPlaceInfo(response, urlResponse, jobId);
            }
            
            if (urlResponse.get() == null || urlResponse.get().getPlaceDetails().isEmpty()) {
                throw new RuntimeException("처리된 장소 데이터가 없습니다");
            }
            
            return urlResponse.get();
            
        } catch (Exception e) {
            log.error("URL 처리 실패", e);
            String errorDetail = String.format("처리 중 오류 발생: %s\n상세 위치: %s:%d",
                e.getMessage(),
                e.getStackTrace()[0].getFileName(),
                e.getStackTrace()[0].getLineNumber());
            
            jobStatusService.setJobStatus(jobId, "Failed", errorDetail);
            throw new RuntimeException(errorDetail, e);
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
    private UrlResponse convertToUrlResponse(Url url, String jobId) {
        try {
            jobStatusService.setResult(jobId, "URL 데이터 변환 시작: " + url.getUrl());
            
            List<PlaceInfo> placeInfoList = new ArrayList<>();
            
            // Fetch 조인으로 N+1 문제 해결
            List<UrlPlace> urlPlaces = urlPlaceRepository.findByUrl_Id(url.getId());
            
            for (UrlPlace urlPlace : urlPlaces) {
                placeInfoList.add(convertToPlaceInfo(urlPlace));
            }

            jobStatusService.setResult(jobId, "변환된 장소 데이터 수: " + placeInfoList.size());

            return UrlResponse.builder()
                .contentInfos(new ArrayList<>())
                .placeDetails(placeInfoList)
                .processingTimeSeconds(0)
                .build();
        } catch (Exception e) {
            jobStatusService.setResult(jobId, "URL 데이터 변환 실패: " + e.getMessage());
            throw e;
        }
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
            Map<String, String> payload = new HashMap<>();
            payload.put("video_url", videoUrl);
            
            return webClient
                .post()
                .uri("/api/v1/youtube/check_subtitles")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Object hasSubtitles = response.get("has_subtitles");
                    return hasSubtitles instanceof Boolean && (Boolean) hasSubtitles;
                })
                .block(Duration.ofSeconds(10));
                
        } catch (Exception e) {
            log.error("자막 체크 실패", e);
            return false;
        }
    }

    @Override
    @Async
    public void processUrlAsync(UrlRequest urlRequest, String jobId, String email) {
        try {
            // 초기 입력값 로깅
            jobStatusService.setResult(jobId, String.format(
                "처리 시작 - 총 %d개 URL: %s", 
                urlRequest.getUrls().size(), 
                String.join(", ", urlRequest.getUrls())
            ));

            AtomicReference<UrlResponse> urlResponse = new AtomicReference<>();
            
            // 1. URL 분류
            Map<Boolean, List<String>> urlGroups = urlRequest.getUrls().stream()
                .collect(Collectors.groupingBy(url -> 
                    urlRepository.findByUrl(url).isPresent()
                ));
            
            // URL 분류 결과 로깅
            jobStatusService.setResult(jobId, String.format(
                "URL 분류 결과 - 캐시된 URL: %d개, 새로운 URL: %d개",
                urlGroups.getOrDefault(true, Collections.emptyList()).size(),
                urlGroups.getOrDefault(false, Collections.emptyList()).size()
            ));
            
            // 2. 캐시 데이터 처리
            if (urlGroups.containsKey(true)) {
                transactionTemplate.execute(status -> {
                    List<Url> cachedUrls = urlRepository.findByUrlIn(urlGroups.get(true));
                    List<PlaceInfo> cachedPlaces = new ArrayList<>();
                    
                    for (Url url : cachedUrls) {
                        jobStatusService.setResult(jobId, "캐시 데이터 처리 중: " + url.getUrl());
                        UrlResponse cached = convertToUrlResponse(url, jobId);
                        if (cached != null && !cached.getPlaceDetails().isEmpty()) {
                            cachedPlaces.addAll(cached.getPlaceDetails());
                            jobStatusService.setResult(jobId, String.format(
                                "캐시된 장소 추가: %s - %d개",
                                url.getUrl(),
                                cached.getPlaceDetails().size()
                            ));
                        } else {
                            jobStatusService.setResult(jobId, "캐시 데이터 없음: " + url.getUrl());
                        }
                    }
                    
                    if (!cachedPlaces.isEmpty()) {
                        UrlResponse cachedResponse = new UrlResponse();
                        cachedResponse.setPlaceDetails(cachedPlaces);
                        urlResponse.set(cachedResponse);
                        jobStatusService.setResult(jobId, "총 캐시된 장소 수: " + cachedPlaces.size());
                    } else {
                        jobStatusService.setResult(jobId, "캐시된 장소 데이터 없음");
                    }
                    return null;
                });
            }

            // 3. 새로운 URL 처리
            List<String> newUrls = urlGroups.getOrDefault(false, new ArrayList<>());
            if (!newUrls.isEmpty()) {
                jobStatusService.setResult(jobId, String.format(
                    "FastAPI 호출 시작 - URLs: %s",
                    String.join(", ", newUrls)
                ));
                
                UrlResponse apiResponse = webClient.post()
                    .uri("/api/v1/contentanalysis")
                    .bodyValue(Map.of("urls", newUrls))
                    .retrieve()
                    .bodyToMono(UrlResponse.class)
                    .timeout(Duration.ofMinutes(15))
                    .doOnSuccess(response -> {
                        jobStatusService.setResult(jobId, String.format(
                            "FastAPI 응답 성공 - 장소 수: %d",
                            response != null ? 
                                (response.getPlaceDetails() != null ? 
                                    response.getPlaceDetails().size() : 0) : 0
                        ));
                    })
                    .doOnError(error -> 
                        jobStatusService.setResult(jobId, "FastAPI 호출 실패: " + error.getMessage())
                    )
                    .block();

                if (apiResponse != null && !apiResponse.getPlaceDetails().isEmpty()) {
                    transactionTemplate.execute(status -> {
                        processPlaceInfo(apiResponse, urlResponse, jobId);
                        for (String url : newUrls) {
                            updateUserUrlStatus(email, url);
                            jobStatusService.setResult(jobId, "URL 상태 업데이트: " + url);
                        }
                        return null;
                    });
                } else {
                    jobStatusService.setResult(jobId, String.format(
                        "FastAPI 응답 무효 - response: %s, placeDetails: %s",
                        apiResponse != null ? "not null" : "null",
                        apiResponse != null && apiResponse.getPlaceDetails() != null ? 
                            apiResponse.getPlaceDetails().size() + "개" : "null"
                    ));
                }
            }

            // 4. 최종 결과 검증
            UrlResponse finalResponse = urlResponse.get();
            jobStatusService.setResult(jobId, String.format(
                "최종 결과 검증 - response: %s, placeDetails: %s",
                finalResponse != null ? "not null" : "null",
                finalResponse != null && finalResponse.getPlaceDetails() != null ? 
                    finalResponse.getPlaceDetails().size() + "개" : "null"
            ));

            if (finalResponse != null && !finalResponse.getPlaceDetails().isEmpty()) {
                String result = objectMapper.writeValueAsString(finalResponse);
                jobStatusService.setJobStatus(jobId, "Completed", result);
            } else {
                throw new RuntimeException("처리된 데이터가 없습니다");
            }
            
        } catch (Exception e) {
            log.error("URL 처리 실패", e);
            String errorDetail = String.format(
                "처리 중 오류 발생: %s\n스택트레이스: %s\n발생 위치: %s:%d",
                e.getMessage(),
                Arrays.toString(e.getStackTrace()),
                e.getStackTrace()[0].getFileName(),
                e.getStackTrace()[0].getLineNumber()
            );
            jobStatusService.setResult(jobId, errorDetail);
            jobStatusService.setJobStatus(jobId, "Failed", errorDetail);
        }
    }

    private void updateUserUrlStatus(String email, String url) {
        UsersUrl usersUrl = usersUrlRepository.findByUserEmailAndUrlUrl(email, url)
            .orElseGet(() -> UsersUrl.builder()
                .user(usersRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found")))
                .url(urlRepository.findByUrl(url)
                    .orElseThrow(() -> new RuntimeException("URL not found")))
                .build());
        usersUrl.setUse(false);
        usersUrlRepository.save(usersUrl);
    }

    public boolean isUser(String urlId, String userEmail) {
        return usersUrlRepository.existsByIdAndUserEmail(urlId, userEmail);
    }

    // Place 정보 처리를 위한 별도 메서드
    private void processPlaceInfo(UrlResponse apiResponse,
                                AtomicReference<UrlResponse> urlResponse, String jobId) {
        try {
            // 입력값 검증
            if (apiResponse == null || apiResponse.getPlaceDetails() == null) {
                jobStatusService.setResult(jobId, "API 응답이 null입니다");
                throw new RuntimeException("API 응답이 null입니다");
            }
            // Place 정보 처리
            List<PlaceInfo> processedPlaces = new ArrayList<>();
            for (PlaceInfo placeInfo : apiResponse.getPlaceDetails()) {
                try {
                    if (placeInfo == null) {
                        jobStatusService.setResult(jobId, "null PlaceInfo 발견, 건너뜀");
                        continue;
                    }

                    jobStatusService.setResult(jobId, "장소 처리 시작: " + placeInfo.getName());

                    // 이미지 URL 처리
                    String imageUrl = "https://via.placeholder.com/300x200?text=No+Image";
                    if (placeInfo.getPhotos() != null && 
                        !placeInfo.getPhotos().isEmpty() && 
                        placeInfo.getPhotos().get(0) != null) {
                        try {
                            imageUrl = imageService.redirectImageUrl(placeInfo.getPhotos().get(0).getUrl());
                        } catch (Exception e) {
                            jobStatusService.setResult(jobId, "이미지 URL 처리 실패: " + e.getMessage());
                        }
                    }

                    // Place 저장
                    Place place = saveOrUpdatePlace(placeInfo, imageUrl);
                    jobStatusService.setResult(jobId, "장소 저장 완료: " + place.getTitle());
                    Url url = urlRepository.findByUrl(placeInfo.getSourceUrl())
                        .orElseGet(() -> {
                            Url newUrl = Url.builder()
                                .url(placeInfo.getSourceUrl())
                                .urlTitle(placeInfo.getSourceUrl())
                                .urlAuthor("system")
                                .build();
                            return urlRepository.save(newUrl);
                        });
                    jobStatusService.setResult(jobId, "URL 저장 완료: " + url.getUrl());

                    saveUrlPlaceMapping(url, place);
                    processedPlaces.add(placeInfo);
                    
                } catch (Exception e) {
                    jobStatusService.setResult(jobId, "장소 처리 실패: " + e.getMessage());
                }
            }

            // 응답 데이터 병합
            if (!processedPlaces.isEmpty()) {
                if (urlResponse.get() == null) {
                    UrlResponse newResponse = new UrlResponse();
                    newResponse.setPlaceDetails(new ArrayList<>(processedPlaces));
                    urlResponse.set(newResponse);
                } else {
                    urlResponse.get().getPlaceDetails().addAll(processedPlaces);
                }
                jobStatusService.setResult(jobId, 
                    String.format("처리 완료 - 총 %d개 장소 처리됨", processedPlaces.size()));
            } else {
                jobStatusService.setResult(jobId, "처리된 장소가 없습니다");
                throw new RuntimeException("처리된 장소가 없습니다");
            }

        } catch (Exception e) {
            jobStatusService.setResult(jobId, "장소 정보 처리 중 오류: " + e.getMessage());
            throw e;
        }
    }
}