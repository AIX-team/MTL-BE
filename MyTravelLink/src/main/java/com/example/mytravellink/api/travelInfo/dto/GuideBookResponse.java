package com.example.mytravellink.api.travelInfo.dto;

import java.util.ArrayList;
import java.util.List;

import com.example.mytravellink.domain.travel.entity.CoursePlace;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuideBookResponse {

  private String success;
  private String message;
  private String guideBookTitle;
  private String travelInfoTitle;
  private String travelInfoId;
  private int courseCnt;
  private List<CourseList> courses;

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CourseList {
    private String courseId;
    private int courseNum;
    private List<CoursePlaceResp> coursePlaces;
  }

  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CoursePlaceResp {
    private int Num;
    private String Id;
    private String Name;
    private String Type;
    private String Description;
    private String Image;
    private String Address;
    private String Hours;
    private String Intro;
    private String Latitude;
    private String Longitude;
  }


  /**
   * CoursePlace 리스트 변환
   * @param coursPlace
   * @return
   */
  public static List<CoursePlaceResp> toCoursePlace(List<CoursePlace> coursPlace) {

    List<CoursePlaceResp> coursePlaceList = new ArrayList<>();
    for (CoursePlace coursePlace : coursPlace) {
      CoursePlaceResp cpl = CoursePlaceResp.builder()
      .Num(coursePlace.getPlaceNum())
      .Id(coursePlace.getPlace().getId())
      .Name(coursePlace.getPlace().getTitle())
      .Type(coursePlace.getPlace().getType())
      .Description(coursePlace.getPlace().getDescription())
      .Image(coursePlace.getPlace().getImage())
      .Address(coursePlace.getPlace().getAddress())
      .Hours(coursePlace.getPlace().getOpenHours())
      .Intro(coursePlace.getPlace().getIntro())
      .Latitude(coursePlace.getPlace().getLatitude().toString())
      .Longitude(coursePlace.getPlace().getLongitude().toString())
      .build();
      coursePlaceList.add(cpl);
    }

    return coursePlaceList;
  }
}
