package com.example.mytravellink.api.travelInfo.dto.travel;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ToString
@Data

@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)

public class PlaceSelectRequest {
  private String travelInfoId;
  private Integer travelDays;
  private String travelTaste;
  private List<String> placeIds;
}
