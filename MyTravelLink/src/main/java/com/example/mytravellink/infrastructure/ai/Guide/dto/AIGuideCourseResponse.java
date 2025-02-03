package com.example.mytravellink.infrastructure.ai.Guide.dto;

import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIGuideCourseResponse {
  private List<CourseResp> courseList;
  
  
  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  private static class CourseResp {
    private int courseNumber;
    private List<CoursePlaceResp> coursePlaceList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class CoursePlaceResp {
      private String placeId;
      private int placeNum;
    }
  }

  @Getter
  @Setter
  @Builder
  public static class CourseDTO {
    private int courseNumber;
    private List<PlaceDTO> places;

    @Getter
    @Setter
    @Builder
    public static class PlaceDTO {
      private String placeId;
      private int placeNum;
    }
  }

////////////////////////////////////////////////////////////////////////////////////////////

  // 외부 사용을 위한 메서드
  public List<CourseDTO> getCourses() {
    return courseList.stream()
      .map(this::convertToCourseDTO)
      .collect(Collectors.toList());
  }
  // private -> CourseDTO 변환 메서드
  private CourseDTO convertToCourseDTO(CourseResp courseResp) {
    return CourseDTO.builder()
      .courseNumber(courseResp.getCourseNumber())
      .places(courseResp.getCoursePlaceList().stream()
        .map(this::convertToPlaceDTO)
        .collect(Collectors.toList()))
      .build();
  }
  // private -> PlaceDTO 변환 메서드
  private CourseDTO.PlaceDTO convertToPlaceDTO(CourseResp.CoursePlaceResp placeResp) {
    return CourseDTO.PlaceDTO.builder()
      .placeId(placeResp.getPlaceId())
      .placeNum(placeResp.getPlaceNum())
      .build();
  }
}