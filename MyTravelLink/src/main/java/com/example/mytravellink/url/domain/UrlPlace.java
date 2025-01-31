package com.example.mytravellink.url.domain;

import com.example.mytravellink.domain.BaseTimeEntity;
import com.example.mytravellink.travel.domain.Place;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;

/**
 * URL 장소 (UrlPlace) 엔티티
 * Url과 Place 간의 다대다 관계를 위한 중간 테이블 엔티티입니다.
 * 복합 키를 사용하여 Url과 Place를 연결합니다.
 */
@Entity
@Table(name = "url_place")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UrlPlace extends BaseTimeEntity {

    @EmbeddedId
    private UrlPlaceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "url_id")
    private Url url;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    private Place place;
    
    @Builder
    public UrlPlace(Url url, Place place) {
        this.id = new UrlPlaceId(url.getId(), place.getId());
        this.url = url;
        this.place = place;
    }
} 