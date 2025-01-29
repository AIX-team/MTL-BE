package com.example.mytravellink.travel.domain;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@EqualsAndHashCode
public class TravelInfoPlaceId implements Serializable {
    private Long travelInfoId;
    private Long placeId;

    @Builder
    public TravelInfoPlaceId(Long travelInfoId, Long placeId) {
        this.travelInfoId = travelInfoId;
        this.placeId = placeId;
    }
} 