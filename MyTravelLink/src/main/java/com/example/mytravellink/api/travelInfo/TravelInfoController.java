//package com.example.mytravellink.api.travelInfo;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.PutMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.example.mytravellink.infrastructure.ai.Guide.dto.AIGuideCourseResponse;
//import com.example.mytravellink.api.travelInfo.dto.travel.GuideBookResponse;
//import com.example.mytravellink.api.travelInfo.dto.travel.PlaceSelectRequest;
//import com.example.mytravellink.api.travelInfo.dto.travel.TravelInfoPlaceResponse;
//import com.example.mytravellink.api.travelInfo.dto.travel.TravelInfoUpdateTitleAndTravelDaysRequest;
//import com.example.mytravellink.api.travelInfo.dto.travel.TravelInfoUrlResponse;
//import com.example.mytravellink.domain.travel.entity.Guide;
//import com.example.mytravellink.domain.travel.entity.Place;
//import com.example.mytravellink.domain.travel.entity.TravelInfo;
//import com.example.mytravellink.domain.travel.service.CourseServiceImpl;
//import com.example.mytravellink.domain.travel.service.GuideServiceImpl;
//import com.example.mytravellink.domain.travel.service.PlaceServiceImpl;
//import com.example.mytravellink.domain.travel.service.TravelInfoServiceImpl;
//import com.example.mytravellink.domain.url.entity.Url;
//import com.example.mytravellink.domain.url.service.UrlServiceImpl;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//
//
//@RestController
//@RequestMapping("/api/v1/travels")
//@RequiredArgsConstructor
//@Slf4j
//public class TravelInfoController {
//
//    private final TravelInfoServiceImpl travelInfoService;
//    private final UrlServiceImpl urlService;
//    private final PlaceServiceImpl placeService;
//    private final GuideServiceImpl guideService;
//    private final CourseServiceImpl courseService;
//    /**
//     * 여행정보 ID 기준 여행정보 및 URL정보 조회
//     * @param travelId
//     * @return ResponseEntity<TravelInfoResponse>
//     */
//    @GetMapping("/travelInfos/{travelId}")
//    public ResponseEntity<TravelInfoUrlResponse> travelInfo(@PathVariable String travelId) {
//        try {
//            TravelInfo travelInfo = travelInfoService.getTravelInfo(travelId);
//            List<Url> urlList = urlService.findUrlByTravelInfoId(travelInfo);
//
//            List<TravelInfoUrlResponse.Url> urlResponseList = urlList.stream()
//                .map(url -> TravelInfoUrlResponse.Url.builder()
//                    .urlId(url.getId())
//                    .urlAddress(url.getUrl())
//                    .title(url.getUrlTitle())
//                    .author(url.getUrlAuthor())
//                    .build())
//                .collect(Collectors.toList());
//
//            TravelInfoUrlResponse travelInfoUrlResponse = TravelInfoUrlResponse.builder()
//                .success("success")
//                .message("success")
//                .travelInfoId(travelInfo.getId())
//                .travelInfoTitle(travelInfo.getTitle())
//                .travelDays(travelInfo.getTravelDays())
//                .urlCnt(urlList.size())
//                .urlList(urlResponseList)
//                .build();
//
//            return new ResponseEntity<>(travelInfoUrlResponse, HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    /**
//     * URL ID 기준 장소 조회
//     * @param urlId
//     * @return ResponseEntity<TravelInfoPlaceResponse>
//     */
//    @GetMapping("/travelInfos/urls/{urlId}")
//    public ResponseEntity<TravelInfoPlaceResponse> travelInfoUrl(@PathVariable String urlId) {
//        try {
//            List<Place> urlPlaceList = urlService.findPlaceByUrlId(urlId);
//
//            List<TravelInfoPlaceResponse.Place> placeResponseList = urlPlaceList.stream()
//                .map(place -> TravelInfoPlaceResponse.Place.builder()
//                    .placeId(place.getId().toString())
//                    .placeType(place.getType())
//                    .placeName(place.getTitle())
//                    .placeAddress(place.getAddress())
//                    .placeImage(place.getImage())
//                    .placeDescription(place.getDescription())
//                    .intro(place.getIntro())
//                    .latitude(place.getLatitude())
//                    .longitude(place.getLongitude())
//                    .build())
//                .collect(Collectors.toList());
//
//
//                TravelInfoPlaceResponse travelInfoPlaceResponse = TravelInfoPlaceResponse.builder()
//                .success("success")
//                .message("success")
//                .content(placeResponseList)
//                .build();
//
//            return new ResponseEntity<>(travelInfoPlaceResponse, HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//
//    /**
//     * 여행정보 ID 기준 장소 조회
//     * @param travelId
//     * @return ResponseEntity<TravelInfoPlaceResponse>
//     */
//    @GetMapping("/travelInfos/{travelId}/places")
//    public ResponseEntity<TravelInfoPlaceResponse> travelInfoPlace(@PathVariable String travelId) {
//        try{
//            List<Place> placeList = travelInfoService.getTravelInfoPlace(travelId);
//
//            if (placeList.isEmpty()) {
//                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
//            }
//
//            List<TravelInfoPlaceResponse.Place> placeResponseList = placeList.stream()
//                .map(place -> TravelInfoPlaceResponse.Place.builder()
//                    .placeId(place.getId().toString())
//                    .placeType(place.getType())
//                    .placeName(place.getTitle())
//                    .placeAddress(place.getAddress())
//                    .placeImage(place.getImage())
//                    .placeDescription(place.getDescription())
//                    .intro(place.getIntro())
//                    .latitude(place.getLatitude())
//                    .longitude(place.getLongitude())
//                    .build())
//                .collect(Collectors.toList());
//
//            TravelInfoPlaceResponse travelInfoPlaceResponse = TravelInfoPlaceResponse.builder()
//                .success("success")
//                .message("success")
//                .content(placeResponseList)
//                .build();
//
//            return new ResponseEntity<>(travelInfoPlaceResponse, HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//
//    /**
//     * 여행 정보 ID 기준 여행 정보 수정
//     * 여행일, 여행제목 수정
//     * @param travelInfoId
//     * @return
//     */
//    @PutMapping("/travelInfos/{travelInfoId}")
//    public ResponseEntity<String> updateTravelInfo(@PathVariable String travelInfoId, @RequestBody TravelInfoUpdateTitleAndTravelDaysRequest travelInfoUpdateTitleAndTravelDaysRequest) {
//        try {
//            String travelInfoTitle = travelInfoUpdateTitleAndTravelDaysRequest.getTravelInfoTitle();
//            Integer travelDays = travelInfoUpdateTitleAndTravelDaysRequest.getTravelDays();
//            travelInfoService.updateTravelInfo(travelInfoId, travelInfoTitle, travelDays);
//            return new ResponseEntity<>("success", HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    /**
//     * 여행정보 ID 기준 AI 추천 장소
//     * @param travelInfoId
//     * @return ResponseEntity<TravelInfoPlaceResponse>
//     */
//    @GetMapping("travelInfos/{travelInfoId}/aiSelect")
//    public ResponseEntity<TravelInfoPlaceResponse> aiSelect(@PathVariable String travelInfoId) {
//        try {
//            Integer travelDays = travelInfoService.getTravelInfo(travelInfoId).getTravelDays();
//            // AI 장소 선택
//            try{
//                TravelInfoPlaceResponse aiSelectPlaceList = placeService.getAISelectPlace(travelInfoId, travelDays);
//                return new ResponseEntity<>(aiSelectPlaceList, HttpStatus.OK);
//            } catch (Exception e) {
//                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//
//
//    /**
//     * 가이드 북 생성
//     * @param PlaceSelectRequest
//     * @return
//     */
//    @PostMapping("/guides")
//    public ResponseEntity<String> createGuide(
//        @RequestBody PlaceSelectRequest placeSelectRequst) {
//        try {
//            Guide guide = Guide.builder()
//                .travelInfo(travelInfoService.getTravelInfo(placeSelectRequst.getTravelInfoId()))
//                .title(placeSelectRequst.getTitle())
//                .travelDays(placeSelectRequst.getTravelDays())
//                .courseCount(placeSelectRequst.getTravelDays())
//                .bookmark(false)
//                .fixed(false)
//                .isDelete(false)
//                .build();
//
//            // AI 가이드 코스 생성
//            try {
//                AIGuideCourseResponse aiGuideCourseResponse = placeService.getAIGuideCourse(placeSelectRequst.getPlaceIds(), placeSelectRequst.getTravelDays());
//
//                // 가이드, 코스, 코스 장소 생성(트랜잭션 처리)
//                guideService.createGuideAndCourses(guide, aiGuideCourseResponse);
//
//            } catch (Exception e) {
//                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//            }
//
//
//
//            return new ResponseEntity<>("success", HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    /**
//     * 가이드 ID 기준 가이드 조회
//     * @param guideId
//     * @return ResponseEntity<GuideBookResponse>
//     */
//    @GetMapping("/guidebooks/{guideId}")
//    public ResponseEntity<GuideBookResponse> guideInfo(@PathVariable String guideId) {
//        try {
//            Guide guide = guideService.getGuide(guideId);
//            TravelInfo travelInfo = guideService.getTravelInfo(guideId);
//            List<GuideBookResponse.CourseList> courseListResp = courseService.getCoursePlace(guideId);
//
//        GuideBookResponse guideBookResponse = GuideBookResponse.builder()
//            .success("success")
//            .message("success")
//            .guideBookTitle(guide.getTitle())
//            .travelInfoTitle(travelInfo.getTitle())
//            .travelInfoId(travelInfo.getId())
//            .courseCnt(guide.getCourseCount())
//            .courses(courseListResp)
//            .build();
//
//            return new ResponseEntity<>(guideBookResponse, HttpStatus.OK);
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//}
//
