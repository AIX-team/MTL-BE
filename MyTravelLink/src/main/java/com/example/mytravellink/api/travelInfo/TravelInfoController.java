package com.example.mytravellink.api.travelInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.example.mytravellink.infrastructure.ai.Guide.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;
import com.example.mytravellink.api.travelInfo.dto.travel.BooleanRequest;
import com.example.mytravellink.api.travelInfo.dto.travel.GuideBookListResponse;
import com.example.mytravellink.api.travelInfo.dto.travel.GuideBookResponse;
import com.example.mytravellink.api.travelInfo.dto.travel.StringRequest;
import com.example.mytravellink.api.travelInfo.dto.travel.StringResponse;
import com.example.mytravellink.api.travelInfo.dto.travel.PlaceSelectRequest;
import com.example.mytravellink.api.travelInfo.dto.travel.TravelInfoListResponse;
import com.example.mytravellink.api.travelInfo.dto.travel.TravelInfoPlaceResponse;
import com.example.mytravellink.api.travelInfo.dto.travel.TravelInfoUpdateTitleAndTravelDaysRequest;
import com.example.mytravellink.api.travelInfo.dto.travel.TravelInfoUrlResponse;
import com.example.mytravellink.auth.handler.JwtTokenProvider;
import com.example.mytravellink.domain.travel.entity.Guide;
import com.example.mytravellink.domain.travel.entity.Place;
import com.example.mytravellink.domain.travel.entity.TravelInfo;
import com.example.mytravellink.domain.travel.service.CourseServiceImpl;
import com.example.mytravellink.domain.travel.service.GuideServiceImpl;
import com.example.mytravellink.domain.travel.service.ImageService;
import com.example.mytravellink.domain.travel.service.PlaceServiceImpl;
import com.example.mytravellink.domain.travel.service.TravelInfoServiceImpl;
import com.example.mytravellink.domain.url.entity.Url;
import com.example.mytravellink.domain.url.service.UrlServiceImpl;
import com.example.mytravellink.domain.job.service.JobStatusService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/travels")
@RequiredArgsConstructor
@Slf4j
public class TravelInfoController {

    private final TravelInfoServiceImpl travelInfoService;
    private final UrlServiceImpl urlService;
    private final PlaceServiceImpl placeService;
    private final GuideServiceImpl guideService;
    private final CourseServiceImpl courseService;
    private final ImageService imageService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JobStatusService jobStatusService;
    private final ObjectMapper objectMapper;

    @Value("${ai.server.url}")
    private String fastAPiUrl;

