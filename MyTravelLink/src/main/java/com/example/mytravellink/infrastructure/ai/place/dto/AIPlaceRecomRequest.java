package com.example.mytravellink.infrastructure.ai.place.dto;

import java.util.List;

import com.example.mytravellink.travel.domain.Place;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AIPlaceRecomRequest {
    private List<Place> placeList;
    private int tasteNum;
    private int dayNum;
}
  