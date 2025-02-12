package com.example.mytravellink.api.user.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkDataResponse {
    private String urlId;
    private String urlTitle;
    private LocalDateTime updateAt;
} 