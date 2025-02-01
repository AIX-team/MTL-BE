package com.example.mytravellink.url.domain;

import com.example.mytravellink.domain.BaseTimeEntity;
import com.example.mytravellink.travel.domain.TravelInfoUrl;
import com.example.mytravellink.users.domain.UsersUrl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;

/**
 * URL (Url) 엔티티
 * 외부 URL 정보를 저장합니다.
 * User, Place와 다대다 관계를 가지며, 각각 중간 테이블을 통해 연결됩니다.
 */
@Entity
@Table(name = "url")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Url extends BaseTimeEntity {
    
    @Id
    @Column(length = 128)  // SHA-512 해시값은 128자
    private String id;
    
    // Url -> UrlPlace (1:N)
    @OneToMany(mappedBy = "url")
    private final List<UrlPlace> urlPlaces = new ArrayList<>();
    
    // Url -> UserUrl (1:N)
    @OneToMany(mappedBy = "url")
    private final List<UsersUrl> userUrls = new ArrayList<>();
    
    // Url -> TravelInfoUrl (1:N)
    @OneToMany(mappedBy = "url")
    private List<TravelInfoUrl> travelInfoUrlList = new ArrayList<>();
    
    
    @Column(nullable = false)
    private String urlTitle;

    @Column(name = "url_author")
    private String urlAuthor;
    
    @Column(nullable = false)
    private String url;

    @Builder
    public Url(String urlTitle, String urlAuthor, String url) {
        this.id = generateHashFromUrl(Arrays.asList(url));
        this.urlTitle = urlTitle;
        this.urlAuthor = urlAuthor;
        this.url = url;
    }
    
    private String generateHashFromUrl(List<String> urls) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            StringBuilder combinedUrls = new StringBuilder();
            
            // URL 리스트를 알파벳 순으로 정렬
            List<String> sortedUrls = new ArrayList<>(urls);
            Collections.sort(sortedUrls);
            
            // 정렬된 URL들을 하나의 문자열로 결합
            for (String url : sortedUrls) {
                combinedUrls.append(url);
            }
            
            byte[] hash = digest.digest(combinedUrls.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해시 알고리즘 생성 중 오류 발생", e);
        }
    }

    public List<UrlPlace> getUrlPlaces() {
        return Collections.unmodifiableList(urlPlaces);
    }
    public List<UsersUrl> getUserUrls() {
        return Collections.unmodifiableList(userUrls);
    }

    public void addUrlPlace(UrlPlace urlPlace) {
        if (urlPlace == null) {
            throw new IllegalArgumentException("urlPlace cannot be null");
        }
        this.urlPlaces.add(urlPlace);
        urlPlace.setUrl(this);
    }

    public void addUserUrl(UsersUrl userUrl) {
        if (userUrl == null) {
            throw new IllegalArgumentException("userUrl cannot be null");
        }
        this.userUrls.add(userUrl);
        userUrl.setUrl(this);
    }
} 