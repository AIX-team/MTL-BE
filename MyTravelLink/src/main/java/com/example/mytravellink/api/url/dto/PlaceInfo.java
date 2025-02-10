package com.example.mytravellink.api.url.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)

public class PlaceInfo {
    private String name;

    @JsonProperty("source_url")
    private String sourceUrl;
    private String description;

    @JsonProperty("formatted_address")
    private String formattedAddress;
    private BigDecimal rating;
    private String phone;
    private String website;

    @JsonProperty("price_level")
    private Integer priceLevel; // Optional 처리

    @JsonProperty("opening_hours")
    private List<String> open_hours;

    private List<PlacePhoto> photos; // PlacePhoto DTO 필요

    @JsonProperty("best_review")
    private String bestReview; // Optional 처리

    @JsonProperty("google_info")
    private Map<String, Object> googleInfo; // Map으로 처리

}
