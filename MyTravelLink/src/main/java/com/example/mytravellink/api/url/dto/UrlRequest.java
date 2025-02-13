package com.example.mytravellink.api.url.dto;

import lombok.*;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UrlRequest {

    private List<String> urls;
    private String email;
    private String travelInfoId;
}
