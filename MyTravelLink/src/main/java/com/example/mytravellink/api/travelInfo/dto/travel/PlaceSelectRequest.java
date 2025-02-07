package com.example.mytravellink.api.travelInfo.dto.travel;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlaceSelectRequest {
  private String travelInfoId;
  private String title;
  private Integer travelDays;
  private List<String> placeIds;
}
