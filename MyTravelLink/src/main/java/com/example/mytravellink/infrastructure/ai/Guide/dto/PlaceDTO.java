package com.example.mytravellink.infrastructure.ai.Guide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class PlaceDTO {

    private String id;
    private String name;
    private String address;

    @JsonProperty("official_description")
    private String officialDescription;

    @JsonProperty("reviewer_description")
    private String reviewerDescription;

    @JsonProperty("place_type")
    private String placeType;

    private float rating;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("business_hours")
    private String businessHours;

    private String website;

    private float latitude;
    private float longitude;
}
