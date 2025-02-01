package com.example.mytravellink.api.travelInfo.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TravelInfoPlaceResponse {
  String success;
  String message;
  List<Place> content;


  @Data
  @Builder
  public static class Place {
    String urlId;
    String placeId;
    String placeType;
    String placeName;
    String placeAddress;
    String placeImage;
    String placeDescription;
    String intro;
    Double latitude;
    Double longitude;
  }
}