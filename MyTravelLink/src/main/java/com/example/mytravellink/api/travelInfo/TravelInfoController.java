package com.example.mytravellink.api.travelInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

import com.example.mytravellink.infrastructure.ai.Guide.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import com.example.mytravellink.auth.handler.JwtTokenProvider;


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
import com.example.mytravellink.domain.job.service.JobStatusService;
import com.example.mytravellink.domain.job.service.JobStatusService.JobStatus;
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

    /**
     * 여행정보 ID 기준 여행정보 및 URL정보 조회
     * @param travelId
     * @return ResponseEntity<TravelInfoResponse>
     */
    @GetMapping("/travelInfos/{travelId}")
    public ResponseEntity<TravelInfoUrlResponse> travelInfo(@PathVariable String travelId) {
        try {
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
    public ResponseEntity<TravelInfoPlaceResponse> travelInfoUrl(@PathVariable String urlId) {
        try {
            List<Place> urlPlaceList = urlService.findPlaceByUrlId(urlId);
            //이미지 URL 리다이렉션
            List<Place> imageConvertPlaceList = imageService.redirectImageUrlPlace(urlPlaceList);

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
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * 여행정보 ID 기준 장소 조회
     * @param travelId
     * @return ResponseEntity<TravelInfoPlaceResponse>
     */
    @GetMapping("/travelInfos/{travelId}/places")
    public ResponseEntity<TravelInfoPlaceResponse> travelInfoPlace(@PathVariable String travelId) {
        try {
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
    public ResponseEntity<String> updateTravelInfo(@PathVariable String travelInfoId, @RequestBody TravelInfoUpdateTitleAndTravelDaysRequest travelInfoUpdateTitleAndTravelDaysRequest) {
        try {
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
    public ResponseEntity<String> deleteTravelInfo(@PathVariable StringRequest request) {
        try {
            travelInfoService.deleteTravelInfo(request.getValue());
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
    @GetMapping("travelInfos/{travelInfoId}/aiSelect")
    public ResponseEntity<TravelInfoPlaceResponse> aiSelect(@PathVariable String travelInfoId) {
        try {
            // 1. 여행 정보 조회
            TravelInfo travelInfo = travelInfoService.getTravelInfo(travelInfoId);
            Integer travelDays = travelInfo.getTravelDays();
            
            // 2. 여행 정보에 포함된 모든 장소 조회
            List<Place> places = travelInfoService.getTravelInfoPlace(travelInfoId);
            
            if (places.isEmpty()) {
                return new ResponseEntity<>(
                    TravelInfoPlaceResponse.builder()
                        .success("error")
                        .message("No places found for recommendation")
                        .content(new ArrayList<>())
                        .build(),
                    HttpStatus.BAD_REQUEST
                );
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
            String aiServiceUrl = "http://221.148.97.237:28001/api/v1/ai/recommend/places";
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
                        
                        // 숫자 데이터 안전하게 변환
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
                
                return new ResponseEntity<>(
                    TravelInfoPlaceResponse.builder()
                        .success("success")
                        .message("Successfully recommended places")
                        .content(placeResponseList)
                        .build(),
                    HttpStatus.OK
                );
            } else {
                throw new RuntimeException("AI service returned error: " + aiResponse.getStatusCode());
            }
        } catch (Exception e) {
            log.error("AI 추천 실패: {}", e.getMessage(), e);
            return new ResponseEntity<>(
                TravelInfoPlaceResponse.builder()
                    .success("error")
                    .message("Failed to get AI recommendations: " + e.getMessage())
                    .content(new ArrayList<>())
                    .build(),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * 사용자 email 기준 여행 정보 조회
     * @param CustomUserDetails
     * @return TravelInfoListResponse
     */
    @GetMapping("/travelInfos/list")
    public ResponseEntity<TravelInfoListResponse> travelInfoList(@RequestHeader("Authorization") String token) {
        log.info("========== 여행 정보 목록 조회 시작 ==========");
        log.debug("요청 헤더 - Authorization: {}", token);

        try {
            // 1. JWT 토큰 처리
            log.info("[1단계] JWT 토큰 처리 시작");
            String email;
            try {
                email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
                log.info("✓ 토큰 처리 성공 - 이메일: {}", email);
            } catch (Exception e) {
                log.error("❌ 토큰 처리 실패: {}", e.getMessage());
                log.error("상세 에러: ", e);
                return new ResponseEntity<>(
                    TravelInfoListResponse.builder()
                        .success("error")
                        .message("토큰 처리 실패: " + e.getMessage())
                        .build(),
                    HttpStatus.UNAUTHORIZED
                );
            }

            // 2. 여행 정보 목록 조회
            log.info("[2단계] 여행 정보 목록 조회 시작");
            List<TravelInfo> travelInfoList;
            try {
                travelInfoList = travelInfoService.getTravelInfoList(email);
                log.info("✓ 여행 정보 조회 성공 - 총 {}건", travelInfoList.size());
                log.debug("조회된 여행 정보: {}", travelInfoList);
            } catch (Exception e) {
                log.error("❌ 여행 정보 조회 실패: {}", e.getMessage());
                log.error("상세 에러: ", e);
                return new ResponseEntity<>(
                    TravelInfoListResponse.builder()
                        .success("error")
                        .message("여행 정보 조회 실패: " + e.getMessage())
                        .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

            // 3. 이미지 처리 및 응답 데이터 구성
            log.info("[3단계] 이미지 처리 및 응답 데이터 구성 시작");
            List<TravelInfoListResponse.Infos> infosList = new ArrayList<>();
            for (TravelInfo travelInfo : travelInfoList) {
                try {
                    log.debug("여행 정보 처리 중 - ID: {}", travelInfo.getId());
                    
                    // 3.1 이미지 URL 조회
                    log.debug("이미지 URL 조회 시작 - travelInfo ID: {}", travelInfo.getId());
                    String imgUrl = placeService.getPlaceImage(travelInfo.getId());
                    log.debug("조회된 이미지 URL: {}", imgUrl);

                    // 3.2 이미지 URL 리다이렉션 처리
                    log.debug("이미지 URL 리다이렉션 처리 시작");
                    String redirectImgUrl = imageService.redirectImageUrl(imgUrl);
                    log.debug("리다이렉션된 이미지 URL: {}", redirectImgUrl);

                    // 3.3 응답 객체 변환
                    log.debug("응답 객체 변환 시작");
                    TravelInfoListResponse.Infos infos = TravelInfoListResponse.convertToInfos(travelInfo, redirectImgUrl);
                    infosList.add(infos);
                    log.info("✓ 여행 정보 처리 완료 - ID: {}", travelInfo.getId());

                } catch (Exception e) {
                    log.error("❌ 여행 정보 처리 실패 - ID: {}: {}", travelInfo.getId(), e.getMessage());
                    log.error("상세 에러: ", e);
                    
                    // 에러 발생해도 기본 이미지로 계속 진행
                    try {
                        log.info("기본 이미지로 대체 처리 시도");
                        TravelInfoListResponse.Infos infos = TravelInfoListResponse.convertToInfos(
                            travelInfo, 
                            "https://default-image-url.com/placeholder.jpg"
                        );
                        infosList.add(infos);
                        log.info("✓ 기본 이미지 대체 처리 성공");
                    } catch (Exception conversionError) {
                        log.error("❌ 기본 이미지 대체 처리 실패: {}", conversionError.getMessage());
                        log.error("상세 에러: ", conversionError);
                    }
                }
            }

            // 4. 최종 응답 생성
            log.info("[4단계] 최종 응답 생성 시작");
            try {
                TravelInfoListResponse response = TravelInfoListResponse.builder()
                    .success("success")
                    .message("success")
                    .travelInfoList(infosList)
                    .build();
                
                log.info("✓ 최종 응답 생성 완료");
                log.debug("응답 데이터: {}", response);
                log.info("========== 여행 정보 목록 조회 완료 ==========");
                
                return new ResponseEntity<>(response, HttpStatus.OK);

            } catch (Exception e) {
                log.error("❌ 최종 응답 생성 실패: {}", e.getMessage());
                log.error("상세 에러: ", e);
                return new ResponseEntity<>(
                    TravelInfoListResponse.builder()
                        .success("error")
                        .message("응답 생성 실패: " + e.getMessage())
                        .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
                );
            }

        } catch (Exception e) {
            log.error("❌ 예상치 못한 오류 발생: {}", e.getMessage());
            log.error("상세 에러: ", e);
            return new ResponseEntity<>(
                TravelInfoListResponse.builder()
                    .success("error")
                    .message("서버 내부 오류: " + e.getMessage())
                    .build(),
                HttpStatus.INTERNAL_SERVER_ERROR
            );
        } finally {
            log.info("========== 여행 정보 목록 조회 종료 ==========\n");
        }
    }

    /**
     * 여행 정보 ID 기준 즐겨찾기 여부 수정
     * @param travelInfoId
     * @return ResponseEntity<TravelInfoListResponse>
     */
    @PutMapping("/travelInfos/{travelInfoId}/favorite")
    public ResponseEntity<String> updateFavorite(@PathVariable String travelInfoId, @RequestBody BooleanRequest booleanRequst) {
        try {
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
    public ResponseEntity<String> updateFixed(@PathVariable String travelInfoId, @RequestBody BooleanRequest booleanRequst) {
        try {
            travelInfoService.updateFixed(travelInfoId, booleanRequst.getIsTrue());
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    

    /*
     * 가이드 북 비동기 생성성
     */
    @PostMapping("/guidebook/async")
    public ResponseEntity<Map<String, String>> processUrlAsync(
            @RequestHeader("Authorization") String token,
            @RequestBody PlaceSelectRequest placeSelectRequest) {
        
        String jobId = UUID.randomUUID().toString();
        
        try {
            String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            
            if (!travelInfoService.isUser(placeSelectRequest.getTravelInfoId(), email)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            jobStatusService.setJobStatus(jobId, "PENDING", null);
            guideService.createGuideAsync(placeSelectRequest, jobId, email);
            
            Map<String, String> response = new HashMap<>();
            response.put("jobId", jobId);
            response.put("status", "ACCEPTED");
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            log.error("비동기 처리 시작 실패", e);
            jobStatusService.setJobStatus(jobId, "FAILED", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /*
     * 가이드 북 비동기 생성 상태 조회
     * @param jobId
     * @return ResponseEntity<Map<String, Object>>
     */
    @GetMapping("/guidebook/status/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String jobId) {
        JobStatus status = jobStatusService.getJobStatus(jobId);
        Map<String, Object> response = new HashMap<>();
        
        response.put("status", status.getStatus());
        
        if ("COMPLETED".equals(status.getStatus())) {
            response.put("guideId", status.getResult());  // result에 guideId가 저장되어 있음
        } else if ("FAILED".equals(status.getStatus())) {
            response.put("error", status.getResult());    // 실패 시 에러 메시지
        }
        return ResponseEntity.ok(response);
    }



    /**
     * 가이드 북 생성
     * @param PlaceSelectRequest
     * @return
     */
    @PostMapping("/guidebook")
    public ResponseEntity<StringResponse> createGuide(
        // @AuthenticationPrincipal CustomUserDetails user,
        @RequestBody PlaceSelectRequest placeSelectRequest, @RequestHeader("Authorization") String token) {
        try {
            //TO-DO: String email = user.getEmail();
            String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            // 1. AI 코스 추천에 요청할 데이터 형식 설정
            AIGuideCourseRequest aiGuideCourseRequest = guideService.convertToAIGuideCourseRequest(placeSelectRequest);

            System.out.println("AI 요청 데이터: " + aiGuideCourseRequest);

            // 2. AI 코스 추천 데이터 받기
            List<AIGuideCourseResponse> aiGuideCourseResponses = placeService.getAIGuideCourse(aiGuideCourseRequest,placeSelectRequest.getTravelDays());

            System.out.println("AI 응답 데이터: " + aiGuideCourseResponses);

            String title = "가이드북" + travelInfoService.getGuideCount(email);

            // 3. 가이드북 생성
            Guide guide = Guide.builder()
                    .travelInfo(travelInfoService.getTravelInfo(placeSelectRequest.getTravelInfoId()))
                    .title(title)
                    .travelDays(placeSelectRequest.getTravelDays())
                    .courseCount(placeSelectRequest.getTravelDays())
                    .planTypes(placeSelectRequest.getTravelTaste()) // 타입별 수정해야됨
                    .isFavorite(false)
                    .fixed(false)
                    .isDelete(false)
                    .build();

            // Guide 객체 확인
            System.out.println("Created Guide: " + guide);

            // 가이드, 코스, 코스 장소 생성(트랜잭션 처리)
            if (aiGuideCourseResponses == null) {
                System.out.println("aiGuideCourseResponses is null");
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            } else {
                System.out.println("AI 코스 데이터 전달 전 확인: " + aiGuideCourseResponses);
            }
            String guideId = guideService.createGuideAndCourses(guide, aiGuideCourseResponses);
            return new ResponseEntity<>(StringResponse.builder()
                .success("success")
                .message("success")
                .value(guideId)
                .build(), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 가이드 ID 기준 가이드 조회
     * @param guideId
     * @return ResponseEntity<GuideBookResponse>
     */
    @GetMapping("/guidebooks/{guideId}")
    public ResponseEntity<GuideBookResponse> guideInfo(@PathVariable String guideId) {
        try {
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
        // @AuthenticationPrincipal CustomUserDetails user 
        @RequestHeader("Authorization") String token
    ) {
        try {
            //TO-DO: String userEmail = user.getEmail();
            String email = jwtTokenProvider.getEmailFromToken(token.replace("Bearer ", ""));
            List<Guide> guideList = guideService.getGuideList(email);
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
    public ResponseEntity<String> updateGuideBookTitle(@PathVariable String guideId, @RequestBody StringRequest request) {
        try {
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
    public ResponseEntity<String> updateGuideBookFavorite(@PathVariable String guideId, @RequestBody BooleanRequest booleanRequest) {
        try {
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
    public ResponseEntity<String> updateGuideBookFixed(@PathVariable String guideId, @RequestBody BooleanRequest booleanRequest) {
        try {
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
    public ResponseEntity<String> deleteGuideBook(@PathVariable String guideId) {
        try {
            guideService.deleteGuideBook(guideId);
            return new ResponseEntity<>("success", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

