package com.example.mytravellink.api.travelInfo.dto.travel;

import java.util.List;

import com.example.mytravellink.domain.travel.entity.Place;
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
  private String title;
  private Integer travelDays;
  private List<AIPlace> places;
}
