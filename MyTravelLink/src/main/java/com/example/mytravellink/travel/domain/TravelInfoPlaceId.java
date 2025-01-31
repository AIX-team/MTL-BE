package com.example.mytravellink.travel.domain;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class TravelInfoPlaceId implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String travelInfoId;
    private String placeId;
    @Builder
    public TravelInfoPlaceId(String travelInfoId, String placeId) {
        this.travelInfoId = travelInfoId;
        this.placeId = placeId;
    }
}
