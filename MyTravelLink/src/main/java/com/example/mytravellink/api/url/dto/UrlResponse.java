package com.example.mytravellink.api.url.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class UrlResponse {

    private String name;
    private String sourceUrl;
    private String description;
    private String formattedAddress;
    private Double rating;
    private String phone;
    private String website;


}
