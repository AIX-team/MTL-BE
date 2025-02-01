package com.example.mytravellink.travel.domain;

import com.example.mytravellink.domain.BaseTimeEntity;
import com.example.mytravellink.user.domain.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
 * 여행 정보 (TravelInfo) 엔티티
 * 사용자의 여행 계획 정보를 저장합니다.
 * User와 다대일 관계를 가지며, Place와는 다대다 관계를 가집니다.
 */
@Entity
@Table(name = "travel_info")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelInfo extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(generator = "uuid2")
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email")
    private User user;

    private Integer travelDays;
    
    private int placeCount;
    @Column(nullable = false)
    private int useCount;
    
    @Column(nullable = false)
    private String title;
    
    private boolean bookmark;
    private boolean fixed;
    private boolean isDelete;

    @OneToMany(mappedBy = "travelInfo")
    private List<Guide> guides = new ArrayList<>();

    @OneToMany(mappedBy = "travelInfo")
    private List<TravelInfoPlace> travelInfoPlaces = new ArrayList<>();

    @OneToMany(mappedBy = "travelInfo")
    private List<TravelInfoUrl> urlList = new ArrayList<>();
    
    @Builder
    public TravelInfo(User user, Integer travelDays, String title, List<TravelInfoUrl> urlList) {
        this.user = user;
        this.travelDays = travelDays;
        this.title = title;
        this.placeCount = 0;
        this.useCount = 0;
        this.bookmark = false;
        this.fixed = false;
        this.isDelete = false;
        this.urlList = urlList;
    }
} 