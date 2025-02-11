package com.example.mytravellink.domain.travel.service;

import java.util.List;

import com.example.mytravellink.api.travelInfo.dto.travel.GuideBookResponse;
import com.example.mytravellink.domain.travel.entity.Place;

public interface ImageService {
  
  public List<GuideBookResponse.CourseList> redirectImageUrl(List<GuideBookResponse.CourseList> courseList);
  public List<Place> redirectImageUrlPlace(List<Place> placeList);
  public String redirectImageUrl(String imageUrl);
}
