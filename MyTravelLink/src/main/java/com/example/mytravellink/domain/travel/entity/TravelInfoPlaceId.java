package com.example.mytravellink.domain.travel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class TravelInfoPlaceId implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Column(name = "travel_info_id", columnDefinition = "VARCHAR(36)")
    private String tId;
    
    @Column(name = "place_id", columnDefinition = "CHAR(36)")
    private UUID pId;
    
    @Builder
    public TravelInfoPlaceId(String travelInfoId, UUID placeId) {
        this.tId = travelInfoId;
        this.pId = placeId;
    }
}
