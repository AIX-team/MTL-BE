package com.example.mytravellink.travel.domain;

import com.example.mytravellink.domain.BaseTimeEntity;
import com.example.mytravellink.url.domain.UrlPlace;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import java.util.ArrayList;
import java.util.List;

/**
 * 장소 (Place) 엔티티
 * 여행지나 관광 명소와 같은 장소 정보를 저장합니다.
 * TravelInfo, Course, Url과 다대다 관계를 가지며, 각각 중간 테이블을 통해 연결됩니다.
 */
@Entity
@Table(name = "place")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Place extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    private String id;
    
    // Place -> TravelInfoPlace (1:N)
    @OneToMany(mappedBy = "place")
    private List<TravelInfoPlace> travelInfoPlaces = new ArrayList<>();

    // Place -> CoursePlace (1:N)
    @OneToMany(mappedBy = "place")
    private List<CoursePlace> coursePlaces = new ArrayList<>();

    // Place -> UrlPlace (1:N)
    @OneToMany(mappedBy = "place")
    private List<UrlPlace> urlPlaces = new ArrayList<>();

    private String address;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String type;
    private String image;
    private Double latitude;
    private Double longitude;

    @Builder
    public Place(String address, String title, String description, String type,
                String image, Double latitude, Double longitude) {
        this.address = address;
        this.title = title;
        this.description = description;
        this.type = type;
        this.image = image;
        this.latitude = latitude;
        this.longitude = longitude;
    }
} 