    /**
     * 여행정보 ID 기준 여행정보 및 URL정보 조회
     * @param travelId
     * @return ResponseEntity<TravelInfoResponse>
     */
    @GetMapping("/travelInfos/{travelId}")
    public ResponseEntity<TravelInfoUrlResponse> travelInfo(@PathVariable String travelId, @RequestHeader("Authorization") String token) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));

            if(!travelInfoService.isUser(travelId, userEmail)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");  // 커스텀 헤더에 에러 메시지 추가
                
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }


            TravelInfo travelInfo = travelInfoService.getTravelInfo(travelId);
            List<Url> urlList = urlService.findUrlByTravelInfoId(travelInfo);

            List<TravelInfoUrlResponse.Url> urlResponseList = urlList.stream()
                .map(url -> TravelInfoUrlResponse.Url.builder()
                    .urlId(url.getId())
                    .urlAddress(url.getUrl())
                    .title(url.getUrlTitle())
                    .author(url.getUrlAuthor())
                    .build())
                .collect(Collectors.toList());

            TravelInfoUrlResponse travelInfoUrlResponse = TravelInfoUrlResponse.builder()
                .success("success")
                .message("success")
                .travelInfoId(travelInfo.getId())
                .travelInfoTitle(travelInfo.getTitle())
                .travelDays(travelInfo.getTravelDays())
                .urlCnt(urlList.size())
                .urlList(urlResponseList)
                .build();

            return new ResponseEntity<>(travelInfoUrlResponse, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * URL ID 기준 장소 조회
     * @param urlId
     * @return ResponseEntity<TravelInfoPlaceResponse>
     */
    @GetMapping("/travelInfos/urls/{urlId}")
    public ResponseEntity<TravelInfoPlaceResponse> travelInfoUrl(@PathVariable String urlId, @RequestHeader("Authorization") String token) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            //사용자 확인
            boolean isUser = urlService.isUser(urlId, userEmail);
            if(!isUser){// 토큰 불일치 메세지 반환
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");  // 커스텀 헤더에 에러 메시지 추가
                
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }
            List<Place> urlPlaceList = urlService.findPlaceByUrlId(urlId);
            //이미지 URL 리다이렉션
            // List<Place> imageConvertPlaceList = imageService.redirectImageUrlPlace(urlPlaceList);

            List<TravelInfoPlaceResponse.Place> placeResponseList = urlPlaceList.stream()
                .map(place -> TravelInfoPlaceResponse.Place.builder()
                    .placeId(place.getId().toString())
                    .placeType(place.getType())
                    .placeName(place.getTitle())
                    .placeAddress(place.getAddress())
                    .placeImage(place.getImage())
                    .placeDescription(place.getDescription())
                    .intro(place.getIntro())
                    .latitude(place.getLatitude())
                    .longitude(place.getLongitude())
                    .build())
                .collect(Collectors.toList());


                TravelInfoPlaceResponse travelInfoPlaceResponse = TravelInfoPlaceResponse.builder()
                .success("success")
                .message("success")
                .content(placeResponseList)
                .build();

            return new ResponseEntity<>(travelInfoPlaceResponse, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 여행정보 ID 기준 장소 조회
     * @param travelId
     * @return ResponseEntity<TravelInfoPlaceResponse>
     */
    @GetMapping("/travelInfos/{travelId}/places")
    public ResponseEntity<TravelInfoPlaceResponse> travelInfoPlace(@PathVariable String travelId, @RequestHeader("Authorization") String token) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            if(!travelInfoService.isUser(travelId, userEmail)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");  // 커스텀 헤더에 에러 메시지 추가
                
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }
            log.info("Fetching places for travelId: {}", travelId);
            List<Place> placeList = travelInfoService.getTravelInfoPlace(travelId);

            // 빈 리스트인 경우에도 정상적인 응답을 반환
            List<Place> imageConvertPlaceList = imageService.redirectImageUrlPlace(placeList);
            List<TravelInfoPlaceResponse.Place> placeResponseList = imageConvertPlaceList.stream()
                .map(place -> TravelInfoPlaceResponse.Place.builder()
                    .placeId(place.getId().toString())
                    .placeType(place.getType())
                    .placeName(place.getTitle())
                    .placeAddress(place.getAddress())
                    .placeImage(place.getImage())
                    .placeDescription(place.getDescription())
                    .intro(place.getIntro())
                    .latitude(place.getLatitude())
                    .longitude(place.getLongitude())
                    .build())
                .collect(Collectors.toList());

            TravelInfoPlaceResponse travelInfoPlaceResponse = TravelInfoPlaceResponse.builder()
                .success("success")
                .message("success")
                .content(placeResponseList)
                .build();

            return new ResponseEntity<>(travelInfoPlaceResponse, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error fetching places for travelId: {} - {}", travelId, e.getMessage(), e);
            TravelInfoPlaceResponse errorResponse = TravelInfoPlaceResponse.builder()
                .success("error")
                .message(e.getMessage())
                .content(new ArrayList<>())
                .build();
            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 여행 정보 ID 기준 여행 정보 수정
     * 여행일, 여행제목 수정
     * @param travelInfoId
     * @return
     */
    @PutMapping("/travelInfos/{travelInfoId}")
    public ResponseEntity<String> updateTravelInfo(
        @PathVariable String travelInfoId, 
        @RequestBody TravelInfoUpdateTitleAndTravelDaysRequest travelInfoUpdateTitleAndTravelDaysRequest, 
        @RequestHeader("Authorization") String token
        ) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            if(!travelInfoService.isUser(travelInfoId, userEmail)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");  // 커스텀 헤더에 에러 메시지 추가
                
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }
            String travelInfoTitle = travelInfoUpdateTitleAndTravelDaysRequest.getTravelInfoTitle();
            Integer travelDays = travelInfoUpdateTitleAndTravelDaysRequest.getTravelDays();
            travelInfoService.updateTravelInfo(travelInfoId, travelInfoTitle, travelDays);
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }          
    }

    /**
     * 여행정보 ID 기준 여행 정보 삭제
     * @param travelInfoId
     * @return
     */
    @DeleteMapping("/travelInfos/{travelInfoId}")
    public ResponseEntity<String> deleteTravelInfo(
        @PathVariable String travelInfoId, 
        @RequestHeader("Authorization") String token
        ) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            if(!travelInfoService.isUser(travelInfoId, userEmail)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");  // 커스텀 헤더에 에러 메시지 추가
                
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }
            travelInfoService.deleteTravelInfo(travelInfoId);
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * 여행정보 ID 기준 AI 추천 장소
     * @param travelInfoId
     * @return ResponseEntity<TravelInfoPlaceResponse>
     */
    @GetMapping("travelInfos/{travelInfoId}/aiSelect/async")
    public ResponseEntity<String> aiSelectAsync(
        @PathVariable String travelInfoId, 
        @RequestHeader("Authorization") String token
    ) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            if(!travelInfoService.isUser(travelInfoId, userEmail)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }

            String jobId = UUID.randomUUID().toString();
            log.info("새로운 AI 추천 작업 시작. JobID: {}", jobId);
            
            // 비동기 작업 시작
            CompletableFuture.runAsync(() -> {
                try {
                    jobStatusService.setStatus(jobId, "Processing");

                    // 1. 여행 정보 조회
                    TravelInfo travelInfo = travelInfoService.getTravelInfo(travelInfoId);
                    Integer travelDays = travelInfo.getTravelDays();
                    
                    // 2. 여행 정보에 포함된 모든 장소 조회
                    List<Place> places = travelInfoService.getTravelInfoPlace(travelInfoId);
                    
                    if (places.isEmpty()) {
                        jobStatusService.setStatus(jobId, "Failed");
                        jobStatusService.setResult(jobId, "No places found for recommendation");
                        return;
                    }
                    
                    // 3. FastAPI AI 서비스 호출을 위한 요청 데이터 준비
                    Map<String, Object> requestBody = new HashMap<>();
                    requestBody.put("travelInfoId", travelInfoId);
                    requestBody.put("travelDays", travelDays);
                    requestBody.put("places", places.stream().map(place -> {
                        Map<String, Object> placeMap = new HashMap<>();
                        placeMap.put("placeId", place.getId().toString());
                        placeMap.put("placeType", place.getType() != null ? place.getType() : "unknown");
                        placeMap.put("placeName", place.getTitle());
                        placeMap.put("placeAddress", place.getAddress());
                        placeMap.put("placeImage", place.getImage());
                        placeMap.put("placeDescription", place.getDescription());
                        placeMap.put("intro", place.getIntro());
                        placeMap.put("latitude", place.getLatitude() != null ? place.getLatitude() : BigDecimal.ZERO);
                        placeMap.put("longitude", place.getLongitude() != null ? place.getLongitude() : BigDecimal.ZERO);
                        return placeMap;
                    }).filter(placeMap -> placeMap.get("placeType") != null).collect(Collectors.toList()));
                    
                    log.info("Sending request to AI service: {}", requestBody);
                    
                    // 4. FastAPI AI 서비스 호출
                    RestTemplate restTemplate = new RestTemplate();
                    String aiServiceUrl = fastAPiUrl + "/api/v1/ai/recommend/places";
                    ResponseEntity<Map> aiResponse = restTemplate.postForEntity(
                        aiServiceUrl,
                        requestBody,
                        Map.class
                    );
                    
                    // 5. AI 서비스 응답 처리
                    if (aiResponse.getStatusCode() == HttpStatus.OK) {
                        Map<String, Object> responseBody = aiResponse.getBody();
                        List<Map<String, Object>> recommendedPlaces = (List<Map<String, Object>>) responseBody.get("content");
                        
                        List<TravelInfoPlaceResponse.Place> placeResponseList = recommendedPlaces.stream()
                            .map(placeData -> {
                                // null 체크 추가
                                String placeId = String.valueOf(placeData.getOrDefault("placeId", ""));
                                String placeType = String.valueOf(placeData.getOrDefault("placeType", "unknown"));
                                String placeName = String.valueOf(placeData.getOrDefault("placeName", ""));
                                String placeAddress = String.valueOf(placeData.getOrDefault("placeAddress", ""));
                                String placeImage = String.valueOf(placeData.getOrDefault("placeImage", ""));
                                String placeDescription = String.valueOf(placeData.getOrDefault("placeDescription", ""));
                                String intro = String.valueOf(placeData.getOrDefault("intro", ""));
                                
                                BigDecimal latitude = new BigDecimal(String.valueOf(placeData.getOrDefault("latitude", "0.0")));
                                BigDecimal longitude = new BigDecimal(String.valueOf(placeData.getOrDefault("longitude", "0.0")));
                                
                                return TravelInfoPlaceResponse.Place.builder()
                                    .placeId(placeId)
                                    .placeType(placeType)
                                    .placeName(placeName)
                                    .placeAddress(placeAddress)
                                    .placeImage(placeImage)
                                    .placeDescription(placeDescription)
                                    .intro(intro)
                                    .latitude(latitude)
                                    .longitude(longitude)
                                    .build();
                            })
                            .collect(Collectors.toList());

                        TravelInfoPlaceResponse response = TravelInfoPlaceResponse.builder()
                            .success("success")
                            .message("Successfully recommended places")
                            .content(placeResponseList)
                            .build();

                        // 작업 완료 및 결과 저장
                        jobStatusService.setStatus(jobId, "Completed");
                        jobStatusService.setResult(jobId, objectMapper.writeValueAsString(response));
                        log.info("AI 추천 완료. JobID: {}", jobId);
                    } else {
                        throw new RuntimeException("AI service returned error: " + aiResponse.getStatusCode());
                    }
                } catch (Exception e) {
                    log.error("AI 추천 실패. JobID: {}", jobId, e);
                    jobStatusService.setStatus(jobId, "Failed");
                    jobStatusService.setResult(jobId, e.getMessage());
                }
            });

            return ResponseEntity.accepted().body(jobId);
        } catch (Exception e) {
            log.error("AI 추천 요청 처리 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 작업 상태 확인 엔드포인트
    @GetMapping("travelInfos/aiSelect/status/{jobId}")
    public ResponseEntity<TravelInfoPlaceResponse> getAiSelectStatus(@PathVariable String jobId) {
        try {
            String status = jobStatusService.getStatus(jobId);
            String result = jobStatusService.getResult(jobId);
            
            if ("Completed".equals(status)) {
                return ResponseEntity.ok(objectMapper.readValue(result, TravelInfoPlaceResponse.class));
            } else if ("Failed".equals(status)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TravelInfoPlaceResponse.builder()
                        .success("error")
                        .message(result)
                        .content(new ArrayList<>())
                        .build());
            } else {
                return ResponseEntity.ok(TravelInfoPlaceResponse.builder()
                    .success("processing")
                    .message("AI recommendation in progress")
                    .content(new ArrayList<>())
                    .build());
            }
        } catch (Exception e) {
            log.error("작업 상태 조회 실패. JobID: {}", jobId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(TravelInfoPlaceResponse.builder()
                    .success("error")
                    .message("Failed to get AI recommendation status")
                    .content(new ArrayList<>())
                    .build());
        }
    }

    /**
     * 사용자 email 기준 여행 정보 조회
     * @param CustomUserDetails
     * @return TravelInfoListResponse
     */
    @GetMapping("/travelInfos/list")
    public ResponseEntity<TravelInfoListResponse> travelInfoList(
        @RequestHeader("Authorization") String token
        ) {
        try{
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            // String userEmail = "user1@example.com";
            List<TravelInfo> travelInfoList = travelInfoService.getTravelInfoList(userEmail);
            List<TravelInfoListResponse.Infos> infosList = new ArrayList<>();
            for(TravelInfo travelInfo : travelInfoList){
                String imgUrl = placeService.getPlaceImage(travelInfo.getId());
                TravelInfoListResponse.Infos infos = TravelInfoListResponse.convertToInfos(travelInfo, imgUrl);
                infosList.add(infos);
            }
            TravelInfoListResponse travelInfoListResponse = TravelInfoListResponse.builder()
                .success("success")
                .message("success")
                .travelInfoList(infosList)
                .build();
            return new ResponseEntity<>(travelInfoListResponse, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 여행 정보 ID 기준 즐겨찾기 여부 수정
     * @param travelInfoId
     * @return ResponseEntity<TravelInfoListResponse>
     */
    @PutMapping("/travelInfos/{travelInfoId}/favorite")
    public ResponseEntity<String> updateFavorite(
        @PathVariable String travelInfoId, 
        @RequestBody BooleanRequest booleanRequst, 
        @RequestHeader("Authorization") String token
        ) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            if(!travelInfoService.isUser(travelInfoId, userEmail)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");  // 커스텀 헤더에 에러 메시지 추가
                
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }
            travelInfoService.updateFavorite(travelInfoId, booleanRequst.getIsTrue());
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 여행 정보 ID 기준 고정 여부 수정
     * @param placeSelectRequst
     * @return
     */
    @PutMapping("/travelInfos/{travelInfoId}/fixed")
    public ResponseEntity<String> updateFixed(
        @PathVariable String travelInfoId, 
        @RequestBody BooleanRequest booleanRequst, 
        @RequestHeader("Authorization") String token
        ) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            if(!travelInfoService.isUser(travelInfoId, userEmail)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");  // 커스텀 헤더에 에러 메시지 추가
                
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }
            travelInfoService.updateFixed(travelInfoId, booleanRequst.getIsTrue());
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 비동기 가이드북 생성 요청
     * @param PlaceSelectRequest
     * @return
     */
    @PostMapping("/guidebook/async")
    public ResponseEntity<String> createGuideAsync(
        @RequestHeader("Authorization") String token,
        @RequestBody PlaceSelectRequest placeSelectRequest
    ) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            if(!guideService.isUser(placeSelectRequest.getTravelInfoId(), userEmail)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }

            String jobId = UUID.randomUUID().toString();
            log.info("새로운 가이드북 생성 작업 시작. JobID: {}", jobId);
            
            // 비동기 작업 시작
            CompletableFuture.runAsync(() -> {
                try {
                    jobStatusService.setStatus(jobId, "Processing");

                    // 1. AI 코스 추천 데이터 형식 설정
                    AIGuideCourseRequest aiGuideCourseRequest = 
                        guideService.convertToAIGuideCourseRequest(placeSelectRequest);
                    log.info("AI 요청 데이터: {}", aiGuideCourseRequest);

                    // 2. AI 코스 추천 데이터 받기
                    List<AIGuideCourseResponse> aiGuideCourseResponses = 
                        placeService.getAIGuideCourse(aiGuideCourseRequest, placeSelectRequest.getTravelDays());
                    log.info("AI 응답 데이터: {}", aiGuideCourseResponses);

                    String title = "가이드북" + travelInfoService.getGuideCount(userEmail);

                    // 3. 가이드북 생성
                    Guide guide = Guide.builder()
                        .travelInfo(travelInfoService.getTravelInfo(placeSelectRequest.getTravelInfoId()))
                        .title(title)
                        .travelDays(placeSelectRequest.getTravelDays())
                        .courseCount(placeSelectRequest.getTravelDays())
                        .planTypes(placeSelectRequest.getTravelTaste())
                        .isFavorite(false)
                        .fixed(false)
                        .isDelete(false)
                        .build();

                    log.info("Created Guide: {}", guide);

                    if (aiGuideCourseResponses == null) {
                        throw new RuntimeException("AI 코스 추천 데이터가 null입니다.");
                    }

                    // 4. 가이드, 코스, 코스 장소 생성
                    String guideId = guideService.createGuideAndCourses(guide, aiGuideCourseResponses).get();
                    
                    // 작업 완료 및 결과 저장
                    jobStatusService.setStatus(jobId, "Completed");
                    jobStatusService.setResult(jobId, guideId);
                    log.info("가이드북 생성 완료. JobID: {}, GuideID: {}", jobId, guideId);

                } catch (Exception e) {
                    log.error("가이드북 생성 실패. JobID: {}", jobId, e);
                    jobStatusService.setStatus(jobId, "Failed");
                    jobStatusService.setResult(jobId, null);
                }
            });

            return ResponseEntity.accepted().body(jobId);

        } catch (Exception e) {
            log.error("가이드북 생성 요청 처리 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 가이드 ID 기준 가이드 조회
     * @param guideId
     * @return ResponseEntity<GuideBookResponse>
     */
    @GetMapping("/guidebooks/{guideId}")
    public ResponseEntity<GuideBookResponse> guideInfo(
        @PathVariable String guideId,
        @RequestHeader("Authorization") String token
        ) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            if(!guideService.isUser(guideId, userEmail)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");  // 커스텀 헤더에 에러 메시지 추가
                
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }
            Guide guide = guideService.getGuide(guideId);
            TravelInfo travelInfo = guideService.getTravelInfo(guide.getTravelInfo().getId());
            List<GuideBookResponse.CourseList> courseListResp = courseService.getCoursePlace(guideId);
            //이미지 URL 리다이렉션
            // List<GuideBookResponse.CourseList> imageUrlList = imageService.redirectImageUrl(courseListResp);

        GuideBookResponse guideBookResponse = GuideBookResponse.builder()
            .success("success")
            .message("success")
            .guideBookTitle(guide.getTitle())
            .travelInfoTitle(travelInfo.getTitle()) 
            .travelInfoId(travelInfo.getId())
            .courseCnt(guide.getCourseCount())
            // .courses(imageUrlList)
            .courses(courseListResp)
            .build();

            return new ResponseEntity<>(guideBookResponse, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 가이드 북 목록 조회
     * @param CustomUserDetails
     * @return ResponseEntity<GuideBookListResponse>
     */
    @Transactional(readOnly = true)
    @GetMapping("/guidebooks/list")
    public ResponseEntity<GuideBookListResponse> guideBookList(
        @RequestHeader("Authorization") String token
    ) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            List<Guide> guideList = guideService.getGuideList(userEmail);
            List<GuideBookListResponse.GuideList> guideListResponse = new ArrayList<>();
            for(Guide guide : guideList){
                List<String> authors = travelInfoService.getUrlAuthors(guide.getTravelInfo().getId());
                TravelInfo travelInfo = guide.getTravelInfo();
                GuideBookListResponse.GuideList tmpGuideList = GuideBookListResponse.GuideList.builder()
                    .id(guide.getId())
                    .title(guide.getTitle())
                    .travelInfoTitle(travelInfo.getTitle())
                    .createAt(guide.getCreateAt().toString())
                    .courseCount(guide.getCourseCount())
                    .isFavorite(guide.isFavorite())
                    .fixed(guide.isFixed())
                    .authors(authors)
                    .build();
                guideListResponse.add(tmpGuideList);
            }
            GuideBookListResponse guideBookListResponse = GuideBookListResponse.builder()
                .success("success")
                .message("success")
                .guideBooks(guideListResponse)
                .build();
            return new ResponseEntity<>(guideBookListResponse, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 가이드 북 제목 수정
     * @param guideId
     * @return ResponseEntity<String>
     */
    @PutMapping("/guidebooks/{guideId}/title")
    public ResponseEntity<String> updateGuideBookTitle(
        @PathVariable String guideId, 
        @RequestBody StringRequest request,
        @RequestHeader("Authorization") String token
        ) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            if(!guideService.isUser(guideId, userEmail)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");  // 커스텀 헤더에 에러 메시지 추가
                
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }
            guideService.updateGuideBookTitle(guideId, request.getValue());
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 가이드 북 즐겨찾기 여부 수정
     * @param guideId
     * @return ResponseEntity<String>
     */
    @PutMapping("/guidebooks/{guideId}/favorite")
    public ResponseEntity<String> updateGuideBookFavorite(
        @PathVariable String guideId, 
        @RequestBody BooleanRequest booleanRequest,
        @RequestHeader("Authorization") String token
        ) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            if(!guideService.isUser(guideId, userEmail)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");  // 커스텀 헤더에 에러 메시지 추가
                
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }
            guideService.updateGuideBookFavorite(guideId, booleanRequest.getIsTrue());
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * 가이드 북 고정 여부 수정
     * @param guideId
     * @return ResponseEntity<String>
     */
    @PutMapping("/guidebooks/{guideId}/fixed")
    public ResponseEntity<String> updateGuideBookFixed(
        @PathVariable String guideId, 
        @RequestBody BooleanRequest booleanRequest,
        @RequestHeader("Authorization") String token
        ) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            if(!guideService.isUser(guideId, userEmail)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");  // 커스텀 헤더에 에러 메시지 추가
                
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }
            guideService.updateGuideBookFixed(guideId, booleanRequest.getIsTrue());
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 가이드 북 삭제
     * @param guideId
     * @return ResponseEntity<String>
     */
    @DeleteMapping("/guidebooks/{guideId}")
    public ResponseEntity<String> deleteGuideBook(
        @PathVariable String guideId,
        @RequestHeader("Authorization") String token
        ) {
        try {
            String userEmail = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            if(!guideService.isUser(guideId, userEmail)){
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Error-Message", "토큰 불일치");  // 커스텀 헤더에 에러 메시지 추가
                
                return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .headers(headers)
                    .build();
            }
            guideService.deleteGuideBook(guideId);
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

