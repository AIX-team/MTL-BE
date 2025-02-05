package com.example.mytravellink.infrastructure.ai.Guide.dto;

import java.util.List;

import com.example.mytravellink.domain.travel.entity.Place;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AIGuideCourseRequest {
  private List<Place> placeList;
  private int dayNum;
}
