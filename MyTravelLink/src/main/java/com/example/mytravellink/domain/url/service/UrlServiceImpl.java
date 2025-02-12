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
    private final YoutubeApiService youtubeApiService;

    @Value("${ai.server.url}")  // application.yml에서 설정
    private String fastAPiUrl;

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
                    .urlTitle(urlRequest.getUrls())
                    .urlAuthor(urlRequest.getUrls())
                    .url(urlRequest.getUrls())
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
        // YouTube URL인 경우 자막 체크 수행
        if (LinkDataResponse.determineType(urlStr).equals("youtube")) {
            String videoId = extractYoutubeVideoId(urlStr);
            if (!youtubeApiService.hasSubtitles(videoId)) {
                throw new RuntimeException("자막이 없는 영상은 선택할 수 없습니다.");
            }
        }

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