package com.example.mytravellink.url.domain;

import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@Getter
@Setter
public class UrlPlaceId implements Serializable {
    private String urlId;
    private String placeId;

    @Builder
    public UrlPlaceId(String urlId, String placeId) {
        this.urlId = urlId;
        this.placeId = placeId;
    }
} 