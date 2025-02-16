package com.example.mytravellink.api.travelInfo.dto.travel;

import java.util.List;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Data
@Builder
public class PlaceSelectRequest {
  private String travelInfoId;
  private Integer travelDays;
  private String travelTaste;
  private List<String> placeIds;
}
