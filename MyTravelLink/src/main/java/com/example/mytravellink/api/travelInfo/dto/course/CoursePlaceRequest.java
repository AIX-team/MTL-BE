package com.example.mytravellink.api.travelInfo.dto.course;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoursePlaceRequest {
  private String courseId;
  private List<PlaceResp> places;

  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class PlaceResp {
    private String id;
    private int num;
  }
  
}
