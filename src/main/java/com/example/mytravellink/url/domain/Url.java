package com.example.mytravellink.url.domain;

import com.example.mytravellink.domain.BaseTimeEntity;

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
    @Column(length = 64)  // SHA-256 해시값은 64자
    private String id;
    
    // Url -> UrlPlace (1:N)
    @OneToMany(mappedBy = "url")
    private List<UrlPlace> urlPlaces = new ArrayList<>();
    
    // Url -> UserUrl (1:N)
    @OneToMany(mappedBy = "url")
    private List<UserUrl> userUrls = new ArrayList<>();
    
    private String urlTitle;
    private String urlAuthor;
    
    @Column(nullable = false)
    private String url;
    
    
    @Builder
    public Url(String urlTitle, String urlAuthor, String url) {
        this.urlTitle = urlTitle;
        this.urlAuthor = urlAuthor;
        this.url = url;
        this.id = generateHashFromUrl(Arrays.asList(url));
    }
    
    private String generateHashFromUrl(List<String> urls) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            StringBuilder combinedUrls = new StringBuilder();
            
            // URL 리스트를 알파벳 순으로 정렬
            List<String> sortedUrls = new ArrayList<>(urls);
            Collections.sort(sortedUrls);
            
            // 정렬된 URL들을 하나의 문자열로 결합
            for (String url : sortedUrls) {
                combinedUrls.append(url);
            }
            
            byte[] hash = digest.digest(combinedUrls.toString().getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("URL 해시 생성 중 오류 발생", e);
        }
    }
} 