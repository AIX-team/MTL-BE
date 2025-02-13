package com.example.mytravellink.domain.url.service;

import com.example.mytravellink.api.user.dto.LinkDataResponse;
import com.example.mytravellink.api.url.dto.*;
import com.example.mytravellink.domain.travel.entity.Place;
import com.example.mytravellink.domain.travel.entity.TravelInfo;
import com.example.mytravellink.domain.travel.repository.PlaceRepository;
import com.example.mytravellink.domain.travel.repository.TravelInfoRepository;
import com.example.mytravellink.domain.travel.entity.TravelInfoUrl;
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
import java.net.URI;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final PlaceRepository placeRepository;
    private final RestTemplate restTemplate;
    private final UrlRepository urlRepository;
    private final UrlPlaceRepository urlPlaceRepository;
    private final TravelInfoUrlRepository travelInfoUrlRepository;
    private final TravelInfoRepository travelInfoRepository;
    private final UsersRepository usersRepository;
    private final UsersUrlRepository usersUrlRepository;

    @Value("${ai.server.url}")  // application.yml에서 설정
    private String fastAPiUrl;

    @Override
    @Transactional
    public UrlResponse processUrl(UrlRequest urlRequest) {
        // 1. FASTAPI에 요청하여 데이터 가져오기
        String requestUrl = fastAPiUrl + "/api/v1/contentanalysis";
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("urls", urlRequest.getUrls());
    
 

        ResponseEntity<UrlResponse> response = restTemplate.postForEntity(
                requestUrl, requestBody, UrlResponse.class
        );
        UrlResponse urlResponse = response.getBody();

        if (urlResponse != null) {
            // 2. URL 고유 ID 생성
            for(String singleUrl : urlRequest.getUrls()) {
                String urlId = generateUrlId(singleUrl);
                // 3. URL 테이블에서 존재하는지 확인 (새 엔티티 생성 시 빌더에서 id() 호출 없이 생성)
                Url urlEntity = urlRepository.findById(urlId).orElseGet(() -> {
                    Url newUrl = Url.builder()
                            .urlTitle(singleUrl)
                            .urlAuthor(singleUrl)
                            .url(singleUrl)
                            .build();
                    // 내부 생성자에서 generateHashFromUrl을 사용하므로 생성된 id와 일치함
                    return urlRepository.save(newUrl);
                });

                // 4. 사용자 정보 조회
                Users user = usersRepository.findByEmail(urlRequest.getEmail())
                             .orElseThrow(() -> new RuntimeException("User not found"));

                // 5. user_url 매핑 존재 여부 및 분석 완료 상태(is_use==0) 체크
                UsersUrlId mappingId = UsersUrlId.builder()
                                    .email(user.getEmail())
                                    .urlId(urlId)
                                    .build();
                Optional<UsersUrl> mappingOpt = usersUrlRepository.findById(mappingId);
                boolean needAnalysis = false;
                if (mappingOpt.isPresent()) {
                    UsersUrl mapping = mappingOpt.get();
                    if (mapping.isUse()) {
                        needAnalysis = true;
                    }
                } else {
                    needAnalysis = true;
                }

                // 6. 필요 시 분석 처리 및 관련 매핑 업데이트/생성
                if (needAnalysis) {
                    // FASTAPI의 분석 결과로부터 각 Place 정보를 처리
                    for (PlaceInfo placeInfo : urlResponse.getPlaceDetails()) {
                        Place place = placeRepository.findByTitle(placeInfo.getName())
                                .orElseGet(() -> {
                                    String openHours = Optional.ofNullable(placeInfo.getOpen_hours())
                                            .filter(list -> !list.isEmpty() && list.stream().anyMatch(str -> !str.isBlank()))
                                            .map(Object::toString)
                                            .orElse(null);
                                    Place newPlace = Place.builder()
                                            .title(placeInfo.getName())
                                            .description(placeInfo.getDescription())
                                            .address(placeInfo.getFormattedAddress())
                                            .image(placeInfo.getPhotos() != null ? placeInfo.getPhotos().toString() : null)
                                            .phone(placeInfo.getPhone())
                                            .website(placeInfo.getWebsite())
                                            .rating(placeInfo.getRating())
                                            .openHours(openHours)
                                            .build();
                                    return placeRepository.save(newPlace);
                                });

                        Optional<UrlPlace> existingUrlPlace = urlPlaceRepository.findByUrlAndPlace(urlEntity, place);
                        if (!existingUrlPlace.isPresent()) {
                            UrlPlace urlPlace = UrlPlace.builder()
                                    .url(urlEntity)
                                    .place(place)
                                    .build();
                            urlPlaceRepository.save(urlPlace);
                        }

                        if (urlRequest.getTravelInfoId() != null && !urlRequest.getTravelInfoId().isEmpty()) {
                            TravelInfo travelInfo = travelInfoRepository.findById(urlRequest.getTravelInfoId())
                                    .orElseThrow(() -> new RuntimeException("TravelInfo not found"));
                            Optional<TravelInfoUrl> existingTravelInfoUrl = travelInfoUrlRepository.findByTravelInfoAndUrl(travelInfo, urlEntity);
                            if (!existingTravelInfoUrl.isPresent()) {
                                TravelInfoUrl travelInfoUrl = TravelInfoUrl.builder()
                                        .travelInfo(travelInfo)
                                        .url(urlEntity)
                                        .build();
                                travelInfoUrlRepository.save(travelInfoUrl);
                            }
                        }
                    }

                    // 분석 완료 후 user_url 매핑 상태를 업데이트 (0: 완료)
                    if (mappingOpt.isPresent()) {
                        UsersUrl mapping = mappingOpt.get();
                        mapping.setUse(false);
                        usersUrlRepository.save(mapping);
                    } else {
                        UsersUrl newMapping = UsersUrl.builder()
                                .id(mappingId)
                                .user(user)
                                .url(urlEntity)
                                .isUse(false)
                                .build();
                        usersUrlRepository.save(newMapping);
                    }
                } else {
                    if (urlRequest.getTravelInfoId() != null && !urlRequest.getTravelInfoId().isEmpty()) {
                        TravelInfo travelInfo = travelInfoRepository.findById(urlRequest.getTravelInfoId())
                                .orElseThrow(() -> new RuntimeException("TravelInfo not found"));
                        Optional<TravelInfoUrl> existingTravelInfoUrl = travelInfoUrlRepository.findByTravelInfoAndUrl(travelInfo, urlEntity);
                        if (!existingTravelInfoUrl.isPresent()) {
                            TravelInfoUrl travelInfoUrl = TravelInfoUrl.builder()
                                    .travelInfo(travelInfo)
                                    .url(urlEntity)
                                    .build();
                            travelInfoUrlRepository.save(travelInfoUrl);
                        }
                    }
                }
            }
        }
        return urlResponse;
    }

    @Override
    public List<Url> findUrlByTravelInfoId(TravelInfo travelInfo) {
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

    @Override
    public List<Place> findPlaceByUrlId(String urlId) {

        // 1. URL에 연결된 UrlPlace 리스트 조회
        List<UrlPlace> urlPlaces = urlPlaceRepository.findByUrl_Id(urlId);

        // 2. UrlPlace 에서 place 리스트 추출 후 반환
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
        travelInfoUrlRepository.save(travelInfoUrl);
    }

    /**
     * 사용자 요청으로 URL을 저장하는 메서드.
     * URL이 존재하지 않으면 Url 테이블에 저장하고, 
     * 그리고 사용자와 URL의 관계를 user_url 테이블에 저장합니다.
     */
    @Override
    @Transactional
    public void saveUserUrl(String email, UserUrlRequest request) {
        String urlStr = request.getUrl();
        

        // Url 테이블에서 기존 URL 엔티티 조회 또는 생성
        Url urlEntity = urlRepository.findById(generateUrlId(urlStr)).orElseGet(() -> {
            Url newUrl = Url.builder()
                    .url(urlStr)
                    .urlTitle(request.getTitle())
                    .urlAuthor(request.getAuthor())
                    .build();
            return urlRepository.save(newUrl);
        });
        
        // 사용자 엔티티 조회 후 매핑 ID 생성
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

    /**
     * 사용자 요청으로 URL을 삭제하는 메서드.
     * 해당 URL이 존재하면 삭제합니다.
     */
    @Override
    @Transactional
    public void deleteUserUrl(String email, String urlId) {
        if (urlRepository.existsById(urlId)) {
            urlRepository.deleteById(urlId);
        }
    }

    /**
     * URL 문자열을 입력받아 SHA-512 해시(16진수 문자열 128자리)를 생성하는 헬퍼 메서드
     */
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
        // 저장 시 사용했던 SHA-512 해시 생성 로직을 사용하여 URL ID 생성
        String id = generateUrlId(url);
        // user_url 매핑 삭제
        UsersUrlId mappingId = UsersUrlId.builder()
                            .email(email)
                            .urlId(id)
                            .build();
        if (usersUrlRepository.existsById(mappingId)) {
            usersUrlRepository.deleteById(mappingId);
        }
        // url 테이블의 URL 삭제
        if (urlRepository.existsById(id)) {
            urlRepository.deleteById(id);
        }
    }

    /**
     * 유튜브 URL에서 videoId를 추출하는 헬퍼 메서드
     */
    private String extractYoutubeVideoId(String url) {
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            if(query != null) {
                String[] params = query.split("&");
                for (String param : params) {
                    String[] keyValue = param.split("=");
                    if (keyValue[0].equals("v") && keyValue.length > 1) {
                        return keyValue[1];
                    }
                }
            }
        } catch (Exception e) {
            // 추출 실패 시 빈 문자열 반환
        }
        return "";
    }
}