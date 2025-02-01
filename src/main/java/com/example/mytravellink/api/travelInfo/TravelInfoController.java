package com.example.mytravellink.api.travelInfo;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.mytravellink.api.travelInfo.dto.TravelInfoPlaceResponse;
import com.example.mytravellink.api.travelInfo.dto.TravelInfoUrlResponse;
import com.example.mytravellink.travel.domain.Place;
import com.example.mytravellink.travel.domain.TravelInfo;
import com.example.mytravellink.travel.service.TravelInfoServiceImpl;
import com.example.mytravellink.url.domain.Url;
import com.example.mytravellink.url.service.UrlServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/travel")
@RequiredArgsConstructor
@Slf4j
public class TravelInfoController {

    private final TravelInfoServiceImpl travelInfoService;
    private final UrlServiceImpl urlService;


    /**
     * 여행정보 ID 기준 여행정보 및 URL정보 조회
     * @param travelId
     * @return ResponseEntity<TravelInfoResponse>
     */
    @GetMapping("/travelInfo/{travelId}/info")
    public ResponseEntity<TravelInfoUrlResponse> travelInfo(@PathVariable String travelId) {
        TravelInfo travelInfo = travelInfoService.getTravelInfo(travelId);
        List<Url> urlList = urlService.findUrlByTravelId(travelId);

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
    }

    /**
     * URL ID 기준 장소 조회
     * @param urlId
     * @return ResponseEntity<TravelInfoPlaceResponse>
     */
    @GetMapping("/travelInfo/url/{urlId}")
    public ResponseEntity<TravelInfoPlaceResponse> travelInfoUrl(@PathVariable String urlId) {
        List<Place> urlPlaceList = urlService.findPlaceByUrlId(urlId);

        List<TravelInfoPlaceResponse.Place> placeResponseList = urlPlaceList.stream()
            .map(place -> TravelInfoPlaceResponse.Place.builder()
                .placeId(place.getId())
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
    }


    /**
     * 여행정보 ID 기준 장소 조회
     * @param travelId
     * @return ResponseEntity<TravelInfoPlaceResponse>
     */
    @GetMapping("/travelInfo/{travelId}/place")
    public ResponseEntity<TravelInfoPlaceResponse> travelInfoPlace(@PathVariable String travelId) {

        List<Place> placeList = travelInfoService.getTravelInfoPlace(travelId);

        if (placeList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        List<TravelInfoPlaceResponse.Place> placeResponseList = placeList.stream()
            .map(place -> TravelInfoPlaceResponse.Place.builder()
                .placeId(place.getId())
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
    }
}
